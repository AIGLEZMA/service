package me.aiglez.service.ui.records

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SchemaCreateScreen(
    schemaId: String?,
    onSaved: (String) -> Unit,
) {
    val viewModel: SchemaCreateViewModel = koinViewModel(
        key = "schema-create-${schemaId.orEmpty()}",
        parameters = { parametersOf(schemaId.orEmpty()) },
    )
    val state by viewModel.uiState.collectAsState()
    SchemaCreateContent(
        state = state,
        onNameChange = viewModel::updateName,
        onAddField = viewModel::addField,
        onFieldNameChange = viewModel::updateFieldName,
        onFieldTypeChange = viewModel::updateFieldType,
        onRemoveField = viewModel::removeField,
        onSave = { viewModel.save(onSaved) },
    )
}

@Composable
private fun SchemaCreateContent(
    state: SchemaCreateUiState,
    onNameChange: (String) -> Unit,
    onAddField: () -> Unit,
    onFieldNameChange: (String, String) -> Unit,
    onFieldTypeChange: (String, FieldType) -> Unit,
    onRemoveField: (String) -> Unit,
    onSave: () -> Unit,
) {
    var fieldPendingDelete by remember { mutableStateOf<SchemaField?>(null) }
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        Text(
            text = if (state.isEditing) "Modify Schema Blueprint" else "Create Schema Blueprint",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        if (state.hasExistingRecords) {
            Text(
                text = "Existing records detected. Existing field types and deletions are locked; append new fields at the end.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        OutlinedTextField(
            value = state.schemaName,
            onValueChange = onNameChange,
            label = { Text("Schema Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onAddField) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Append Field")
            }
            Button(
                onClick = onSave,
                enabled = state.schemaName.isNotBlank() && state.fields.isNotEmpty() && !state.isSaving,
            ) {
                Text(if (state.isSaving) "Saving..." else "Save Blueprint")
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize().padding(end = 12.dp),
            ) {
                items(state.fields, key = { it.id }) { field ->
                    val locked = state.hasExistingRecords && field.id in state.lockedExistingFieldIds
                    FieldEditorRow(
                        field = field,
                        locked = locked,
                        onNameChange = { onFieldNameChange(field.id, it) },
                        onTypeChange = { onFieldTypeChange(field.id, it) },
                        onRemove = { fieldPendingDelete = field },
                    )
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            )
        }
    }

    val deleting = fieldPendingDelete
    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { fieldPendingDelete = null },
            title = { Text("Remove field?") },
            text = { Text("Removing ${deleting.name} changes the schema blueprint before it is saved.") },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveField(deleting.id)
                        fieldPendingDelete = null
                    },
                ) {
                    Text("Remove Field")
                }
            },
            dismissButton = {
                TextButton(onClick = { fieldPendingDelete = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun FieldEditorRow(
    field: SchemaField,
    locked: Boolean,
    onNameChange: (String) -> Unit,
    onTypeChange: (FieldType) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = field.name,
            onValueChange = onNameChange,
            label = { Text("Field Name") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        FieldTypeSelector(
            selected = field.type,
            enabled = !locked,
            onSelected = onTypeChange,
            modifier = Modifier.width(180.dp),
        )
        if (locked) {
            Icon(Icons.Default.Lock, contentDescription = "Locked existing field")
        } else {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove field")
            }
        }
    }
}

@Composable
private fun FieldTypeSelector(
    selected: FieldType,
    enabled: Boolean,
    onSelected: (FieldType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            enabled = enabled,
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(selected.name)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            FieldType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    },
                )
            }
        }
    }
}
