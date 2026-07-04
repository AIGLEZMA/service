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

@Composable
internal fun PaletteIconTile(
    label: String,
    glyph: String,
    enabled: Boolean,
    onClick: () -> Unit,
    onDrop: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    var itemOrigin by remember { mutableStateOf(Offset.Zero) }
    var lastDragPosition by remember { mutableStateOf<Offset?>(null) }
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = Color(0xFFFAFAFA),
        contentColor = if (enabled) Color(0xFF4A4A4A) else Color(0xFF9CA3AF),
        modifier = Modifier
            .then(modifier)
            .height(74.dp)
            .border(1.dp, Color(0xFFD1D5DB), MaterialTheme.shapes.extraSmall)
            .onGloballyPositioned { coordinates ->
                itemOrigin = coordinates.boundsInWindow().topLeft
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        lastDragPosition = itemOrigin + offset
                    },
                    onDrag = { change, _ ->
                        lastDragPosition = itemOrigin + change.position
                        change.consume()
                    },
                    onDragEnd = {
                        lastDragPosition?.let(onDrop)
                        lastDragPosition = null
                    },
                    onDragCancel = {
                        lastDragPosition = null
                    },
                )
            },
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(glyph, style = MaterialTheme.typography.titleLarge, color = if (enabled) Color(0xFF4A4A4A) else Color(0xFF9CA3AF))
            Spacer(Modifier.height(3.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) Color(0xFF4A4A4A) else Color(0xFF9CA3AF),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun ComponentPaletteList(
    items: List<ComponentPaletteItem>,
    onAddElement: (TemplateElementType, Float, Float) -> Unit,
    onDropElement: (TemplateElementType, Offset) -> Unit,
) {
    if (items.isEmpty()) {
        EmptyPaletteText("No components")
        return
    }
    items.groupBy { it.group }.forEach { (group, groupItems) ->
        PaletteGroupHeader(group)
        groupItems.forEach { item ->
            PaletteListItem(
                label = item.label,
                meta = if (item.enabled) item.type.name else "Coming soon",
                glyph = item.glyph,
                enabled = item.enabled,
                onClick = { onAddElement(item.type, 64f, 64f) },
                onDrop = { onDropElement(item.type, it) },
            )
        }
    }
}

@Composable
internal fun DataFieldPaletteList(
    fields: List<SchemaField>,
    schemaName: String,
    onAddDataField: (String, String, String, Float, Float) -> Unit,
    onDropDataField: (String, String, String, Offset) -> Unit,
) {
    Text(schemaName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (fields.isEmpty()) {
        EmptyPaletteText("No data fields")
        return
    }
    fields.forEach { field ->
        PaletteListItem(
            label = field.name,
            meta = field.slug.ifBlank { field.type.name },
            glyph = field.type.name.take(2).uppercase(),
            enabled = true,
            onClick = { onAddDataField(schemaName, field.slug, field.name, 64f, 64f) },
            onDrop = { onDropDataField(schemaName, field.slug, field.name, it) },
        )
    }
}

@Composable
internal fun PaletteGroupHeader(label: String) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 6.dp),
    )
}

@Composable
internal fun EmptyPaletteText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
internal fun PaletteListItem(
    label: String,
    meta: String,
    glyph: String,
    enabled: Boolean,
    onClick: () -> Unit,
    onDrop: (Offset) -> Unit,
) {
    var itemOrigin by remember { mutableStateOf(Offset.Zero) }
    var lastDragPosition by remember { mutableStateOf<Offset?>(null) }
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = if (enabled) Color(0xFFF8FAFC) else Color(0xFFF1F5F9),
        contentColor = if (enabled) Color(0xFF4A4A4A) else Color(0xFF9CA3AF),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, Color(0xFFE2E8F0), MaterialTheme.shapes.extraSmall)
            .onGloballyPositioned { coordinates ->
                itemOrigin = coordinates.boundsInWindow().topLeft
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        lastDragPosition = itemOrigin + offset
                    },
                    onDrag = { change, _ ->
                        lastDragPosition = itemOrigin + change.position
                        change.consume()
                    },
                    onDragEnd = {
                        lastDragPosition?.let(onDrop)
                        lastDragPosition = null
                    },
                    onDragCancel = {
                        lastDragPosition = null
                    },
                )
            },
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PaletteGlyph(glyph = glyph, enabled = enabled)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text("+", style = MaterialTheme.typography.titleMedium, color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun PaletteGlyph(
    glyph: String,
    enabled: Boolean,
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(
                color = if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color(0xFFE2E8F0),
                shape = MaterialTheme.shapes.extraSmall,
            )
            .border(1.dp, if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else Color(0xFFCBD5E1), MaterialTheme.shapes.extraSmall),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            glyph,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

internal fun componentPaletteItems(): List<ComponentPaletteItem> {
    return listOf(
        ComponentPaletteItem("Text", "Tt", TemplateElementType.Text, group = "Basic"),
        ComponentPaletteItem("Rectangle", "Rect", TemplateElementType.Rectangle, group = "Basic"),
        ComponentPaletteItem("Line", "Line", TemplateElementType.Line, group = "Basic"),
        ComponentPaletteItem("Image", "IMG", TemplateElementType.Image, group = "Media"),
        ComponentPaletteItem("Circle", "Circ", TemplateElementType.Circle, group = "Shapes"),
        ComponentPaletteItem("QR Code", "QR", TemplateElementType.QRCode, group = "Data"),
        ComponentPaletteItem("Barcode", "|||", TemplateElementType.Barcode, group = "Data"),
        ComponentPaletteItem("Table", "Tbl", TemplateElementType.Table, group = "Layout", enabled = false),
        ComponentPaletteItem("List", "List", TemplateElementType.List, group = "Layout"),
        ComponentPaletteItem("Section", "Sec", TemplateElementType.Area, group = "Layout", enabled = false),
        ComponentPaletteItem("Area", "Box", TemplateElementType.Area, group = "Layout", enabled = false),
    )
}



