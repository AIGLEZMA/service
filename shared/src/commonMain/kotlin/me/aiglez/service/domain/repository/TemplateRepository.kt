package me.aiglez.service.domain.repository

import kotlinx.coroutines.flow.Flow
import me.aiglez.service.domain.models.Template

interface TemplateRepository {
    fun getActiveTemplates(schemaId: String): Flow<List<Template>>
    suspend fun saveTemplate(template: Template)
    suspend fun archiveTemplate(templateId: String)
}


