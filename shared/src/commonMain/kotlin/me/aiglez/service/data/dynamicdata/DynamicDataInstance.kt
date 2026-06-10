package me.aiglez.service.data.dynamicdata

data class DynamicDataInstance(
    val id: Long,
    val dynamicDataId: Long,
    val values: Map<String, DynamicDataValue?>
)

sealed interface DynamicDataValue {
    data class Text(val value: String) : DynamicDataValue
    data class Number(val value: Long) : DynamicDataValue
    data class Decimal(val value: Double) : DynamicDataValue
    data class Boolean(val value: kotlin.Boolean) : DynamicDataValue
    data class DynamicDataRef(val instanceId: Long) : DynamicDataValue
    data class ListOf(val values: List<DynamicDataValue?>) : DynamicDataValue
}
