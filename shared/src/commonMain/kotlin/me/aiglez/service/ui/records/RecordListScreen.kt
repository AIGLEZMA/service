package me.aiglez.service.ui.records

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
            title = { Text("Archive record?") },
            text = { Text("This will hide the selected record from the active ledger.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.archiveRecord(record.id)
                        pendingArchive = null
                    },
                ) {
                    Text("Archive Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingArchive = null }) {
                    Text("Cancel")
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
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateRecord,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Entry") },
            )
        },
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text(
                        text = state.schema?.name ?: "Records",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${state.records.size} active entries",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(onClick = onImportCsv) {
                    Icon(Icons.Default.UploadFile, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Import CSV Dataset")
                }
            }
            RecordTable(
                schema = state.schema,
                records = state.records,
                onArchiveRecord = onArchiveRecord,
            )
        }
    }
}

@Composable
private fun RecordTable(
    schema: DataSchema?,
    records: List<DataRecord>,
    onArchiveRecord: (DataRecord) -> Unit,
) {
    val fields = schema?.fields.orEmpty()
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            fields.forEach { field ->
                Text(field.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
            Text("Actions", modifier = Modifier.width(128.dp), fontWeight = FontWeight.SemiBold)
        }
        Box(modifier = Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize().padding(end = 12.dp),
            ) {
                items(records, key = { it.id }) { record ->
                    RecordRow(
                        schema = schema,
                        record = record,
                        onArchiveRecord = onArchiveRecord,
                    )
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun RecordRow(
    schema: DataSchema?,
    record: DataRecord,
    onArchiveRecord: (DataRecord) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .background(if (hovered) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        schema?.fields.orEmpty().forEach { field ->
            Text(
                text = record.values[field.slug] ?: record.values[field.id].orEmpty(),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TextButton(onClick = { onArchiveRecord(record) }, modifier = Modifier.width(128.dp)) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Archive")
        }
    }
}
