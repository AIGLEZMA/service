package me.aiglez.service.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.Template
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.repository.RecordRepository
import me.aiglez.service.domain.repository.TemplateRepository
import me.aiglez.service.ui.common.newUiId
import me.aiglez.service.ui.templates.editor.expressionIdentifier

enum class TemplateStartingPoint {
    Blank,
    Simple,
}

data class TemplateCreateUiState(
    val name: String = "",
    val targetSchemaId: String = "",
    val pageSize: String = "A4",
    val startingPoint: TemplateStartingPoint = TemplateStartingPoint.Blank,
    val schemas: List<DataSchema> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val canCreate: Boolean
        get() = name.isNotBlank() && targetSchemaId.isNotBlank() && !isSaving
}

class TemplateCreateViewModel(
    recordRepository: RecordRepository,
    private val templateRepository: TemplateRepository,
    private val logger: Logger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplateCreateUiState())
    val uiState: StateFlow<TemplateCreateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            recordRepository.getActiveSchemas().collect { schemas ->
                _uiState.update { state ->
                    state.copy(
                        schemas = schemas,
                        targetSchemaId = state.targetSchemaId
                            .takeIf { selectedId -> schemas.any { it.id == selectedId } }
                            ?: schemas.firstOrNull()?.id.orEmpty(),
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun selectSchema(schemaId: String) {
        _uiState.update { it.copy(targetSchemaId = schemaId, errorMessage = null) }
    }

    fun selectPageSize(pageSize: String) {
        _uiState.update { it.copy(pageSize = pageSize, errorMessage = null) }
    }

    fun selectStartingPoint(startingPoint: TemplateStartingPoint) {
        _uiState.update { it.copy(startingPoint = startingPoint, errorMessage = null) }
    }

    fun create(onCreated: (String) -> Unit) {
        val state = _uiState.value
        val schema = state.schemas.firstOrNull { it.id == state.targetSchemaId } ?: return
        val name = state.name.trim()
        if (name.isBlank() || state.isSaving) return

        val template = createTemplateDraft(
            name = name,
            schema = schema,
            pageSize = state.pageSize,
            startingPoint = state.startingPoint,
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                templateRepository.saveTemplate(template)
                logger.i { "Created template ${template.id}" }
                onCreated(template.id)
            } catch (cause: Throwable) {
                logger.e(cause) { "Template creation failed" }
                _uiState.update { it.copy(errorMessage = "Impossible de créer le modèle. Réessayez.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}

internal fun createTemplateDraft(
    name: String,
    schema: DataSchema,
    pageSize: String,
    startingPoint: TemplateStartingPoint,
    idFactory: (String) -> String = ::newUiId,
): Template = Template(
    id = idFactory("template"),
    name = name.trim(),
    targetSchemaId = schema.id,
    pageSize = pageSize,
    elements = when (startingPoint) {
        TemplateStartingPoint.Blank -> emptyList()
        TemplateStartingPoint.Simple -> simpleStarterElements(name.trim(), schema, pageSize, idFactory)
    },
)

internal fun simpleStarterElements(
    templateName: String,
    schema: DataSchema,
    pageSize: String = "A4",
    idFactory: (String) -> String = ::newUiId,
): List<TemplateElement> {
    val schemaKey = expressionIdentifier(schema.id).ifBlank { expressionIdentifier(schema.name) }
    val pageDimensions = templatePageDimensions(pageSize)
    val contentWidth = (pageDimensions.width - 96f).coerceAtLeast(120f)
    val header = listOf(
        TemplateElement.Text(
            id = idFactory("title"),
            name = "Titre",
            x = 48f,
            y = 48f,
            width = contentWidth,
            height = 42f,
            zIndex = 1,
            staticText = templateName,
            fontSize = 22f,
            fontWeight = 700,
            color = "#0F172A",
        ),
        TemplateElement.Line(
            id = idFactory("divider"),
            name = "Séparateur",
            x1 = 48f,
            y1 = 102f,
            x2 = pageDimensions.width - 48f,
            y2 = 102f,
            thickness = 1f,
            zIndex = 2,
        ),
    )
    val fields = schema.fields.take(6).mapIndexed { index, field ->
        val fieldKey = expressionIdentifier(field.slug).ifBlank { expressionIdentifier(field.name) }
        val expression = if (schemaKey.isNotBlank() && fieldKey.isNotBlank()) {
            "{{ $schemaKey.$fieldKey }}"
        } else {
            ""
        }
        TemplateElement.Text(
            id = idFactory("field"),
            name = field.name,
            x = 48f,
            y = 132f + index * 52f,
            width = contentWidth,
            height = 36f,
            zIndex = index + 3,
            staticText = "${field.name} : $expression",
            placeholderTag = "[DataRecord:${field.slug}]",
            fontSize = 12f,
            color = "#1F2937",
        )
    }
    return header + fields
}
