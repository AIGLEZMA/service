package me.aiglez.service.ui.state

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.aiglez.service.data.dynamicdata.DynamicData
import me.aiglez.service.data.dynamicdata.DynamicDataField
import me.aiglez.service.data.dynamicdata.DynamicDataFieldType

class ServiceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        ServiceUiState(dynamicData = sampleDynamicData())
    )
    val uiState: StateFlow<ServiceUiState> = _uiState.asStateFlow()

    fun selectDynamicData(id: Long?) {
        _uiState.update { currentState ->
            currentState.copy(selectedDynamicDataId = id)
        }
    }

    fun addDynamicData(name: String, fields: List<DynamicDataField>) {
        _uiState.update { currentState ->
            val newId = (currentState.dynamicData.maxOfOrNull { it.id } ?: 0L) + 1L
            val newData = DynamicData(id = newId, name = name, fields = fields)
            currentState.copy(dynamicData = currentState.dynamicData + newData)
        }
    }
}

private fun sampleDynamicData(): List<DynamicData> = List(18) { index ->
    val id = index + 1L
    DynamicData(
        id = id,
        name = "Données dynamiques $id",
        fields = listOf(
            DynamicDataField(
                name = "Nom",
                type = DynamicDataFieldType.Text,
            ),
            DynamicDataField(
                name = "Actif",
                optional = true,
                type = DynamicDataFieldType.Boolean,
            ),
        ),
    )
}
