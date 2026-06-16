package me.aiglez.service.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.repository.RecordRepository

data class RecordListUiState(
    val schema: DataSchema? = null,
    val records: List<DataRecord> = emptyList(),
)

class RecordListViewModel(
    private val schemaId: String,
    private val recordRepository: RecordRepository,
    private val logger: Logger,
) : ViewModel() {

    val uiState: StateFlow<RecordListUiState> = combine(
        recordRepository.getActiveSchemas().map { schemas -> schemas.firstOrNull { it.id == schemaId } },
        recordRepository.getActiveRecords(schemaId),
    ) { schema, records ->
        RecordListUiState(schema = schema, records = records)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordListUiState())

    fun archiveRecord(recordId: String) {
        viewModelScope.launch {
            recordRepository.archiveRecord(recordId)
        }
    }

    fun onImportCsvClicked() {
        logger.d { "CSV Import triggered placeholder" }
    }
}
