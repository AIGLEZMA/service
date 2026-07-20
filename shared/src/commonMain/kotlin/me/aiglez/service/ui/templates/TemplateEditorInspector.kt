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
import me.aiglez.service.domain.models.templateTableCellKey
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
internal fun RightInspectorPanel(
    state: TemplateEditorState,
    onSetCanvasMetric: (CanvasMetric, Float) -> Unit,
    onUpdateBounds: (PageRect) -> Unit,
    onUpdateCommon: (CommonProperty, String) -> Unit,
    onUpdateText: (String) -> Unit,
    onUpdateTextColor: (String) -> Unit,
    onUpdateTextFontSize: (Float) -> Unit,
    onUpdateTextFontFamily: (String) -> Unit,
    onUpdateTextFontStyle: (TemplateTextStyle) -> Unit,
    onUpdateTextLineHeight: (Float) -> Unit,
    onUpdateTextLetterSpacing: (Float) -> Unit,
    onUpdateTextAlign: (String) -> Unit,
    onUpdateTextVerticalAlign: (String) -> Unit,
    onUpdateTextDirection: (TemplateTextDirection) -> Unit,
    onUpdateTextBackground: (String) -> Unit,
    onUpdateTextPadding: (Float) -> Unit,
    onUpdateTextBorderColor: (String) -> Unit,
    onUpdateTextBorderWidth: (Float) -> Unit,
    onUpdateTextBorderStyle: (TemplateBorderStyle) -> Unit,
    onUpdateTextBorderRadius: (Float) -> Unit,
    onUpdateRectangleFill: (String) -> Unit,
    onUpdateRectangleBorderColor: (String) -> Unit,
    onUpdateRectangleBorderWidth: (Float) -> Unit,
    onUpdateRectangleBorderStyle: (TemplateBorderStyle) -> Unit,
    onUpdateRectangleBorderRadius: (Float) -> Unit,
    onUpdateCircleFill: (String) -> Unit,
    onUpdateCircleBorderColor: (String) -> Unit,
    onUpdateCircleBorderWidth: (Float) -> Unit,
    onUpdateCircleBorderStyle: (TemplateBorderStyle) -> Unit,
    onChooseImage: () -> Unit,
    onResizeImageToIntrinsic: () -> Unit,
    onFitImageFrameToAspect: () -> Unit,
    onUpdateImageContentMode: (TemplateImageContentMode) -> Unit,
    onUpdateImageAlignment: (TemplateImageAlignment) -> Unit,
    onUpdateImageBackground: (String) -> Unit,
    onUpdateImageBorderColor: (String) -> Unit,
    onUpdateImageBorderWidth: (Float) -> Unit,
    onUpdateImageBorderRadius: (Float) -> Unit,
    onUpdateQrText: (String) -> Unit,
    onUpdateQrForeground: (String) -> Unit,
    onUpdateQrBackground: (String) -> Unit,
    onUpdateQrQuietZone: (Float) -> Unit,
    onUpdateQrBorderColor: (String) -> Unit,
    onUpdateQrBorderWidth: (Float) -> Unit,
    onUpdateBarcodeText: (String) -> Unit,
    onUpdateBarcodeFormat: (TemplateBarcodeFormat) -> Unit,
    onUpdateBarcodeForeground: (String) -> Unit,
    onUpdateBarcodeBackground: (String) -> Unit,
    onUpdateBarcodeQuietZone: (Float) -> Unit,
    onUpdateBarcodeShowText: (Boolean) -> Unit,
    onUpdateBarcodeFontSize: (Float) -> Unit,
    onUpdateBarcodeBorderColor: (String) -> Unit,
    onUpdateBarcodeBorderWidth: (Float) -> Unit,
    onUpdateListFieldSlug: (String) -> Unit,
    onUpdateListPrefix: (String) -> Unit,
    onUpdateListSuffix: (String) -> Unit,
    onUpdateListItemSeparator: (String) -> Unit,
    onUpdateListMaxItems: (Float) -> Unit,
    onUpdateListMaxItemLength: (Float) -> Unit,
    onUpdateListColumns: (Float) -> Unit,
    onUpdateListColumnGap: (Float) -> Unit,
    onUpdateListItemSpacing: (Float) -> Unit,
    onUpdateListPadding: (Float) -> Unit,
    onUpdateListFontFamily: (String) -> Unit,
    onUpdateListFontSize: (Float) -> Unit,
    onUpdateListColor: (String) -> Unit,
    onUpdateListBackground: (String) -> Unit,
    onUpdateListBorderColor: (String) -> Unit,
    onUpdateListBorderWidth: (Float) -> Unit,
    onUpdateListBorderStyle: (TemplateBorderStyle) -> Unit,
    onUpdateListBorderRadius: (Float) -> Unit,
    onUpdateTableRows: (Float) -> Unit,
    onUpdateTableColumns: (Float) -> Unit,
    onUpdateTableHeaderRows: (Float) -> Unit,
    onUpdateTableFontFamily: (String) -> Unit,
    onUpdateTableFontSize: (Float) -> Unit,
    onUpdateTableTextColor: (String) -> Unit,
    onUpdateTableBackground: (String) -> Unit,
    onUpdateTableHeaderBackground: (String) -> Unit,
    onUpdateTableHeaderColor: (String) -> Unit,
    onUpdateTableAlternateRowColor: (String) -> Unit,
    onUpdateTableUseAlternateRows: (Boolean) -> Unit,
    onUpdateTableTextAlign: (String) -> Unit,
    onUpdateTableVerticalAlign: (String) -> Unit,
    onUpdateTablePadding: (Float) -> Unit,
    onUpdateTableBorderColor: (String) -> Unit,
    onUpdateTableBorderWidth: (Float) -> Unit,
    onUpdateTableBorderStyle: (TemplateBorderStyle) -> Unit,
    onUpdateTableBorderRadius: (Float) -> Unit,
    onUpdateTableGridBorderColor: (String) -> Unit,
    onUpdateTableGridBorderWidth: (Float) -> Unit,
    onUpdateTableCellText: (Int, Int, String) -> Unit,
    onUpdateTableCellBackground: (Int, Int, String) -> Unit,
    onUpdateTableCellTextColor: (Int, Int, String) -> Unit,
    onUpdateTableCellBorderColor: (Int, Int, String) -> Unit,
    onUpdateTableCellBorderWidth: (Int, Int, Float) -> Unit,
    onUpdateTableCellTextAlign: (Int, Int, String) -> Unit,
    onUpdateTableCellVerticalAlign: (Int, Int, String) -> Unit,
    onUpdateTableCellPadding: (Int, Int, Float) -> Unit,
    onDeleteSelected: () -> Unit,
) {
    val element = state.selectedElement
    val selectedCount = state.selectedElements.size
    var inspectorPage by remember { mutableStateOf(InspectorPage.Component) }
    Surface(
        modifier = Modifier.width(336.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Inspector", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = inspectorPage == InspectorPage.Component,
                    onClick = { inspectorPage = InspectorPage.Component },
                    label = { Text("Component") },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = inspectorPage == InspectorPage.PageSetup,
                    onClick = { inspectorPage = InspectorPage.PageSetup },
                    label = { Text("Page setup") },
                    modifier = Modifier.weight(1f),
                )
            }
            if (inspectorPage == InspectorPage.PageSetup) {
                PageSetupSection(
                    canvas = state.canvas,
                    onSetCanvasMetric = onSetCanvasMetric,
                )
                return@Column
            }
            if (element == null) {
                Text("No selection", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                return@Column
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(element.type.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(
                    if (selectedCount > 1) "$selectedCount selected" else "#${element.zIndex}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            InspectorSection("Element")
            TextFieldRow("Name", element.name) { onUpdateCommon(CommonProperty.Name, it) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ToggleRow("Locked", element.locked, { onUpdateCommon(CommonProperty.Locked, it.toString()) }, Modifier.weight(1f))
                ToggleRow("Visible", element.visible, { onUpdateCommon(CommonProperty.Visible, it.toString()) }, Modifier.weight(1f))
            }

            InspectorSection("Position")
            NumericGridRow(
                "X" to element.x,
                "Y" to element.y,
                onFirstChange = { onUpdateBounds(GeometryService.getElementBounds(element).copy(x = it)) },
                onSecondChange = { onUpdateBounds(GeometryService.getElementBounds(element).copy(y = it)) },
            )
            NumericGridRow(
                "Width" to element.width,
                "Height" to element.height,
                onFirstChange = { onUpdateBounds(GeometryService.getElementBounds(element).copy(width = it)) },
                onSecondChange = { onUpdateBounds(GeometryService.getElementBounds(element).copy(height = it)) },
            )
            NumericGridRow(
                "Right" to element.x + element.width,
                "Bottom" to element.y + element.height,
                onFirstChange = { onUpdateBounds(GeometryService.getElementBounds(element).copy(width = it - element.x)) },
                onSecondChange = { onUpdateBounds(GeometryService.getElementBounds(element).copy(height = it - element.y)) },
            )

            InspectorSection("Arrange")
            RotationRow(
                value = element.rotation,
                onValueChange = { onUpdateCommon(CommonProperty.Rotation, it.toString()) },
            )
            SliderRow("Opacity", element.opacity, 0f..1f, { onUpdateCommon(CommonProperty.Opacity, it.toString()) })
            NumericFieldRow("Z index", element.zIndex.toFloat()) { onUpdateCommon(CommonProperty.ZIndex, it.toInt().toString()) }
            HorizontalDivider()
            when (element) {
                is TemplateElement.Text -> {
                    InspectorSection("Text")
                    ExpressionTextEditor(
                        value = element.staticText.orEmpty(),
                        schema = state.schema,
                        legacyPlaceholder = element.placeholderTag,
                        onValueChange = onUpdateText,
                    )
                    DropdownRow("Font", listOf("Inter", "Sans", "Serif", "Monospace"), element.fontFamily, onUpdateTextFontFamily)
                    EnumRow("Style", TemplateTextStyle.entries.map { it.name }, element.fontStyle.name) {
                        onUpdateTextFontStyle(TemplateTextStyle.valueOf(it))
                    }
                    SliderRow("Font size", element.fontSize, 6f..96f, onUpdateTextFontSize)
                    SliderRow("Line height", element.lineHeight, 0.8f..3f, onUpdateTextLineHeight)
                    SliderRow("Letter spacing", element.letterSpacing, -2f..12f, onUpdateTextLetterSpacing)
                    EnumRow("Text align", listOf("left", "center", "right", "justify"), element.textAlign) {
                        onUpdateTextAlign(it)
                    }
                    EnumRow("Vertical align", listOf("top", "middle", "bottom"), element.verticalAlign) {
                        onUpdateTextVerticalAlign(it)
                    }
                    EnumRow("Direction", TemplateTextDirection.entries.map { it.name }, element.textDirection.name) {
                        onUpdateTextDirection(TemplateTextDirection.valueOf(it))
                    }
                    ColorFieldRow("Text color", element.color, onUpdateTextColor)
                    ColorFieldRow("Background", element.backgroundColor, onUpdateTextBackground)
                    SliderRow("Padding", element.padding, 0f..48f, onUpdateTextPadding)
                    InspectorSection("Border")
                    ColorFieldRow("Border color", element.borderColor, onUpdateTextBorderColor)
                    BorderStyleRow("Border style", element.borderStyle, onUpdateTextBorderStyle)
                    SliderRow("Border width", element.borderWidth, 0f..12f, onUpdateTextBorderWidth)
                    SliderRow("Border radius", element.borderRadius, 0f..48f, onUpdateTextBorderRadius)
                }
                is TemplateElement.Image -> {
                    InspectorSection("Image")
                    Button(
                        onClick = onChooseImage,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (element.sourcePath.isBlank()) "Choose from file" else "Replace file")
                    }
                    if (element.sourceName.isNotBlank()) {
                        Text(
                            element.sourceName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (element.intrinsicWidth > 0 && element.intrinsicHeight > 0) {
                        Text(
                            "${element.intrinsicWidth} x ${element.intrinsicHeight}px",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    EnumRow("Resize", TemplateImageContentMode.entries.map { it.name }, element.contentMode.name) {
                        onUpdateImageContentMode(TemplateImageContentMode.valueOf(it))
                    }
                    DropdownRow(
                        label = "Alignment",
                        options = TemplateImageAlignment.entries.map { it.name },
                        selected = element.alignment.name,
                        onSelect = { onUpdateImageAlignment(TemplateImageAlignment.valueOf(it)) },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onFitImageFrameToAspect,
                            enabled = element.intrinsicWidth > 0 && element.intrinsicHeight > 0,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Fit aspect")
                        }
                        OutlinedButton(
                            onClick = onResizeImageToIntrinsic,
                            enabled = element.intrinsicWidth > 0 && element.intrinsicHeight > 0,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Original size")
                        }
                    }
                    ColorFieldRow("Background", element.backgroundColor, onUpdateImageBackground)
                    InspectorSection("Border")
                    ColorFieldRow("Border color", element.borderColor, onUpdateImageBorderColor)
                    SliderRow("Border width", element.borderWidth, 0f..12f, onUpdateImageBorderWidth)
                    SliderRow("Border radius", element.borderRadius, 0f..72f, onUpdateImageBorderRadius)
                }
                is TemplateElement.Circle -> {
                    InspectorSection("Fill")
                    ColorFieldRow("Fill color", element.fillColor, onUpdateCircleFill)
                    InspectorSection("Border")
                    ColorFieldRow("Border color", element.borderColor, onUpdateCircleBorderColor)
                    BorderStyleRow("Border style", element.borderStyle, onUpdateCircleBorderStyle)
                    SliderRow("Border width", element.borderWidth, 0f..12f, onUpdateCircleBorderWidth)
                }
                is TemplateElement.QRCode -> {
                    InspectorSection("QR code")
                    OutlinedTextField(
                        value = element.text,
                        onValueChange = onUpdateQrText,
                        label = { Text("Text") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 3,
                        maxLines = 5,
                    )
                    SliderRow("Quiet zone", element.quietZone.toFloat(), 0f..8f, onUpdateQrQuietZone)
                    ColorFieldRow("Foreground", element.foregroundColor, onUpdateQrForeground)
                    ColorFieldRow("Background", element.backgroundColor, onUpdateQrBackground)
                    InspectorSection("Border")
                    ColorFieldRow("Border color", element.borderColor, onUpdateQrBorderColor)
                    SliderRow("Border width", element.borderWidth, 0f..12f, onUpdateQrBorderWidth)
                }
                is TemplateElement.Barcode -> {
                    InspectorSection("Barcode")
                    OutlinedTextField(
                        value = element.text,
                        onValueChange = onUpdateBarcodeText,
                        label = { Text("Text") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    EnumRow("Format", TemplateBarcodeFormat.entries.map { it.name }, element.format.name) {
                        onUpdateBarcodeFormat(TemplateBarcodeFormat.valueOf(it))
                    }
                    SliderRow("Quiet zone", element.quietZone.toFloat(), 0f..40f, onUpdateBarcodeQuietZone)
                    ToggleRow("Show text", element.showText, onUpdateBarcodeShowText)
                    SliderRow("Text size", element.fontSize, 6f..32f, onUpdateBarcodeFontSize)
                    ColorFieldRow("Foreground", element.foregroundColor, onUpdateBarcodeForeground)
                    ColorFieldRow("Background", element.backgroundColor, onUpdateBarcodeBackground)
                    InspectorSection("Border")
                    ColorFieldRow("Border color", element.borderColor, onUpdateBarcodeBorderColor)
                    SliderRow("Border width", element.borderWidth, 0f..12f, onUpdateBarcodeBorderWidth)
                }
                is TemplateElement.List -> {
                    val listFieldSlugs = state.schema?.fields
                        .orEmpty()
                        .filter { it.type == FieldType.LIST }
                        .map { it.slug }
                        .filter { it.isNotBlank() }
                    val fieldOptions = (listOf("Select field") + listFieldSlugs + element.fieldSlug.takeIf { it.isNotBlank() && it !in listFieldSlugs }.orEmpty())
                        .distinct()
                    InspectorSection("List data")
                    DropdownRow(
                        label = "Field",
                        options = fieldOptions,
                        selected = element.fieldSlug.ifBlank { "Select field" },
                        onSelect = { value -> onUpdateListFieldSlug(if (value == "Select field") "" else value) },
                    )
                    if (listFieldSlugs.isEmpty()) {
                        Text(
                            "No LIST fields in this schema.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextFieldRow("Prefix", element.prefix, onValueChange = onUpdateListPrefix)
                    TextFieldRow("Suffix", element.suffix, onValueChange = onUpdateListSuffix)
                    TextFieldRow("Separator", element.itemSeparator, onValueChange = onUpdateListItemSeparator)
                    SliderRow("Max items", element.maxItems.toFloat(), 1f..80f, onUpdateListMaxItems)
                    SliderRow("Max length", element.maxItemLength.toFloat(), 1f..240f, onUpdateListMaxItemLength)
                    InspectorSection("Layout")
                    SliderRow("Columns", element.columns.toFloat(), 1f..6f, onUpdateListColumns)
                    SliderRow("Column gap", element.columnGap, 0f..80f, onUpdateListColumnGap)
                    SliderRow("Item spacing", element.itemSpacing, 0f..32f, onUpdateListItemSpacing)
                    SliderRow("Padding", element.padding, 0f..48f, onUpdateListPadding)
                    InspectorSection("Text")
                    DropdownRow("Font", listOf("Inter", "Sans", "Serif", "Monospace"), element.fontFamily, onUpdateListFontFamily)
                    SliderRow("Font size", element.fontSize, 6f..48f, onUpdateListFontSize)
                    ColorFieldRow("Text color", element.color, onUpdateListColor)
                    ColorFieldRow("Background", element.backgroundColor, onUpdateListBackground)
                    InspectorSection("Border")
                    ColorFieldRow("Border color", element.borderColor, onUpdateListBorderColor)
                    BorderStyleRow("Border style", element.borderStyle, onUpdateListBorderStyle)
                    SliderRow("Border width", element.borderWidth, 0f..12f, onUpdateListBorderWidth)
                    SliderRow("Border radius", element.borderRadius, 0f..48f, onUpdateListBorderRadius)
                }
                is TemplateElement.Table -> {
                    var selectedRow by remember(element.id, element.rows) { mutableStateOf(0) }
                    var selectedColumn by remember(element.id, element.columns) { mutableStateOf(0) }
                    selectedRow = selectedRow.coerceIn(0, (element.rows - 1).coerceAtLeast(0))
                    selectedColumn = selectedColumn.coerceIn(0, (element.columns - 1).coerceAtLeast(0))
                    val cell = element.cells[templateTableCellKey(selectedRow, selectedColumn)]
                    val isHeaderCell = selectedRow < element.headerRows
                    val cellText = cell?.text ?: defaultTableCellText(selectedRow, selectedColumn, element.headerRows)
                    val cellBackground = cell?.backgroundColor
                        ?: if (isHeaderCell) element.headerBackgroundColor
                        else if (element.useAlternateRows && selectedRow % 2 == 1) element.alternateRowColor
                        else element.backgroundColor
                    val cellTextColor = cell?.color ?: if (isHeaderCell) element.headerColor else element.color

                    InspectorSection("Table")
                    SliderRow("Rows", element.rows.toFloat(), 1f..40f, onUpdateTableRows)
                    SliderRow("Columns", element.columns.toFloat(), 1f..12f, onUpdateTableColumns)
                    SliderRow("Header rows", element.headerRows.toFloat(), 0f..element.rows.toFloat().coerceAtLeast(1f), onUpdateTableHeaderRows)
                    ToggleRow("Alternate rows", element.useAlternateRows, onUpdateTableUseAlternateRows)
                    InspectorSection("Text")
                    DropdownRow("Font", listOf("Inter", "Sans", "Serif", "Monospace"), element.fontFamily, onUpdateTableFontFamily)
                    SliderRow("Font size", element.fontSize, 6f..48f, onUpdateTableFontSize)
                    EnumRow("Text align", listOf("left", "center", "right"), element.textAlign, onUpdateTableTextAlign)
                    EnumRow("Vertical align", listOf("top", "middle", "bottom"), element.verticalAlign, onUpdateTableVerticalAlign)
                    SliderRow("Padding", element.padding, 0f..32f, onUpdateTablePadding)
                    ColorFieldRow("Text color", element.color, onUpdateTableTextColor)
                    ColorFieldRow("Background", element.backgroundColor, onUpdateTableBackground)
                    ColorFieldRow("Header background", element.headerBackgroundColor, onUpdateTableHeaderBackground)
                    ColorFieldRow("Header text", element.headerColor, onUpdateTableHeaderColor)
                    ColorFieldRow("Alternate row", element.alternateRowColor, onUpdateTableAlternateRowColor)
                    InspectorSection("Border")
                    ColorFieldRow("Border color", element.borderColor, onUpdateTableBorderColor)
                    BorderStyleRow("Border style", element.borderStyle, onUpdateTableBorderStyle)
                    SliderRow("Border width", element.borderWidth, 0f..12f, onUpdateTableBorderWidth)
                    SliderRow("Border radius", element.borderRadius, 0f..48f, onUpdateTableBorderRadius)
                    ColorFieldRow("Cell border", element.cellBorderColor, onUpdateTableGridBorderColor)
                    SliderRow("Cell border width", element.cellBorderWidth, 0f..8f, onUpdateTableGridBorderWidth)
                    InspectorSection("Selected cell")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        DropdownRow(
                            label = "Row",
                            options = (0 until element.rows).map { (it + 1).toString() },
                            selected = (selectedRow + 1).toString(),
                            onSelect = { selectedRow = it.toIntOrNull()?.minus(1)?.coerceIn(0, element.rows - 1) ?: selectedRow },
                            modifier = Modifier.weight(1f),
                        )
                        DropdownRow(
                            label = "Column",
                            options = (0 until element.columns).map { (it + 1).toString() },
                            selected = (selectedColumn + 1).toString(),
                            onSelect = { selectedColumn = it.toIntOrNull()?.minus(1)?.coerceIn(0, element.columns - 1) ?: selectedColumn },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    ExpressionTextEditor(
                        value = cellText,
                        schema = state.schema,
                        legacyPlaceholder = null,
                        onValueChange = { onUpdateTableCellText(selectedRow, selectedColumn, it) },
                    )
                    ColorFieldRow("Cell background", cellBackground) { onUpdateTableCellBackground(selectedRow, selectedColumn, it) }
                    ColorFieldRow("Cell text", cellTextColor) { onUpdateTableCellTextColor(selectedRow, selectedColumn, it) }
                    ColorFieldRow("Cell border", cell?.borderColor ?: element.cellBorderColor) {
                        onUpdateTableCellBorderColor(selectedRow, selectedColumn, it)
                    }
                    SliderRow("Cell border width", cell?.borderWidth ?: element.cellBorderWidth, 0f..8f) {
                        onUpdateTableCellBorderWidth(selectedRow, selectedColumn, it)
                    }
                    EnumRow("Cell align", listOf("left", "center", "right"), cell?.textAlign ?: element.textAlign) {
                        onUpdateTableCellTextAlign(selectedRow, selectedColumn, it)
                    }
                    EnumRow("Cell vertical", listOf("top", "middle", "bottom"), cell?.verticalAlign ?: element.verticalAlign) {
                        onUpdateTableCellVerticalAlign(selectedRow, selectedColumn, it)
                    }
                    SliderRow("Cell padding", cell?.padding ?: element.padding, 0f..32f) {
                        onUpdateTableCellPadding(selectedRow, selectedColumn, it)
                    }
                }
                is TemplateElement.Rectangle -> {
                    InspectorSection("Fill")
                    ColorFieldRow("Fill color", element.fillColor, onUpdateRectangleFill)
                    InspectorSection("Border")
                    ColorFieldRow("Border color", element.borderColor, onUpdateRectangleBorderColor)
                    BorderStyleRow("Border style", element.borderStyle, onUpdateRectangleBorderStyle)
                    SliderRow("Border width", element.borderWidth, 0f..12f, onUpdateRectangleBorderWidth)
                    SliderRow("Border radius", element.borderRadius, 0f..72f, onUpdateRectangleBorderRadius)
                }
                is TemplateElement.Line -> Unit
            }
            Button(
                onClick = onDeleteSelected,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete")
            }
        }
    }
}
