package me.aiglez.service.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import me.aiglez.service.data.database.elementsAdapter
import me.aiglez.service.data.database.fieldsAdapter
import me.aiglez.service.data.database.valuesMapAdapter
import me.aiglez.service.database.AppDatabase
import me.aiglez.service.database.RecordEntity
import me.aiglez.service.database.SchemaEntity
import me.aiglez.service.database.TemplateEntity
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField

class SqlDelightRecordRepositoryTest {

    @Test
    fun `updates a schema that already has records`() = runBlocking {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            AppDatabase.Schema.create(driver)
            driver.execute(null, "PRAGMA foreign_keys = ON", 0)
            val repository = SqlDelightRecordRepository(
                database = AppDatabase(
                    driver = driver,
                    SchemaEntityAdapter = SchemaEntity.Adapter(fieldsAdapter),
                    RecordEntityAdapter = RecordEntity.Adapter(valuesMapAdapter),
                    TemplateEntityAdapter = TemplateEntity.Adapter(elementsAdapter),
                ),
                logger = Logger(StaticConfig(logWriterList = emptyList()), "RepositoryTest"),
            )
            val schema = DataSchema(
                id = "customers",
                name = "Customers",
                fields = listOf(SchemaField("name", "Name", "name", FieldType.TEXT)),
            )
            repository.saveSchema(schema)
            repository.saveRecord(
                DataRecord(
                    id = "customer-1",
                    schemaId = schema.id,
                    values = mapOf("name" to "Ada"),
                ),
            )

            repository.saveSchema(schema.copy(name = "Clients"))

            assertEquals("Clients", repository.getActiveSchemas().first().single().name)
            assertEquals("Ada", repository.getActiveRecords(schema.id).first().single().values["name"])
        } finally {
            driver.close()
        }
    }
}
