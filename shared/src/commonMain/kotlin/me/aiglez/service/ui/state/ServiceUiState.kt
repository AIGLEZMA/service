package me.aiglez.service.ui.state

import me.aiglez.service.data.dynamicdata.DynamicData

data class ServiceUiState(
    val dynamicData: List<DynamicData> = emptyList(),
    val selectedDynamicDataId: Long? = null
)
