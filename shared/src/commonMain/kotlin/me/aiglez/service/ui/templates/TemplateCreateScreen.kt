package me.aiglez.service.ui.templates

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

private val SupportedPageSizes = listOf("A4", "A5", "Letter")

@Composable
fun TemplateCreateScreen(
    onCreated: (String) -> Unit,
    onCancel: () -> Unit,
    onCreateSchema: () -> Unit,
    viewModel: TemplateCreateViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    TemplateCreateContent(
        state = state,
        onNameChange = viewModel::updateName,
        onSchemaSelected = viewModel::selectSchema,
        onPageSizeSelected = viewModel::selectPageSize,
        onStartingPointSelected = viewModel::selectStartingPoint,
        onCreate = { viewModel.create(onCreated) },
        onCancel = onCancel,
        onCreateSchema = onCreateSchema,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemplateCreateContent(
    state: TemplateCreateUiState,
    onNameChange: (String) -> Unit,
    onSchemaSelected: (String) -> Unit,
    onPageSizeSelected: (String) -> Unit,
    onStartingPointSelected: (TemplateStartingPoint) -> Unit,
    onCreate: () -> Unit,
    onCancel: () -> Unit,
    onCreateSchema: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Nouveau modèle PDF", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Choisissez ses paramètres avant d’ouvrir l’éditeur.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (state.isLoading) {
                    Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.schemas.isEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Créez d’abord un modèle de données pour pouvoir lier des champs au document.")
                        Button(onClick = onCreateSchema) { Text("Créer un modèle de données") }
                    }
                } else {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nom du modèle") },
                        placeholder = { Text("ex : Facture client") },
                        singleLine = true,
                    )

                    LabeledSection("Modèle de données principal") {
                        SchemaSelector(
                            selectedSchemaId = state.targetSchemaId,
                            schemas = state.schemas.map { it.id to it.name },
                            onSelected = onSchemaSelected,
                        )
                    }

                    LabeledSection("Format de page") {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SupportedPageSizes.forEach { pageSize ->
                                FilterChip(
                                    selected = state.pageSize == pageSize,
                                    onClick = { onPageSizeSelected(pageSize) },
                                    label = { Text(pageSize) },
                                )
                            }
                        }
                    }

                    LabeledSection("Point de départ") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StartingPointButton(
                                title = "Document vierge",
                                icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                                selected = state.startingPoint == TemplateStartingPoint.Blank,
                                onClick = { onStartingPointSelected(TemplateStartingPoint.Blank) },
                                modifier = Modifier.weight(1f),
                            )
                            StartingPointButton(
                                title = "Mise en page simple",
                                icon = Icons.Default.Description,
                                selected = state.startingPoint == TemplateStartingPoint.Simple,
                                onClick = { onStartingPointSelected(TemplateStartingPoint.Simple) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    state.errorMessage?.let { message ->
                        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onCancel, enabled = !state.isSaving) { Text("Annuler") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onCreate, enabled = state.canCreate) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.isSaving) "Création…" else "Créer et ouvrir")
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun SchemaSelector(
    selectedSchemaId: String,
    schemas: List<Pair<String, String>>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(schemas.firstOrNull { it.first == selectedSchemaId }?.second ?: "Choisir")
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            schemas.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelected(id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun StartingPointButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        border = if (selected) null else ButtonDefaults.outlinedButtonBorder(enabled = true),
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(title)
    }
}
