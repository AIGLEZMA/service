package me.aiglez.service.ui.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.repository.RecordRepository

class SidebarViewModel(
    recordRepository: RecordRepository,
) : ViewModel() {
    val schemas: StateFlow<List<DataSchema>> = recordRepository.getActiveSchemas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}



