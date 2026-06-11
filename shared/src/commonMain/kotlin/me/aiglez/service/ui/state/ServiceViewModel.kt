package me.aiglez.service.ui.state

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.aiglez.service.data.dynamicdata.DynamicData
import me.aiglez.service.data.dynamicdata.DynamicDataField
import me.aiglez.service.data.dynamicdata.DynamicDataFieldType
import me.aiglez.service.data.dynamicdata.DynamicDataInstance

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

    fun deleteDynamicData(id: Long) {
        _uiState.update { currentState ->
            currentState.copy(
                dynamicData = currentState.dynamicData.filterNot { it.id == id },
                dynamicDataInstances = currentState.dynamicDataInstances.filterNot { it.dynamicDataId == id },
                selectedDynamicDataId = currentState.selectedDynamicDataId.takeIf { it != id },
            )
        }
    }

    fun addDynamicDataInstance(instance: DynamicDataInstance) {
        _uiState.update { currentState ->
            val newId = (currentState.dynamicDataInstances.maxOfOrNull { it.id } ?: 0L) + 1L
            currentState.copy(
                dynamicDataInstances = currentState.dynamicDataInstances + instance.copy(id = newId),
                selectedDynamicDataId = instance.dynamicDataId,
            )
        }
    }

    fun addDynamicDataInstances(
        dynamicDataId: Long,
        instances: List<DynamicDataInstance>,
    ) {
        if (instances.isEmpty()) return

        _uiState.update { currentState ->
            val firstId = (currentState.dynamicDataInstances.maxOfOrNull { it.id } ?: 0L) + 1L
            val newInstances = instances.mapIndexed { index, instance ->
                instance.copy(
                    id = firstId + index,
                    dynamicDataId = dynamicDataId,
                )
            }
            currentState.copy(
                dynamicDataInstances = currentState.dynamicDataInstances + newInstances,
                selectedDynamicDataId = dynamicDataId,
            )
        }
    }
}

private fun sampleDynamicData(): List<DynamicData> = listOf(
    DynamicData(
        id = 1L,
        name = "People CSV",
        fields = listOf(
            DynamicDataField(
                name = "Name",
                type = DynamicDataFieldType.Text,
            ),
            DynamicDataField(
                name = "Age",
                optional = true,
                type = DynamicDataFieldType.Number,
            ),
            DynamicDataField(
                name = "Active",
                type = DynamicDataFieldType.Boolean,
            ),
        ),
    ),
    DynamicData(
        id = 2L,
        name = "Clients CSV",
        fields = listOf(
            DynamicDataField(
                name = "Nom",
                type = DynamicDataFieldType.Text,
            ),
            DynamicDataField(
                name = "Email",
                optional = true,
                type = DynamicDataFieldType.Text,
            ),
            DynamicDataField(
                name = "Telephone",
                optional = true,
                type = DynamicDataFieldType.Text,
            ),
            DynamicDataField(
                name = "Actif",
                type = DynamicDataFieldType.Boolean,
            ),
        ),
    ),
    DynamicData(
        id = 3L,
        name = "Installations CSV",
        fields = listOf(
            DynamicDataField(
                name = "Reference",
                type = DynamicDataFieldType.Text,
            ),
            DynamicDataField(
                name = "Client",
                type = DynamicDataFieldType.Text,
            ),
            DynamicDataField(
                name = "Puissance kW",
                optional = true,
                type = DynamicDataFieldType.Decimal,
            ),
            DynamicDataField(
                name = "Nombre modules",
                optional = true,
                type = DynamicDataFieldType.Number,
            ),
            DynamicDataField(
                name = "En service",
                type = DynamicDataFieldType.Boolean,
            ),
        ),
    ),
    DynamicData(
        id = 4L,
        name = "Interventions CSV",
        fields = listOf(
            DynamicDataField(
                name = "Date",
                type = DynamicDataFieldType.Text,
            ),
            DynamicDataField(
                name = "Technicien",
                type = DynamicDataFieldType.Text,
            ),
            DynamicDataField(
                name = "Duree minutes",
                optional = true,
                type = DynamicDataFieldType.Number,
            ),
            DynamicDataField(
                name = "Montant HT",
                optional = true,
                type = DynamicDataFieldType.Decimal,
            ),
            DynamicDataField(
                name = "Terminee",
                type = DynamicDataFieldType.Boolean,
            ),
        ),
    ),
)
