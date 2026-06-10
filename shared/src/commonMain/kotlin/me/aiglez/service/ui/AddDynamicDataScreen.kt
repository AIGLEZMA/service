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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.aiglez.service.data.dynamicdata.DynamicData
import me.aiglez.service.data.dynamicdata.DynamicDataField
import me.aiglez.service.data.dynamicdata.DynamicDataFieldType
import me.aiglez.service.data.dynamicdata.DynamicDataInstance
import me.aiglez.service.data.dynamicdata.DynamicDataValue

@Composable
fun AddDynamicDataScreen(
    dynamicData: DynamicData,
    onSave: (DynamicDataInstance) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fieldStates = remember(dynamicData) {
        dynamicData.fields.map { field -> FieldInstanceState(field) }
    }
    val canSave = fieldStates.all { it.isValid() }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 920.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EntryHeader(
                dynamicData = dynamicData,
                requiredCount = dynamicData.fields.count { !it.optional },
            )

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
            ) {
                items(fieldStates, key = { it.field.name }) { state ->
                    FieldInputRow(state = state)
                }
            }

            SaveBar(
                canSave = canSave,
                invalidCount = fieldStates.count { !it.isValid() },
                onSave = {
                    val values = fieldStates.associate { state -> state.field.name to state.toValue() }
                    onSave(
                        DynamicDataInstance(
                            id = 0L,
                            dynamicDataId = dynamicData.id,
                            values = values,
                        )
                    )
                },
            )
        }
    }
}

@Composable
private fun EntryHeader(
    dynamicData: DynamicData,
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
                    text = "SAISIE DE DONNÉES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = dynamicData.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Renseignez les champs requis avant enregistrement.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("${dynamicData.fields.size} champs") },
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
private fun FieldInputRow(state: FieldInstanceState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.width(220.dp).padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = state.field.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (state.field.optional) "Optionnel" else "Obligatoire",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FieldInput(state = state)
            }
        }
    }
}

@Composable
private fun FieldInput(state: FieldInstanceState) {
    when (state.field.type) {
        DynamicDataFieldType.Text -> {
            OutlinedTextField(
                state = state.textFieldState,
                modifier = Modifier.fillMaxWidth(),
                lineLimits = TextFieldLineLimits.SingleLine,
                placeholder = { Text("Valeur texte") },
            )
        }

        DynamicDataFieldType.Number -> {
            OutlinedTextField(
                state = state.textFieldState,
                modifier = Modifier.fillMaxWidth(),
                lineLimits = TextFieldLineLimits.SingleLine,
                placeholder = { Text("Nombre entier") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            ValidationHint(state = state, message = "Entrez un nombre entier valide.")
        }

        DynamicDataFieldType.Decimal -> {
            OutlinedTextField(
                state = state.textFieldState,
                modifier = Modifier.fillMaxWidth(),
                lineLimits = TextFieldLineLimits.SingleLine,
                placeholder = { Text("Nombre décimal") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            ValidationHint(state = state, message = "Entrez un nombre decimal valide.")
        }

        DynamicDataFieldType.Boolean -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.booleanValue == true,
                    onClick = { state.booleanValue = true },
                    label = { Text("Oui") },
                    shape = RoundedCornerShape(4.dp),
                )
                FilterChip(
                    selected = state.booleanValue == false,
                    onClick = { state.booleanValue = false },
                    label = { Text("Non") },
                    shape = RoundedCornerShape(4.dp),
                )
                if (state.field.optional) {
                    FilterChip(
                        selected = state.booleanValue == null,
                        onClick = { state.booleanValue = null },
                        label = { Text("Non renseigné") },
                        shape = RoundedCornerShape(4.dp),
                    )
                }
            }
        }

        is DynamicDataFieldType.DynamicDataRef,
        is DynamicDataFieldType.ListOf -> {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Text(
                    text = "Champ non pris en charge pour le moment.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ValidationHint(state: FieldInstanceState, message: String) {
    if (state.hasInputError()) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun SaveBar(
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
                    text = if (canSave) "Prêt à enregistrer" else "$invalidCount champ(s) à corriger",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
                Text(
                    text = "Les données seront ajoutées au modèle sélectionné.",
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

private class FieldInstanceState(
    val field: DynamicDataField,
) {
    val textFieldState = TextFieldState()
    var booleanValue by mutableStateOf<Boolean?>(if (field.optional) null else false)

    fun isValid(): Boolean {
        val text = textFieldState.text.toString()
        return when (field.type) {
            DynamicDataFieldType.Text -> field.optional || text.isNotBlank()
            DynamicDataFieldType.Number -> (field.optional && text.isBlank()) || text.toLongOrNull() != null
            DynamicDataFieldType.Decimal -> (field.optional && text.isBlank()) || text.toDoubleOrNull() != null
            DynamicDataFieldType.Boolean -> field.optional || booleanValue != null
            is DynamicDataFieldType.DynamicDataRef,
            is DynamicDataFieldType.ListOf -> field.optional
        }
    }

    fun hasInputError(): Boolean {
        val text = textFieldState.text.toString()
        if (text.isBlank()) return !field.optional && field.type == DynamicDataFieldType.Text
        return when (field.type) {
            DynamicDataFieldType.Number -> text.toLongOrNull() == null
            DynamicDataFieldType.Decimal -> text.toDoubleOrNull() == null
            else -> false
        }
    }

    fun toValue(): DynamicDataValue? {
        val text = textFieldState.text.toString()
        if (field.optional && text.isBlank()) return null

        return when (field.type) {
            DynamicDataFieldType.Text -> DynamicDataValue.Text(text)
            DynamicDataFieldType.Number -> text.toLongOrNull()?.let { DynamicDataValue.Number(it) }
            DynamicDataFieldType.Decimal -> text.toDoubleOrNull()?.let { DynamicDataValue.Decimal(it) }
            DynamicDataFieldType.Boolean -> booleanValue?.let { DynamicDataValue.Boolean(it) }
            is DynamicDataFieldType.DynamicDataRef,
            is DynamicDataFieldType.ListOf -> null
        }
    }
}
