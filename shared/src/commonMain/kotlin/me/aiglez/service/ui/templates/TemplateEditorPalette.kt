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
internal fun LeftComponentPalette(
    state: TemplateEditorState,
    onAddElement: (TemplateElementType, Float, Float) -> Unit,
    onAddDataField: (String, String, String, Float, Float) -> Unit,
    onDropElement: (TemplateElementType, Offset) -> Unit,
    onDropDataField: (String, String, String, Offset) -> Unit,
) {
    var selectedTab by remember { mutableStateOf(PaletteTab.Components) }
    Surface(
        modifier = Modifier.width(214.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(start = 10.dp, top = 10.dp, end = 8.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PaletteTabButton(
                    label = "Components",
                    selected = selectedTab == PaletteTab.Components,
                    onClick = { selectedTab = PaletteTab.Components },
                )
                PaletteTabButton(
                    label = "Data schemas",
                    selected = selectedTab == PaletteTab.DataFields,
                    onClick = { selectedTab = PaletteTab.DataFields },
                )
            }
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(end = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (selectedTab) {
                    PaletteTab.Components -> ComponentIconGrid(
                        items = componentPaletteItems(),
                        onAddElement = onAddElement,
                        onDropElement = onDropElement,
                    )
                    PaletteTab.DataFields -> DataSchemaTreeList(
                        primarySchema = state.schema,
                        availableSchemas = state.availableSchemas,
                        onAddDataField = onAddDataField,
                        onDropDataField = onDropDataField,
                    )
                }
            }
        }
    }
}

@Composable
internal fun PaletteModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
    )
}

@Composable
internal fun PaletteTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (selected) Color(0xFFF8FAFC) else Color(0xFFBDBDBD),
        contentColor = if (selected) Color(0xFF4A4A4A) else Color.White,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.height(36.dp),
    ) {
        Box(
            modifier = Modifier
                .border(
                    width = if (selected) 0.dp else 1.dp,
                    color = if (selected) Color.Transparent else Color(0xFFBDBDBD),
                    shape = MaterialTheme.shapes.extraSmall,
                )
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
internal fun ComponentIconGrid(
    items: List<ComponentPaletteItem>,
    onAddElement: (TemplateElementType, Float, Float) -> Unit,
    onDropElement: (TemplateElementType, Offset) -> Unit,
) {
    items.chunked(2).forEach { rowItems ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            rowItems.forEach { item ->
                PaletteIconTile(
                    label = item.label,
                    glyph = item.glyph,
                    enabled = item.enabled,
                    onClick = { onAddElement(item.type, 64f, 64f) },
                    onDrop = { onDropElement(item.type, it) },
                    modifier = Modifier.weight(1f),
                )
            }
            if (rowItems.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun DataSchemaTreeList(
    primarySchema: DataSchema?,
    availableSchemas: List<DataSchema>,
    onAddDataField: (String, String, String, Float, Float) -> Unit,
    onDropDataField: (String, String, String, Offset) -> Unit,
) {
    val rootSchemas = (listOfNotNull(primarySchema) + availableSchemas).distinctBy { it.id }
    if (rootSchemas.isEmpty()) {
        EmptyPaletteText("No data schemas")
        return
    }
    var collapsedSchemaIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var collapsedBranchIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        rootSchemas.forEach { schema ->
            val expanded = schema.id !in collapsedSchemaIds
            DataSchemaTreeSection(
                schema = schema,
                availableSchemas = availableSchemas,
                expanded = expanded,
                onToggle = { collapsedSchemaIds = toggleTreeKey(collapsedSchemaIds, schema.id) },
                collapsedBranchIds = collapsedBranchIds,
                onToggleBranch = { branchId -> collapsedBranchIds = toggleTreeKey(collapsedBranchIds, branchId) },
                onAddDataField = onAddDataField,
                onDropDataField = onDropDataField,
            )
        }
    }
}

@Composable
internal fun DataSchemaTreeSection(
    schema: DataSchema,
    availableSchemas: List<DataSchema>,
    expanded: Boolean,
    onToggle: () -> Unit,
    collapsedBranchIds: Set<String>,
    onToggleBranch: (String) -> Unit,
    onAddDataField: (String, String, String, Float, Float) -> Unit,
    onDropDataField: (String, String, String, Offset) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        PaletteSchemaHeader(
            label = schema.name,
            fieldCount = schema.fields.size,
            expanded = expanded,
            onToggle = onToggle,
        )
        if (!expanded) {
            return@Column
        }
        if (schema.fields.isEmpty()) {
            EmptyPaletteText("No data fields")
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                TreeRail(modifier = Modifier.fillMaxHeight())
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    DataFieldTreeBranch(
                        schema = schema,
                        availableSchemas = availableSchemas,
                        visitedSchemaIds = setOf(schema.id),
                        collapsedBranchIds = collapsedBranchIds,
                        onToggleBranch = onToggleBranch,
                        onAddDataField = onAddDataField,
                        onDropDataField = onDropDataField,
                    )
                }
            }
        }
    }
}

@Composable
internal fun PaletteSchemaHeader(
    label: String,
    fieldCount: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Surface(
        onClick = onToggle,
        color = Color(0xFFF3F4F6),
        contentColor = Color(0xFF374151),
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE5E7EB), MaterialTheme.shapes.extraSmall),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TreeDisclosure(expanded = expanded)
            Text(
                text = "{}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = fieldCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B7280),
            )
        }
    }
}

@Composable
internal fun TreeRail(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.width(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.width(2.dp).fillMaxHeight().background(Color(0xFFC7D2FE)),
        )
    }
}

@Composable
internal fun DataFieldTreeBranch(
    schema: DataSchema,
    availableSchemas: List<DataSchema>,
    visitedSchemaIds: Set<String>,
    collapsedBranchIds: Set<String>,
    onToggleBranch: (String) -> Unit,
    onAddDataField: (String, String, String, Float, Float) -> Unit,
    onDropDataField: (String, String, String, Offset) -> Unit,
) {
    schema.fields.forEach { field ->
        val childSchema = field.referenceSchemaId
            ?.let { referenceSchemaId -> availableSchemas.firstOrNull { it.id == referenceSchemaId } }
            ?.takeUnless { it.id in visitedSchemaIds }
        val branchId = childSchema?.let { "${schema.id}:${field.id}:${it.id}" }.orEmpty()
        val branchExpanded = branchId.isNotBlank() && branchId !in collapsedBranchIds
        DataFieldTreeItem(
            schemaName = schema.name,
            field = field,
            hasChildren = childSchema != null,
            expanded = branchExpanded,
            onToggleBranch = {
                if (branchId.isNotBlank()) onToggleBranch(branchId)
            },
            onAddDataField = onAddDataField,
            onDropDataField = onDropDataField,
        )
        if (childSchema != null && branchExpanded) {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(start = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                TreeRail(modifier = Modifier.fillMaxHeight())
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    DataFieldTreeBranch(
                        schema = childSchema,
                        availableSchemas = availableSchemas,
                        visitedSchemaIds = visitedSchemaIds + childSchema.id,
                        collapsedBranchIds = collapsedBranchIds,
                        onToggleBranch = onToggleBranch,
                        onAddDataField = onAddDataField,
                        onDropDataField = onDropDataField,
                    )
                }
            }
        }
    }
}

@Composable
internal fun DataFieldTreeItem(
    schemaName: String,
    field: SchemaField,
    hasChildren: Boolean,
    expanded: Boolean,
    onToggleBranch: () -> Unit,
    onAddDataField: (String, String, String, Float, Float) -> Unit,
    onDropDataField: (String, String, String, Offset) -> Unit,
) {
    var itemOrigin by remember { mutableStateOf(Offset.Zero) }
    var lastDragPosition by remember { mutableStateOf<Offset?>(null) }
    Surface(
        onClick = { onAddDataField(schemaName, field.slug, field.name, 64f, 64f) },
        color = Color.White,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE5E7EB), MaterialTheme.shapes.extraSmall)
            .onGloballyPositioned { coordinates ->
                itemOrigin = coordinates.boundsInWindow().topLeft
            }
            .pointerInput(schemaName, field.id) {
                detectDragGestures(
                    onDragStart = { offset ->
                        lastDragPosition = itemOrigin + offset
                    },
                    onDrag = { change, _ ->
                        lastDragPosition = itemOrigin + change.position
                        change.consume()
                    },
                    onDragEnd = {
                        lastDragPosition?.let { onDropDataField(schemaName, field.slug, field.name, it) }
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
            modifier = Modifier.fillMaxWidth().padding(start = 5.dp, top = 3.dp, end = 7.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (hasChildren) {
                TreeToggle(expanded = expanded, onClick = onToggleBranch)
            } else {
                Spacer(Modifier.size(width = 16.dp, height = 22.dp))
            }
            FieldTypeGlyph(field)
            Text(
                text = fieldDisplayLabel(field),
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF374151),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun TreeToggle(
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        contentColor = Color(0xFF6B7280),
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.size(width = 16.dp, height = 22.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (expanded) "v" else ">",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
internal fun TreeDisclosure(expanded: Boolean) {
    Box(
        modifier = Modifier.size(width = 16.dp, height = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (expanded) "v" else ">",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7280),
            maxLines = 1,
        )
    }
}

@Composable
internal fun FieldTypeGlyph(field: SchemaField) {
    Surface(
        color = fieldGlyphBackground(field),
        contentColor = fieldGlyphColor(field),
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.size(width = 28.dp, height = 22.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = fieldGlyph(field),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

internal fun toggleTreeKey(keys: Set<String>, key: String): Set<String> {
    return if (key in keys) keys - key else keys + key
}

internal fun fieldDisplayLabel(field: SchemaField): String {
    return field.slug.ifBlank { field.name }
}

internal fun fieldGlyph(field: SchemaField): String {
    val label = "${field.slug} ${field.name}".lowercase()
    return when {
        "date" in label -> "CAL"
        "image" in label || "logo" in label || "photo" in label -> "IMG"
        field.type == FieldType.TEXT -> "Tt"
        field.type == FieldType.NUMBER -> "123"
        field.type == FieldType.DOUBLE -> "#"
        field.type == FieldType.REFERENCE -> "{}"
        field.type == FieldType.LIST -> "[]"
        else -> "?"
    }
}

internal fun fieldGlyphBackground(field: SchemaField): Color {
    val label = "${field.slug} ${field.name}".lowercase()
    return when {
        "date" in label -> Color(0xFFEFF6FF)
        "image" in label || "logo" in label || "photo" in label -> Color(0xFFF0FDF4)
        field.type == FieldType.TEXT -> Color(0xFFF3F4F6)
        field.type == FieldType.NUMBER || field.type == FieldType.DOUBLE -> Color(0xFFFFF7ED)
        field.type == FieldType.REFERENCE -> Color(0xFFF5F3FF)
        field.type == FieldType.LIST -> Color(0xFFECFEFF)
        else -> Color(0xFFF3F4F6)
    }
}

internal fun fieldGlyphColor(field: SchemaField): Color {
    val label = "${field.slug} ${field.name}".lowercase()
    return when {
        "date" in label -> Color(0xFF1D4ED8)
        "image" in label || "logo" in label || "photo" in label -> Color(0xFF15803D)
        field.type == FieldType.TEXT -> Color(0xFF374151)
        field.type == FieldType.NUMBER || field.type == FieldType.DOUBLE -> Color(0xFFC2410C)
        field.type == FieldType.REFERENCE -> Color(0xFF6D28D9)
        field.type == FieldType.LIST -> Color(0xFF0E7490)
        else -> Color(0xFF374151)
    }
}
