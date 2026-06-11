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
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.aiglez.service.data.dynamicdata.DynamicData
import me.aiglez.service.data.dynamicdata.DynamicDataField
import me.aiglez.service.data.dynamicdata.DynamicDataFieldType

private class FieldState(
    val nameState: TextFieldState = TextFieldState(),
    initialOptional: Boolean = false,
    initialType: DynamicDataFieldType = DynamicDataFieldType.Text,
) {
    var optional by mutableStateOf(initialOptional)
    var type by mutableStateOf(initialType)
}

@Composable
fun CreateDynamicDataScreen(
    availableDynamicData: List<DynamicData>,
    onSave: (String, List<DynamicDataField>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nameState = rememberTextFieldState()
    val fields = remember { mutableStateListOf<FieldState>() }
    val canSave = nameState.text.isNotBlank() && fields.isNotEmpty() && fields.all { it.nameState.text.isNotBlank() }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 920.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StructureHeader(
                fieldCount = fields.size,
                requiredCount = fields.count { !it.optional },
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
                        state = nameState,
                        modifier = Modifier.fillMaxWidth(),
                        lineLimits = TextFieldLineLimits.SingleLine,
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
                    onClick = { fields.add(FieldState()) },
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ajouter")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
            ) {
                items(fields, key = { it.hashCode() }) { field ->
                    FieldDefinitionRow(
                        field = field,
                        onRemove = { fields.remove(field) },
                        availableDynamicData = availableDynamicData,
                    )
                }
            }

            SaveStructureBar(
                canSave = canSave,
                invalidCount = countInvalidItems(nameState, fields),
                onSave = {
                    onSave(
                        nameState.text.toString(),
                        fields.map { field ->
                            DynamicDataField(
                                name = field.nameState.text.toString(),
                                optional = field.optional,
                                type = field.type,
                            )
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun StructureHeader(
    fieldCount: Int,
    requiredCount: Int,
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "MODÈLE DE DONNÉES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Nouvelle structure",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Créez une structure réutilisable pour vos futures saisies.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("$fieldCount champs") },
                    shape = RoundedCornerShape(4.dp),
                )
                AssistChip(
                    onClick = {},
                    label = { Text("$requiredCount requis") },
                    shape = RoundedCornerShape(4.dp),
                )
            }
        }
    }
}

@Composable
private fun FieldDefinitionRow(
    field: FieldState,
    onRemove: () -> Unit,
    availableDynamicData: List<DynamicData>,
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
                    state = field.nameState,
                    modifier = Modifier.weight(1.4f),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    placeholder = { Text("Nom du champ") },
                )

                TypeSelector(
                    selectedType = field.type,
                    onTypeSelected = { field.type = it },
                    availableDynamicData = availableDynamicData,
                    modifier = Modifier.weight(1f),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Checkbox(
                        checked = field.optional,
                        onCheckedChange = { field.optional = it },
                    )
                    Text(
                        text = "Optionnel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            ComplexTypeConfiguration(
                field = field,
                availableDynamicData = availableDynamicData,
            )
        }
    }
}

@Composable
private fun ComplexTypeConfiguration(
    field: FieldState,
    availableDynamicData: List<DynamicData>,
) {
    when (val type = field.type) {
        is DynamicDataFieldType.DynamicDataRef -> {
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
                    selectedId = type.dynamicDataId,
                    availableDynamicData = availableDynamicData,
                    onIdSelected = { field.type = DynamicDataFieldType.DynamicDataRef(it) },
                )
            }
        }

        is DynamicDataFieldType.ListOf -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Liste de",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ListTypeSelector(
                    selectedType = type.itemType,
                    onTypeSelected = { field.type = DynamicDataFieldType.ListOf(it) },
                )
            }
        }

        else -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSelector(
    selectedType: DynamicDataFieldType,
    onTypeSelected: (DynamicDataFieldType) -> Unit,
    availableDynamicData: List<DynamicData>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = getTypeLabel(selectedType),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            singleLine = true,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Texte") },
                onClick = { onTypeSelected(DynamicDataFieldType.Text); expanded = false },
            )
            DropdownMenuItem(
                text = { Text("Nombre entier") },
                onClick = { onTypeSelected(DynamicDataFieldType.Number); expanded = false },
            )
            DropdownMenuItem(
                text = { Text("Nombre décimal") },
                onClick = { onTypeSelected(DynamicDataFieldType.Decimal); expanded = false },
            )
            DropdownMenuItem(
                text = { Text("Booléen") },
                onClick = { onTypeSelected(DynamicDataFieldType.Boolean); expanded = false },
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Référence") },
                enabled = availableDynamicData.isNotEmpty(),
                onClick = {
                    val firstId = availableDynamicData.firstOrNull()?.id ?: return@DropdownMenuItem
                    onTypeSelected(DynamicDataFieldType.DynamicDataRef(firstId))
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Liste") },
                onClick = {
                    onTypeSelected(DynamicDataFieldType.ListOf(DynamicDataFieldType.Text))
                    expanded = false
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefSelector(
    selectedId: Long,
    availableDynamicData: List<DynamicData>,
    onIdSelected: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedData = availableDynamicData.find { it.id == selectedId }

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
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).widthIn(min = 240.dp),
            singleLine = true,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            availableDynamicData.forEach { data ->
                DropdownMenuItem(
                    text = { Text(data.name) },
                    onClick = { onIdSelected(data.id); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListTypeSelector(
    selectedType: DynamicDataFieldType,
    onTypeSelected: (DynamicDataFieldType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = getTypeLabel(selectedType),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).widthIn(min = 220.dp),
            singleLine = true,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Texte") },
                onClick = { onTypeSelected(DynamicDataFieldType.Text); expanded = false },
            )
            DropdownMenuItem(
                text = { Text("Nombre entier") },
                onClick = { onTypeSelected(DynamicDataFieldType.Number); expanded = false },
            )
            DropdownMenuItem(
                text = { Text("Nombre décimal") },
                onClick = { onTypeSelected(DynamicDataFieldType.Decimal); expanded = false },
            )
            DropdownMenuItem(
                text = { Text("Booléen") },
                onClick = { onTypeSelected(DynamicDataFieldType.Boolean); expanded = false },
            )
        }
    }
}

@Composable
private fun SaveStructureBar(
    canSave: Boolean,
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
                    text = if (canSave) "Prêt à enregistrer" else "$invalidCount élément(s) à compléter",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Le modèle sera disponible dans la liste des données dynamiques.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = onSave,
                enabled = canSave,
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Enregistrer")
            }
        }
    }
}

private fun countInvalidItems(nameState: TextFieldState, fields: List<FieldState>): Int {
    val invalidName = if (nameState.text.isBlank()) 1 else 0
    val invalidFields = if (fields.isEmpty()) 1 else fields.count { it.nameState.text.isBlank() }
    return invalidName + invalidFields
}

private fun getTypeLabel(type: DynamicDataFieldType): String {
    return when (type) {
        DynamicDataFieldType.Text -> "Texte"
        DynamicDataFieldType.Number -> "Nombre"
        DynamicDataFieldType.Decimal -> "Décimal"
        DynamicDataFieldType.Boolean -> "Booléen"
        is DynamicDataFieldType.DynamicDataRef -> "Référence"
        is DynamicDataFieldType.ListOf -> "Liste"
    }
}
