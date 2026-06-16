package me.aiglez.service.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import me.aiglez.service.domain.models.Template
import me.aiglez.service.domain.repository.RecordRepository
import me.aiglez.service.domain.repository.TemplateRepository

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
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val recordRepository: RecordRepository,
    private val templateRepository: TemplateRepository,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = recordRepository.getActiveSchemas()
        .flatMapLatest { schemas ->
            if (schemas.isEmpty()) {
                flowOf(DashboardUiState())
            } else {
                val templateFlows = schemas.map { schema ->
                    templateRepository.getActiveTemplates(schema.id).map { templates ->
                        schema to templates
                    }
                }
                val recordFlows = schemas.map { schema ->
                    recordRepository.getActiveRecords(schema.id).map { records ->
                        schema.id to records.size
                    }
                }
                combine(
                    combine(templateFlows) { it.toList() },
                    combine(recordFlows) { it.toList() },
                ) { templatePairs, recordPairs ->
                    val templates = templatePairs.flatMap { (schema, schemaTemplates) ->
                        schemaTemplates.map { DashboardTemplateItem(it, schema.name) }
                    }
                    DashboardUiState(
                        stats = DashboardStatsState(
                            totalTemplates = templates.size,
                            totalSchemas = schemas.size,
                            totalRecords = recordPairs.sumOf { it.second },
                            generatedPdfs = 0,
                        ),
                        templates = templates,
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
