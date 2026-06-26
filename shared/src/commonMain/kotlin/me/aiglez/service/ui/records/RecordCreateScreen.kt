package me.aiglez.service.ui.records

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
    val canSave = state.schema != null && !state.isSaving

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val listState = rememberLazyListState()
        Box(
            modifier = Modifier.widthIn(max = 920.dp).fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CreateHeader(
                    schemaName = state.schema?.name ?: "",
                    fieldCount = state.schema?.fields?.size ?: 0,
                )

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 2.dp),
                    ) {
                        items(state.schema?.fields.orEmpty(), key = { it.id }) { field ->
                            FormFieldCard(field = field) {
                                DynamicFieldInput(
                                    field = field,
                                    value = state.values[field.slug].orEmpty(),
                                    referenceOptions = state.referenceOptions[field.id].orEmpty(),
                                    onValueChange = { onValueChange(field.slug, it, field.type) },
                                )
                            }
                        }
                    }
                }

                SaveBar(
                    canSave = canSave,
                    isSaving = state.isSaving,
                    onSave = onSave,
                )
            }

            if (state.schema?.fields.orEmpty().isNotEmpty()) {
                androidx.compose.foundation.VerticalScrollbar(
                    adapter = androidx.compose.foundation.rememberScrollbarAdapter(listState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun CreateHeader(
    schemaName: String,
    fieldCount: Int,
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
                    text = "NOUVELLE ENTRÉE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = schemaName.ifBlank { "Donnée" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Saisissez les informations requises pour enregistrer cette entrée.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.width(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("$fieldCount champs à renseigner") },
                    shape = RoundedCornerShape(4.dp),
                )
            }
        }
    }
}

@Composable
private fun FormFieldCard(
    field: SchemaField,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = field.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                AssistChip(
                    onClick = {},
                    label = { Text(getFieldTypeLabel(field.type)) },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(20.dp)
                )
            }
            content()
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
    val focusManager = LocalFocusManager.current

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
            singleLine = field.type != FieldType.LIST,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            placeholder = {
                Text(
                    when (field.type) {
                        FieldType.NUMBER -> "ex: 123"
                        FieldType.DOUBLE -> "ex: 45.67"
                        FieldType.LIST -> "Saisissez un élément par ligne..."
                        else -> "Entrez la valeur..."
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReferenceDropdown(
    field: SchemaField,
    value: String,
    options: List<DataRecord>,
    onValueChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedRecord = options.find { it.id == value }
    val displayText = selectedRecord?.values?.values?.firstOrNull().orEmpty().ifBlank { value }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Sélectionner une référence...") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Aucune option disponible", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = {},
                    enabled = false
                )
            } else {
                options.forEach { record ->
                    val label = record.values.values.firstOrNull().orEmpty().ifBlank { record.id }
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onValueChange(record.id)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SaveBar(
    canSave: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
) {
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
                    text = if (isSaving) "Enregistrement en cours..." else "Prêt à enregistrer",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (canSave && !isSaving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "La donnée sera ajoutée au registre de ce modèle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AppTooltip(text = "Enregistrer les modifications de cette entrée") {
                Button(
                    onClick = onSave,
                    enabled = canSave && !isSaving,
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isSaving) "Sauvegarde..." else "Enregistrer")
                }
            }
        }
    }
}

private fun getFieldTypeLabel(type: FieldType): String {
    return when (type) {
        FieldType.TEXT -> "Texte"
        FieldType.NUMBER -> "Nombre entier"
        FieldType.DOUBLE -> "Nombre décimal"
        FieldType.REFERENCE -> "Référence"
        FieldType.LIST -> "Liste"
    }
}
