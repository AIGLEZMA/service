package me.aiglez.service.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField
import me.aiglez.service.domain.repository.RecordRepository
import me.aiglez.service.ui.common.newUiId
import me.aiglez.service.ui.common.slugify

data class SchemaCreateUiState(
    val schemaId: String = "",
    val schemaName: String = "",
    val fields: List<SchemaField> = emptyList(),
    val lockedExistingFieldIds: Set<String> = emptySet(),
    val hasExistingRecords: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val availableSchemas: List<DataSchema> = emptyList(),
)

class SchemaCreateViewModel(
    private val schemaId: String,
    private val recordRepository: RecordRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchemaCreateUiState(schemaId = schemaId))
    val uiState: StateFlow<SchemaCreateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            recordRepository.getActiveSchemas().collect { schemas ->
                _uiState.update { current ->
                    val next = current.copy(availableSchemas = schemas)
                    if (schemaId.isNotBlank() && !next.isEditing) {
                        val schema = schemas.firstOrNull { it.id == schemaId }
                        if (schema != null) {
                            next.copy(
                                schemaId = schema.id,
                                schemaName = schema.name,
                                fields = schema.fields,
                                lockedExistingFieldIds = schema.fields.map { it.id }.toSet(),
                                isEditing = true,
                            )
                        } else {
                            next
                        }
                    } else {
                        next
                    }
                }
            }
        }
        if (schemaId.isNotBlank()) {
            viewModelScope.launch {
                recordRepository.getActiveRecords(schemaId).collect { records ->
                    _uiState.update {
                        it.copy(
                            hasExistingRecords = records.isNotEmpty(),
                            lockedExistingFieldIds = if (records.isEmpty()) {
                                emptySet()
                            } else {
                                it.lockedExistingFieldIds.ifEmpty { it.fields.map { field -> field.id }.toSet() }
                            },
                        )
                    }
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(schemaName = name) }
    }

    fun addField() {
        val ordinal = _uiState.value.fields.size + 1
        val name = "Champ $ordinal"
        _uiState.update {
            it.copy(
                fields = it.fields + SchemaField(
                    id = newUiId("field"),
                    name = name,
                    slug = slugify(name),
                    type = FieldType.TEXT,
                ),
            )
        }
    }

    fun updateFieldName(fieldId: String, name: String) {
        _uiState.update { state ->
            state.copy(
                fields = state.fields.map { field ->
                    if (field.id == fieldId) {
                        field.copy(name = name, slug = slugify(name))
                    } else {
                        field
                    }
                },
            )
        }
    }

    fun updateFieldType(fieldId: String, type: FieldType) {
        _uiState.update { state ->
            if (state.hasExistingRecords && fieldId in state.lockedExistingFieldIds) {
                state
            } else {
                state.copy(
                    fields = state.fields.map { field ->
                        if (field.id == fieldId) {
                            val defaultRefId = if (type == FieldType.REFERENCE) {
                                state.availableSchemas.firstOrNull { it.id != state.schemaId }?.id ?: ""
                            } else null
                            field.copy(type = type, referenceSchemaId = defaultRefId)
                        } else {
                            field
                        }
                    },
                )
            }
        }
    }

    fun updateFieldReference(fieldId: String, referenceSchemaId: String) {
        _uiState.update { state ->
            if (state.hasExistingRecords && fieldId in state.lockedExistingFieldIds) {
                state
            } else {
                state.copy(
                    fields = state.fields.map { field ->
                        if (field.id == fieldId) field.copy(referenceSchemaId = referenceSchemaId) else field
                    },
                )
            }
        }
    }

    fun removeField(fieldId: String) {
        _uiState.update { state ->
            if (state.hasExistingRecords && fieldId in state.lockedExistingFieldIds) {
                state
            } else {
                state.copy(fields = state.fields.filterNot { it.id == fieldId })
            }
        }
    }

    fun save(onSaved: (String) -> Unit) {
        val current = _uiState.value
        val id = current.schemaId.ifBlank { newUiId("schema") }
        if (current.schemaName.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            recordRepository.saveSchema(
                DataSchema(
                    id = id,
                    name = current.schemaName.trim(),
                    fields = current.fields,
                ),
            )
            _uiState.update { it.copy(isSaving = false, schemaId = id) }
            onSaved(id)
        }
    }
}



