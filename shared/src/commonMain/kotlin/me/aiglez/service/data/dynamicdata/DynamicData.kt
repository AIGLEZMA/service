package me.aiglez.service.data.dynamicdata

data class DynamicData(
    val id: Long,
    val name: String,
    val fields: List<DynamicDataField>
)