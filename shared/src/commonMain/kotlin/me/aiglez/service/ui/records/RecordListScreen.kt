package me.aiglez.service.ui.records

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.SchemaField
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val RecordColumnWidth = 172.dp
private val RecordActionColumnWidth = 56.dp
private const val DefaultVisibleColumnCount = 5

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTooltip(
    text: String,
    content: @Composable () -> Unit
) {
    TooltipArea(
        tooltip = {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                tonalElevation = 4.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        },
        delayMillis = 500,
        content = content
    )
}

@Composable
fun RecordListScreen(
    schemaId: String,
    onCreateRecord: () -> Unit,
) {
    val viewModel: RecordListViewModel = koinViewModel(
        key = "record-list-$schemaId",
        parameters = { parametersOf(schemaId) },
    )
    val state by viewModel.uiState.collectAsState()
    var pendingArchive by remember { mutableStateOf<DataRecord?>(null) }

    RecordListContent(
        state = state,
        onCreateRecord = onCreateRecord,
        onImportCsv = viewModel::onImportCsvClicked,
        onArchiveRecord = { pendingArchive = it },
    )

    val record = pendingArchive
    if (record != null) {
        AlertDialog(
            onDismissRequest = { pendingArchive = null },
            title = { Text("Archiver la donnée ?") },
            text = { Text("Cela masquera la donnée sélectionnée dans le registre actif.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.archiveRecord(record.id)
                        pendingArchive = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Archiver")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingArchive = null }) {
                    Text("Annuler")
                }
            },
        )
    }
}

@Composable
private fun RecordListContent(
    state: RecordListUiState,
    onCreateRecord: () -> Unit,
    onImportCsv: () -> Unit,
    onArchiveRecord: (DataRecord) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    val filteredRecords = remember(state.records, searchQuery) {
        if (searchQuery.isBlank()) {
            state.records
        } else {
            state.records.filter { record ->
                record.values.values.any { it.contains(searchQuery, ignoreCase = true) }
            }
        }
    }
    val fields = state.schema?.fields.orEmpty()
    val fieldIds = remember(fields) { fields.map { it.id } }
    var visibleFieldIds by remember(fieldIds) {
        mutableStateOf(fields.take(DefaultVisibleColumnCount).map { it.id }.toSet())
    }
    val visibleFields = remember(fields, visibleFieldIds) {
        fields.filter { it.id in visibleFieldIds }.ifEmpty { fields.take(1) }
    }
    var selectedRecordId by remember(state.schema?.id) { mutableStateOf<String?>(null) }
    val selectedRecord = remember(state.records, selectedRecordId) {
        state.records.firstOrNull { it.id == selectedRecordId }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val listState = rememberLazyListState()
        Box(
            modifier = Modifier.widthIn(max = 1280.dp).fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                RecordHeader(
                    schemaName = state.schema?.name ?: "Données",
                    fieldCount = state.schema?.fields?.size ?: 0,
                    recordCount = state.records.size,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Rechercher dans les données...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                AppTooltip(text = "Effacer la recherche") {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    )

                    ColumnPickerButton(
                        fields = fields,
                        visibleFieldIds = visibleFieldIds,
                        onToggleField = { field ->
                            visibleFieldIds = if (field.id in visibleFieldIds) {
                                if (visibleFieldIds.size > 1) visibleFieldIds - field.id else visibleFieldIds
                            } else {
                                visibleFieldIds + field.id
                            }
                        },
                    )

                    AppTooltip(text = "Importer des données depuis un fichier CSV") {
                        OutlinedButton(
                            onClick = onImportCsv,
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Importer CSV")
                        }
                    }

                    AppTooltip(text = "Ajouter une nouvelle entrée à ce modèle") {
                        Button(
                            onClick = onCreateRecord,
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Ajouter")
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (state.records.isEmpty()) {
                        EmptyState(
                            isSearchActive = searchQuery.isNotEmpty(),
                            onCreateClick = onCreateRecord,
                            onClearSearch = { searchQuery = "" }
                        )
                    } else if (filteredRecords.isEmpty()) {
                        EmptyState(
                            isSearchActive = true,
                            onCreateClick = onCreateRecord,
                            onClearSearch = { searchQuery = "" }
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            RecordTable(
                                schema = state.schema,
                                visibleFields = visibleFields,
                                records = filteredRecords,
                                selectedRecordId = selectedRecordId,
                                listState = listState,
                                onRecordClick = { selectedRecordId = it.id },
                                onArchiveRecord = onArchiveRecord,
                                modifier = Modifier.weight(1f),
                            )

                            if (selectedRecord != null) {
                                RecordDetailPanel(
                                    schema = state.schema,
                                    record = selectedRecord,
                                    onClose = { selectedRecordId = null },
                                    modifier = Modifier.width(360.dp).fillMaxHeight(),
                                )
                            }
                        }
                    }
                }
            }

            if (filteredRecords.isNotEmpty()) {
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun ColumnPickerButton(
    fields: List<SchemaField>,
    visibleFieldIds: Set<String>,
    onToggleField: (SchemaField) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        AppTooltip(text = "Choisir les colonnes visibles") {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = fields.isNotEmpty(),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("${visibleFieldIds.size.coerceAtLeast(1)} colonnes")
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            fields.forEach { field ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Checkbox(
                                checked = field.id in visibleFieldIds,
                                onCheckedChange = null,
                            )
                            Text(
                                text = field.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    onClick = { onToggleField(field) },
                )
            }
        }
    }
}

@Composable
private fun RecordHeader(
    schemaName: String,
    fieldCount: Int,
    recordCount: Int,
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
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "REGISTRE DE DONNÉES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = schemaName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Consultez, recherchez et gérez les données associées à ce modèle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.width(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("$recordCount entrées") },
                    shape = RoundedCornerShape(4.dp),
                )
                AssistChip(
                    onClick = {},
                    label = { Text("$fieldCount champs") },
                    shape = RoundedCornerShape(4.dp),
                )
            }
        }
    }
}

@Composable
private fun RecordTable(
    schema: DataSchema?,
    visibleFields: List<SchemaField>,
    records: List<DataRecord>,
    selectedRecordId: String?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onRecordClick: (DataRecord) -> Unit,
    onArchiveRecord: (DataRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalScrollState = rememberScrollState()
    val tableWidth = RecordColumnWidth * visibleFields.size.toFloat() + RecordActionColumnWidth + 32.dp

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().horizontalScroll(horizontalScrollState),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(
                modifier = Modifier.width(tableWidth).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                visibleFields.forEach { field ->
                    Text(
                        text = field.name,
                        modifier = Modifier.width(RecordColumnWidth),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(48.dp))
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 2.dp, bottom = 14.dp),
            ) {
                items(records, key = { it.id }) { record ->
                    RecordRow(
                        schema = schema,
                        visibleFields = visibleFields,
                        record = record,
                        selected = record.id == selectedRecordId,
                        horizontalScrollState = horizontalScrollState,
                        tableWidth = tableWidth,
                        onClick = { onRecordClick(record) },
                        onArchiveRecord = onArchiveRecord,
                    )
                }
            }

            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(horizontalScrollState),
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(end = 12.dp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecordRow(
    schema: DataSchema?,
    visibleFields: List<SchemaField>,
    record: DataRecord,
    selected: Boolean,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    tableWidth: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    onArchiveRecord: (DataRecord) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val clipboardManager = LocalClipboardManager.current

    ContextMenuArea(
        items = {
            listOf(
                ContextMenuItem("Copier les valeurs") {
                    val textToCopy = schema?.fields.orEmpty().joinToString(", ") { field ->
                        "${field.name}: ${record.values[field.slug] ?: record.values[field.id].orEmpty()}"
                    }
                    clipboardManager.setText(AnnotatedString(textToCopy))
                },
                ContextMenuItem("Archiver la donnée") { onArchiveRecord(record) },
            )
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
                .hoverable(interactionSource),
            shape = RoundedCornerShape(8.dp),
            color = when {
                selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                hovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                else -> MaterialTheme.colorScheme.surface
            },
            border = BorderStroke(
                1.dp,
                if (selected || hovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
            ),
            tonalElevation = if (hovered) 2.dp else 1.dp,
        ) {
            Row(
                modifier = Modifier
                    .width(tableWidth)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                visibleFields.forEachIndexed { index, field ->
                    val value = record.values[field.slug] ?: record.values[field.id].orEmpty()
                    Text(
                        text = value,
                        modifier = Modifier.width(RecordColumnWidth),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (index == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                AppTooltip(text = "Archiver cette ligne de donnée") {
                    IconButton(
                        onClick = { onArchiveRecord(record) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Archiver",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordDetailPanel(
    schema: DataSchema?,
    record: DataRecord,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Détail de la donnée",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = schema?.name.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = "Fermer", modifier = Modifier.size(18.dp))
                }
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(schema?.fields.orEmpty(), key = { it.id }) { field ->
                    DetailFieldRow(
                        field = field,
                        value = record.values[field.slug] ?: record.values[field.id].orEmpty(),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailFieldRow(
    field: SchemaField,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = field.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun EmptyState(
    isSearchActive: Boolean,
    onCreateClick: () -> Unit,
    onClearSearch: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (isSearchActive) "Aucun résultat trouvé" else "Aucune donnée enregistrée",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (isSearchActive) {
                    "Aucune ligne de donnée ne correspond à votre recherche actuelle."
                } else {
                    "Commencez par ajouter votre première entrée pour ce modèle de données."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (isSearchActive) {
                OutlinedButton(
                    onClick = onClearSearch,
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text("Réinitialiser la recherche")
                }
            } else {
                Button(
                    onClick = onCreateClick,
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ajouter une donnée")
                }
            }
        }
    }
}
