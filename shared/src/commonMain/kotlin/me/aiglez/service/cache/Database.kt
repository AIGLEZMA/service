package me.aiglez.service.cache

import app.cash.sqldelight.db.SqlDriver
import me.aiglez.service.data.dynamicdata.DynamicData
import me.aiglez.service.data.dynamicdata.DynamicDataField
import me.aiglez.service.data.dynamicdata.DynamicDataFieldType
import me.aiglez.service.data.dynamicdata.DynamicDataInstance
import me.aiglez.service.data.dynamicdata.DynamicDataValue

internal class Database(driver: SqlDriver) {
    constructor(databaseDriverFactory: DatabaseDriverFactory) : this(databaseDriverFactory.createDriver())

    private val database = AppDatabase(driver)
    private val dbQuery = database.appDatabaseQueries

    internal fun getAllDynamicData(): List<DynamicData> {
        return dbQuery.selectDynamicData(::mapDynamicData).executeAsList()
    }

    internal fun getDynamicData(id: Long): DynamicData? {
        return dbQuery.selectDynamicDataById(id, ::mapDynamicData).executeAsOneOrNull()
    }

    internal fun saveDynamicData(dynamicData: DynamicData) {
        dbQuery.transaction {
            dbQuery.upsertDynamicData(
                id = dynamicData.id,
                name = dynamicData.name
            )
            dbQuery.deleteFieldsForDynamicData(dynamicData.id)
            dynamicData.fields.forEachIndexed { index, field ->
                dbQuery.insertDynamicDataField(
                    dynamic_data_id = dynamicData.id,
                    position = index.toLong(),
                    name = field.name,
                    optional = field.optional,
                    type = field.type.toDatabaseValue()
                )
            }
        }
    }

    internal fun deleteDynamicData(id: Long) {
        dbQuery.transaction {
            dbQuery.deleteInstancesForDynamicData(id)
            dbQuery.deleteDynamicData(id)
        }
    }

    internal fun getDynamicDataInstances(dynamicDataId: Long): List<DynamicDataInstance> {
        return dbQuery.selectDynamicDataInstances(dynamicDataId, ::mapDynamicDataInstance).executeAsList()
    }

    internal fun getDynamicDataInstance(id: Long): DynamicDataInstance? {
        return dbQuery.selectDynamicDataInstanceById(id, ::mapDynamicDataInstance).executeAsOneOrNull()
    }

    internal fun saveDynamicDataInstance(instance: DynamicDataInstance) {
        dbQuery.transaction {
            dbQuery.upsertDynamicDataInstance(
                id = instance.id,
                dynamic_data_id = instance.dynamicDataId,
            )
            dbQuery.deleteValuesForDynamicDataInstance(instance.id)
            instance.values.forEach { (fieldName, value) ->
                val databaseValue = value.toDatabaseValue()
                dbQuery.insertDynamicDataInstanceValue(
                    instance_id = instance.id,
                    field_name = fieldName,
                    value_type = databaseValue.type,
                    value_payload = databaseValue.payload,
                )
            }
        }
    }

    internal fun deleteDynamicDataInstance(id: Long) {
        dbQuery.deleteDynamicDataInstance(id)
    }

    private fun mapDynamicData(id: Long, name: String): DynamicData {
        return DynamicData(
            id = id,
            name = name,
            fields = dbQuery
                .selectFieldsForDynamicData(id, ::mapDynamicDataField)
                .executeAsList()
        )
    }

    private fun mapDynamicDataField(
        id: Long,
        dynamicDataId: Long,
        position: Long,
        name: String,
        optional: Boolean,
        type: String
    ): DynamicDataField {
        return DynamicDataField(
            name = name,
            optional = optional,
            type = type.toDynamicDataFieldType()
        )
    }

    private fun mapDynamicDataInstance(id: Long, dynamicDataId: Long): DynamicDataInstance {
        return DynamicDataInstance(
            id = id,
            dynamicDataId = dynamicDataId,
            values = dbQuery
                .selectValuesForDynamicDataInstance(id)
                .executeAsList()
                .associate { row ->
                    row.field_name to row.value_type.toDynamicDataValue(row.value_payload)
                }
        )
    }

    private fun DynamicDataFieldType.toDatabaseValue(): String {
        return when (this) {
            DynamicDataFieldType.Text -> TYPE_TEXT
            DynamicDataFieldType.Number -> TYPE_NUMBER
            DynamicDataFieldType.Decimal -> TYPE_DECIMAL
            DynamicDataFieldType.Boolean -> TYPE_BOOLEAN
            is DynamicDataFieldType.DynamicDataRef -> "$TYPE_DYNAMIC_DATA_REF:$dynamicDataId"
            is DynamicDataFieldType.ListOf -> "$TYPE_LIST:${itemType.toDatabaseValue()}"
        }
    }

    private fun String.toDynamicDataFieldType(): DynamicDataFieldType {
        return when (this) {
            TYPE_TEXT -> DynamicDataFieldType.Text
            TYPE_NUMBER -> DynamicDataFieldType.Number
            TYPE_DECIMAL -> DynamicDataFieldType.Decimal
            TYPE_BOOLEAN -> DynamicDataFieldType.Boolean
            else -> when {
                startsWith("$TYPE_DYNAMIC_DATA_REF:") -> {
                    val dynamicDataId = removePrefix("$TYPE_DYNAMIC_DATA_REF:").toLong()
                    DynamicDataFieldType.DynamicDataRef(dynamicDataId)
                }
                startsWith("$TYPE_LIST:") -> {
                    val itemType = removePrefix("$TYPE_LIST:").toDynamicDataFieldType()
                    DynamicDataFieldType.ListOf(itemType)
                }
                else -> error("Unknown dynamic data field type: $this")
            }
        }
    }

    private fun DynamicDataValue?.toDatabaseValue(): DatabaseValue {
        return when (this) {
            null -> DatabaseValue(TYPE_NULL, null)
            is DynamicDataValue.Text -> DatabaseValue(TYPE_TEXT, value)
            is DynamicDataValue.Number -> DatabaseValue(TYPE_NUMBER, value.toString())
            is DynamicDataValue.Decimal -> DatabaseValue(TYPE_DECIMAL, value.toString())
            is DynamicDataValue.Boolean -> DatabaseValue(TYPE_BOOLEAN, value.toString())
            is DynamicDataValue.DynamicDataRef -> DatabaseValue(TYPE_DYNAMIC_DATA_REF, instanceId.toString())
            is DynamicDataValue.ListOf -> DatabaseValue(TYPE_LIST, values.toListPayload())
        }
    }

    private fun String.toDynamicDataValue(payload: String?): DynamicDataValue? {
        return when (this) {
            TYPE_NULL -> null
            TYPE_TEXT -> DynamicDataValue.Text(payload.orEmpty())
            TYPE_NUMBER -> DynamicDataValue.Number(requireNotNull(payload).toLong())
            TYPE_DECIMAL -> DynamicDataValue.Decimal(requireNotNull(payload).toDouble())
            TYPE_BOOLEAN -> DynamicDataValue.Boolean(requireNotNull(payload).toBooleanStrict())
            TYPE_DYNAMIC_DATA_REF -> DynamicDataValue.DynamicDataRef(requireNotNull(payload).toLong())
            TYPE_LIST -> DynamicDataValue.ListOf(payload.orEmpty().toValueList())
            else -> error("Unknown dynamic data value type: $this")
        }
    }

    private fun List<DynamicDataValue?>.toListPayload(): String {
        return buildString {
            this@toListPayload.forEach { value ->
                val encoded = value.toNestedValuePayload()
                append(encoded.length)
                append(':')
                append(encoded)
            }
        }
    }

    private fun String.toValueList(): List<DynamicDataValue?> {
        val values = mutableListOf<DynamicDataValue?>()
        var cursor = 0
        while (cursor < length) {
            val separatorIndex = indexOf(':', startIndex = cursor)
            check(separatorIndex >= cursor) { "Invalid list payload: $this" }
            val itemLength = substring(cursor, separatorIndex).toInt()
            val itemStart = separatorIndex + 1
            val itemEnd = itemStart + itemLength
            check(itemEnd <= length) { "Invalid list payload: $this" }
            values += substring(itemStart, itemEnd).toNestedDynamicDataValue()
            cursor = itemEnd
        }
        return values
    }

    private fun DynamicDataValue?.toNestedValuePayload(): String {
        val databaseValue = toDatabaseValue()
        return "${databaseValue.type}|${databaseValue.payload.orEmpty()}"
    }

    private fun String.toNestedDynamicDataValue(): DynamicDataValue? {
        val typeEnd = indexOf('|')
        check(typeEnd >= 0) { "Invalid nested value payload: $this" }
        val type = substring(0, typeEnd)
        val payload = substring(typeEnd + 1).ifEmpty { null }
        return type.toDynamicDataValue(payload)
    }

    private data class DatabaseValue(
        val type: String,
        val payload: String?,
    )

    private companion object {
        const val TYPE_NULL = "null"
        const val TYPE_TEXT = "text"
        const val TYPE_NUMBER = "number"
        const val TYPE_DECIMAL = "decimal"
        const val TYPE_BOOLEAN = "boolean"
        const val TYPE_DYNAMIC_DATA_REF = "dynamic_data_ref"
        const val TYPE_LIST = "list"
    }
}
