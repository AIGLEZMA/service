package me.aiglez.service.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.Template
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.repository.RecordRepository
import me.aiglez.service.domain.repository.TemplateRepository

data class TokenTranslation(
    val token: String,
    val value: String,
)

data class CompiledTextBlock(
    val x: Float,
    val y: Float,
    val text: String,
)

data class CompiledLineBlock(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val thickness: Float,
)

data class CompileUiState(
    val template: Template? = null,
    val schema: DataSchema? = null,
    val records: List<DataRecord> = emptyList(),
    val selectedRecordId: String = "",
    val translations: List<TokenTranslation> = emptyList(),
    val textBlocks: List<CompiledTextBlock> = emptyList(),
    val lineBlocks: List<CompiledLineBlock> = emptyList(),
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CompileViewModel(
    private val templateId: String,
    private val recordRepository: RecordRepository,
    private val templateRepository: TemplateRepository,
    private val logger: Logger,
) : ViewModel() {

    private val selectedRecordId = MutableStateFlow("")

    private val templateAndSchema = recordRepository.getActiveSchemas().flatMapLatest { schemas ->
        if (schemas.isEmpty()) {
            flowOf(null to null)
        } else {
            combine(schemas.map { schema ->
                templateRepository.getActiveTemplates(schema.id).map { templates ->
                    schema to templates.firstOrNull { it.id == templateId }
                }
            }) { matches ->
                matches.firstOrNull { it.second != null } ?: (null to null)
            }
        }
    }

    val uiState: StateFlow<CompileUiState> = templateAndSchema.flatMapLatest { (schema, template) ->
        if (schema == null || template == null) {
            flowOf(CompileUiState())
        } else {
            combine(
                recordRepository.getActiveRecords(schema.id),
                selectedRecordId,
            ) { records, selected ->
                val selectedId = selected.ifBlank { records.firstOrNull()?.id.orEmpty() }
                val selectedRecord = records.firstOrNull { it.id == selectedId }
                val values = selectedRecord?.values.orEmpty()
                val translations = schema.fields.map { field ->
                    TokenTranslation(
                        token = "[DataRecord:${field.slug}]",
                        value = values[field.slug].orEmpty(),
                    )
                }
                CompileUiState(
                    template = template,
                    schema = schema,
                    records = records,
                    selectedRecordId = selectedId,
                    translations = translations,
                    textBlocks = compileText(template, values),
                    lineBlocks = compileLines(template),
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompileUiState())

    fun selectRecord(recordId: String) {
        selectedRecordId.value = recordId
    }

    fun exportPdf() {
        logger.i { "Export PDF Document triggered for template $templateId and record ${uiState.value.selectedRecordId}" }
    }

    private fun compileText(template: Template, values: Map<String, String>): List<CompiledTextBlock> {
        return template.elements.mapNotNull { element ->
            val textElement = element as? TemplateElement.Text ?: return@mapNotNull null
            val text = textElement.staticText
                ?: textElement.placeholderTag?.let { tag ->
                    val key = tag.removePrefix("[DataRecord:").removeSuffix("]")
                    values[key].orEmpty()
                }
                ?: ""
            CompiledTextBlock(textElement.x, textElement.y, text)
        }
    }

    private fun compileLines(template: Template): List<CompiledLineBlock> {
        return template.elements.mapNotNull { element ->
            val line = element as? TemplateElement.Line ?: return@mapNotNull null
            CompiledLineBlock(line.x1, line.y1, line.x2, line.y2, line.thickness)
        }
    }
}
