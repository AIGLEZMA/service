package me.aiglez.service.ui.records

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RecordCreateScreen(
    schemaId: String,
    onSaved: () -> Unit,
) {
    val viewModel: RecordCreateViewModel = koinViewModel(
        key = "record-create-$schemaId",
        parameters = { parametersOf(schemaId) },
    )
    val state by viewModel.uiState.collectAsState()
    RecordCreateContent(
        state = state,
        onValueChange = viewModel::updateValue,
        onSave = { viewModel.save(onSaved) },
    )
}

@Composable
private fun RecordCreateContent(
    state: RecordCreateUiState,
    onValueChange: (String, String, FieldType) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = "Add ${state.schema?.name ?: "Record"} Entry",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize().padding(end = 12.dp),
            ) {
                items(state.schema?.fields.orEmpty(), key = { it.id }) { field ->
                    DynamicFieldInput(
                        field = field,
                        value = state.values[field.slug].orEmpty(),
                        referenceOptions = state.referenceOptions[field.id].orEmpty(),
                        onValueChange = { onValueChange(field.slug, it, field.type) },
                    )
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            )
        }
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onSave,
                enabled = state.schema != null && !state.isSaving,
            ) {
                Text(if (state.isSaving) "Saving..." else "Save Entry")
            }
        }
    }
}

@Composable
private fun DynamicFieldInput(
    field: SchemaField,
    value: String,
    referenceOptions: List<DataRecord>,
    onValueChange: (String) -> Unit,
) {
    when (field.type) {
        FieldType.REFERENCE -> ReferenceDropdown(
            field = field,
            value = value,
            options = referenceOptions,
            onValueChange = onValueChange,
        )

        else -> OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(field.name) },
            singleLine = field.type != FieldType.LIST,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ReferenceDropdown(
    field: SchemaField,
    value: String,
    options: List<DataRecord>,
    onValueChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(value.ifBlank { field.name })
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { record ->
                DropdownMenuItem(
                    text = { Text(record.values.values.firstOrNull().orEmpty().ifBlank { record.id }) },
                    onClick = {
                        onValueChange(record.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
