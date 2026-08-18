package me.aiglez.service.ui.shell

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun UnsavedChangesDialog(
    isSaving: Boolean,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Modifications non enregistrées") },
        text = { Text("Enregistrez les modifications avant de quitter l’éditeur, ou ignorez-les définitivement.") },
        confirmButton = {
            Button(onClick = onSave, enabled = !isSaving) {
                Text(if (isSaving) "Enregistrement…" else "Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, enabled = !isSaving) { Text("Annuler") }
            TextButton(onClick = onDiscard, enabled = !isSaving) { Text("Ignorer") }
        },
    )
}
