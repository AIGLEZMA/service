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

internal fun DrawScope.drawSmartGuideOverlay(
    smartGuides: List<SmartGuide>,
    distanceLabels: List<DistanceLabel>,
    activeVerticalGuide: Float?,
    activeHorizontalGuide: Float?,
    snapPulse: Float,
    pageScale: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    smartGuides.forEach { guide ->
        val style = smartGuideStyle(guide.kind)
        when (guide.orientation) {
            SmartGuideOrientation.Vertical -> {
                val x = guide.position * pageScale
                val y1 = guide.spanStart * pageScale
                val y2 = guide.spanEnd * pageScale
                drawLine(
                    color = style.color,
                    start = Offset(x, y1),
                    end = Offset(x, y2),
                    strokeWidth = style.width.toPx(),
                    pathEffect = style.pathEffect(),
                )
                drawGuideLabel(guide.label, Offset(x + 6.dp.toPx(), (y1 + y2) / 2f), textMeasurer, style.color)
            }
            SmartGuideOrientation.Horizontal -> {
                val y = guide.position * pageScale
                val x1 = guide.spanStart * pageScale
                val x2 = guide.spanEnd * pageScale
                drawLine(
                    color = style.color,
                    start = Offset(x1, y),
                    end = Offset(x2, y),
                    strokeWidth = style.width.toPx(),
                    pathEffect = style.pathEffect(),
                )
                drawGuideLabel(guide.label, Offset((x1 + x2) / 2f + 6.dp.toPx(), y + 6.dp.toPx()), textMeasurer, style.color)
            }
        }
    }

    distanceLabels.forEach { label ->
        val color = Color(0xFF111827)
        val start = Offset(label.start.x * pageScale, label.start.y * pageScale)
        val end = Offset(label.end.x * pageScale, label.end.y * pageScale)
        drawLine(color, start, end, strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
        if (label.horizontal) {
            drawLine(color, Offset(start.x, start.y - 5.dp.toPx()), Offset(start.x, start.y + 5.dp.toPx()), strokeWidth = 1.dp.toPx())
            drawLine(color, Offset(end.x, end.y - 5.dp.toPx()), Offset(end.x, end.y + 5.dp.toPx()), strokeWidth = 1.dp.toPx())
        } else {
            drawLine(color, Offset(start.x - 5.dp.toPx(), start.y), Offset(start.x + 5.dp.toPx(), start.y), strokeWidth = 1.dp.toPx())
            drawLine(color, Offset(end.x - 5.dp.toPx(), end.y), Offset(end.x + 5.dp.toPx(), end.y), strokeWidth = 1.dp.toPx())
        }
        drawGuideLabel("${label.label}px", Offset((start.x + end.x) / 2f + 5.dp.toPx(), (start.y + end.y) / 2f + 5.dp.toPx()), textMeasurer, color)
    }

    drawSnapIcon(
        activeVerticalGuide = activeVerticalGuide,
        activeHorizontalGuide = activeHorizontalGuide,
        snapPulse = snapPulse,
        pageScale = pageScale,
        textMeasurer = textMeasurer,
    )
}

internal fun DrawScope.drawPreSnapBounds(bounds: PageRect) {
    val color = Color(0xFFDC2626)
    drawRect(
        color = color.copy(alpha = 0.08f),
        topLeft = Offset(bounds.x, bounds.y),
        size = Size(bounds.width, bounds.height),
    )
    drawRect(
        color = color.copy(alpha = 0.72f),
        topLeft = Offset(bounds.x, bounds.y),
        size = Size(bounds.width, bounds.height),
        style = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f),
        ),
    )
}

internal fun DrawScope.drawTargetHighlight(
    bounds: PageRect,
    pageScale: Float,
    snapPulse: Float,
) {
    val inset = 3.dp.toPx() + snapPulse * 2.dp.toPx()
    val color = Color(0xFF2563EB)
    drawRoundRect(
        color = color.copy(alpha = 0.10f + snapPulse * 0.06f),
        topLeft = Offset(bounds.x - inset, bounds.y - inset),
        size = Size(bounds.width + inset * 2f, bounds.height + inset * 2f),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
    )
    drawRoundRect(
        color = color.copy(alpha = 0.82f),
        topLeft = Offset(bounds.x - inset, bounds.y - inset),
        size = Size(bounds.width + inset * 2f, bounds.height + inset * 2f),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        style = Stroke(width = (1.25f + snapPulse) * pageScale.coerceAtLeast(0.5f)),
    )
}

internal fun DrawScope.drawSnapIcon(
    activeVerticalGuide: Float?,
    activeHorizontalGuide: Float?,
    snapPulse: Float,
    pageScale: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    if (activeVerticalGuide == null && activeHorizontalGuide == null) return
    val x = activeVerticalGuide?.times(pageScale)?.coerceIn(12.dp.toPx(), size.width - 42.dp.toPx()) ?: 18.dp.toPx()
    val y = activeHorizontalGuide?.times(pageScale)?.coerceIn(12.dp.toPx(), size.height - 42.dp.toPx()) ?: 18.dp.toPx()
    val iconSize = 24.dp.toPx() + snapPulse * 3.dp.toPx()
    val topLeft = Offset(x + 8.dp.toPx(), y + 8.dp.toPx())
    val color = guideStyles().active.color

    drawRoundRect(
        color = Color.White.copy(alpha = 0.94f),
        topLeft = topLeft,
        size = Size(iconSize, iconSize),
        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
    )
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = Size(iconSize, iconSize),
        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
        style = Stroke(width = 1.25.dp.toPx()),
    )
    val cx = topLeft.x + iconSize / 2f
    val cy = topLeft.y + iconSize / 2f
    drawLine(color, Offset(cx, topLeft.y + 5.dp.toPx()), Offset(cx, topLeft.y + iconSize - 5.dp.toPx()), strokeWidth = 1.5.dp.toPx())
    drawLine(color, Offset(topLeft.x + 5.dp.toPx(), cy), Offset(topLeft.x + iconSize - 5.dp.toPx(), cy), strokeWidth = 1.5.dp.toPx())
    drawCircle(color.copy(alpha = 0.18f), radius = 4.dp.toPx() + snapPulse * 2.dp.toPx(), center = Offset(cx, cy))

    if (snapPulse > 0.35f) {
        drawGuideLabel("Snap", Offset(topLeft.x + iconSize + 6.dp.toPx(), cy), textMeasurer, color)
    }
}

internal fun smartGuideStyle(kind: SmartGuideKind): GuideLineStyle {
    return when (kind) {
        SmartGuideKind.LeftEdge,
        SmartGuideKind.RightEdge,
        SmartGuideKind.TopEdge,
        SmartGuideKind.BottomEdge,
        SmartGuideKind.SameX,
        SmartGuideKind.SameY -> GuideLineStyle(Color(0xFF2563EB), 1.25.dp)
        SmartGuideKind.HorizontalCenter,
        SmartGuideKind.VerticalCenter -> GuideLineStyle(Color(0xFF0F766E), 1.25.dp, floatArrayOf(10f, 5f))
        SmartGuideKind.Baseline -> GuideLineStyle(Color(0xFF9333EA), 1.25.dp, floatArrayOf(2f, 4f))
        SmartGuideKind.EqualSpacing -> GuideLineStyle(Color(0xFFF59E0B), 1.5.dp, floatArrayOf(8f, 4f))
        SmartGuideKind.SameWidth,
        SmartGuideKind.SameHeight -> GuideLineStyle(Color(0xFFDB2777), 1.25.dp, floatArrayOf(4f, 3f))
    }
}

internal fun DrawScope.drawGuideLabel(
    text: String,
    anchor: Offset,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    color: Color,
) {
    val layout = textMeasurer.measure(
        text = text,
        style = androidx.compose.ui.text.TextStyle(
            color = Color.White,
            fontSize = 10.sp,
        ),
    )
    val paddingX = 5.dp.toPx()
    val paddingY = 3.dp.toPx()
    drawRoundRect(
        color = color.copy(alpha = 0.92f),
        topLeft = Offset(anchor.x, anchor.y - layout.size.height / 2f - paddingY),
        size = Size(layout.size.width + paddingX * 2f, layout.size.height + paddingY * 2f),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
    )
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(anchor.x + paddingX, anchor.y - layout.size.height / 2f),
    )
}

internal fun textBaseline(element: TemplateElement, bounds: PageRect): Float? {
    return when (element) {
        is TemplateElement.Text -> bounds.y + element.padding + element.fontSize
        else -> null
    }
}

internal fun spanAround(aStart: Float, aEnd: Float, bStart: Float, bEnd: Float): Pair<Float, Float> {
    return min(aStart, bStart) to max(aEnd, bEnd)
}

internal fun rangesOverlap(aStart: Float, aEnd: Float, bStart: Float, bEnd: Float): Boolean {
    return max(aStart, bStart) <= min(aEnd, bEnd)
}

internal fun overlapMidpoint(aStart: Float, aEnd: Float, bStart: Float, bEnd: Float): Float {
    val start = max(aStart, bStart)
    val end = min(aEnd, bEnd)
    return if (start <= end) (start + end) / 2f else (aStart + aEnd) / 2f
}

internal data class GuideStyles(
    val pageEdge: GuideLineStyle,
    val pageCenter: GuideLineStyle,
    val margin: GuideLineStyle,
    val printable: GuideLineStyle,
    val bleed: GuideLineStyle,
    val trim: GuideLineStyle,
    val safeArea: GuideLineStyle,
    val headerFooter: GuideLineStyle,
    val grid: GuideLineStyle,
    val column: GuideLineStyle,
    val row: GuideLineStyle,
    val baseline: GuideLineStyle,
    val ruler: GuideLineStyle,
    val custom: GuideLineStyle,
    val active: GuideLineStyle,
)

internal data class GuideLineStyle(
    val color: Color,
    val width: androidx.compose.ui.unit.Dp,
    val dash: FloatArray? = null,
) {
    fun pathEffect(): PathEffect? {
        return dash?.let { PathEffect.dashPathEffect(it, 0f) }
    }
}

internal fun guideStyles(): GuideStyles {
    return GuideStyles(
        pageEdge = GuideLineStyle(Color(0xFF475569), 1.dp),
        pageCenter = GuideLineStyle(Color(0xFF0F766E).copy(alpha = 0.72f), 1.dp, floatArrayOf(12f, 7f)),
        margin = GuideLineStyle(Color(0xFF7C3AED).copy(alpha = 0.78f), 1.dp),
        printable = GuideLineStyle(Color(0xFF0EA5E9).copy(alpha = 0.66f), 0.9.dp, floatArrayOf(12f, 4f)),
        bleed = GuideLineStyle(Color(0xFFEA580C).copy(alpha = 0.78f), 1.dp, floatArrayOf(8f, 5f)),
        trim = GuideLineStyle(Color(0xFF111827).copy(alpha = 0.62f), 1.15.dp, floatArrayOf(16f, 4f)),
        safeArea = GuideLineStyle(Color(0xFF16A34A).copy(alpha = 0.72f), 1.dp, floatArrayOf(3f, 4f)),
        headerFooter = GuideLineStyle(Color(0xFF64748B).copy(alpha = 0.70f), 1.dp, floatArrayOf(7f, 4f)),
        grid = GuideLineStyle(Color(0xFFE2E8F0).copy(alpha = 0.74f), 0.5.dp),
        column = GuideLineStyle(Color(0xFF0891B2).copy(alpha = 0.62f), 0.75.dp, floatArrayOf(10f, 6f)),
        row = GuideLineStyle(Color(0xFF4F46E5).copy(alpha = 0.56f), 0.75.dp, floatArrayOf(10f, 6f)),
        baseline = GuideLineStyle(Color(0xFF9333EA).copy(alpha = 0.42f), 0.65.dp, floatArrayOf(2f, 5f)),
        ruler = GuideLineStyle(Color(0xFF0284C7).copy(alpha = 0.82f), 1.dp, floatArrayOf(14f, 5f, 3f, 5f)),
        custom = GuideLineStyle(Color(0xFFDB2777).copy(alpha = 0.86f), 1.25.dp, floatArrayOf(5f, 3f)),
        active = GuideLineStyle(Color(0xFFDC2626), 1.5.dp),
    )
}

internal fun DrawScope.drawVerticalGuideLines(
    values: List<Float>,
    pageScale: Float,
    pageHeight: Float,
    style: GuideLineStyle,
) {
    values.distinctNear().forEach { value ->
        val x = value * pageScale
        drawLine(
            color = style.color,
            start = Offset(x, 0f),
            end = Offset(x, pageHeight),
            strokeWidth = style.width.toPx(),
            pathEffect = style.pathEffect(),
        )
    }
}

internal fun DrawScope.drawHorizontalGuideLines(
    values: List<Float>,
    pageScale: Float,
    pageWidth: Float,
    style: GuideLineStyle,
) {
    values.distinctNear().forEach { value ->
        val y = value * pageScale
        drawLine(
            color = style.color,
            start = Offset(0f, y),
            end = Offset(pageWidth, y),
            strokeWidth = style.width.toPx(),
            pathEffect = style.pathEffect(),
        )
    }
}

internal fun guidePositions(length: Float, step: Float): List<Float> {
    if (step <= 0f) return emptyList()
    val positions = mutableListOf<Float>()
    var value = 0f
    while (value <= length) {
        positions += value
        value += step
    }
    return positions
}

internal fun edgePair(inset: Float, length: Float): List<Float> {
    return if (inset <= 0f) emptyList() else listOf(inset, length - inset)
}

internal fun documentDivisions(length: Float, margin: Float, count: Int): List<Float> {
    if (count <= 1) return emptyList()
    val start = margin.coerceIn(0f, length / 2f)
    val end = (length - margin).coerceAtLeast(start)
    val step = (end - start) / count
    return buildList {
        for (index in 1 until count) {
            add(start + step * index)
        }
    }
}

internal fun List<Float>.distinctNear(): List<Float> {
    return filter { it.isFinite() }
        .sorted()
        .fold(emptyList()) { acc, value ->
            if (acc.lastOrNull()?.let { kotlin.math.abs(it - value) < 0.01f } == true) acc else acc + value
        }
}

internal fun CanvasState.toSnapGuideSet(): SnapGuideSet {
    return SnapGuideSet(
        pageSize = PageSize(PageWidth, PageHeight),
        margin = if (snapToMargins) pageMargin else 0f,
        printableInset = printableInset,
        bleedInset = if (snapToMargins) bleedInset else 0f,
        trimInset = trimInset,
        safeAreaInset = safeAreaInset,
        headerGuide = headerGuide,
        footerGuide = footerGuide,
        gridSize = if (snapToGrid) gridSize else 0f,
        columns = documentColumns,
        rows = documentRows,
        baselineGrid = if (snapToGrid) baselineGrid else 0f,
        customVerticalGuides = if (snapToGuides) customVerticalGuides else emptyList(),
        customHorizontalGuides = if (snapToGuides) customHorizontalGuides else emptyList(),
        rulerVerticalGuides = if (snapToGuides) rulerVerticalGuides else emptyList(),
        rulerHorizontalGuides = if (snapToGuides) rulerHorizontalGuides else emptyList(),
        includePageCenter = snapToPageCenter,
    )
}
