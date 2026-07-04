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

internal fun buildPageRenderItems(
    elements: List<TemplateElement>,
    pageScale: Float,
    zoom: Float,
    textMeasurer: TextMeasurer,
    expressionContext: TemplateExpressionContext,
    resolveExpressions: Boolean,
): List<PageRenderItem> {
    return elements
        .asSequence()
        .filter { it.visible }
        .sortedBy { it.zIndex }
        .map { element ->
            when (element) {
                is TemplateElement.Text -> {
                    val bounds = GeometryService.getElementBounds(element).scaled(pageScale)
                    val contentWidth = (bounds.width - element.padding * pageScale * 2f).coerceAtLeast(0f)
                    val contentHeight = (bounds.height - element.padding * pageScale * 2f).coerceAtLeast(0f)
                    val rawText = element.staticText
                        ?: element.placeholderTag?.let { placeholder ->
                            if (resolveExpressions) renderLegacyPlaceholder(placeholder, expressionContext) else placeholder
                        }
                        ?: ""
                    val displayText = if (resolveExpressions) renderTemplateText(rawText, expressionContext) else rawText
                    val measuredText = textMeasurer.measure(
                        text = displayText,
                        style = androidx.compose.ui.text.TextStyle(
                            color = parseColor(element.color).withElementOpacity(element.opacity),
                            fontFamily = element.fontFamily.toComposeFontFamily(),
                            fontWeight = FontWeight(element.fontWeight),
                            fontStyle = element.fontStyle.toComposeFontStyle(),
                            fontSize = (element.fontSize * zoom).sp,
                            lineHeight = (element.fontSize * element.lineHeight * zoom).sp,
                            letterSpacing = (element.letterSpacing * zoom).sp,
                            textAlign = element.textAlign.toComposeTextAlign(),
                            textDirection = element.textDirection.toComposeTextDirection(),
                            textDecoration = if (element.underline) TextDecoration.Underline else null,
                        ),
                        overflow = TextOverflow.Clip,
                        softWrap = true,
                        constraints = Constraints(
                            maxWidth = contentWidth.toInt().coerceAtLeast(0),
                            maxHeight = contentHeight.toInt().coerceAtLeast(0),
                        ),
                    )
                    val verticalOffset = when (element.verticalAlign.lowercase()) {
                        "middle", "center" -> ((contentHeight - measuredText.size.height) / 2f).coerceAtLeast(0f)
                        "bottom" -> (contentHeight - measuredText.size.height).coerceAtLeast(0f)
                        else -> 0f
                    }
                    TextRenderItem(
                        element = element,
                        bounds = bounds,
                        background = parseColor(element.backgroundColor).withElementOpacity(element.opacity),
                        borderColor = parseColor(element.borderColor).withElementOpacity(element.opacity),
                        textLayout = measuredText,
                        textTopLeft = Offset(
                            bounds.x + element.padding * pageScale,
                            bounds.y + element.padding * pageScale + verticalOffset,
                        ),
                    )
                }
                is TemplateElement.Rectangle -> {
                    RectangleRenderItem(
                        element = element,
                        bounds = GeometryService.getElementBounds(element).scaled(pageScale),
                        fillColor = parseColor(element.fillColor).withElementOpacity(element.opacity),
                        borderColor = parseColor(element.borderColor).withElementOpacity(element.opacity),
                    )
                }
                is TemplateElement.Image -> {
                    val image = element.sourcePath.takeIf { it.isNotBlank() }?.let(::loadTemplateImageBitmap)
                    ImageRenderItem(
                        element = element,
                        bounds = GeometryService.getElementBounds(element).scaled(pageScale),
                        bitmap = image?.bitmap,
                        intrinsicWidth = image?.width ?: element.intrinsicWidth,
                        intrinsicHeight = image?.height ?: element.intrinsicHeight,
                        background = parseColor(element.backgroundColor).withElementOpacity(element.opacity),
                        borderColor = parseColor(element.borderColor).withElementOpacity(element.opacity),
                    )
                }
                is TemplateElement.Circle -> {
                    CircleRenderItem(
                        element = element,
                        bounds = GeometryService.getElementBounds(element).scaled(pageScale),
                        fillColor = parseColor(element.fillColor).withElementOpacity(element.opacity),
                        borderColor = parseColor(element.borderColor).withElementOpacity(element.opacity),
                    )
                }
                is TemplateElement.QRCode -> {
                    val renderedText = if (resolveExpressions) renderTemplateText(element.text, expressionContext) else element.text
                    QRCodeRenderItem(
                        element = element,
                        bounds = GeometryService.getElementBounds(element).scaled(pageScale),
                        matrix = generateTemplateQrMatrix(renderedText, element.quietZone),
                        foregroundColor = parseColor(element.foregroundColor).withElementOpacity(element.opacity),
                        backgroundColor = parseColor(element.backgroundColor).withElementOpacity(element.opacity),
                        borderColor = parseColor(element.borderColor).withElementOpacity(element.opacity),
                    )
                }
                is TemplateElement.Barcode -> {
                    val bounds = GeometryService.getElementBounds(element).scaled(pageScale)
                    val renderedText = if (resolveExpressions) renderTemplateText(element.text, expressionContext) else element.text
                    val textLayout = if (element.showText) {
                        textMeasurer.measure(
                            text = renderedText,
                            style = androidx.compose.ui.text.TextStyle(
                                color = parseColor(element.foregroundColor).withElementOpacity(element.opacity),
                                fontFamily = FontFamily.Monospace,
                                fontSize = (element.fontSize * zoom).sp,
                                textAlign = TextAlign.Center,
                            ),
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                            constraints = Constraints(maxWidth = bounds.width.toInt().coerceAtLeast(0)),
                        )
                    } else {
                        null
                    }
                    BarcodeRenderItem(
                        element = element,
                        bounds = bounds,
                        matrix = generateTemplateBarcodeMatrix(renderedText, element.format, element.quietZone),
                        foregroundColor = parseColor(element.foregroundColor).withElementOpacity(element.opacity),
                        backgroundColor = parseColor(element.backgroundColor).withElementOpacity(element.opacity),
                        borderColor = parseColor(element.borderColor).withElementOpacity(element.opacity),
                        textLayout = textLayout,
                    )
                }
                is TemplateElement.List -> {
                    val bounds = GeometryService.getElementBounds(element).scaled(pageScale)
                    val textColor = parseColor(element.color).withElementOpacity(element.opacity)
                    ListRenderItem(
                        element = element,
                        bounds = bounds,
                        background = parseColor(element.backgroundColor).withElementOpacity(element.opacity),
                        borderColor = parseColor(element.borderColor).withElementOpacity(element.opacity),
                        textColor = textColor,
                        itemLayouts = buildListItemLayouts(
                            element = element,
                            bounds = bounds,
                            pageScale = pageScale,
                            zoom = zoom,
                            textMeasurer = textMeasurer,
                            expressionContext = expressionContext,
                            resolveExpressions = resolveExpressions,
                            textColor = textColor,
                        ),
                    )
                }
                is TemplateElement.Line -> {
                    LineRenderItem(
                        element = element,
                        bounds = GeometryService.getElementBounds(element).scaled(pageScale),
                        start = Offset(element.x1 * pageScale, element.y1 * pageScale),
                        end = Offset(element.x2 * pageScale, element.y2 * pageScale),
                        color = Color(0xFF111827).withElementOpacity(element.opacity),
                        strokeWidth = element.thickness * pageScale,
                    )
                }
            }
        }
        .toList()
}

internal fun buildListItemLayouts(
    element: TemplateElement.List,
    bounds: PageRect,
    pageScale: Float,
    zoom: Float,
    textMeasurer: TextMeasurer,
    expressionContext: TemplateExpressionContext,
    resolveExpressions: Boolean,
    textColor: Color,
): kotlin.collections.List<ListItemRenderLayout> {
    val items = templateListValues(
        value = if (resolveExpressions) expressionContext.data[element.fieldSlug] else element.fieldSlug.toTemplateListPlaceholder(),
        separator = element.itemSeparator,
    )
        .take(element.maxItems.coerceAtLeast(1))
        .map { item -> element.prefix + truncateListItem(item, element.maxItemLength) + element.suffix }
        .filter { it.isNotBlank() }
    if (items.isEmpty()) return emptyList()

    val columns = element.columns.coerceIn(1, 6)
    val padding = element.padding * pageScale
    val columnGap = element.columnGap * pageScale
    val itemSpacing = element.itemSpacing * pageScale
    val availableWidth = (bounds.width - padding * 2f - columnGap * (columns - 1)).coerceAtLeast(1f)
    val columnWidth = (availableWidth / columns).coerceAtLeast(1f)
    val itemsPerColumn = ((items.size + columns - 1) / columns).coerceAtLeast(1)

    val layouts = mutableListOf<ListItemRenderLayout>()
    for (column in 0 until columns) {
        var y = bounds.y + padding
        val x = bounds.x + padding + column * (columnWidth + columnGap)
        val start = column * itemsPerColumn
        val end = min(start + itemsPerColumn, items.size)
        for (index in start until end) {
            val layout = textMeasurer.measure(
                text = items[index],
                style = androidx.compose.ui.text.TextStyle(
                    color = textColor,
                    fontFamily = element.fontFamily.toComposeFontFamily(),
                    fontWeight = FontWeight(element.fontWeight),
                    fontSize = (element.fontSize * zoom).sp,
                ),
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                constraints = Constraints(
                    maxWidth = columnWidth.toInt().coerceAtLeast(1),
                    maxHeight = (bounds.bottom - y).toInt().coerceAtLeast(0),
                ),
            )
            if (y + layout.size.height > bounds.bottom - padding / 2f) break
            layouts += ListItemRenderLayout(layout = layout, topLeft = Offset(x, y))
            y += layout.size.height + itemSpacing
        }
    }
    return layouts
}

internal fun String.toTemplateListPlaceholder(): String {
    if (isBlank()) return ""
    return if (contains(".")) "{{ $this }}" else "{{ data.$this }}"
}

internal fun templateListValues(value: Any?, separator: String): kotlin.collections.List<String> {
    return when (value) {
        is Collection<*> -> value.map { stringifyListValue(it) }.filter { it.isNotBlank() }
        is String -> {
            val actualSeparator = separator.ifEmpty { "," }
            value.split(actualSeparator).map { it.trim() }.filter { it.isNotBlank() }
        }
        null -> emptyList()
        else -> listOf(stringifyListValue(value)).filter { it.isNotBlank() }
    }
}

internal fun stringifyListValue(value: Any?): String {
    return when (value) {
        null -> ""
        is Collection<*> -> value.joinToString(", ") { stringifyListValue(it) }
        else -> value.toString()
    }
}

internal fun truncateListItem(value: String, maxLength: Int): String {
    val limit = maxLength.coerceAtLeast(1)
    return if (value.length <= limit) value else value.take((limit - 1).coerceAtLeast(0)) + "..."
}



