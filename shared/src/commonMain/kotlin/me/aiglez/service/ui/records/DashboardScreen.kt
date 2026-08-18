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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import me.aiglez.service.ui.components.TemplateCardAction
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    onOpenTemplate: (String) -> Unit,
    onCreateTemplate: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var renameTarget by remember { mutableStateOf<DashboardTemplateItem?>(null) }
    var renameValue by remember { mutableStateOf("") }
    var archiveTarget by remember { mutableStateOf<DashboardTemplateItem?>(null) }
    var deleteTarget by remember { mutableStateOf<DashboardTemplateItem?>(null) }
    val feedback = state.errorMessage ?: state.message

    LaunchedEffect(feedback) {
        if (feedback != null) {
            snackbarHostState.showSnackbar(feedback)
            viewModel.clearFeedback()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DashboardContent(
            state = state,
            onOpenTemplate = onOpenTemplate,
            onCreateTemplate = onCreateTemplate,
            onRenameTemplate = { item ->
                renameTarget = item
                renameValue = item.template.name
            },
            onDuplicateTemplate = { viewModel.duplicateTemplate(it.template) },
            onArchiveTemplate = { archiveTarget = it },
            onRestoreTemplate = { viewModel.restoreTemplate(it.template) },
            onDeleteTemplate = { deleteTarget = it },
        )
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    renameTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Renommer le modèle") },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = { Text("Nom") },
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameTemplate(item.template, renameValue)
                        renameTarget = null
                    },
                    enabled = renameValue.isNotBlank() && !state.isActionInProgress,
                ) { Text("Renommer") }
            },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("Annuler") } },
        )
    }

    archiveTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { archiveTarget = null },
            title = { Text("Archiver le modèle ?") },
            text = { Text("\"${item.template.name}\" sera déplacé dans les modèles archivés.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.archiveTemplate(item.template)
                        archiveTarget = null
                    },
                    enabled = !state.isActionInProgress,
                ) { Text("Archiver") }
            },
            dismissButton = { TextButton(onClick = { archiveTarget = null }) { Text("Annuler") } },
        )
    }

    deleteTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Supprimer définitivement ?") },
            text = { Text("\"${item.template.name}\" et sa mise en page seront supprimés sans possibilité de restauration.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTemplate(item.template)
                        deleteTarget = null
                    },
                    enabled = !state.isActionInProgress,
                ) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Annuler") } },
        )
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onOpenTemplate: (String) -> Unit,
    onCreateTemplate: () -> Unit,
    onRenameTemplate: (DashboardTemplateItem) -> Unit,
    onDuplicateTemplate: (DashboardTemplateItem) -> Unit,
    onArchiveTemplate: (DashboardTemplateItem) -> Unit,
    onRestoreTemplate: (DashboardTemplateItem) -> Unit,
    onDeleteTemplate: (DashboardTemplateItem) -> Unit,
) {
    var showLoading by remember { mutableStateOf(false) }
    var showArchived by remember { mutableStateOf(false) }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (showArchived) "Modèles archivés" else "Modèles enregistrés",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !showArchived,
                    onClick = { showArchived = false },
                    label = { Text("Actifs (${state.templates.size})") },
                )
                FilterChip(
                    selected = showArchived,
                    onClick = { showArchived = true },
                    label = { Text("Archivés (${state.archivedTemplates.size})") },
                )
            }
        }
        val displayedTemplates = if (showArchived) state.archivedTemplates else state.templates
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
        } else if (displayedTemplates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    EmptyRoutePlaceholder(
                        title = if (showArchived) "Aucun modèle archivé" else "Aucun modèle pour le moment",
                        detail = if (showArchived) {
                            "Les modèles archivés pourront être restaurés depuis cet espace."
                        } else {
                            "Créez un modèle pour générer des documents à partir de vos données."
                        },
                    )
                    if (!showArchived) {
                        Button(onClick = onCreateTemplate) { Text("Créer un modèle") }
                    }
                }
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
                    items(displayedTemplates, key = { it.template.id }) { item ->
                        TemplateCard(
                            title = item.template.name,
                            description = item.schemaName,
                            icon = if (item.template.elements.isEmpty()) Icons.Default.Tune else Icons.Default.Description,
                            onClick = if (showArchived) null else ({ onOpenTemplate(item.template.id) }),
                            modifier = Modifier.fillMaxWidth(),
                            actions = if (showArchived) {
                                listOf(
                                    TemplateCardAction("Restaurer") { onRestoreTemplate(item) },
                                    TemplateCardAction("Supprimer définitivement") { onDeleteTemplate(item) },
                                )
                            } else {
                                listOf(
                                    TemplateCardAction("Renommer") { onRenameTemplate(item) },
                                    TemplateCardAction("Dupliquer") { onDuplicateTemplate(item) },
                                    TemplateCardAction("Archiver") { onArchiveTemplate(item) },
                                )
                            },
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
