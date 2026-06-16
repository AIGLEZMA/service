package me.aiglez.service.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.repository.RecordRepository
import me.aiglez.service.ui.common.newUiId

data class RecordCreateUiState(
    val schema: DataSchema? = null,
    val values: Map<String, String> = emptyMap(),
    val referenceOptions: Map<String, List<DataRecord>> = emptyMap(),
    val isSaving: Boolean = false,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RecordCreateViewModel(
    private val schemaId: String,
    private val recordRepository: RecordRepository,
) : ViewModel() {

    private val values = MutableStateFlow<Map<String, String>>(emptyMap())
    private val saving = MutableStateFlow(false)

    val uiState: StateFlow<RecordCreateUiState> = recordRepository.getActiveSchemas()
        .map { schemas -> schemas.firstOrNull { it.id == schemaId } }
        .flatMapLatest { schema ->
            val referenceFields = schema?.fields.orEmpty()
                .filter { it.type == FieldType.REFERENCE && !it.referenceSchemaId.isNullOrBlank() }
            val referenceOptions = if (referenceFields.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(referenceFields.map { field ->
                    recordRepository.getActiveRecords(field.referenceSchemaId.orEmpty()).map { records ->
                        field.id to records
                    }
                }) { pairs -> pairs.toMap() }
            }
            combine(values, saving, referenceOptions) { currentValues, isSaving, options ->
                RecordCreateUiState(
                    schema = schema,
                    values = currentValues,
                    referenceOptions = options,
                    isSaving = isSaving,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordCreateUiState())

    fun updateValue(fieldSlug: String, value: String, type: FieldType) {
        val filtered = when (type) {
            FieldType.NUMBER -> value.filter { it.isDigit() }
            FieldType.DOUBLE -> value.filterIndexed { index, char ->
                char.isDigit() || (char == '.' && value.indexOf('.') == index)
            }

            else -> value
        }
        values.update { it + (fieldSlug to filtered) }
    }

    fun save(onSaved: () -> Unit) {
        val schema = uiState.value.schema ?: return
        viewModelScope.launch {
            saving.value = true
            recordRepository.saveRecord(
                DataRecord(
                    id = newUiId("record"),
                    schemaId = schema.id,
                    values = uiState.value.values,
                ),
            )
            saving.value = false
            onSaved()
        }
    }
}
