package me.aiglez.service.domain.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface TemplateElement {

    @Serializable
    data class Text(
        val x: Float,
        val y: Float,
        val staticText: String? = null,
        val placeholderTag: String? = null // e.g., "[DataRecord:client_age]"
    ) : TemplateElement

    @Serializable
    data class Line(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val thickness: Float
    ) : TemplateElement
}

@Serializable
data class Template(
    val id: String,
    val name: String,
    val targetSchemaId: String,
    val pageSize: String = "A4",
    val elements: List<TemplateElement>,
    val isArchived: Boolean = false // Soft delete flag
)