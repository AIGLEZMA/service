package me.aiglez.service.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class DataSchema(
    val id: String,
    val name: String,
    val fields: List<SchemaField>,
    val isArchived: Boolean = false // Soft delete flag
)
