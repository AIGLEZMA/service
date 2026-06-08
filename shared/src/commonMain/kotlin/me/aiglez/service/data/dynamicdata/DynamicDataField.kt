package me.aiglez.service.data.dynamicdata

data class DynamicDataField(
    val name: String,
    val optional: Boolean = false,
    val type: DynamicDataFieldType
)