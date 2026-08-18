package me.aiglez.service.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.aiglez.service.domain.models.Template
import me.aiglez.service.domain.repository.RecordRepository
import me.aiglez.service.domain.repository.TemplateRepository
import me.aiglez.service.ui.common.newUiId

data class DashboardStatsState(
    val totalTemplates: Int = 0,
    val totalSchemas: Int = 0,
    val totalRecords: Int = 0,
    val generatedPdfs: Int = 0,
)

data class DashboardTemplateItem(
    val template: Template,
    val schemaName: String,
)

data class DashboardUiState(
    val stats: DashboardStatsState = DashboardStatsState(),
    val templates: List<DashboardTemplateItem> = emptyList(),
    val archivedTemplates: List<DashboardTemplateItem> = emptyList(),
    val isLoading: Boolean = true,
    val isActionInProgress: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)

private data class DashboardActionState(
    val isInProgress: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val recordRepository: RecordRepository,
    private val templateRepository: TemplateRepository,
    private val logger: Logger,
) : ViewModel() {

    private val actionState = MutableStateFlow(DashboardActionState())

    private val dashboardData = recordRepository.getActiveSchemas()
        .flatMapLatest { schemas ->
            if (schemas.isEmpty()) {
                flowOf(DashboardUiState(isLoading = false))
            } else {
                val templateFlows = schemas.map { schema ->
                    combine(
                        templateRepository.getActiveTemplates(schema.id),
                        templateRepository.getArchivedTemplates(schema.id),
                    ) { active, archived -> Triple(schema, active, archived) }
                }
                val recordFlows = schemas.map { schema ->
                    recordRepository.getActiveRecords(schema.id)
                        .map { records -> schema.id to records.size }
                }
                combine(
                    combine(templateFlows) { it.toList() },
                    combine(recordFlows) { it.toList() },
                ) { templatePairs, recordPairs ->
                    val templates = templatePairs.flatMap { (schema, active, _) ->
                        active.map { DashboardTemplateItem(it, schema.name) }
                    }
                    val archivedTemplates = templatePairs.flatMap { (schema, _, archived) ->
                        archived.map { DashboardTemplateItem(it, schema.name) }
                    }
                    DashboardUiState(
                        stats = DashboardStatsState(
                            totalTemplates = templates.size,
                            totalSchemas = schemas.size,
                            totalRecords = recordPairs.sumOf { it.second },
                            generatedPdfs = 0,
                        ),
                        templates = templates,
                        archivedTemplates = archivedTemplates,
                        isLoading = false,
                    )
                }
            }
        }

    val uiState: StateFlow<DashboardUiState> = combine(dashboardData, actionState) { data, action ->
        data.copy(
            isActionInProgress = action.isInProgress,
            message = action.message,
            errorMessage = action.errorMessage,
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun renameTemplate(template: Template, name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank() || trimmedName == template.name) return
        runTemplateAction("Modèle renommé.") {
            templateRepository.saveTemplate(template.copy(name = trimmedName))
        }
    }

    fun duplicateTemplate(template: Template) {
        runTemplateAction("Modèle dupliqué.") {
            templateRepository.saveTemplate(
                template.copy(
                    id = newUiId("template"),
                    name = "Copie de ${template.name}",
                    isArchived = false,
                ),
            )
        }
    }

    fun archiveTemplate(template: Template) {
        runTemplateAction("Modèle archivé.") {
            templateRepository.archiveTemplate(template.id)
        }
    }

    fun restoreTemplate(template: Template) {
        runTemplateAction("Modèle restauré.") {
            templateRepository.restoreTemplate(template.id)
        }
    }

    fun deleteTemplate(template: Template) {
        runTemplateAction("Modèle supprimé définitivement.") {
            templateRepository.deleteTemplate(template.id)
        }
    }

    fun clearFeedback() {
        actionState.value = DashboardActionState()
    }

    private fun runTemplateAction(successMessage: String, action: suspend () -> Unit) {
        if (actionState.value.isInProgress) return
        viewModelScope.launch {
            actionState.value = DashboardActionState(isInProgress = true)
            try {
                action()
                actionState.value = DashboardActionState(message = successMessage)
            } catch (cause: Throwable) {
                logger.e(cause) { "Template action failed" }
                actionState.value = DashboardActionState(errorMessage = "L’opération a échoué. Réessayez.")
            }
        }
    }
}
