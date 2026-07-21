package me.aiglez.service.ui.records

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.aiglez.service.ui.components.EmptyRoutePlaceholder
import me.aiglez.service.ui.components.MetricCard
import me.aiglez.service.ui.components.TemplateCard
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    onOpenTemplate: (String) -> Unit,
    onCreateTemplate: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    DashboardContent(
        state = state,
        onOpenTemplate = onOpenTemplate,
        onCreateTemplate = onCreateTemplate,
    )
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onOpenTemplate: (String) -> Unit,
    onCreateTemplate: () -> Unit,
) {
    var showLoading by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) {
        showLoading = false
        if (state.isLoading) {
            delay(600)
            showLoading = true
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            MetricCard("MODÈLES", state.stats.totalTemplates.toString(), null, Modifier.weight(1f))
            MetricCard("MODÈLES DE DONNÉE", state.stats.totalSchemas.toString(), null, Modifier.weight(1f))
            MetricCard("DONNÉES", state.stats.totalRecords.toString(), null, Modifier.weight(1f))
            MetricCard("PDF GÉNÉRÉS", state.stats.generatedPdfs.toString(), null, Modifier.weight(1f))
        }
        Text(
            text = "Modèles enregistrés",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (showLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator()
                        Text("Chargement des modèles…")
                    }
                }
            }
        } else if (state.templates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyRoutePlaceholder(
                    title = "Aucun modèle pour le moment",
                    detail = "Créez un modèle pour générer des documents à partir de vos données.",
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                val gridState = rememberLazyGridState()
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    state = gridState,
                    contentPadding = PaddingValues(end = 12.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.templates, key = { it.template.id }) { item ->
                        TemplateCard(
                            title = item.template.name,
                            description = item.schemaName,
                            icon = if (item.template.elements.isEmpty()) Icons.Default.Tune else Icons.Default.Description,
                            onClick = { onOpenTemplate(item.template.id) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(gridState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                )
            }
        }
    }
}

