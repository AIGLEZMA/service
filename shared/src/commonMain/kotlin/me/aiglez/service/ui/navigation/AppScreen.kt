package me.aiglez.service.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppScreen {
    @Serializable data object Dashboard : AppScreen
    @Serializable data object SchemaManagement : AppScreen
    @Serializable data object SchemaCreate : AppScreen
    @Serializable data class SchemaEdit(val schemaId: String) : AppScreen
    @Serializable data class RecordList(val schemaId: String) : AppScreen
    @Serializable data class RecordCreate(val schemaId: String) : AppScreen
    @Serializable data class Compile(val templateId: String) : AppScreen
}
