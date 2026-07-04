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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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

internal data class TemplateExpressionSnippet(
    val label: String,
    val value: String,
    val description: String,
)

@Composable
internal fun ExpressionTextEditor(
    value: String,
    schema: DataSchema?,
    legacyPlaceholder: String?,
    onValueChange: (String) -> Unit,
) {
    val expressionContext = remember(schema) {
        schema?.let { sampleSchemaExpressionContext(listOf(it), it) } ?: sampleExpressionContext(emptyList<SchemaField>())
    }
    val previewSource = value.ifBlank { legacyPlaceholder.orEmpty() }
    val preview = remember(previewSource, expressionContext) {
        if (previewSource.startsWith("[DataRecord:")) {
            renderLegacyPlaceholder(previewSource, expressionContext)
        } else {
            renderTemplateText(previewSource, expressionContext)
        }
    }
    val snippets = remember(schema) { buildTemplateExpressionSnippets(schema) }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Value") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 3,
            maxLines = 6,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                OutlinedButton(onClick = { menuExpanded = true }, modifier = Modifier.height(34.dp)) {
                    Text("Insert")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    snippets.forEach { snippet ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(snippet.label, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        snippet.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            onClick = {
                                onValueChange(appendTemplateSnippet(value, snippet.value))
                                menuExpanded = false
                            },
                        )
                    }
                }
            }
            Text(
                text = preview.ifBlank { " " },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

internal fun buildTemplateExpressionSnippets(schema: DataSchema?): List<TemplateExpressionSnippet> {
    val fields = schema?.fields.orEmpty()
    val schemaKey = schema?.name?.let(::expressionIdentifier).orEmpty().ifBlank { "RecordSchema" }
    val firstField = fields.firstOrNull()?.let { expressionIdentifier(it.name).ifBlank { it.slug } } ?: "FieldName"
    val firstPath = "$schemaKey.$firstField"
    val fieldSnippets = fields.map { field ->
        val fieldKey = expressionIdentifier(field.name).ifBlank { field.slug.ifBlank { field.id } }
        TemplateExpressionSnippet(
            label = field.name.ifBlank { field.slug },
            value = "{{ $schemaKey.$fieldKey }}",
            description = "$schemaKey.$fieldKey",
        )
    }
    return fieldSnippets + listOf(
        TemplateExpressionSnippet("Default", "{{ default($firstPath, \"N/A\") }}", "fallback value"),
        TemplateExpressionSnippet("If", "{{ if(eq($schemaKey.Status, \"paid\"), \"Paid\", \"Pending\") }}", "conditional text"),
        TemplateExpressionSnippet("Coalesce", "{{ coalesce($firstPath, \"Untitled\") }}", "first available value"),
        TemplateExpressionSnippet("Currency", "{{ currency($schemaKey.Total, \"USD\") }}", "money formatting"),
        TemplateExpressionSnippet("Percent", "{{ percent($schemaKey.Discount) }}", "percentage formatting"),
        TemplateExpressionSnippet("Uppercase", "{{ upper($firstPath) }}", "text formatting"),
        TemplateExpressionSnippet("Compare", "{{ if(gt($schemaKey.Amount, 100), \"High\", \"Standard\") }}", "number comparison"),
        TemplateExpressionSnippet("Join", "{{ join($schemaKey.Tags, \", \") }}", "list formatting"),
    )
}

internal fun appendTemplateSnippet(value: String, snippet: String): String {
    if (value.isBlank()) return snippet
    val separator = if (value.endsWith(" ") || value.endsWith("\n")) "" else " "
    return value + separator + snippet
}

@Composable
internal fun TextFieldRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
internal fun NumericFieldRow(
    label: String,
    value: Float,
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit,
) {
    var draft by remember(value) { mutableStateOf(value.formatForInput()) }
    OutlinedTextField(
        value = draft,
        onValueChange = { input ->
            draft = input
            input.toFloatOrNull()?.let(onValueChange)
        },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

@Composable
internal fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun InspectorSection(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
internal fun NumericGridRow(
    first: Pair<String, Float>,
    second: Pair<String, Float>,
    onFirstChange: (Float) -> Unit,
    onSecondChange: (Float) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        NumericFieldRow(first.first, first.second, Modifier.weight(1f), onFirstChange)
        NumericFieldRow(second.first, second.second, Modifier.weight(1f), onSecondChange)
    }
}

@Composable
internal fun PageSetupSection(
    canvas: CanvasState,
    onSetCanvasMetric: (CanvasMetric, Float) -> Unit,
) {
    InspectorSection("Page Setup")
    NumericGridRow(
        "Margin" to canvas.pageMargin,
        "Printable" to canvas.printableInset,
        onFirstChange = { onSetCanvasMetric(CanvasMetric.PageMargin, it) },
        onSecondChange = { onSetCanvasMetric(CanvasMetric.PrintableInset, it) },
    )
    NumericGridRow(
        "Bleed" to canvas.bleedInset,
        "Trim" to canvas.trimInset,
        onFirstChange = { onSetCanvasMetric(CanvasMetric.BleedInset, it) },
        onSecondChange = { onSetCanvasMetric(CanvasMetric.TrimInset, it) },
    )
    NumericGridRow(
        "Safe" to canvas.safeAreaInset,
        "Snap" to canvas.snapThreshold,
        onFirstChange = { onSetCanvasMetric(CanvasMetric.SafeAreaInset, it) },
        onSecondChange = { onSetCanvasMetric(CanvasMetric.SnapThreshold, it) },
    )
    NumericGridRow(
        "Header" to canvas.headerGuide,
        "Footer" to canvas.footerGuide,
        onFirstChange = { onSetCanvasMetric(CanvasMetric.HeaderGuide, it) },
        onSecondChange = { onSetCanvasMetric(CanvasMetric.FooterGuide, it) },
    )
    NumericGridRow(
        "Columns" to canvas.documentColumns.toFloat(),
        "Rows" to canvas.documentRows.toFloat(),
        onFirstChange = { onSetCanvasMetric(CanvasMetric.DocumentColumns, it) },
        onSecondChange = { onSetCanvasMetric(CanvasMetric.DocumentRows, it) },
    )
    NumericGridRow(
        "Grid" to canvas.gridSize,
        "Baseline" to canvas.baselineGrid,
        onFirstChange = { onSetCanvasMetric(CanvasMetric.GridSize, it) },
        onSecondChange = { onSetCanvasMetric(CanvasMetric.BaselineGrid, it) },
    )
    ToggleRow(
        label = "Page outline",
        checked = canvas.showPageOutline,
        onCheckedChange = { checked -> onSetCanvasMetric(CanvasMetric.ShowPageOutline, if (checked) 1f else 0f) },
    )
}

@Composable
internal fun SliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value.formatForInput(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun RotationRow(
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    val standardAngles = listOf(0f, 45f, 90f, -45f, -90f, 180f, -180f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Rotation", style = MaterialTheme.typography.labelMedium)
            Text("${value.roundToInt()}°", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(
            value = value.coerceIn(-180f, 180f),
            onValueChange = { onValueChange(GeometryService.snapRotation(it).coerceIn(-180f, 180f)) },
            valueRange = -180f..180f,
            steps = 359,
            modifier = Modifier.fillMaxWidth(),
        )
        NumericFieldRow("Rotation", value) { onValueChange(it.coerceIn(-180f, 180f)) }
        standardAngles.chunked(4).forEach { rowAngles ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                rowAngles.forEach { angle ->
                    FilterChip(
                        selected = value.roundToInt() == angle.toInt(),
                        onClick = { onValueChange(angle) },
                        label = { Text("${angle.toInt()}°") },
                    )
                }
            }
        }
    }
}

@Composable
internal fun DropdownRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, style = MaterialTheme.typography.labelMedium)
                Text(selected, style = MaterialTheme.typography.bodyMedium)
            }
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
internal fun ColorFieldRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var draftColor by remember(value) { mutableStateOf(normalizeColorHex(value)) }
    fun openPicker() {
        draftColor = normalizeColorHex(value)
        expanded = true
    }
    fun applyDraftColor() {
        val normalized = normalizeColorHex(draftColor)
        draftColor = normalized
        onValueChange(normalized)
        expanded = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                onClick = ::openPicker,
                color = parseColor(value),
                modifier = Modifier
                    .size(34.dp)
                    .border(1.dp, Color(0xFFCBD5E1)),
            ) {}
            TextFieldRow(label, value, Modifier.weight(1f), onValueChange)
        }
        if (expanded) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { expanded = false },
            ) {
                val colorPickerController = rememberColorPickerController()
                Column(
                    modifier = Modifier.width(280.dp).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(label, style = MaterialTheme.typography.labelMedium)
                    HsvColorPicker(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        controller = colorPickerController,
                        initialColor = parseColor(draftColor),
                        onColorChanged = { colorEnvelope ->
                            if (colorEnvelope.fromUser) {
                                draftColor = normalizePickerHex(colorEnvelope.hexCode)
                            }
                        },
                    )
                    Text("Brightness", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    BrightnessSlider(
                        modifier = Modifier.fillMaxWidth().height(24.dp),
                        controller = colorPickerController,
                        initialColor = parseColor(draftColor),
                    )
                    Text("Alpha", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    AlphaSlider(
                        modifier = Modifier.fillMaxWidth().height(24.dp),
                        controller = colorPickerController,
                        initialColor = parseColor(draftColor),
                    )
                    TextFieldRow("Hex", draftColor) { draftColor = it }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            color = parseColor(draftColor),
                            modifier = Modifier
                                .size(28.dp)
                                .border(1.dp, Color(0xFFCBD5E1)),
                        ) {}
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { expanded = false }) {
                                Text("Cancel")
                            }
                            Button(onClick = ::applyDraftColor) {
                                Text("Apply")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BorderStyleRow(
    label: String,
    selected: TemplateBorderStyle,
    onSelect: (TemplateBorderStyle) -> Unit,
) {
    DropdownRow(
        label = label,
        options = TemplateBorderStyle.entries.map { it.name.lowercase() },
        selected = selected.name.lowercase(),
        onSelect = { option ->
            TemplateBorderStyle.entries.firstOrNull { it.name.equals(option, ignoreCase = true) }?.let(onSelect)
        },
    )
}

@Composable
internal fun EnumRow(
    label: String,
    values: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            values.forEach { value ->
                FilterChip(
                    selected = value == selected,
                    onClick = { onSelect(value) },
                    label = { Text(value) },
                )
            }
        }
    }
}
