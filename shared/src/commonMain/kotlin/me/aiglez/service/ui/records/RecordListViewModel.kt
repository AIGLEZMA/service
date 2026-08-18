package me.aiglez.service.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.aiglez.service.data.csv.CsvImportPreview
import me.aiglez.service.data.csv.CsvImportReadResult
import me.aiglez.service.data.csv.CsvImportSource
import me.aiglez.service.data.csv.DataRecordCsvImporter
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.repository.RecordRepository
import me.aiglez.service.io.pickCsvFile
import me.aiglez.service.ui.common.newUiId

data class CsvImportUiState(
    val source: CsvImportSource? = null,
    val mappings: Map<String, String?> = emptyMap(),
    val preview: CsvImportPreview? = null,
    val isPicking: Boolean = false,
    val isImporting: Boolean = false,
    val error: String? = null,
)

data class RecordListUiState(
    val schema: DataSchema? = null,
    val records: List<DataRecord> = emptyList(),
    val csvImport: CsvImportUiState = CsvImportUiState(),
    val isArchiving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)

private data class RecordMutationState(
    val isArchiving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)

class RecordListViewModel(
    private val schemaId: String,
    private val recordRepository: RecordRepository,
    private val logger: Logger,
) : ViewModel() {

    private val csvImport = MutableStateFlow(CsvImportUiState())
    private val mutationState = MutableStateFlow(RecordMutationState())

    val uiState: StateFlow<RecordListUiState> = combine(
        recordRepository.getActiveSchemas().map { schemas -> schemas.firstOrNull { it.id == schemaId } },
        recordRepository.getActiveRecords(schemaId),
        csvImport,
        mutationState,
    ) { schema, records, importState, mutation ->
        RecordListUiState(
            schema = schema,
            records = records,
            csvImport = importState,
            isArchiving = mutation.isArchiving,
            message = mutation.message,
            errorMessage = mutation.errorMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordListUiState())

    fun archiveRecord(recordId: String) {
        if (mutationState.value.isArchiving) return
        viewModelScope.launch {
            mutationState.value = RecordMutationState(isArchiving = true)
            try {
                recordRepository.archiveRecord(recordId)
                mutationState.value = RecordMutationState(message = "Donnée archivée.")
            } catch (cause: Throwable) {
                logger.e(cause) { "Record archive failed" }
                mutationState.value = RecordMutationState(errorMessage = "Impossible d’archiver la donnée. Réessayez.")
            }
        }
    }

    fun clearMutationFeedback() {
        mutationState.value = RecordMutationState()
    }

    fun onImportCsvClicked() {
        val schema = uiState.value.schema ?: return
        if (csvImport.value.isPicking) return
        viewModelScope.launch {
            csvImport.value = CsvImportUiState(isPicking = true)
            runCatching { pickCsvFile() }.fold(
                onSuccess = { selection ->
                    if (selection == null) {
                        csvImport.value = CsvImportUiState()
                        return@fold
                    }
                    when (val result = DataRecordCsvImporter.read(selection.fileName, selection.content)) {
                        is CsvImportReadResult.Success -> {
                            val mappings = DataRecordCsvImporter.suggestMappings(schema, result.source)
                            csvImport.value = CsvImportUiState(
                                source = result.source,
                                mappings = mappings,
                                preview = DataRecordCsvImporter.preview(schema, result.source, mappings),
                            )
                        }
                        is CsvImportReadResult.Failure -> {
                            csvImport.value = CsvImportUiState(error = result.message)
                        }
                    }
                },
                onFailure = { error ->
                    logger.e(error) { "CSV file selection failed" }
                    csvImport.value = CsvImportUiState(
                        error = "Impossible d'ouvrir le fichier CSV : ${error.message ?: "erreur inconnue"}",
                    )
                },
            )
        }
    }

    fun updateCsvMapping(fieldId: String, columnName: String?) {
        val schema = uiState.value.schema ?: return
        val current = csvImport.value
        val source = current.source ?: return
        val mappings = current.mappings + (fieldId to columnName)
        csvImport.value = current.copy(
            mappings = mappings,
            preview = DataRecordCsvImporter.preview(schema, source, mappings),
        )
    }

    fun dismissCsvImport() {
        if (!csvImport.value.isImporting) csvImport.value = CsvImportUiState()
    }

    fun importCsvRecords() {
        val preview = csvImport.value.preview?.takeIf { it.canImport } ?: return
        if (csvImport.value.isImporting) return
        viewModelScope.launch {
            csvImport.update { it.copy(isImporting = true, error = null) }
            runCatching {
                preview.records.forEach { values ->
                    recordRepository.saveRecord(
                        DataRecord(
                            id = newUiId("record"),
                            schemaId = schemaId,
                            values = values,
                        ),
                    )
                }
            }.fold(
                onSuccess = {
                    logger.i { "Imported ${preview.records.size} records from CSV" }
                    csvImport.value = CsvImportUiState()
                },
                onFailure = { error ->
                    logger.e(error) { "CSV import failed" }
                    csvImport.update {
                        it.copy(
                            isImporting = false,
                            error = "L'import a échoué : ${error.message ?: "erreur inconnue"}",
                        )
                    }
                },
            )
        }
    }
}
