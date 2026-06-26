package me.aiglez.service.ui.records

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SchemaCreateScreen(
    schemaId: String?,
    onSaved: (String) -> Unit,
) {
    val uniqueKey = remember(schemaId) {
        if (schemaId.isNullOrBlank()) {
            "schema-create-new-${me.aiglez.service.ui.common.newUiId("session")}"
        } else {
            "schema-create-$schemaId"
        }
    }
    val viewModel: SchemaCreateViewModel = koinViewModel(
        key = uniqueKey,
        parameters = { parametersOf(schemaId.orEmpty()) },
    )
    val state by viewModel.uiState.collectAsState()
    SchemaCreateContent(
        state = state,
        onNameChange = viewModel::updateName,
        onAddField = viewModel::addField,
        onFieldNameChange = viewModel::updateFieldName,
        onFieldTypeChange = viewModel::updateFieldType,
        onFieldReferenceChange = viewModel::updateFieldReference,
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
    onFieldReferenceChange: (String, String) -> Unit,
    onRemoveField: (String) -> Unit,
    onSave: () -> Unit,
) {
    var fieldPendingDelete by remember { mutableStateOf<SchemaField?>(null) }
    val canSave = state.schemaName.isNotBlank() &&
            state.fields.isNotEmpty() &&
            state.fields.all { it.name.isNotBlank() } &&
            !state.isSaving

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 920.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StructureHeader(
                isEditing = state.isEditing,
                hasExistingRecords = state.hasExistingRecords,
                fieldCount = state.fields.size,
            )

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
                    Text(
                        text = "Nom du modèle",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    OutlinedTextField(
                        value = state.schemaName,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("ex: Client, Facture, Intervention") },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Champs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Définissez les informations à renseigner pour ce modèle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                OutlinedButton(
                    onClick = onAddField,
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ajouter")
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
                ) {
                    items(state.fields, key = { it.id }) { field ->
                        val locked = state.hasExistingRecords && field.id in state.lockedExistingFieldIds
                        FieldDefinitionRow(
                            field = field,
                            locked = locked,
                            availableSchemas = state.availableSchemas.filter { it.id != state.schemaId },
                            onNameChange = { onFieldNameChange(field.id, it) },
                            onTypeChange = { onFieldTypeChange(field.id, it) },
                            onReferenceSchemaSelected = { onFieldReferenceChange(field.id, it) },
                            onRemove = { fieldPendingDelete = field },
                        )
                    }
                }
                androidx.compose.foundation.VerticalScrollbar(
                    adapter = androidx.compose.foundation.rememberScrollbarAdapter(listState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                )
            }

            SaveStructureBar(
                canSave = canSave,
                isSaving = state.isSaving,
                invalidCount = countInvalidItems(state.schemaName, state.fields),
                onSave = onSave,
            )
        }
    }

    val deleting = fieldPendingDelete
    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { fieldPendingDelete = null },
            title = { Text("Supprimer le champ ?") },
            text = { Text("La suppression de ${deleting.name} modifie le Schéma de donnée avant sa sauvegarde.") },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveField(deleting.id)
                        fieldPendingDelete = null
                    },
                ) {
                    Text("Supprimer le champ")
                }
            },
            dismissButton = {
                TextButton(onClick = { fieldPendingDelete = null }) {
                    Text("Annuler")
                }
            },
        )
    }
}

@Composable
private fun StructureHeader(
    isEditing: Boolean,
    hasExistingRecords: Boolean,
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
                    text = "MODÈLE DE DONNÉES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (isEditing) "Modifier la structure" else "Nouvelle structure",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (hasExistingRecords) {
                        "Des données existantes ont été détectées. Les types de champs existants et les suppressions sont verrouillés ; ajoutez les nouveaux champs à la fin."
                    } else {
                        "Créez une structure réutilisable pour vos futures saisies."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.width(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
private fun FieldDefinitionRow(
    field: SchemaField,
    locked: Boolean,
    availableSchemas: List<DataSchema>,
    onNameChange: (String) -> Unit,
    onTypeChange: (FieldType) -> Unit,
    onReferenceSchemaSelected: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = field.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.weight(1.4f),
                    singleLine = true,
                    placeholder = { Text("Nom du champ") },
                    readOnly = locked,
                )

                TypeSelector(
                    selectedType = field.type,
                    onTypeSelected = onTypeChange,
                    enabled = !locked,
                    modifier = Modifier.weight(1f),
                )

                if (locked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Verrouillé",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            ComplexTypeConfiguration(
                field = field,
                availableSchemas = availableSchemas,
                onReferenceSchemaSelected = onReferenceSchemaSelected,
            )
        }
    }
}

@Composable
private fun ComplexTypeConfiguration(
    field: SchemaField,
    availableSchemas: List<DataSchema>,
    onReferenceSchemaSelected: (String) -> Unit,
) {
    if (field.type == FieldType.REFERENCE) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Référence vers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RefSelector(
                selectedId = field.referenceSchemaId.orEmpty(),
                availableSchemas = availableSchemas,
                onIdSelected = onReferenceSchemaSelected,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSelector(
    selectedType: FieldType,
    onTypeSelected: (FieldType) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = getTypeLabel(selectedType),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            singleLine = true,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Texte") },
                onClick = { onTypeSelected(FieldType.TEXT); expanded = false },
            )
            DropdownMenuItem(
                text = { Text("Nombre entier") },
                onClick = { onTypeSelected(FieldType.NUMBER); expanded = false },
            )
            DropdownMenuItem(
                text = { Text("Nombre décimal") },
                onClick = { onTypeSelected(FieldType.DOUBLE); expanded = false },
            )
            DropdownMenuItem(
                text = { Text("Référence") },
                onClick = { onTypeSelected(FieldType.REFERENCE); expanded = false },
            )
            DropdownMenuItem(
                text = { Text("Liste") },
                onClick = { onTypeSelected(FieldType.LIST); expanded = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefSelector(
    selectedId: String,
    availableSchemas: List<DataSchema>,
    onIdSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedData = availableSchemas.find { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selectedData?.name ?: "Sélectionner",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).widthIn(min = 240.dp),
            singleLine = true,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            availableSchemas.forEach { data ->
                DropdownMenuItem(
                    text = { Text(data.name) },
                    onClick = { onIdSelected(data.id); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun SaveStructureBar(
    canSave: Boolean,
    isSaving: Boolean,
    invalidCount: Int,
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
                    text = if (isSaving) {
                        "Enregistrement en cours..."
                    } else if (canSave) {
                        "Prêt à enregistrer"
                    } else {
                        "$invalidCount élément(s) à compléter"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (canSave && !isSaving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Le modèle sera disponible dans la liste des données dynamiques.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

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

private fun countInvalidItems(schemaName: String, fields: List<SchemaField>): Int {
    val invalidName = if (schemaName.isBlank()) 1 else 0
    val invalidFields = if (fields.isEmpty()) 1 else fields.count { it.name.isBlank() }
    return invalidName + invalidFields
}

private fun getTypeLabel(type: FieldType): String {
    return when (type) {
        FieldType.TEXT -> "Texte"
        FieldType.NUMBER -> "Nombre entier"
        FieldType.DOUBLE -> "Nombre décimal"
        FieldType.REFERENCE -> "Référence"
        FieldType.LIST -> "Liste"
    }
}
