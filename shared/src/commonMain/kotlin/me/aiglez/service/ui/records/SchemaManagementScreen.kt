package me.aiglez.service.ui.records

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.aiglez.service.domain.models.DataSchema
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SchemaManagementScreen(
    onEditSchema: (String) -> Unit,
    onOpenRecords: (String) -> Unit,
    viewModel: SchemaManagementViewModel = koinViewModel(),
) {
    val schemas by viewModel.schemas.collectAsState()
    var pendingArchive by remember { mutableStateOf<DataSchema?>(null) }

    SchemaManagementContent(
        schemas = schemas,
        onEditSchema = onEditSchema,
        onOpenRecords = onOpenRecords,
        onArchiveRequested = { pendingArchive = it },
    )

    val schema = pendingArchive
    if (schema != null) {
        AlertDialog(
            onDismissRequest = { pendingArchive = null },
            title = { Text("Archive schema?") },
            text = { Text("Archiving ${schema.name} hides it and its active navigation entry. Existing records remain in storage.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.archiveSchema(schema.id)
                        pendingArchive = null
                    },
                ) {
                    Text("Archive Schema")
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
private fun SchemaManagementContent(
    schemas: List<DataSchema>,
    onEditSchema: (String) -> Unit,
    onOpenRecords: (String) -> Unit,
    onArchiveRequested: (DataSchema) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("Schema Management Ledger", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text("Schema Name", modifier = Modifier.weight(2f), fontWeight = FontWeight.SemiBold)
            Text("Total Configured Fields", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            Text("Row Actions", modifier = Modifier.weight(2f), fontWeight = FontWeight.SemiBold)
        }
        Box(modifier = Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize().padding(end = 12.dp),
            ) {
                items(schemas, key = { it.id }) { schema ->
                    SchemaRow(
                        schema = schema,
                        onEditSchema = onEditSchema,
                        onOpenRecords = onOpenRecords,
                        onArchiveRequested = onArchiveRequested,
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
private fun SchemaRow(
    schema: DataSchema,
    onEditSchema: (String) -> Unit,
    onOpenRecords: (String) -> Unit,
    onArchiveRequested: (DataSchema) -> Unit,
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
        Text(schema.name, modifier = Modifier.weight(2f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(schema.fields.size.toString(), modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(2f)) {
            OutlinedButton(onClick = { onEditSchema(schema.id) }) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Modify Blueprint")
            }
            OutlinedButton(onClick = { onOpenRecords(schema.id) }) {
                Icon(Icons.Default.TableRows, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("View Captured Records")
            }
            TextButton(onClick = { onArchiveRequested(schema) }) {
                Icon(Icons.Default.Archive, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Archive Schema")
            }
        }
    }
}
