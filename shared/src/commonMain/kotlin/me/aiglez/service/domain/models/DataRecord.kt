package me.aiglez.service.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class DataRecord(
    val id: String,
    val schemaId: String,
    val values: Map<String, String>, // Key is SchemaField.slug or SchemaField.id
    val isArchived: Boolean = false  // Soft delete flag
)