package me.aiglez.service.domain.repository

import kotlinx.coroutines.flow.Flow
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema

interface RecordRepository {
    // Schema operations
    fun getActiveSchemas(): Flow<List<DataSchema>>
    suspend fun saveSchema(schema: DataSchema)
    suspend fun archiveSchema(schemaId: String)

    // Record operations
    fun getActiveRecords(schemaId: String): Flow<List<DataRecord>>
    suspend fun saveRecord(record: DataRecord)
    suspend fun archiveRecord(recordId: String)
}