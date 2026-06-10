package me.aiglez.service.ui.state

import me.aiglez.service.data.dynamicdata.DynamicData
import me.aiglez.service.data.dynamicdata.DynamicDataInstance

data class ServiceUiState(
    val dynamicData: List<DynamicData> = emptyList(),
    val dynamicDataInstances: List<DynamicDataInstance> = emptyList(),
    val selectedDynamicDataId: Long? = null
)
