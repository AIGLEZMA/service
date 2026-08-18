package me.aiglez.service.ui.records

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.aiglez.service.domain.models.DataSchema
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

@OptIn(ExperimentalFoundationApi::class)
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
    val focusManager = LocalFocusManager.current
    var fieldPendingDelete by remember { mutableStateOf<SchemaField?>(null) }
    val canSave = state.schemaName.isNotBlank() &&
            state.fields.isNotEmpty() &&
            state.fields.all { it.name.isNotBlank() } &&
            !state.isSaving

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
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(8.dp),
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

                    AppTooltip(text = "Ajouter un nouveau champ à ce modèle") {
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
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp),
                    ) {
                        if (state.fields.isEmpty()) {
                            item {
                                EmptyFieldsState(onAddClick = onAddField)
                            }
                        }

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
                }

                SaveStructureBar(
                    canSave = canSave,
                    isSaving = state.isSaving,
                    errorMessage = state.errorMessage,
                    invalidCount = countInvalidItems(state.schemaName, state.fields),
                    onSave = onSave,
                )
            }

            if (state.fields.isNotEmpty()) {
                androidx.compose.foundation.VerticalScrollbar(
                    adapter = androidx.compose.foundation.rememberScrollbarAdapter(listState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                )
            }
        }
    }

    val deleting = fieldPendingDelete
    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { fieldPendingDelete = null },
            title = { Text("Supprimer le champ ?") },
            text = { Text("La suppression de \"${deleting.name}\" modifie le modèle de données avant sa sauvegarde.") },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveField(deleting.id)
                        fieldPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Supprimer")
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

@OptIn(ExperimentalFoundationApi::class)
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
    val focusManager = LocalFocusManager.current
    ContextMenuArea(
        items = {
            if (locked) {
                emptyList()
            } else {
                listOf(
                    ContextMenuItem("Supprimer le champ") { onRemove() }
                )
            }
        }
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
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        shape = RoundedCornerShape(8.dp),
                    )

                    TypeSelector(
                        selectedType = field.type,
                        onTypeSelected = onTypeChange,
                        enabled = !locked,
                        modifier = Modifier.weight(1f),
                    )

                    if (locked) {
                        AppTooltip(text = "Type et suppression verrouillés pour protéger les données existantes") {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Type et suppression verrouillés",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        AppTooltip(text = "Supprimer ce champ") {
                            IconButton(onClick = onRemove) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
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

@Composable
private fun EmptyFieldsState(onAddClick: () -> Unit) {
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
                    text = "Aucun champ défini",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Ajoutez au moins un champ pour créer le modèle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedButton(
                onClick = onAddClick,
                shape = RoundedCornerShape(4.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ajouter")
            }
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
            shape = RoundedCornerShape(8.dp),
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
            shape = RoundedCornerShape(8.dp),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SaveStructureBar(
    canSave: Boolean,
    isSaving: Boolean,
    errorMessage: String?,
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
                    text = when {
                        isSaving -> "Enregistrement en cours..."
                        errorMessage != null -> errorMessage
                        canSave -> "Prêt à enregistrer"
                        else -> "$invalidCount élément(s) à compléter"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        errorMessage != null -> MaterialTheme.colorScheme.error
                        canSave && !isSaving -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Text(
                    text = "Le modèle sera disponible dans la liste des modèles de données.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AppTooltip(text = if (canSave) "Sauvegarder les modifications de ce modèle" else "Complétez les champs requis avant de sauvegarder") {
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
