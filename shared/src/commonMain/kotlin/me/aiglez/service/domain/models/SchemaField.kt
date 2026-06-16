package me.aiglez.service.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class SchemaField(
    val id: String,
    val name: String,
    val slug: String, // Immutable key generated from name (e.g., "client_age") used for template tags
    val type: FieldType,
    val referenceSchemaId: String? = null // populates if type == REFERENCE
)