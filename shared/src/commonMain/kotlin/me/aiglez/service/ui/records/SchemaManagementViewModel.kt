package me.aiglez.service.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.repository.RecordRepository

class SchemaManagementViewModel(
    private val recordRepository: RecordRepository,
    private val logger: Logger,
) : ViewModel() {

    val schemas: StateFlow<List<DataSchema>> = recordRepository.getActiveSchemas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isArchiving = MutableStateFlow(false)
    val isArchiving = _isArchiving.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun archiveSchema(schemaId: String) {
        if (_isArchiving.value) return
        viewModelScope.launch {
            _isArchiving.value = true
            _errorMessage.value = null
            _message.value = null
            try {
                recordRepository.archiveSchema(schemaId)
                _message.value = "Modèle de données archivé."
            } catch (cause: Throwable) {
                logger.e(cause) { "Schema archive failed" }
                _errorMessage.value = "Impossible d’archiver le modèle de données. Réessayez."
            } finally {
                _isArchiving.value = false
            }
        }
    }

    fun clearFeedback() {
        _errorMessage.value = null
        _message.value = null
    }
}
