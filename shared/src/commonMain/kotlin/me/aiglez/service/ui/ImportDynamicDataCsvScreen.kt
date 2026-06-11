package me.aiglez.service.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import me.aiglez.service.data.dynamicdata.CsvImportIssue
import me.aiglez.service.data.dynamicdata.CsvImportIssueSeverity
import me.aiglez.service.data.dynamicdata.CsvImportPreview
import me.aiglez.service.data.dynamicdata.CsvImportReadResult
import me.aiglez.service.data.dynamicdata.CsvImportSource
import me.aiglez.service.data.dynamicdata.DynamicData
import me.aiglez.service.data.dynamicdata.DynamicDataCsvImporter
import me.aiglez.service.data.dynamicdata.DynamicDataField
import me.aiglez.service.data.dynamicdata.DynamicDataFieldType
import me.aiglez.service.data.dynamicdata.DynamicDataInstance
import me.aiglez.service.io.CsvFilePicker

@Composable
fun ImportDynamicDataCsvScreen(
    dynamicData: DynamicData,
    onImport: (List<DynamicDataInstance>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var source by remember { mutableStateOf<CsvImportSource?>(null) }
    var readError by remember { mutableStateOf<String?>(null) }
    var mappings by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }

    val preview = remember(dynamicData, source, mappings) {
        source?.let {
            DynamicDataCsvImporter.preview(
                dynamicData = dynamicData,
                source = it,
                mappings = mappings,
            )
        }
    }

    fun pickCsvFile() {
        readError = null
        val selection = runCatching { CsvFilePicker.pickCsvFile() }.getOrElse { error ->
            readError = "Impossible d'ouvrir le fichier CSV: ${error.message ?: "erreur inconnue"}"
            return
        } ?: return

        when (val result = DynamicDataCsvImporter.read(selection.fileName, selection.content)) {
            is CsvImportReadResult.Success -> {
                source = result.source
                mappings = DynamicDataCsvImporter.suggestMappings(dynamicData, result.source)
            }
            is CsvImportReadResult.Failure -> {
                source = null
                mappings = emptyMap()
                readError = result.message
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 980.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ImportHeader(
                dynamicData = dynamicData,
                source = source,
                readError = readError,
                onPickCsvFile = ::pickCsvFile,
            )

            if (source == null) {
                EmptyImportState(onPickCsvFile = ::pickCsvFile)
            } else {
                CsvImportContent(
                    dynamicData = dynamicData,
                    source = source!!,
                    mappings = mappings,
                    preview = preview,
                    onMappingChange = { fieldName, columnName ->
                        mappings = mappings + (fieldName to columnName)
                    },
                    modifier = Modifier.weight(1f),
                )

                ImportFooter(
                    preview = preview,
                    onImport = {
                        preview?.instances?.takeIf { preview.canImport }?.let(onImport)
                    },
                )
            }
        }
    }
}

@Composable
private fun ImportHeader(
    dynamicData: DynamicData,
    source: CsvImportSource?,
    readError: String?,
    onPickCsvFile: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "IMPORT CSV",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = dynamicData.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = readError ?: source?.let {
                        "Fichier ${it.fileName} chargé avec ${it.rows.size} ligne(s), séparateur '${it.delimiter.toDisplayDelimiter()}'."
                    } ?: "Sélectionnez un CSV, associez les colonnes aux champs, puis validez les erreurs avant import.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (readError == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Button(
                onClick = onPickCsvFile,
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (source == null) "Choisir un CSV" else "Changer de CSV")
            }
        }
    }
}

@Composable
private fun EmptyImportState(onPickCsvFile: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Aucun fichier sélectionné",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "La première ligne du CSV doit contenir les en-têtes de colonnes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedButton(
                onClick = onPickCsvFile,
                shape = RoundedCornerShape(4.dp),
            ) {
                Text("Parcourir")
            }
        }
    }
}

@Composable
private fun CsvImportContent(
    dynamicData: DynamicData,
    source: CsvImportSource,
    mappings: Map<String, String?>,
    preview: CsvImportPreview?,
    onMappingChange: (String, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 2.dp),
    ) {
        item {
            CsvSummary(source = source, preview = preview)
        }

        item {
            SectionTitle(
                title = "Association des colonnes",
                detail = "Chaque champ obligatoire doit pointer vers une colonne CSV.",
            )
        }

        items(dynamicData.fields, key = { it.name }) { field ->
            FieldMappingRow(
                field = field,
                headers = source.headers,
                selectedColumn = mappings[field.name],
                onSelectedColumn = { onMappingChange(field.name, it) },
            )
        }

        item {
            SectionTitle(
                title = "Contrôles avant import",
                detail = "Les erreurs bloquent l'import. Les avertissements documentent les champs ignorés.",
            )
        }

        if (preview?.issues.isNullOrEmpty()) {
            item {
                IssueEmptyState()
            }
        } else {
            items(preview.issues.take(10)) { issue ->
                IssueRow(issue = issue)
            }
            if (preview.issues.size > 10) {
                item {
                    Text(
                        text = "+ ${preview.issues.size - 10} autre(s) problème(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CsvSummary(
    source: CsvImportSource,
    preview: CsvImportPreview?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AssistChip(
                onClick = {},
                label = { Text("${source.headers.size} colonne(s)") },
                shape = RoundedCornerShape(4.dp),
            )
            AssistChip(
                onClick = {},
                label = { Text("${source.rows.size} ligne(s)") },
                shape = RoundedCornerShape(4.dp),
            )
            AssistChip(
                onClick = {},
                label = { Text("${preview?.instances?.size ?: 0} importable(s)") },
                shape = RoundedCornerShape(4.dp),
            )
            AssistChip(
                onClick = {},
                label = { Text("${preview?.errors?.size ?: 0} erreur(s)") },
                shape = RoundedCornerShape(4.dp),
            )
            AssistChip(
                onClick = {},
                label = { Text("${preview?.warnings?.size ?: 0} avertissement(s)") },
                shape = RoundedCornerShape(4.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    detail: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FieldMappingRow(
    field: DynamicDataField,
    headers: List<String>,
    selectedColumn: String?,
    onSelectedColumn: (String?) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = field.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${field.type.toImportLabel()} · ${if (field.optional) "Optionnel" else "Obligatoire"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            ColumnSelector(
                headers = headers,
                selectedColumn = selectedColumn,
                onSelectedColumn = onSelectedColumn,
            )
        }
    }
}

@Composable
private fun ColumnSelector(
    headers: List<String>,
    selectedColumn: String?,
    onSelectedColumn: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.width(280.dp),
        ) {
            Text(
                text = selectedColumn ?: "Ne pas importer",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Ne pas importer") },
                onClick = {
                    onSelectedColumn(null)
                    expanded = false
                },
            )
            headers.forEach { header ->
                DropdownMenuItem(
                    text = { Text(header) },
                    onClick = {
                        onSelectedColumn(header)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun IssueEmptyState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(
            text = "Aucune erreur détectée. Les lignes peuvent être importées.",
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun IssueRow(issue: CsvImportIssue) {
    val isError = issue.severity == CsvImportIssueSeverity.Error
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = if (isError) "Erreur" else "Avertissement",
                style = MaterialTheme.typography.labelSmall,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = issue.contextLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = issue.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ImportFooter(
    preview: CsvImportPreview?,
    onImport: () -> Unit,
) {
    val canImport = preview?.canImport == true
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (canImport) {
                        "${preview.instances.size} entrée(s) prête(s)"
                    } else {
                        "Import bloqué"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (canImport) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
                Text(
                    text = if (canImport) {
                        "Les nouvelles entrées seront ajoutées au modèle sélectionné."
                    } else {
                        "Corrigez les erreurs de mapping ou de données avant d'importer."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = onImport,
                enabled = canImport,
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Text("Importer")
            }
        }
    }
}

private fun CsvImportIssue.contextLabel(): String {
    val parts = listOfNotNull(
        rowNumber?.let { "Ligne $it" },
        fieldName?.let { "Champ $it" },
        columnName?.let { "Colonne $it" },
    )
    return parts.joinToString(" · ").ifBlank { "Configuration" }
}

private fun Char.toDisplayDelimiter(): String {
    return if (this == '\t') "\\t" else toString()
}

private fun DynamicDataFieldType.toImportLabel(): String {
    return when (this) {
        DynamicDataFieldType.Text -> "Texte"
        DynamicDataFieldType.Number -> "Nombre entier"
        DynamicDataFieldType.Decimal -> "Nombre décimal"
        DynamicDataFieldType.Boolean -> "Booléen"
        is DynamicDataFieldType.DynamicDataRef -> "Référence"
        is DynamicDataFieldType.ListOf -> "Liste"
    }
}
