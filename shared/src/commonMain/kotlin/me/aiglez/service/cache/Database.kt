package me.aiglez.service.cache

import app.cash.sqldelight.db.SqlDriver
import me.aiglez.service.data.dynamicdata.DynamicData
import me.aiglez.service.data.dynamicdata.DynamicDataField
import me.aiglez.service.data.dynamicdata.DynamicDataFieldType

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
        dbQuery.deleteDynamicData(id)
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

    private companion object {
        const val TYPE_TEXT = "text"
        const val TYPE_NUMBER = "number"
        const val TYPE_DECIMAL = "decimal"
        const val TYPE_BOOLEAN = "boolean"
        const val TYPE_DYNAMIC_DATA_REF = "dynamic_data_ref"
        const val TYPE_LIST = "list"
    }
}
