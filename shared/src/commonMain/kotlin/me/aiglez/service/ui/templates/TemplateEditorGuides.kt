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

internal fun DrawScope.drawEditorGuides(
    guideSet: SnapGuideSet,
    canvas: CanvasState,
    zoom: Float,
    pageScale: Float,
    activeVerticalGuide: Float?,
    activeHorizontalGuide: Float?,
) {
    val pageWidth = guideSet.pageSize.width * pageScale
    val pageHeight = guideSet.pageSize.height * pageScale
    val styles = guideStyles()
    val showDenseGuides = zoom >= 0.72f
    val showLayoutDivisions = zoom >= 0.52f
    val showPrintZones = zoom >= 0.34f
    val showUserGuides = zoom >= 0.44f

    if (canvas.showGrid && showDenseGuides && guideSet.gridSize * pageScale >= 8f) {
        drawVerticalGuideLines(guidePositions(guideSet.pageSize.width, guideSet.gridSize), pageScale, pageHeight, styles.grid)
        drawHorizontalGuideLines(guidePositions(guideSet.pageSize.height, guideSet.gridSize), pageScale, pageWidth, styles.grid)
    }
    if (canvas.showGrid && showDenseGuides && guideSet.baselineGrid * pageScale >= 8f) {
        drawHorizontalGuideLines(guidePositions(guideSet.pageSize.height, guideSet.baselineGrid), pageScale, pageWidth, styles.baseline)
    }

    if (canvas.showGuides && showLayoutDivisions) {
        drawVerticalGuideLines(documentDivisions(guideSet.pageSize.width, guideSet.margin, guideSet.columns), pageScale, pageHeight, styles.column)
        drawHorizontalGuideLines(documentDivisions(guideSet.pageSize.height, guideSet.margin, guideSet.rows), pageScale, pageWidth, styles.row)
    }

    if (canvas.showGuides && showPrintZones) {
        drawVerticalGuideLines(edgePair(guideSet.printableInset, guideSet.pageSize.width), pageScale, pageHeight, styles.printable)
        drawHorizontalGuideLines(edgePair(guideSet.printableInset, guideSet.pageSize.height), pageScale, pageWidth, styles.printable)
        if (canvas.showBleed) {
            drawVerticalGuideLines(edgePair(guideSet.bleedInset, guideSet.pageSize.width), pageScale, pageHeight, styles.bleed)
            drawHorizontalGuideLines(edgePair(guideSet.bleedInset, guideSet.pageSize.height), pageScale, pageWidth, styles.bleed)
        }
        drawVerticalGuideLines(edgePair(guideSet.trimInset, guideSet.pageSize.width), pageScale, pageHeight, styles.trim)
        drawHorizontalGuideLines(edgePair(guideSet.trimInset, guideSet.pageSize.height), pageScale, pageWidth, styles.trim)
        if (canvas.showSafeArea) {
            drawVerticalGuideLines(edgePair(guideSet.safeAreaInset, guideSet.pageSize.width), pageScale, pageHeight, styles.safeArea)
            drawHorizontalGuideLines(edgePair(guideSet.safeAreaInset, guideSet.pageSize.height), pageScale, pageWidth, styles.safeArea)
        }
    }
    if (canvas.showMargins) {
        drawVerticalGuideLines(edgePair(guideSet.margin, guideSet.pageSize.width), pageScale, pageHeight, styles.margin)
        drawHorizontalGuideLines(edgePair(guideSet.margin, guideSet.pageSize.height), pageScale, pageWidth, styles.margin)
    }
    if (canvas.showGuides && showPrintZones) {
        drawHorizontalGuideLines(listOf(guideSet.headerGuide, guideSet.footerGuide), pageScale, pageWidth, styles.headerFooter)
    }

    drawVerticalGuideLines(listOf(0f, guideSet.pageSize.width), pageScale, pageHeight, styles.pageEdge)
    drawHorizontalGuideLines(listOf(0f, guideSet.pageSize.height), pageScale, pageWidth, styles.pageEdge)
    drawVerticalGuideLines(listOf(guideSet.pageSize.width / 2f), pageScale, pageHeight, styles.pageCenter)
    drawHorizontalGuideLines(listOf(guideSet.pageSize.height / 2f), pageScale, pageWidth, styles.pageCenter)

    if (canvas.showGuides && showUserGuides) {
        drawVerticalGuideLines(guideSet.rulerVerticalGuides, pageScale, pageHeight, styles.ruler)
        drawHorizontalGuideLines(guideSet.rulerHorizontalGuides, pageScale, pageWidth, styles.ruler)
        drawVerticalGuideLines(guideSet.customVerticalGuides, pageScale, pageHeight, styles.custom)
        drawHorizontalGuideLines(guideSet.customHorizontalGuides, pageScale, pageWidth, styles.custom)
    }

    drawActiveSnapGuides(
        guideSet = guideSet,
        pageScale = pageScale,
        activeVerticalGuide = activeVerticalGuide,
        activeHorizontalGuide = activeHorizontalGuide,
    )
}

internal fun DrawScope.drawActiveSnapGuides(
    guideSet: SnapGuideSet,
    pageScale: Float,
    activeVerticalGuide: Float?,
    activeHorizontalGuide: Float?,
    snapPulse: Float = 0f,
) {
    val pageWidth = guideSet.pageSize.width * pageScale
    val pageHeight = guideSet.pageSize.height * pageScale
    val styles = guideStyles()

    activeVerticalGuide?.let { value ->
        val x = value * pageScale
        drawLine(
            color = styles.active.color.copy(alpha = 0.86f + snapPulse * 0.14f),
            start = Offset(x, 0f),
            end = Offset(x, pageHeight),
            strokeWidth = styles.active.width.toPx() + snapPulse * 1.5.dp.toPx(),
            pathEffect = styles.active.pathEffect(),
        )
    }
    activeHorizontalGuide?.let { value ->
        val y = value * pageScale
        drawLine(
            color = styles.active.color.copy(alpha = 0.86f + snapPulse * 0.14f),
            start = Offset(0f, y),
            end = Offset(pageWidth, y),
            strokeWidth = styles.active.width.toPx() + snapPulse * 1.5.dp.toPx(),
            pathEffect = styles.active.pathEffect(),
        )
    }
}

internal fun buildSmartGuides(
    activeElement: TemplateElement,
    activeBounds: PageRect,
    otherElements: List<TemplateElement>,
    threshold: Float,
): List<SmartGuide> {
    if (threshold <= 0f) return emptyList()
    val guides = mutableListOf<SmartGuide>()
    val activeBaseline = textBaseline(activeElement, activeBounds)

    otherElements.filter { it.visible }.forEach { other ->
        val otherBounds = GeometryService.getElementBounds(other)
        val verticalSpan = spanAround(activeBounds.y, activeBounds.bottom, otherBounds.y, otherBounds.bottom)
        val horizontalSpan = spanAround(activeBounds.x, activeBounds.right, otherBounds.x, otherBounds.right)

        fun addVertical(activeValue: Float, otherValue: Float, kind: SmartGuideKind, label: String) {
            if (abs(activeValue - otherValue) <= threshold) {
                guides += SmartGuide(
                    orientation = SmartGuideOrientation.Vertical,
                    position = otherValue,
                    kind = kind,
                    spanStart = verticalSpan.first,
                    spanEnd = verticalSpan.second,
                    label = label,
                )
            }
        }

        fun addHorizontal(activeValue: Float, otherValue: Float, kind: SmartGuideKind, label: String) {
            if (abs(activeValue - otherValue) <= threshold) {
                guides += SmartGuide(
                    orientation = SmartGuideOrientation.Horizontal,
                    position = otherValue,
                    kind = kind,
                    spanStart = horizontalSpan.first,
                    spanEnd = horizontalSpan.second,
                    label = label,
                )
            }
        }

        addVertical(activeBounds.x, otherBounds.x, SmartGuideKind.LeftEdge, "left / X")
        addVertical(activeBounds.right, otherBounds.right, SmartGuideKind.RightEdge, "right")
        addVertical(activeBounds.centerX, otherBounds.centerX, SmartGuideKind.HorizontalCenter, "center X = x + w/2")
        addHorizontal(activeBounds.y, otherBounds.y, SmartGuideKind.TopEdge, "top / Y")
        addHorizontal(activeBounds.bottom, otherBounds.bottom, SmartGuideKind.BottomEdge, "bottom")
        addHorizontal(activeBounds.centerY, otherBounds.centerY, SmartGuideKind.VerticalCenter, "center Y = y + h/2")

        if (abs(activeBounds.width - otherBounds.width) <= threshold) {
            guides += SmartGuide(
                orientation = SmartGuideOrientation.Vertical,
                position = activeBounds.right,
                kind = SmartGuideKind.SameWidth,
                spanStart = verticalSpan.first,
                spanEnd = verticalSpan.second,
                label = "same width",
            )
        }
        if (abs(activeBounds.height - otherBounds.height) <= threshold) {
            guides += SmartGuide(
                orientation = SmartGuideOrientation.Horizontal,
                position = activeBounds.bottom,
                kind = SmartGuideKind.SameHeight,
                spanStart = horizontalSpan.first,
                spanEnd = horizontalSpan.second,
                label = "same height",
            )
        }

        val otherBaseline = textBaseline(other, otherBounds)
        if (activeBaseline != null && otherBaseline != null) {
            addHorizontal(activeBaseline, otherBaseline, SmartGuideKind.Baseline, "baseline")
        }
    }

    guides += equalSpacingGuides(activeBounds, otherElements.map { GeometryService.getElementBounds(it) }, threshold)
    return guides.distinctBy { "${it.orientation}-${it.kind}-${it.position.roundToInt()}-${it.label}" }
}

internal fun equalSpacingGuides(activeBounds: PageRect, otherBounds: List<PageRect>, threshold: Float): List<SmartGuide> {
    val guides = mutableListOf<SmartGuide>()

    val horizontalNeighbors = otherBounds
        .filter { rangesOverlap(activeBounds.y, activeBounds.bottom, it.y, it.bottom) }
    val left = horizontalNeighbors.filter { it.right <= activeBounds.x }.maxByOrNull { it.right }
    val right = horizontalNeighbors.filter { it.x >= activeBounds.right }.minByOrNull { it.x }
    if (left != null && right != null) {
        val leftGap = activeBounds.x - left.right
        val rightGap = right.x - activeBounds.right
        if (leftGap >= 0f && rightGap >= 0f && abs(leftGap - rightGap) <= threshold) {
            guides += SmartGuide(
                orientation = SmartGuideOrientation.Vertical,
                position = activeBounds.centerX,
                kind = SmartGuideKind.EqualSpacing,
                spanStart = min(left.y, min(activeBounds.y, right.y)),
                spanEnd = max(left.bottom, max(activeBounds.bottom, right.bottom)),
                label = "equal spacing ${formatCompact((leftGap + rightGap) / 2f)}",
            )
        }
    }

    val verticalNeighbors = otherBounds
        .filter { rangesOverlap(activeBounds.x, activeBounds.right, it.x, it.right) }
    val top = verticalNeighbors.filter { it.bottom <= activeBounds.y }.maxByOrNull { it.bottom }
    val bottom = verticalNeighbors.filter { it.y >= activeBounds.bottom }.minByOrNull { it.y }
    if (top != null && bottom != null) {
        val topGap = activeBounds.y - top.bottom
        val bottomGap = bottom.y - activeBounds.bottom
        if (topGap >= 0f && bottomGap >= 0f && abs(topGap - bottomGap) <= threshold) {
            guides += SmartGuide(
                orientation = SmartGuideOrientation.Horizontal,
                position = activeBounds.centerY,
                kind = SmartGuideKind.EqualSpacing,
                spanStart = min(top.x, min(activeBounds.x, bottom.x)),
                spanEnd = max(top.right, max(activeBounds.right, bottom.right)),
                label = "equal spacing ${formatCompact((topGap + bottomGap) / 2f)}",
            )
        }
    }

    return guides
}

internal fun buildDistanceLabels(
    activeBounds: PageRect,
    otherElements: List<TemplateElement>,
): List<DistanceLabel> {
    val otherBounds = otherElements.filter { it.visible }.map { GeometryService.getElementBounds(it) }
    val labels = mutableListOf<DistanceLabel>()

    val horizontalNeighbors = otherBounds
        .filter { rangesOverlap(activeBounds.y, activeBounds.bottom, it.y, it.bottom) }
    val left = horizontalNeighbors.filter { it.right <= activeBounds.x }.maxByOrNull { it.right }
    val right = horizontalNeighbors.filter { it.x >= activeBounds.right }.minByOrNull { it.x }
    if (left != null) {
        val y = overlapMidpoint(activeBounds.y, activeBounds.bottom, left.y, left.bottom)
        val distance = activeBounds.x - left.right
        labels += DistanceLabel(PagePoint(left.right, y), PagePoint(activeBounds.x, y), formatCompact(distance), horizontal = true)
    }
    if (right != null) {
        val y = overlapMidpoint(activeBounds.y, activeBounds.bottom, right.y, right.bottom)
        val distance = right.x - activeBounds.right
        labels += DistanceLabel(PagePoint(activeBounds.right, y), PagePoint(right.x, y), formatCompact(distance), horizontal = true)
    }

    val verticalNeighbors = otherBounds
        .filter { rangesOverlap(activeBounds.x, activeBounds.right, it.x, it.right) }
    val top = verticalNeighbors.filter { it.bottom <= activeBounds.y }.maxByOrNull { it.bottom }
    val bottom = verticalNeighbors.filter { it.y >= activeBounds.bottom }.minByOrNull { it.y }
    if (top != null) {
        val x = overlapMidpoint(activeBounds.x, activeBounds.right, top.x, top.right)
        val distance = activeBounds.y - top.bottom
        labels += DistanceLabel(PagePoint(x, top.bottom), PagePoint(x, activeBounds.y), formatCompact(distance), horizontal = false)
    }
    if (bottom != null) {
        val x = overlapMidpoint(activeBounds.x, activeBounds.right, bottom.x, bottom.right)
        val distance = bottom.y - activeBounds.bottom
        labels += DistanceLabel(PagePoint(x, activeBounds.bottom), PagePoint(x, bottom.y), formatCompact(distance), horizontal = false)
    }

    return labels.filter { it.label != "0" }
}

internal fun buildSnapTargetElementIds(
    activeBounds: PageRect,
    otherElements: List<TemplateElement>,
    threshold: Float,
): Set<String> {
    if (threshold <= 0f) return emptySet()
    return otherElements
        .filter { it.visible }
        .filter { other ->
            val otherBounds = GeometryService.getElementBounds(other)
            listOf(
                abs(activeBounds.x - otherBounds.x),
                abs(activeBounds.right - otherBounds.right),
                abs(activeBounds.centerX - otherBounds.centerX),
                abs(activeBounds.y - otherBounds.y),
                abs(activeBounds.bottom - otherBounds.bottom),
                abs(activeBounds.centerY - otherBounds.centerY),
                abs(activeBounds.width - otherBounds.width),
                abs(activeBounds.height - otherBounds.height),
            ).any { it <= threshold }
        }
        .map { it.id }
        .toSet()
}

internal fun effectiveSnapThreshold(
    baseThreshold: Float,
    zoom: Float,
): Float {
    if (baseThreshold <= 0f) return 0f
    val precisionScale = zoom.coerceAtLeast(1f)
    return (baseThreshold / precisionScale).coerceAtLeast(0.75f)
}
