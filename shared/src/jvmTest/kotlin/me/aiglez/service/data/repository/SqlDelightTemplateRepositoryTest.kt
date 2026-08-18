package me.aiglez.service.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.aiglez.service.data.database.elementsAdapter
import me.aiglez.service.data.database.fieldsAdapter
import me.aiglez.service.data.database.valuesMapAdapter
import me.aiglez.service.database.AppDatabase
import me.aiglez.service.database.RecordEntity
import me.aiglez.service.database.SchemaEntity
import me.aiglez.service.database.TemplateEntity
import me.aiglez.service.domain.models.Template

class SqlDelightTemplateRepositoryTest {
    @Test
    fun archivesRestoresAndDeletesTemplate() = runBlocking {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            AppDatabase.Schema.create(driver)
            val repository = SqlDelightTemplateRepository(
                database = AppDatabase(
                    driver = driver,
                    SchemaEntityAdapter = SchemaEntity.Adapter(fieldsAdapter),
                    RecordEntityAdapter = RecordEntity.Adapter(valuesMapAdapter),
                    TemplateEntityAdapter = TemplateEntity.Adapter(elementsAdapter),
                ),
                logger = Logger(StaticConfig(logWriterList = emptyList()), "RepositoryTest"),
            )
            val template = Template(
                id = "template-1",
                name = "Invoice",
                targetSchemaId = "schema-1",
                pageSize = "A4",
                elements = emptyList(),
            )
            repository.saveTemplate(template)

            repository.saveTemplate(template.copy(name = "Updated invoice"))
            assertEquals("Updated invoice", repository.getActiveTemplates(template.targetSchemaId).first().single().name)

            val duplicate = template.copy(id = "template-2", name = "Copy of invoice")
            repository.saveTemplate(duplicate)
            assertEquals(2, repository.getActiveTemplates(template.targetSchemaId).first().size)
            repository.deleteTemplate(duplicate.id)

            repository.archiveTemplate(template.id)

            assertTrue(repository.getActiveTemplates(template.targetSchemaId).first().isEmpty())
            assertEquals(template.id, repository.getArchivedTemplates(template.targetSchemaId).first().single().id)

            repository.restoreTemplate(template.id)

            assertEquals(template.id, repository.getActiveTemplates(template.targetSchemaId).first().single().id)
            assertTrue(repository.getArchivedTemplates(template.targetSchemaId).first().isEmpty())

            repository.archiveTemplate(template.id)
            repository.deleteTemplate(template.id)

            assertTrue(repository.getArchivedTemplates(template.targetSchemaId).first().isEmpty())
        } finally {
            driver.close()
        }
    }
}
