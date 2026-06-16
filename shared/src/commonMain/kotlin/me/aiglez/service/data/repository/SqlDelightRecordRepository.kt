package me.aiglez.service.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.aiglez.service.database.AppDatabase
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.repository.RecordRepository

class SqlDelightRecordRepository(
    private val database: AppDatabase,
    private val logger: Logger
) : RecordRepository {

    private val queries = database.appDatabaseQueries

    override fun getActiveSchemas(): Flow<List<DataSchema>> {
        return queries.getActiveSchemas()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities ->
                entities.map { DataSchema(it.id, it.name, it.fields, it.isArchived) }
            }
    }

    override suspend fun saveSchema(schema: DataSchema) {
        logger.d { "Saving Schema: ${schema.name} (${schema.id})" }
        queries.insertSchema(schema.id, schema.name, schema.fields, schema.isArchived)
    }

    override suspend fun archiveSchema(schemaId: String) {
        logger.i { "Soft-deleting/Archiving schema: $schemaId" }
        queries.archiveSchema(schemaId)
    }

    override fun getActiveRecords(schemaId: String): Flow<List<DataRecord>> {
        return queries.getActiveRecordsBySchema(schemaId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities ->
                entities.map { DataRecord(it.id, it.schemaId, it.valuesMap, it.isArchived) }
            }
    }

    override suspend fun saveRecord(record: DataRecord) {
        logger.d { "Saving Record instance for schema: ${record.schemaId}" }
        queries.insertRecord(record.id, record.schemaId, record.values, record.isArchived)
    }

    override suspend fun archiveRecord(recordId: String) {
        logger.i { "Soft-deleting record instance: $recordId" }
        queries.archiveRecord(recordId)
    }
}