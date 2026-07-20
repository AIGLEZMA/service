package me.aiglez.service.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.aiglez.service.domain.models.Template
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.repository.RecordRepository
import me.aiglez.service.domain.repository.TemplateRepository

class SeededTemplateRepository(
    private val delegate: TemplateRepository,
    private val recordRepository: RecordRepository,
    private val logger: Logger,
) : TemplateRepository by delegate {

    private val seedMutex = Mutex()
    private var hasSeeded = false

    override fun getActiveTemplates(schemaId: String): Flow<List<Template>> {
        return delegate.getActiveTemplates(schemaId).onStart { seedIfNeeded() }
    }

    private suspend fun seedIfNeeded() = withContext(Dispatchers.Default) {
        seedMutex.withLock {
            if (hasSeeded) return@withLock

            val activeSchemaIds = recordRepository.getActiveSchemas().first().map { it.id }.toSet()
            frenchTemplatesBySchemaId.forEach { (schemaId, templates) ->
                if (schemaId !in activeSchemaIds) return@forEach
                val activeTemplateIds = delegate.getActiveTemplates(schemaId).first().map { it.id }.toSet()
                templates.filterNot { it.id in activeTemplateIds }.forEach { template ->
                    delegate.saveTemplate(template)
                }
            }
            hasSeeded = true
            logger.i { "Seeded French template library." }
        }
    }
}

private val frenchTemplatesBySchemaId by lazy {
    mapOf(
        ClientSchemaId to listOf(interventionReportTemplate),
    )
}

private val interventionReportTemplate = Template(
    id = "template_fiche_intervention",
    name = "Fiche d'intervention",
    targetSchemaId = ClientSchemaId,
    pageSize = "A4",
    elements = listOf(
        TemplateElement.Text(
            id = "fiche-title",
            name = "Titre",
            x = 48f,
            y = 42f,
            width = 360f,
            height = 42f,
            staticText = "FICHE D'INTERVENTION",
            fontSize = 22f,
            fontWeight = 700,
            color = "#0F172A",
        ),
        TemplateElement.Text(
            id = "fiche-date",
            name = "Date",
            x = 430f,
            y = 48f,
            width = 118f,
            height = 32f,
            staticText = "Date : ____/____/________",
            fontSize = 11f,
            fontWeight = 700,
            color = "#0F172A",
        ),
        TemplateElement.Rectangle(
            id = "client-panel",
            name = "Bloc client",
            x = 48f,
            y = 112f,
            width = 236f,
            height = 96f,
            fillColor = "#F8FAFC",
            borderColor = "#CBD5E1",
            borderWidth = 1f,
        ),
        TemplateElement.Text(
            id = "client-heading",
            name = "Titre client",
            x = 62f,
            y = 126f,
            width = 200f,
            height = 22f,
            staticText = "Client",
            fontSize = 13f,
            fontWeight = 700,
            color = "#003366",
        ),
        TemplateElement.Text(
            id = "client-details",
            name = "Infos client",
            x = 62f,
            y = 154f,
            width = 200f,
            height = 42f,
            staticText = "Nom : {{ Client.nom }}\nNuméro : {{ Client.numero }}",
            fontSize = 11f,
            lineHeight = 1.35f,
            color = "#0F172A",
        ),
        TemplateElement.Rectangle(
            id = "intervenant-panel",
            name = "Bloc intervenant",
            x = 312f,
            y = 112f,
            width = 236f,
            height = 96f,
            fillColor = "#F8FAFC",
            borderColor = "#CBD5E1",
            borderWidth = 1f,
        ),
        TemplateElement.Text(
            id = "intervenant-heading",
            name = "Titre intervenant",
            x = 326f,
            y = 126f,
            width = 200f,
            height = 22f,
            staticText = "Intervenant",
            fontSize = 13f,
            fontWeight = 700,
            color = "#003366",
        ),
        TemplateElement.Text(
            id = "intervenant-details",
            name = "Infos intervenant",
            x = 326f,
            y = 154f,
            width = 200f,
            height = 42f,
            staticText = "Nom : {{ Intervenant.nom }}\nPrénom : {{ Intervenant.prenom }}",
            fontSize = 11f,
            lineHeight = 1.35f,
            color = "#0F172A",
        ),
        TemplateElement.Rectangle(
            id = "time-panel",
            name = "Bloc horaires",
            x = 48f,
            y = 238f,
            width = 500f,
            height = 58f,
            fillColor = "#FFFFFF",
            borderColor = "#CBD5E1",
            borderWidth = 1f,
        ),
        TemplateElement.Text(
            id = "time-heading",
            name = "Titre horaires",
            x = 62f,
            y = 252f,
            width = 150f,
            height = 20f,
            staticText = "Temps d'intervention",
            fontSize = 12f,
            fontWeight = 700,
            color = "#003366",
        ),
        TemplateElement.Text(
            id = "time-details",
            name = "Horaires",
            x = 240f,
            y = 252f,
            width = 280f,
            height = 22f,
            staticText = "De ______ à ______",
            fontSize = 12f,
            fontWeight = 700,
            color = "#0F172A",
        ),
        TemplateElement.Rectangle(
            id = "notes-panel",
            name = "Bloc notes",
            x = 48f,
            y = 326f,
            width = 500f,
            height = 230f,
            fillColor = "#FFFFFF",
            borderColor = "#CBD5E1",
            borderWidth = 1f,
        ),
        TemplateElement.Text(
            id = "notes-heading",
            name = "Titre notes",
            x = 62f,
            y = 342f,
            width = 200f,
            height = 22f,
            staticText = "Notes d'intervention",
            fontSize = 13f,
            fontWeight = 700,
            color = "#003366",
        ),
        TemplateElement.Text(
            id = "notes-content",
            name = "Notes",
            x = 62f,
            y = 374f,
            width = 470f,
            height = 156f,
            staticText = "\n\n\n\n\n\n",
            fontSize = 11f,
            lineHeight = 1.35f,
            color = "#0F172A",
        ),
        TemplateElement.Text(
            id = "signature-client",
            name = "Signature client",
            x = 48f,
            y = 602f,
            width = 220f,
            height = 58f,
            staticText = "Signature client :",
            fontSize = 11f,
            fontWeight = 700,
            color = "#0F172A",
        ),
        TemplateElement.Line(
            id = "signature-client-line",
            name = "Ligne signature client",
            x1 = 48f,
            y1 = 662f,
            x2 = 268f,
            y2 = 662f,
            thickness = 1f,
        ),
        TemplateElement.Text(
            id = "signature-intervenant",
            name = "Signature intervenant",
            x = 328f,
            y = 602f,
            width = 220f,
            height = 58f,
            staticText = "Signature intervenant :",
            fontSize = 11f,
            fontWeight = 700,
            color = "#0F172A",
        ),
        TemplateElement.Line(
            id = "signature-intervenant-line",
            name = "Ligne signature intervenant",
            x1 = 328f,
            y1 = 662f,
            x2 = 548f,
            y2 = 662f,
            thickness = 1f,
        ),
    ),
)
