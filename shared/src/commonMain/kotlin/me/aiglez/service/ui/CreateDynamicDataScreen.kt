package me.aiglez.service.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    initialType: DynamicDataFieldType = DynamicDataFieldType.Text
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

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Main Info Section
        OutlinedTextField(
            label = { Text("Nom de la structure de données") },
            state = nameState,
            modifier = Modifier.fillMaxWidth(),
            lineLimits = TextFieldLineLimits.SingleLine,
            placeholder = { Text("ex: Client, Facture...") }
        )

        // Fields Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Champs (${fields.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { fields.add(FieldState()) },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ajouter un champ")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(fields) { field ->
                FieldItem(
                    field = field,
                    onRemove = { fields.remove(field) },
                    availableDynamicData = availableDynamicData
                )
            }

            if (fields.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Aucun champ défini. Ajoutez au moins un champ pour continuer.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    onSave(
                        nameState.text.toString(),
                        fields.map {
                            DynamicDataField(
                                name = it.nameState.text.toString(),
                                optional = it.optional,
                                type = it.type
                            )
                        }
                    )
                },
                enabled = nameState.text.isNotEmpty() && fields.isNotEmpty() && fields.all { it.nameState.text.isNotEmpty() },
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Enregistrer")
            }
        }
    }
}

@Composable
private fun FieldItem(
    field: FieldState,
    onRemove: () -> Unit,
    availableDynamicData: List<DynamicData>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    state = field.nameState,
                    label = { Text("Nom du champ") },
                    modifier = Modifier.weight(1.5f),
                    lineLimits = TextFieldLineLimits.SingleLine
                )

                TypeSelector(
                    selectedType = field.type,
                    onTypeSelected = { field.type = it },
                    availableDynamicData = availableDynamicData,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Checkbox(checked = field.optional, onCheckedChange = { field.optional = it })
                    Text("Optionnel", style = MaterialTheme.typography.bodySmall)
                }

                IconButton(
                    onClick = onRemove,
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                }
            }

            // Special configuration for complex types
            when (val type = field.type) {
                is DynamicDataFieldType.DynamicDataRef -> {
                    RefConfiguration(
                        selectedId = type.dynamicDataId,
                        availableDynamicData = availableDynamicData,
                        onIdSelected = { field.type = DynamicDataFieldType.DynamicDataRef(it) }
                    )
                }

                is DynamicDataFieldType.ListOf -> {
                    ListConfiguration(
                        selectedType = type.itemType,
                        onTypeSelected = { field.type = DynamicDataFieldType.ListOf(it) }
                    )
                }

                else -> {}
            }
        }
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
        modifier = modifier
    ) {
        OutlinedTextField(
            value = getTypeLabel(selectedType),
            onValueChange = {},
            readOnly = true,
            label = { Text("Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Texte") },
                onClick = { onTypeSelected(DynamicDataFieldType.Text); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Nombre Entier") },
                onClick = { onTypeSelected(DynamicDataFieldType.Number); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Nombre Décimal") },
                onClick = { onTypeSelected(DynamicDataFieldType.Decimal); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Booléen (Oui/Non)") },
                onClick = { onTypeSelected(DynamicDataFieldType.Boolean); expanded = false }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Référence...") },
                onClick = {
                    val firstId = availableDynamicData.firstOrNull()?.id ?: 0L
                    onTypeSelected(DynamicDataFieldType.DynamicDataRef(firstId))
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Liste de...") },
                onClick = {
                    onTypeSelected(DynamicDataFieldType.ListOf(DynamicDataFieldType.Text))
                    expanded = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefConfiguration(
    selectedId: Long,
    availableDynamicData: List<DynamicData>,
    onIdSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedData = availableDynamicData.find { it.id == selectedId }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Pointer vers :", style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = selectedData?.name ?: "Sélectionner...",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).widthIn(min = 200.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableDynamicData.forEach { data ->
                    DropdownMenuItem(
                        text = { Text(data.name) },
                        onClick = { onIdSelected(data.id); expanded = false }
                    )
                }
                if (availableDynamicData.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Aucune donnée disponible") },
                        onClick = { expanded = false },
                        enabled = false
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListConfiguration(
    selectedType: DynamicDataFieldType,
    onTypeSelected: (DynamicDataFieldType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Liste de :", style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = getTypeLabel(selectedType) ?: "Sélectionner...",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).widthIn(min = 200.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Texte") },
                    onClick = { onTypeSelected(DynamicDataFieldType.Text); expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Nombre Entier") },
                    onClick = { onTypeSelected(DynamicDataFieldType.Number); expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Nombre Décimal") },
                    onClick = { onTypeSelected(DynamicDataFieldType.Decimal); expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Booléen (Oui/Non)") },
                    onClick = { onTypeSelected(DynamicDataFieldType.Boolean); expanded = false }
                )
            }
        }
    }
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
