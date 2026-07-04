package me.aiglez.service.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.repository.RecordRepository

class SchemaManagementViewModel(
    private val recordRepository: RecordRepository,
) : ViewModel() {

    val schemas: StateFlow<List<DataSchema>> = recordRepository.getActiveSchemas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun archiveSchema(schemaId: String) {
        viewModelScope.launch {
            recordRepository.archiveSchema(schemaId)
        }
    }
}



