package me.aiglez.service.ui.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.aiglez.service.ui.templates.editor.TemplateEditorState

@Composable
internal fun PreviewGenerationToolbar(
    state: TemplateEditorState,
    onHomeClick: () -> Unit,
    onChooseDataClick: () -> Unit,
    onExportPdf: () -> Unit,
    onEditClick: (() -> Unit)?,
) {
    val requiredCount = state.previewSchemaIds.size
    val selectedCount = state.previewSchemaIds.count { state.selectedPreviewRecords[it] != null }
    val hasRequiredData = requiredCount > 0
    val isReady = state.canGeneratePreviewPdf

    Surface(
        modifier = Modifier.fillMaxWidth().height(108.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedButton(onClick = onHomeClick, modifier = Modifier.height(42.dp)) {
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Accueil")
            }

            Column(modifier = Modifier.width(190.dp)) {
                Text(
                    text = state.template?.name ?: "Aperçu du modèle",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Préparez votre document en deux étapes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                )
                state.message?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            PreviewGenerationSteps(
                state = state,
                selectedCount = selectedCount,
                requiredCount = requiredCount,
                hasRequiredData = hasRequiredData,
                isReady = isReady,
                onChooseDataClick = onChooseDataClick,
                onExportPdf = onExportPdf,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )

            if (onEditClick != null) {
                OutlinedButton(onClick = onEditClick, modifier = Modifier.height(42.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Modifier le modèle")
                }
            }
        }
    }
}

@Composable
private fun PreviewGenerationSteps(
    state: TemplateEditorState,
    selectedCount: Int,
    requiredCount: Int,
    hasRequiredData: Boolean,
    isReady: Boolean,
    onChooseDataClick: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StepNumber("1", isReady)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Sélectionner les données", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = when {
                        !hasRequiredData -> "Aucun champ de données dans ce modèle"
                        isReady -> "$selectedCount donnée${if (selectedCount > 1) "s" else ""} sélectionnée${if (selectedCount > 1) "s" else ""}"
                        else -> "$selectedCount sur $requiredCount sélectionnée${if (selectedCount > 1) "s" else ""} — obligatoire"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isReady) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                )
            }
            Button(onClick = onChooseDataClick, enabled = state.template != null && hasRequiredData) {
                Text(if (selectedCount > 0) "Modifier les données" else "Choisir les données")
            }

            Text("→", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            StepNumber("2", isReady, primary = true)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Générer le document", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = if (isReady) "Votre PDF est prêt à être généré" else "Disponible après l’étape 1",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = onExportPdf,
                enabled = state.template != null && isReady && !state.isExporting,
                modifier = Modifier.height(42.dp),
            ) {
                Text(if (state.isExporting) "Génération…" else "Générer le PDF")
            }
        }
    }
}

@Composable
private fun StepNumber(value: String, completed: Boolean, primary: Boolean = false) {
    val background = when {
        primary && completed -> MaterialTheme.colorScheme.primary
        primary -> MaterialTheme.colorScheme.surfaceVariant
        completed -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val foreground = when {
        primary && completed -> MaterialTheme.colorScheme.onPrimary
        primary -> MaterialTheme.colorScheme.onSurfaceVariant
        completed -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(shape = MaterialTheme.shapes.extraLarge, color = background) {
        Text(
            text = value,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.titleMedium,
            color = foreground,
        )
    }
}
