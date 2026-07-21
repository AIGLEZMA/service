package me.aiglez.service.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.aiglez.service.database.AppDatabase
import me.aiglez.service.domain.models.Template
import me.aiglez.service.domain.repository.TemplateRepository

class SqlDelightTemplateRepository(
    database: AppDatabase,
    private val logger: Logger,
) : TemplateRepository {

    private val queries = database.appDatabaseQueries

    override fun getActiveTemplates(schemaId: String): Flow<List<Template>> {
        return queries.getActiveTemplatesBySchema(schemaId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { templates ->
                templates.map {
                    Template(
                        id = it.id,
                        name = it.name,
                        targetSchemaId = it.targetSchemaId,
                        pageSize = it.pageSize,
                        elements = it.elements,
                        isArchived = it.isArchived,
                    )
                }
            }
    }

    override suspend fun saveTemplate(template: Template) = withContext(Dispatchers.Default) {
        logger.d { "Saving template: ${template.name} (${template.id})" }
        queries.insertTemplate(
            id = template.id,
            name = template.name,
            targetSchemaId = template.targetSchemaId,
            pageSize = template.pageSize,
            elements = template.elements,
            isArchived = template.isArchived,
        )
        Unit
    }

    override suspend fun archiveTemplate(templateId: String) = withContext(Dispatchers.Default) {
        logger.i { "Archiving template: $templateId" }
        queries.archiveTemplate(templateId)
        Unit
    }
}

