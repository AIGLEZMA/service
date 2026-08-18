package me.aiglez.service.data.database

import app.cash.sqldelight.ColumnAdapter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.aiglez.service.domain.models.SchemaField
import me.aiglez.service.domain.models.TemplateElement

private val templateJson = Json {
    classDiscriminator = "_elementType"
    ignoreUnknownKeys = true
}

val fieldsAdapter = object : ColumnAdapter<List<SchemaField>, String> {
    override fun decode(databaseValue: String): List<SchemaField> =
        Json.decodeFromString(databaseValue)

    override fun encode(value: List<SchemaField>): String =
        Json.encodeToString(value)
}

val valuesMapAdapter = object : ColumnAdapter<Map<String, String>, String> {
    override fun decode(databaseValue: String): Map<String, String> =
        Json.decodeFromString(databaseValue)

    override fun encode(value: Map<String, String>): String =
        Json.encodeToString(value)
}

val elementsAdapter = object : ColumnAdapter<List<TemplateElement>, String> {
    override fun decode(databaseValue: String): List<TemplateElement> =
        templateJson.decodeFromString(databaseValue)

    override fun encode(value: List<TemplateElement>): String =
        templateJson.encodeToString(value)
}
