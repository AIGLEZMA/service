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

internal fun androidx.compose.ui.input.pointer.PointerEvent.toInteractionModifiers(): InteractionModifiers {
    val modifiers = keyboardModifiers
    return InteractionModifiers(
        shift = modifiers.isPointerShiftPressed,
        alt = modifiers.isPointerAltPressed,
        command = modifiers.isPointerCtrlPressed || modifiers.isPointerMetaPressed,
    )
}

internal data class RulerSpec(
    val minorStep: Float,
    val minorsPerMajor: Int,
)

internal fun rulerSpec(unit: String): RulerSpec {
    return when (unit) {
        "mm" -> RulerSpec(minorStep = mmToPageUnits(5f), minorsPerMajor = 2)
        "cm" -> RulerSpec(minorStep = mmToPageUnits(5f), minorsPerMajor = 2)
        "inch" -> RulerSpec(minorStep = 18f, minorsPerMajor = 4)
        else -> RulerSpec(minorStep = 10f, minorsPerMajor = 10)
    }
}

internal fun formatRulerValue(value: Float, unit: String): String {
    return when (unit) {
        "mm" -> pageUnitsToMm(value).roundToInt().toString()
        "cm" -> formatCompact(pageUnitsToMm(value) / 10f)
        "inch" -> formatCompact(value / 72f)
        else -> value.roundToInt().toString()
    }
}

internal fun formatPageValue(value: Float, unit: String): String {
    val formatted = when (unit) {
        "mm" -> formatCompact(pageUnitsToMm(value))
        "cm" -> formatCompact(pageUnitsToMm(value) / 10f)
        "inch" -> formatCompact(value / 72f)
        else -> value.roundToInt().toString()
    }
    return "$formatted $unit"
}

internal fun pageSizeLabel(pageSize: String?): String {
    return when (pageSize?.lowercase()) {
        "letter" -> "Letter - 8.5 x 11 inch"
        "a5" -> "A5 - 148 x 210 mm"
        "a4", null -> "A4 - 210 x 297 mm"
        else -> "$pageSize - ${formatPageValue(PageWidth, "mm")} x ${formatPageValue(PageHeight, "mm")}"
    }
}

internal fun pageUnitsToMm(value: Float): Float {
    return value / 72f * 25.4f
}

internal fun mmToPageUnits(value: Float): Float {
    return value / 25.4f * 72f
}

internal fun formatCompact(value: Float): String {
    val rounded = kotlin.math.round(value * 10f) / 10f
    return if (rounded % 1f == 0f) rounded.toInt().toString() else rounded.toString()
}

internal fun PageRect.scaled(scale: Float): PageRect {
    return PageRect(x * scale, y * scale, width * scale, height * scale)
}

internal fun PageRect.expanded(amount: Float): PageRect {
    return PageRect(
        x = x - amount,
        y = y - amount,
        width = width + amount * 2f,
        height = height + amount * 2f,
    )
}

internal fun PageRect.clampedToPage(): PageRect {
    val left = x.coerceIn(0f, PageWidth)
    val top = y.coerceIn(0f, PageHeight)
    val right = right.coerceIn(left, PageWidth)
    val bottom = bottom.coerceIn(top, PageHeight)
    return PageRect(
        x = left,
        y = top,
        width = (right - left).coerceAtLeast(1f),
        height = (bottom - top).coerceAtLeast(1f),
    )
}

internal fun List<PageRect>.renderUnionBounds(): PageRect? {
    if (isEmpty()) return null
    val left = minOf { it.x }
    val top = minOf { it.y }
    val right = maxOf { it.right }
    val bottom = maxOf { it.bottom }
    return PageRect(left, top, right - left, bottom - top)
}

internal fun PageRect.center(): Offset {
    return Offset(x + width / 2f, y + height / 2f)
}

internal fun imageDestinationRect(
    frame: PageRect,
    imageWidth: Int,
    imageHeight: Int,
    mode: TemplateImageContentMode,
    alignment: TemplateImageAlignment,
): PageRect {
    if (mode == TemplateImageContentMode.Stretch || imageWidth <= 0 || imageHeight <= 0) {
        return frame
    }
    val scale = when (mode) {
        TemplateImageContentMode.Fit -> min(frame.width / imageWidth, frame.height / imageHeight)
        TemplateImageContentMode.Fill -> max(frame.width / imageWidth, frame.height / imageHeight)
        TemplateImageContentMode.Stretch -> 1f
    }.coerceAtLeast(0.001f)
    val width = imageWidth * scale
    val height = imageHeight * scale
    val x = when (alignment) {
        TemplateImageAlignment.TopLeft,
        TemplateImageAlignment.Left,
        TemplateImageAlignment.BottomLeft -> frame.x
        TemplateImageAlignment.TopRight,
        TemplateImageAlignment.Right,
        TemplateImageAlignment.BottomRight -> frame.right - width
        else -> frame.x + (frame.width - width) / 2f
    }
    val y = when (alignment) {
        TemplateImageAlignment.TopLeft,
        TemplateImageAlignment.Top,
        TemplateImageAlignment.TopRight -> frame.y
        TemplateImageAlignment.BottomLeft,
        TemplateImageAlignment.Bottom,
        TemplateImageAlignment.BottomRight -> frame.bottom - height
        else -> frame.y + (frame.height - height) / 2f
    }
    return PageRect(x, y, width, height)
}

internal fun DrawScope.drawImagePlaceholder(bounds: PageRect, pageScale: Float) {
    val inset = (10f * pageScale).coerceAtLeast(6f)
    val color = Color(0xFF94A3B8)
    drawRect(
        color = color.copy(alpha = 0.10f),
        topLeft = Offset(bounds.x + inset, bounds.y + inset),
        size = Size((bounds.width - inset * 2f).coerceAtLeast(1f), (bounds.height - inset * 2f).coerceAtLeast(1f)),
        style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f), 0f)),
    )
    drawLine(
        color = color.copy(alpha = 0.72f),
        start = Offset(bounds.x + inset, bounds.bottom - inset),
        end = Offset(bounds.x + bounds.width * 0.42f, bounds.y + bounds.height * 0.54f),
        strokeWidth = 1.5.dp.toPx(),
    )
    drawLine(
        color = color.copy(alpha = 0.72f),
        start = Offset(bounds.x + bounds.width * 0.42f, bounds.y + bounds.height * 0.54f),
        end = Offset(bounds.right - inset, bounds.bottom - inset),
        strokeWidth = 1.5.dp.toPx(),
    )
    drawCircle(
        color = color.copy(alpha = 0.42f),
        radius = (8f * pageScale).coerceIn(4f, 14f),
        center = Offset(bounds.right - inset * 1.7f, bounds.y + inset * 1.7f),
    )
}

internal fun PageRect.centerPagePoint(): PagePoint {
    return PagePoint(centerX, centerY)
}

internal fun PagePoint.rotatedAround(
    pivot: PagePoint,
    degrees: Float,
): PagePoint {
    if (abs(degrees) < 0.001f) return this
    val radians = degrees * PI.toFloat() / 180f
    val dx = x - pivot.x
    val dy = y - pivot.y
    val cosValue = cos(radians)
    val sinValue = sin(radians)
    return PagePoint(
        x = pivot.x + dx * cosValue - dy * sinValue,
        y = pivot.y + dx * sinValue + dy * cosValue,
    )
}

internal data class SelectionControlHit(
    val resizeHandle: ResizeHandle? = null,
    val isRotation: Boolean = false,
)

internal fun hitTestSelectionControl(
    element: TemplateElement,
    point: PagePoint,
    density: Float,
    pageScale: Float,
): SelectionControlHit? {
    val bounds = GeometryService.getElementBounds(element)
    val rotationHandleOffset = dpToPageUnits(RotationHandleOffsetDp, density, pageScale)
    if (
        isRotationHandleHit(
            bounds = bounds,
            rotation = element.rotation,
            point = point,
            hitSize = dpToPageUnits(RotationHandleHitSizeDp, density, pageScale),
            handleOffset = rotationHandleOffset,
        )
    ) {
        return SelectionControlHit(isRotation = true)
    }

    val handle = GeometryService.hitTestResizeHandle(
        bounds = bounds,
        point = point.rotatedAround(bounds.centerPagePoint(), -element.rotation),
        handleSize = dpToPageUnits(ResizeHandleHitSizeDp, density, pageScale),
    )
    return handle?.let { SelectionControlHit(resizeHandle = it) }
}

internal fun dpToPageUnits(valueDp: Float, density: Float, pageScale: Float): Float {
    return valueDp * density / pageScale
}

internal fun isRotationHandleHit(
    bounds: PageRect,
    rotation: Float,
    point: PagePoint,
    hitSize: Float,
    handleOffset: Float,
): Boolean {
    val pivot = bounds.centerPagePoint()
    val handleCenter = PagePoint(bounds.centerX, bounds.y - handleOffset).rotatedAround(pivot, rotation)
    val dx = point.x - handleCenter.x
    val dy = point.y - handleCenter.y
    val radius = hitSize / 2f
    return dx * dx + dy * dy <= radius * radius
}

internal fun angleFromCenter(
    center: PagePoint,
    point: PagePoint,
): Float {
    return (atan2(point.y - center.y, point.x - center.x) * 180f / PI.toFloat())
}

internal fun normalizeEditorRotation(value: Float): Float {
    var normalized = value % 360f
    if (normalized > 180f) normalized -= 360f
    if (normalized < -180f) normalized += 360f
    return normalized
}

internal fun Float.formatForInput(): String {
    val rounded = kotlin.math.round(this * 10f) / 10f
    return if (rounded % 1f == 0f) rounded.toInt().toString() else rounded.toString()
}

internal fun parseColor(value: String): Color {
    val clean = value.trim().removePrefix("#")
    val argb = when (clean.length) {
        6 -> "FF$clean"
        8 -> clean
        else -> "FF111827"
    }
    return runCatching { Color(argb.toLong(16)) }.getOrElse { Color(0xFF111827) }
}

internal fun normalizePickerHex(value: String): String {
    return normalizeColorHex(value)
}

internal fun normalizeColorHex(value: String): String {
    val clean = value.trim().removePrefix("#").uppercase()
    return when (clean.length) {
        6 -> "#$clean"
        8 -> if (clean.startsWith("FF")) "#${clean.drop(2)}" else "#$clean"
        else -> "#111827"
    }
}

internal fun Color.withElementOpacity(opacity: Float): Color {
    return copy(alpha = alpha * opacity.coerceIn(0f, 1f))
}

internal fun DrawScope.drawStyledRoundRectBorder(
    color: Color,
    bounds: PageRect,
    radius: Float,
    width: Float,
    style: TemplateBorderStyle,
) {
    val cornerRadius = CornerRadius(radius, radius)
    fun drawBorder(strokeWidth: Float, inset: Float = 0f, pathEffect: PathEffect? = null) {
        drawRoundRect(
            color = color,
            topLeft = Offset(bounds.x + inset, bounds.y + inset),
            size = Size((bounds.width - inset * 2f).coerceAtLeast(0f), (bounds.height - inset * 2f).coerceAtLeast(0f)),
            cornerRadius = cornerRadius,
            style = Stroke(width = strokeWidth, pathEffect = pathEffect),
        )
    }
    when (style) {
        TemplateBorderStyle.Dotted -> drawBorder(width, pathEffect = PathEffect.dashPathEffect(floatArrayOf(width, width * 1.8f)))
        TemplateBorderStyle.Dashed -> drawBorder(width, pathEffect = PathEffect.dashPathEffect(floatArrayOf(width * 4f, width * 2f)))
        TemplateBorderStyle.Double -> {
            val stroke = (width / 3f).coerceAtLeast(1f)
            drawBorder(stroke)
            drawBorder(stroke, inset = width * 1.5f)
        }
        TemplateBorderStyle.Solid,
        TemplateBorderStyle.Groove,
        TemplateBorderStyle.Ridge,
        TemplateBorderStyle.Inset,
        TemplateBorderStyle.Outset -> drawBorder(width)
    }
}

internal fun DrawScope.drawStyledOvalBorder(
    color: Color,
    bounds: PageRect,
    width: Float,
    style: TemplateBorderStyle,
) {
    fun drawBorder(strokeWidth: Float, inset: Float = 0f, pathEffect: PathEffect? = null) {
        drawOval(
            color = color,
            topLeft = Offset(bounds.x + inset, bounds.y + inset),
            size = Size((bounds.width - inset * 2f).coerceAtLeast(0f), (bounds.height - inset * 2f).coerceAtLeast(0f)),
            style = Stroke(width = strokeWidth, pathEffect = pathEffect),
        )
    }
    when (style) {
        TemplateBorderStyle.Dotted -> drawBorder(width, pathEffect = PathEffect.dashPathEffect(floatArrayOf(width, width * 1.8f)))
        TemplateBorderStyle.Dashed -> drawBorder(width, pathEffect = PathEffect.dashPathEffect(floatArrayOf(width * 4f, width * 2f)))
        TemplateBorderStyle.Double -> {
            val stroke = (width / 3f).coerceAtLeast(1f)
            drawBorder(stroke)
            drawBorder(stroke, inset = width * 1.5f)
        }
        TemplateBorderStyle.Solid,
        TemplateBorderStyle.Groove,
        TemplateBorderStyle.Ridge,
        TemplateBorderStyle.Inset,
        TemplateBorderStyle.Outset -> drawBorder(width)
    }
}

internal fun DrawScope.drawQrMatrix(
    matrix: TemplateQrMatrix,
    bounds: PageRect,
    color: Color,
) {
    val moduleSize = min(bounds.width / matrix.width, bounds.height / matrix.height).coerceAtLeast(0.1f)
    val qrWidth = matrix.width * moduleSize
    val qrHeight = matrix.height * moduleSize
    val left = bounds.x + (bounds.width - qrWidth) / 2f
    val top = bounds.y + (bounds.height - qrHeight) / 2f
    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) {
            if (matrix.isDark(x, y)) {
                drawRect(
                    color = color,
                    topLeft = Offset(left + x * moduleSize, top + y * moduleSize),
                    size = Size(moduleSize + 0.02f, moduleSize + 0.02f),
                )
            }
        }
    }
}

internal fun DrawScope.drawBarcodeMatrix(
    matrix: TemplateBarcodeMatrix,
    bounds: PageRect,
    color: Color,
) {
    val moduleWidth = (bounds.width / matrix.width).coerceAtLeast(0.1f)
    val moduleHeight = (bounds.height / matrix.height).coerceAtLeast(0.1f)
    for (x in 0 until matrix.width) {
        var y = 0
        while (y < matrix.height) {
            if (!matrix.isDark(x, y)) {
                y += 1
                continue
            }
            val startY = y
            while (y < matrix.height && matrix.isDark(x, y)) {
                y += 1
            }
            drawRect(
                color = color,
                topLeft = Offset(bounds.x + x * moduleWidth, bounds.y + startY * moduleHeight),
                size = Size(moduleWidth + 0.02f, (y - startY) * moduleHeight + 0.02f),
            )
        }
    }
}

internal fun DrawScope.drawBarcodePlaceholder(bounds: PageRect, pageScale: Float) {
    val color = Color(0xFF94A3B8)
    val bars = 28
    val gap = (2f * pageScale).coerceAtLeast(1f)
    val barWidth = ((bounds.width - gap * (bars - 1)) / bars).coerceAtLeast(1f)
    for (index in 0 until bars) {
        if (index % 3 == 1) continue
        val heightInset = if (index % 5 == 0) 4f * pageScale else 10f * pageScale
        drawRect(
            color = color.copy(alpha = 0.34f),
            topLeft = Offset(bounds.x + index * (barWidth + gap), bounds.y + heightInset),
            size = Size(barWidth, (bounds.height - heightInset * 2f).coerceAtLeast(1f)),
        )
    }
}

internal fun DrawScope.drawListPlaceholder(bounds: PageRect, pageScale: Float) {
    val color = Color(0xFF94A3B8)
    val lineHeight = (12f * pageScale).coerceAtLeast(8f)
    val left = bounds.x + (10f * pageScale).coerceAtLeast(6f)
    var y = bounds.y + (10f * pageScale).coerceAtLeast(6f)
    repeat(4) { index ->
        val width = bounds.width * when (index) {
            1 -> 0.62f
            2 -> 0.72f
            else -> 0.52f
        }
        drawRoundRect(
            color = color.copy(alpha = 0.28f),
            topLeft = Offset(left, y),
            size = Size((width - left + bounds.x).coerceAtLeast(8f), 3.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        )
        y += lineHeight
    }
}

internal fun DrawScope.drawQrPlaceholder(bounds: PageRect, pageScale: Float) {
    val color = Color(0xFF94A3B8)
    val inset = (12f * pageScale).coerceAtLeast(8f)
    val cellSize = ((min(bounds.width, bounds.height) - inset * 2f) / 7f).coerceAtLeast(2f)
    val left = bounds.x + (bounds.width - cellSize * 7f) / 2f
    val top = bounds.y + (bounds.height - cellSize * 7f) / 2f
    for (y in 0 until 7) {
        for (x in 0 until 7) {
            val finder = (x in 0..1 || x in 5..6 || y in 0..1 || y in 5..6) || (x in 2..4 && y in 2..4)
            if (finder && (x + y) % 2 == 0) {
                drawRect(
                    color = color.copy(alpha = 0.35f),
                    topLeft = Offset(left + x * cellSize, top + y * cellSize),
                    size = Size(cellSize * 0.82f, cellSize * 0.82f),
                )
            }
        }
    }
}

internal fun String.toComposeFontFamily(): FontFamily {
    return when (lowercase()) {
        "serif" -> FontFamily.Serif
        "monospace" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        else -> FontFamily.SansSerif
    }
}

internal fun TemplateTextStyle.toComposeFontStyle(): FontStyle {
    return when (this) {
        TemplateTextStyle.Italic,
        TemplateTextStyle.Oblique -> FontStyle.Italic
        TemplateTextStyle.Normal -> FontStyle.Normal
    }
}

internal fun String.toComposeTextAlign(): TextAlign {
    return when (lowercase()) {
        "center" -> TextAlign.Center
        "right", "end" -> TextAlign.End
        "justify" -> TextAlign.Justify
        else -> TextAlign.Start
    }
}

internal fun TemplateTextDirection.toComposeTextDirection(): TextDirection {
    return when (this) {
        TemplateTextDirection.Rtl -> TextDirection.Rtl
        TemplateTextDirection.Ltr -> TextDirection.Ltr
    }
}



