package me.aiglez.service.ui.templates

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondary
import androidx.compose.ui.input.pointer.isTertiary
import androidx.compose.ui.input.pointer.isAltPressed as isPointerAltPressed
import androidx.compose.ui.input.pointer.isCtrlPressed as isPointerCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed as isPointerMetaPressed
import androidx.compose.ui.input.pointer.isShiftPressed as isPointerShiftPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.InlineAlignment
import me.aiglez.service.domain.models.SchemaField
import me.aiglez.service.domain.models.TemplateBarcodeFormat
import me.aiglez.service.domain.models.TemplateBorderStyle
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.models.TemplateElementType
import me.aiglez.service.domain.models.TemplateImageAlignment
import me.aiglez.service.domain.models.TemplateImageContentMode
import me.aiglez.service.domain.models.TemplateTextDirection
import me.aiglez.service.domain.models.TemplateTextStyle
import me.aiglez.service.ui.templates.editor.CanvasMetrics
import me.aiglez.service.ui.templates.editor.CanvasMetric
import me.aiglez.service.ui.templates.editor.CanvasState
import me.aiglez.service.ui.templates.editor.CommonProperty
import me.aiglez.service.ui.templates.editor.GeometryService
import me.aiglez.service.ui.templates.editor.PageSize
import me.aiglez.service.ui.templates.editor.PagePoint
import me.aiglez.service.ui.templates.editor.PageRect
import me.aiglez.service.ui.templates.editor.ResizeHandle
import me.aiglez.service.ui.templates.editor.SnapGuideSet
import me.aiglez.service.ui.templates.editor.TemplateExpressionContext
import me.aiglez.service.ui.templates.editor.TemplateEditorState
import me.aiglez.service.ui.templates.editor.expressionIdentifier
import me.aiglez.service.ui.templates.editor.recordExpressionContext
import me.aiglez.service.ui.templates.editor.renderLegacyPlaceholder
import me.aiglez.service.ui.templates.editor.renderTemplateText
import me.aiglez.service.ui.templates.editor.sampleExpressionContext
import me.aiglez.service.ui.templates.editor.sampleSchemaExpressionContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun TopEditorToolbar(
    state: TemplateEditorState,
    previewOnly: Boolean = false,
    onHomeClick: () -> Unit = {},
    onEditClick: (() -> Unit)? = null,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onExportPdf: () -> Unit,
    onPreviewButtonClick: () -> Unit,
    onToggleSampleData: () -> Unit,
    onSetZoom: (Float) -> Unit,
    onSetRulerUnit: (String) -> Unit,
    onToggleSnap: () -> Unit,
    onSetNudgeDistance: (Float) -> Unit,
    onSetCanvasMetric: (CanvasMetric, Float) -> Unit,
    onZoomCommand: (ZoomCommand) -> Unit,
    onSelectSimilar: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onDuplicate: () -> Unit,
    onDeleteSelected: () -> Unit,
    onGroup: () -> Unit,
    onUngroup: () -> Unit,
    onLock: () -> Unit,
    onHide: () -> Unit,
    onAlign: (SelectionAlignment) -> Unit,
    onDistribute: (DistributionAxis) -> Unit,
    onMatchSize: (SizeMatchAxis) -> Unit,
) {
    if (previewOnly) {
        PreviewGenerationToolbar(
            state = state,
            onHomeClick = onHomeClick,
            onChooseDataClick = onPreviewButtonClick,
            onExportPdf = onExportPdf,
            onEditClick = onEditClick,
        )
        return
    }
    Surface(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 430.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ToolbarIconGroup {
                    IconButton(onClick = onHomeClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Home, contentDescription = "Accueil")
                    }
                    if (!previewOnly) {
                        IconButton(onClick = onUndo, enabled = state.canUndo, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Annuler")
                        }
                        IconButton(onClick = onRedo, enabled = state.canRedo, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Rétablir")
                        }
                        IconButton(onClick = onSave, enabled = state.template != null && !state.isSaving, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Save, contentDescription = "Enregistrer")
                        }
                    }
                }
                Column(
                    modifier = Modifier.width(172.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = state.template?.name ?: "Éditeur de modèle",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = pageSizeLabel(state.template?.pageSize),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (!previewOnly) {
                    ToolbarDivider()
                    ToolbarIconGroup {
                        EditorActionsMenu(
                        canvas = state.canvas,
                        enabled = state.selectedElements.isNotEmpty(),
                        onCopy = onCopy,
                        onPaste = onPaste,
                        onDuplicate = onDuplicate,
                        onDelete = onDeleteSelected,
                        onGroup = onGroup,
                        onUngroup = onUngroup,
                        onLock = onLock,
                        onHide = onHide,
                        onSelectSimilar = onSelectSimilar,
                        onSetNudgeDistance = onSetNudgeDistance,
                    )
                        AlignmentMenu(
                        enabled = state.selectedElements.isNotEmpty(),
                        onAlign = onAlign,
                        onDistribute = onDistribute,
                        onMatchSize = onMatchSize,
                    )
                        ViewMenu(
                        canvas = state.canvas,
                        showSampleData = state.showSampleData,
                        onSetRulerUnit = onSetRulerUnit,
                        onSetCanvasMetric = onSetCanvasMetric,
                        onToggleSampleData = onToggleSampleData,
                    )
                        SnapMenu(
                        canvas = state.canvas,
                        onToggleSnap = onToggleSnap,
                        onSetCanvasMetric = onSetCanvasMetric,
                        )
                    }
                }
                state.message?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(180.dp),
                    )
                }
                OutlinedButton(
                    onClick = onPreviewButtonClick,
                    enabled = state.template != null,
                    modifier = Modifier.height(36.dp),
                ) {
                    Text(if (previewOnly) "Choisir les données" else if (state.isPreviewMode) "Modifier" else "Aperçu")
                }
                if (previewOnly && onEditClick != null) {
                    OutlinedButton(
                        onClick = onEditClick,
                        enabled = state.template != null,
                        modifier = Modifier.height(36.dp),
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Modifier le modèle")
                    }
                }
                OutlinedButton(
                    onClick = onExportPdf,
                    enabled = state.template != null && !state.isExporting,
                    modifier = Modifier.height(36.dp),
                ) {
                    Text(if (state.isExporting) "Génération…" else "Générer le PDF")
                }
            }
            ToolbarZoomControls(
                zoom = state.canvas.zoom,
                hasSelection = state.selectedElements.isNotEmpty(),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp),
                onSetZoom = onSetZoom,
                onZoomCommand = onZoomCommand,
            )
        }
    }
}

@Composable
internal fun PreviewRecordDialog(
    state: TemplateEditorState,
    onDismiss: () -> Unit,
    onRecordSelected: (String, String) -> Unit,
    onShowPreview: () -> Unit,
) {
    val schemas = state.previewSchemas
    val canPreview = schemas.isNotEmpty() && schemas.all { !state.selectedPreviewRecordIds[it.id].isNullOrBlank() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choisir les données") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                if (schemas.isEmpty()) {
                    Text("Ce modèle n’utilise aucun modèle de données.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    schemas.forEach { schema ->
                        PreviewRecordDropdown(
                            schema = schema,
                            records = state.previewRecordsBySchemaId[schema.id].orEmpty(),
                            selectedRecordId = state.selectedPreviewRecordIds[schema.id],
                            onSelect = { recordId -> onRecordSelected(schema.id, recordId) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onShowPreview, enabled = canPreview) {
                Text("Afficher l’aperçu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
    )
}

@Composable
internal fun PreviewRecordDropdown(
    schema: DataSchema,
    records: List<DataRecord>,
    selectedRecordId: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = records.firstOrNull { it.id == selectedRecordId }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(schema.name, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = records.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(38.dp),
            ) {
                Text(
                    text = selected?.let { previewRecordLabel(it, schema.fields) } ?: if (records.isEmpty()) "Aucune donnée enregistrée" else "Choisir une donnée",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                records.forEach { record ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(previewRecordLabel(record, schema.fields), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(record.id, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        onClick = {
                            onSelect(record.id)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun EditorStatusBar(
    state: TemplateEditorState,
) {
    val selected = state.selectedElement
    val selectedBounds = selected?.let(GeometryService::getElementBounds)
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().height(30.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Page 1 / 1", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Zoom : ${(state.canvas.zoom * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
            Text(
                text = selected?.let { "Sélection : ${it.type.name} - ${it.name}" } ?: "Sélection : aucune",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(220.dp),
            )
            Text(
                text = selectedBounds?.let {
                    "X: ${formatCompact(it.x)} Y: ${formatCompact(it.y)} W: ${formatCompact(it.width)} H: ${formatCompact(it.height)}"
                } ?: "X: - Y: - W: - H: -",
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text("Curseur : canevas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Magnétisme : ${if (state.canvas.snapEnabled) "ACTIF" else "INACTIF"}", style = MaterialTheme.typography.labelMedium)
            Text("Grille : ${formatCompact(state.canvas.gridSize)} px", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
internal fun ToolbarDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.height(34.dp),
        ) {
            if (label.isNotBlank()) {
                Text("$label ", style = MaterialTheme.typography.labelMedium)
            }
            Text(selected, style = MaterialTheme.typography.labelMedium)
        }
        if (expanded) {
            DropdownMenu(expanded = true, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun EditorActionsMenu(
    canvas: CanvasState,
    enabled: Boolean,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onGroup: () -> Unit,
    onUngroup: () -> Unit,
    onLock: () -> Unit,
    onHide: () -> Unit,
    onSelectSimilar: () -> Unit,
    onSetNudgeDistance: (Float) -> Unit,
) {
    ToolbarMenu("Modifier") { close ->
        MenuCommand("Copier", enabled, onCopy, close)
        MenuCommand("Coller", true, onPaste, close)
        MenuCommand("Dupliquer", enabled, onDuplicate, close)
        MenuCommand("Supprimer", enabled, onDelete, close)
        HorizontalDivider()
        MenuCommand("Grouper", enabled, onGroup, close)
        MenuCommand("Dégrouper", enabled, onUngroup, close)
        MenuCommand("Verrouiller", enabled, onLock, close)
        MenuCommand("Masquer", enabled, onHide, close)
        MenuCommand("Sélectionner les éléments similaires", enabled, onSelectSimilar, close)
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Déplacement précis : ${canvas.nudgeDistance.formatForInput()} px") },
            onClick = {},
        )
        DropdownMenuItem(
            text = { Text("Réduire le déplacement") },
            onClick = { onSetNudgeDistance(canvas.nudgeDistance - 1f) },
        )
        DropdownMenuItem(
            text = { Text("Augmenter le déplacement") },
            onClick = { onSetNudgeDistance(canvas.nudgeDistance + 1f) },
        )
    }
}

@Composable
internal fun AlignmentMenu(
    enabled: Boolean,
    onAlign: (SelectionAlignment) -> Unit,
    onDistribute: (DistributionAxis) -> Unit,
    onMatchSize: (SizeMatchAxis) -> Unit,
) {
    ToolbarMenu("Aligner") { close ->
        MenuCommand("Aligner à gauche", enabled, { onAlign(SelectionAlignment.Left) }, close)
        MenuCommand("Centrer horizontalement", enabled, { onAlign(SelectionAlignment.Center) }, close)
        MenuCommand("Aligner à droite", enabled, { onAlign(SelectionAlignment.Right) }, close)
        MenuCommand("Aligner en haut", enabled, { onAlign(SelectionAlignment.Top) }, close)
        MenuCommand("Centrer verticalement", enabled, { onAlign(SelectionAlignment.Middle) }, close)
        MenuCommand("Aligner en bas", enabled, { onAlign(SelectionAlignment.Bottom) }, close)
        HorizontalDivider()
        MenuCommand("Distribuer horizontalement", enabled, { onDistribute(DistributionAxis.Horizontal) }, close)
        MenuCommand("Distribuer verticalement", enabled, { onDistribute(DistributionAxis.Vertical) }, close)
        HorizontalDivider()
        MenuCommand("Uniformiser la largeur", enabled, { onMatchSize(SizeMatchAxis.Width) }, close)
        MenuCommand("Uniformiser la hauteur", enabled, { onMatchSize(SizeMatchAxis.Height) }, close)
    }
}

@Composable
internal fun ViewMenu(
    canvas: CanvasState,
    showSampleData: Boolean,
    onSetRulerUnit: (String) -> Unit,
    onSetCanvasMetric: (CanvasMetric, Float) -> Unit,
    onToggleSampleData: () -> Unit,
) {
    ToolbarMenu("Affichage") { _ ->
        CheckMenuItem("Afficher les exemples de données", showSampleData) { onToggleSampleData() }
        HorizontalDivider()
        CheckMenuItem("Afficher les règles", canvas.showRulers) { onSetCanvasMetric(CanvasMetric.ShowRulers, it.asMetric()) }
        CheckMenuItem("Afficher la grille", canvas.showGrid) { onSetCanvasMetric(CanvasMetric.ShowGrid, it.asMetric()) }
        CheckMenuItem("Afficher les repères", canvas.showGuides) { onSetCanvasMetric(CanvasMetric.ShowGuides, it.asMetric()) }
        CheckMenuItem("Afficher les marges", canvas.showMargins) { onSetCanvasMetric(CanvasMetric.ShowMargins, it.asMetric()) }
        CheckMenuItem("Afficher le fond perdu", canvas.showBleed) { onSetCanvasMetric(CanvasMetric.ShowBleed, it.asMetric()) }
        CheckMenuItem("Afficher la zone de sécurité", canvas.showSafeArea) { onSetCanvasMetric(CanvasMetric.ShowSafeArea, it.asMetric()) }
        CheckMenuItem("Afficher l’ombre de la page", canvas.showPageShadow) { onSetCanvasMetric(CanvasMetric.ShowPageShadow, it.asMetric()) }
        HorizontalDivider()
        DropdownMenuItem(text = { Text("Unité : ${canvas.rulerUnit}") }, onClick = {})
        RulerUnits.forEach { unit ->
            DropdownMenuItem(
                text = { Text(unit) },
                onClick = { onSetRulerUnit(unit) },
            )
        }
    }
}

@Composable
internal fun SnapMenu(
    canvas: CanvasState,
    onToggleSnap: () -> Unit,
    onSetCanvasMetric: (CanvasMetric, Float) -> Unit,
) {
    ToolbarMenu("Magnétisme") { _ ->
        CheckMenuItem("Activer le magnétisme", canvas.snapEnabled) { onToggleSnap() }
        CheckMenuItem("Aligner sur la grille", canvas.snapToGrid) { onSetCanvasMetric(CanvasMetric.SnapToGrid, it.asMetric()) }
        CheckMenuItem("Aligner sur les objets", canvas.snapToObjects) { onSetCanvasMetric(CanvasMetric.SnapToObjects, it.asMetric()) }
        CheckMenuItem("Aligner sur les repères", canvas.snapToGuides) { onSetCanvasMetric(CanvasMetric.SnapToGuides, it.asMetric()) }
        CheckMenuItem("Aligner sur les marges", canvas.snapToMargins) { onSetCanvasMetric(CanvasMetric.SnapToMargins, it.asMetric()) }
        CheckMenuItem("Aligner au centre de la page", canvas.snapToPageCenter) { onSetCanvasMetric(CanvasMetric.SnapToPageCenter, it.asMetric()) }
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Tolérance : ${canvas.snapThreshold.formatForInput()} px") },
            onClick = {},
        )
        DropdownMenuItem(
            text = { Text("Réduire la tolérance") },
            onClick = { onSetCanvasMetric(CanvasMetric.SnapThreshold, canvas.snapThreshold - 1f) },
        )
        DropdownMenuItem(
            text = { Text("Augmenter la tolérance") },
            onClick = { onSetCanvasMetric(CanvasMetric.SnapThreshold, canvas.snapThreshold + 1f) },
        )
    }
}

@Composable
internal fun ToolbarMenu(
    label: String,
    content: @Composable (close: () -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }, modifier = Modifier.height(34.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            content { expanded = false }
        }
    }
}

@Composable
internal fun ToolbarIconGroup(
    content: @Composable () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.height(38.dp).padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            content()
        }
    }
}

@Composable
internal fun ToolbarZoomControls(
    zoom: Float,
    hasSelection: Boolean,
    modifier: Modifier = Modifier,
    onSetZoom: (Float) -> Unit,
    onZoomCommand: (ZoomCommand) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ToolbarIconGroup {
            TextButton(
                onClick = { onSetZoom((zoom - 0.1f).coerceIn(MinZoom, MaxZoom)) },
                modifier = Modifier.width(34.dp).height(34.dp),
            ) {
                Text("-")
            }
            ToolbarDropdown(
                label = "",
                selected = "${(zoom * 100).toInt()}%",
                options = listOf("25%", "50%", "75%", "100%", "125%", "150%", "200%"),
                onSelect = { value ->
                    value.removeSuffix("%").toFloatOrNull()?.let { onSetZoom(it / 100f) }
                },
            )
            TextButton(
                onClick = { onSetZoom((zoom + 0.1f).coerceIn(MinZoom, MaxZoom)) },
                modifier = Modifier.width(34.dp).height(34.dp),
            ) {
                Text("+")
            }
        }
        ToolbarIconGroup {
            CompactToolbarButton("Sélection", enabled = hasSelection) { onZoomCommand(ZoomCommand.Selection) }
            CompactToolbarButton("Page") { onZoomCommand(ZoomCommand.FitPage) }
            CompactToolbarButton("Largeur") { onZoomCommand(ZoomCommand.FitWidth) }
            CompactToolbarButton("100%") { onZoomCommand(ZoomCommand.Reset) }
        }
    }
}

@Composable
internal fun CompactToolbarButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(34.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
internal fun ToolbarDivider() {
    VerticalDivider(modifier = Modifier.height(26.dp))
}

@Composable
internal fun MenuCommand(
    label: String,
    enabled: Boolean,
    action: () -> Unit,
    close: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(label) },
        enabled = enabled,
        onClick = {
            action()
            close()
        },
    )
}

@Composable
internal fun CheckMenuItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(checked = checked, onCheckedChange = null)
                Text(label)
            }
        },
        onClick = { onCheckedChange(!checked) },
    )
}

internal fun Boolean.asMetric(): Float = if (this) 1f else 0f

internal fun previewRecordLabel(record: DataRecord, fields: List<SchemaField>): String {
    return fields
        .asSequence()
        .mapNotNull { field -> record.values[field.slug] ?: record.values[field.id] }
        .firstOrNull { it.isNotBlank() }
        ?: record.values.values.firstOrNull { it.isNotBlank() }
        ?: record.id
}
