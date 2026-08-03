package me.aiglez.service.ui.records

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.aiglez.service.data.csv.CsvImportIssue
import me.aiglez.service.data.csv.CsvImportIssueSeverity
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField

@Composable
internal fun CsvImportDialog(
    schema: DataSchema,
    state: CsvImportUiState,
    onMappingChange: (String, String?) -> Unit,
    onChooseFile: () -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit,
) {
    val source = state.source
    val preview = state.preview
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.width(860.dp).heightIn(max = 720.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Column {
                CsvImportHeader(schema.name, state, onChooseFile)
                HorizontalDivider()
                if (source == null) {
                    CsvImportError(state.error.orEmpty(), onChooseFile)
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false).fillMaxWidth(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SummaryChip("${source.headers.size} colonnes")
                                SummaryChip("${source.rows.size} lignes")
                                SummaryChip("${preview?.records?.size ?: 0} importables")
                                SummaryChip("${preview?.errors?.size ?: 0} erreurs")
                            }
                        }
                        item {
                            SectionHeading(
                                title = "Association des colonnes",
                                detail = "Vérifiez la colonne utilisée pour chaque champ du modèle.",
                            )
                        }
                        items(schema.fields, key = { it.id }) { field ->
                            CsvFieldMappingRow(
                                field = field,
                                headers = source.headers,
                                selectedColumn = state.mappings[field.id],
                                onSelectedColumn = { onMappingChange(field.id, it) },
                            )
                        }
                        item {
                            SectionHeading(
                                title = "Contrôle avant import",
                                detail = "Les erreurs doivent être corrigées avant de continuer.",
                            )
                        }
                        if (preview?.issues.isNullOrEmpty() && state.error == null) {
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(
                                        "Aucune erreur détectée. Les données sont prêtes à être importées.",
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        } else {
                            state.error?.let { error ->
                                item { CsvIssueCard(null, error) }
                            }
                            items(preview?.issues.orEmpty().take(12)) { issue ->
                                CsvIssueCard(issue, issue.message)
                            }
                            val remaining = (preview?.issues?.size ?: 0) - 12
                            if (remaining > 0) {
                                item {
                                    Text(
                                        "+ $remaining autre(s) problème(s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
                HorizontalDivider()
                CsvImportFooter(state, onImport, onDismiss)
            }
        }
    }
}

@Composable
private fun CsvImportHeader(
    schemaName: String,
    state: CsvImportUiState,
    onChooseFile: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("IMPORT CSV", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(schemaName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                state.source?.let {
                    "${it.fileName} · séparateur ${it.delimiter.displayName()} · ${it.rows.size} ligne(s)"
                } ?: "Choisissez un fichier CSV à importer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        OutlinedButton(onClick = onChooseFile, enabled = !state.isImporting) {
            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (state.source == null) "Choisir un fichier" else "Changer de fichier")
        }
    }
}

@Composable
private fun CsvImportError(message: String, onChooseFile: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Impossible de préparer l'import", style = MaterialTheme.typography.titleMedium)
        Text(message, color = MaterialTheme.colorScheme.error)
        Button(onClick = onChooseFile) { Text("Choisir un autre fichier") }
    }
}

@Composable
private fun SummaryChip(label: String) {
    AssistChip(onClick = {}, label = { Text(label) }, shape = RoundedCornerShape(6.dp))
}

@Composable
private fun SectionHeading(title: String, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CsvFieldMappingRow(
    field: SchemaField,
    headers: List<String>,
    selectedColumn: String?,
    onSelectedColumn: (String?) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(field.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(field.type.importLabel(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            CsvColumnSelector(headers, selectedColumn, onSelectedColumn)
        }
    }
}

@Composable
private fun CsvColumnSelector(
    headers: List<String>,
    selectedColumn: String?,
    onSelectedColumn: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.width(280.dp)) {
            Text(selectedColumn ?: "Ne pas importer", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Ne pas importer") },
                onClick = { onSelectedColumn(null); expanded = false },
            )
            headers.forEach { header ->
                DropdownMenuItem(
                    text = { Text(header) },
                    onClick = { onSelectedColumn(header); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun CsvIssueCard(issue: CsvImportIssue?, message: String) {
    val isError = issue == null || issue.severity == CsvImportIssueSeverity.Error
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                issue?.contextLabel() ?: "Erreur",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
            Text(message, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CsvImportFooter(state: CsvImportUiState, onImport: () -> Unit, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onDismiss, enabled = !state.isImporting) { Text("Annuler") }
        Spacer(Modifier.width(10.dp))
        Button(onClick = onImport, enabled = state.preview?.canImport == true && !state.isImporting) {
            if (state.isImporting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(if (state.isImporting) "Import en cours…" else "Importer ${state.preview?.records?.size ?: 0} ligne(s)")
        }
    }
}

private fun CsvImportIssue.contextLabel(): String = listOfNotNull(
    rowNumber?.let { "Ligne $it" },
    fieldName?.let { "Champ $it" },
    columnName?.let { "Colonne $it" },
).joinToString(" · ").ifBlank { "Configuration" }

private fun Char.displayName(): String = when (this) {
    '\t' -> "tabulation"
    ';' -> "point-virgule"
    else -> "virgule"
}

private fun FieldType.importLabel(): String = when (this) {
    FieldType.TEXT -> "Texte"
    FieldType.NUMBER -> "Nombre entier"
    FieldType.DOUBLE -> "Nombre décimal"
    FieldType.REFERENCE -> "Référence"
    FieldType.LIST -> "Liste"
}
