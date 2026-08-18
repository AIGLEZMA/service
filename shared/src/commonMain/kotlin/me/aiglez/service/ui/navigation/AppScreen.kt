package me.aiglez.service.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppScreen {
    @Serializable
    data object Dashboard : AppScreen
    @Serializable
    data object Help : AppScreen
    @Serializable
    data object SchemaManagement : AppScreen
    @Serializable
    data object SchemaCreate : AppScreen
    @Serializable
    data object TemplateCreate : AppScreen
    @Serializable
    data class SchemaEdit(val schemaId: String) : AppScreen
    @Serializable
    data class RecordList(val schemaId: String) : AppScreen
    @Serializable
    data class RecordCreate(val schemaId: String) : AppScreen
    @Serializable
    data class TemplatePreview(val templateId: String) : AppScreen
    @Serializable
    data class TemplateEditor(val templateId: String) : AppScreen
}
