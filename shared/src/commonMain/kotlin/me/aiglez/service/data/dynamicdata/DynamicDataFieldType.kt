package me.aiglez.service.data.dynamicdata

sealed interface DynamicDataFieldType {

    data object Text : DynamicDataFieldType

    data object Number : DynamicDataFieldType

    data object Decimal : DynamicDataFieldType

    data object Boolean : DynamicDataFieldType

    data class DynamicDataRef(

        val dynamicDataId: Long

    ) : DynamicDataFieldType

    data class ListOf(

        val itemType: DynamicDataFieldType

    ) : DynamicDataFieldType

}