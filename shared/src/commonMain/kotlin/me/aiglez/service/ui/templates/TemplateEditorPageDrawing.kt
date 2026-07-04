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
internal fun PageBaseCanvas(
    guideSet: SnapGuideSet,
    canvas: CanvasState,
    isPreviewMode: Boolean,
    zoom: Float,
    pageScale: Float,
    renderItems: List<PageRenderItem>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawRect(Color.White)
        if (!isPreviewMode) {
            drawEditorGuides(
                guideSet = guideSet,
                canvas = canvas,
                zoom = zoom,
                pageScale = pageScale,
                activeVerticalGuide = null,
                activeHorizontalGuide = null,
            )
        }
        renderItems.forEach { item ->
            when (val element = item.element) {
                is TemplateElement.Text -> {
                    val textItem = item as TextRenderItem
                    val bounds = textItem.bounds
                    withTransform({
                        rotate(degrees = element.rotation, pivot = bounds.center())
                    }) {
                        val cornerRadius = CornerRadius(element.borderRadius * pageScale, element.borderRadius * pageScale)
                        drawRoundRect(
                            color = textItem.background,
                            topLeft = Offset(bounds.x, bounds.y),
                            size = Size(bounds.width, bounds.height),
                            cornerRadius = cornerRadius,
                        )
                        if (element.borderWidth > 0f) {
                            drawStyledRoundRectBorder(
                                color = textItem.borderColor,
                                bounds = bounds,
                                radius = element.borderRadius * pageScale,
                                width = element.borderWidth * pageScale,
                                style = element.borderStyle,
                            )
                        }
                        clipRect(
                            left = bounds.x,
                            top = bounds.y,
                            right = bounds.right,
                            bottom = bounds.bottom,
                        ) {
                            drawText(
                                textLayoutResult = textItem.textLayout,
                                topLeft = textItem.textTopLeft,
                            )
                        }
                    }
                }
                is TemplateElement.Rectangle -> {
                    val rectangleItem = item as RectangleRenderItem
                    val bounds = rectangleItem.bounds
                    withTransform({
                        rotate(degrees = element.rotation, pivot = bounds.center())
                    }) {
                        val cornerRadius = CornerRadius(element.borderRadius * pageScale, element.borderRadius * pageScale)
                        drawRoundRect(
                            color = rectangleItem.fillColor,
                            topLeft = Offset(bounds.x, bounds.y),
                            size = Size(bounds.width, bounds.height),
                            cornerRadius = cornerRadius,
                        )
                        if (element.borderWidth > 0f) {
                            drawStyledRoundRectBorder(
                                color = rectangleItem.borderColor,
                                bounds = bounds,
                                radius = element.borderRadius * pageScale,
                                width = element.borderWidth * pageScale,
                                style = element.borderStyle,
                            )
                        }
                    }
                }
                is TemplateElement.Image -> {
                    val imageItem = item as ImageRenderItem
                    val bounds = imageItem.bounds
                    withTransform({
                        rotate(degrees = element.rotation, pivot = bounds.center())
                    }) {
                        val cornerRadius = CornerRadius(element.borderRadius * pageScale, element.borderRadius * pageScale)
                        drawRoundRect(
                            color = imageItem.background,
                            topLeft = Offset(bounds.x, bounds.y),
                            size = Size(bounds.width, bounds.height),
                            cornerRadius = cornerRadius,
                        )
                        val bitmap = imageItem.bitmap
                        if (bitmap == null) {
                            drawImagePlaceholder(bounds, pageScale)
                        } else {
                            val destination = imageDestinationRect(
                                frame = bounds,
                                imageWidth = imageItem.intrinsicWidth,
                                imageHeight = imageItem.intrinsicHeight,
                                mode = element.contentMode,
                                alignment = element.alignment,
                            )
                            clipRect(
                                left = bounds.x,
                                top = bounds.y,
                                right = bounds.right,
                                bottom = bounds.bottom,
                            ) {
                                drawImage(
                                    image = bitmap,
                                    dstOffset = IntOffset(destination.x.roundToInt(), destination.y.roundToInt()),
                                    dstSize = IntSize(
                                        destination.width.roundToInt().coerceAtLeast(1),
                                        destination.height.roundToInt().coerceAtLeast(1),
                                    ),
                                    alpha = element.opacity,
                                )
                            }
                        }
                        if (element.borderWidth > 0f) {
                            drawStyledRoundRectBorder(
                                color = imageItem.borderColor,
                                bounds = bounds,
                                radius = element.borderRadius * pageScale,
                                width = element.borderWidth * pageScale,
                                style = TemplateBorderStyle.Solid,
                            )
                        }
                    }
                }
                is TemplateElement.Circle -> {
                    val circleItem = item as CircleRenderItem
                    val bounds = circleItem.bounds
                    withTransform({
                        rotate(degrees = element.rotation, pivot = bounds.center())
                    }) {
                        drawOval(
                            color = circleItem.fillColor,
                            topLeft = Offset(bounds.x, bounds.y),
                            size = Size(bounds.width, bounds.height),
                        )
                        if (element.borderWidth > 0f) {
                            drawStyledOvalBorder(
                                color = circleItem.borderColor,
                                bounds = bounds,
                                width = element.borderWidth * pageScale,
                                style = element.borderStyle,
                            )
                        }
                    }
                }
                is TemplateElement.QRCode -> {
                    val qrItem = item as QRCodeRenderItem
                    val bounds = qrItem.bounds
                    withTransform({
                        rotate(degrees = element.rotation, pivot = bounds.center())
                    }) {
                        drawRect(
                            color = qrItem.backgroundColor,
                            topLeft = Offset(bounds.x, bounds.y),
                            size = Size(bounds.width, bounds.height),
                        )
                        val matrix = qrItem.matrix
                        if (matrix == null) {
                            drawQrPlaceholder(bounds, pageScale)
                        } else {
                            drawQrMatrix(
                                matrix = matrix,
                                bounds = bounds,
                                color = qrItem.foregroundColor,
                            )
                        }
                        if (element.borderWidth > 0f) {
                            drawStyledRoundRectBorder(
                                color = qrItem.borderColor,
                                bounds = bounds,
                                radius = 0f,
                                width = element.borderWidth * pageScale,
                                style = TemplateBorderStyle.Solid,
                            )
                        }
                    }
                }
                is TemplateElement.Barcode -> {
                    val barcodeItem = item as BarcodeRenderItem
                    val bounds = barcodeItem.bounds
                    withTransform({
                        rotate(degrees = element.rotation, pivot = bounds.center())
                    }) {
                        drawRect(
                            color = barcodeItem.backgroundColor,
                            topLeft = Offset(bounds.x, bounds.y),
                            size = Size(bounds.width, bounds.height),
                        )
                        val labelHeight = barcodeItem.textLayout?.size?.height?.toFloat()?.plus(4.dp.toPx()) ?: 0f
                        val barcodeBounds = bounds.copy(height = (bounds.height - labelHeight).coerceAtLeast(1f))
                        val matrix = barcodeItem.matrix
                        if (matrix == null) {
                            drawBarcodePlaceholder(barcodeBounds, pageScale)
                        } else {
                            drawBarcodeMatrix(
                                matrix = matrix,
                                bounds = barcodeBounds,
                                color = barcodeItem.foregroundColor,
                            )
                        }
                        barcodeItem.textLayout?.let { layout ->
                            drawText(
                                textLayoutResult = layout,
                                topLeft = Offset(
                                    bounds.x + (bounds.width - layout.size.width) / 2f,
                                    barcodeBounds.bottom + 2.dp.toPx(),
                                ),
                            )
                        }
                        if (element.borderWidth > 0f) {
                            drawStyledRoundRectBorder(
                                color = barcodeItem.borderColor,
                                bounds = bounds,
                                radius = 0f,
                                width = element.borderWidth * pageScale,
                                style = TemplateBorderStyle.Solid,
                            )
                        }
                    }
                }
                is TemplateElement.List -> {
                    val listItem = item as ListRenderItem
                    val bounds = listItem.bounds
                    withTransform({
                        rotate(degrees = element.rotation, pivot = bounds.center())
                    }) {
                        val cornerRadius = CornerRadius(element.borderRadius * pageScale, element.borderRadius * pageScale)
                        drawRoundRect(
                            color = listItem.background,
                            topLeft = Offset(bounds.x, bounds.y),
                            size = Size(bounds.width, bounds.height),
                            cornerRadius = cornerRadius,
                        )
                        if (element.borderWidth > 0f) {
                            drawStyledRoundRectBorder(
                                color = listItem.borderColor,
                                bounds = bounds,
                                radius = element.borderRadius * pageScale,
                                width = element.borderWidth * pageScale,
                                style = element.borderStyle,
                            )
                        }
                        clipRect(
                            left = bounds.x,
                            top = bounds.y,
                            right = bounds.right,
                            bottom = bounds.bottom,
                        ) {
                            if (listItem.itemLayouts.isEmpty()) {
                                drawListPlaceholder(bounds, pageScale)
                            } else {
                                listItem.itemLayouts.forEach { itemLayout ->
                                    drawText(
                                        textLayoutResult = itemLayout.layout,
                                        topLeft = itemLayout.topLeft,
                                    )
                                }
                            }
                        }
                    }
                }
                is TemplateElement.Line -> {
                    val lineItem = item as LineRenderItem
                    val bounds = lineItem.bounds
                    withTransform({
                        rotate(degrees = element.rotation, pivot = bounds.center())
                    }) {
                        drawLine(
                            color = lineItem.color,
                            start = lineItem.start,
                            end = lineItem.end,
                            strokeWidth = lineItem.strokeWidth,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PageOverlayCanvas(
    selectedElements: List<TemplateElement>,
    primarySelectedElementId: String?,
    selectionRect: PageRect?,
    activeVerticalGuide: Float?,
    activeHorizontalGuide: Float?,
    activeSmartGuides: List<SmartGuide>,
    activeDistanceLabels: List<DistanceLabel>,
    activeMeasurementLabel: DistanceLabel?,
    activePreSnapBounds: PageRect?,
    activeTargetElements: List<TemplateElement>,
    snapPulse: Float,
    guideSet: SnapGuideSet,
    pageScale: Float,
    textMeasurer: TextMeasurer,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawActiveSnapGuides(
            guideSet = guideSet,
            pageScale = pageScale,
            activeVerticalGuide = activeVerticalGuide,
            activeHorizontalGuide = activeHorizontalGuide,
            snapPulse = snapPulse,
        )
        activePreSnapBounds?.let { bounds ->
            drawPreSnapBounds(bounds.scaled(pageScale))
        }
        activeTargetElements.forEach { target ->
            val bounds = GeometryService.getElementBounds(target).scaled(pageScale)
            drawTargetHighlight(bounds, pageScale, snapPulse)
        }
        selectedElements.forEach { selected ->
            val bounds = GeometryService.getElementBounds(selected).scaled(pageScale)
            val selectionColor = if (selected.locked) Color(0xFFDC2626) else Color(0xFF2563EB)
            withTransform({
                rotate(degrees = selected.rotation, pivot = bounds.center())
            }) {
                drawRect(
                    color = selectionColor,
                    topLeft = Offset(bounds.x, bounds.y),
                    size = Size(bounds.width, bounds.height),
                    style = Stroke(width = 2.dp.toPx()),
                )
                if (selected.id == primarySelectedElementId) {
                    val rotationCenter = Offset(bounds.x + bounds.width / 2f, bounds.y - RotationHandleOffsetDp.dp.toPx())
                    val topCenter = Offset(bounds.x + bounds.width / 2f, bounds.y)
                    drawLine(
                        color = selectionColor,
                        start = topCenter,
                        end = rotationCenter,
                        strokeWidth = 1.dp.toPx(),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = RotationHandleRadiusDp.dp.toPx(),
                        center = rotationCenter,
                    )
                    drawCircle(
                        color = selectionColor,
                        radius = RotationHandleRadiusDp.dp.toPx(),
                        center = rotationCenter,
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                    GeometryService.handleCenters(GeometryService.getElementBounds(selected)).forEach { (_, center) ->
                        val handleSize = ResizeHandleVisualSizeDp.dp.toPx()
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(center.x * pageScale - handleSize / 2f, center.y * pageScale - handleSize / 2f),
                            size = Size(handleSize, handleSize),
                        )
                        drawRect(
                            color = selectionColor,
                            topLeft = Offset(center.x * pageScale - handleSize / 2f, center.y * pageScale - handleSize / 2f),
                            size = Size(handleSize, handleSize),
                            style = Stroke(width = 1.dp.toPx()),
                        )
	                    }
                }
            }
        }
        selectionRect?.let { rect ->
            val scaled = rect.scaled(pageScale)
            drawRect(
                color = Color(0xFF2563EB).copy(alpha = 0.10f),
                topLeft = Offset(scaled.x, scaled.y),
                size = Size(scaled.width, scaled.height),
            )
            drawRect(
                color = Color(0xFF2563EB),
                topLeft = Offset(scaled.x, scaled.y),
                size = Size(scaled.width, scaled.height),
                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)),
            )
        }
        drawSmartGuideOverlay(
            smartGuides = activeSmartGuides,
            distanceLabels = activeMeasurementLabel?.let { activeDistanceLabels + it } ?: activeDistanceLabels,
            activeVerticalGuide = activeVerticalGuide,
            activeHorizontalGuide = activeHorizontalGuide,
            snapPulse = snapPulse,
            pageScale = pageScale,
            textMeasurer = textMeasurer,
        )
    }
}

@Composable
internal fun HorizontalRuler(
    width: Float,
    zoom: Float,
    pageScale: Float,
    unit: String,
) {
    val textMeasurer = rememberTextMeasurer()
    val spec = rulerSpec(unit)
    Canvas(
        modifier = Modifier
            .width((width * zoom).dp)
            .height(RulerThickness)
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFCBD5E1))
    ) {
        var value = 0f
        var tickIndex = 0
        while (value <= width) {
            val x = value * pageScale
            val isMajor = tickIndex % spec.minorsPerMajor == 0
            val tickHeight = if (isMajor) 14.dp.toPx() else 7.dp.toPx()
            drawLine(
                color = if (isMajor) Color(0xFF64748B) else Color(0xFFCBD5E1),
                start = Offset(x, size.height),
                end = Offset(x, size.height - tickHeight),
                strokeWidth = 1.dp.toPx(),
            )
            if (isMajor) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = formatRulerValue(value, unit),
                    topLeft = Offset(x + 3.dp.toPx(), 3.dp.toPx()),
                    style = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFF475569),
                        fontSize = 9.sp,
                    ),
                )
            }
            value += spec.minorStep
            tickIndex += 1
        }
    }
}

@Composable
internal fun VerticalRuler(
    height: Float,
    zoom: Float,
    pageScale: Float,
    unit: String,
) {
    val textMeasurer = rememberTextMeasurer()
    val spec = rulerSpec(unit)
    Canvas(
        modifier = Modifier
            .width(RulerThickness)
            .height((height * zoom).dp)
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFCBD5E1))
    ) {
        var value = 0f
        var tickIndex = 0
        while (value <= height) {
            val y = value * pageScale
            val isMajor = tickIndex % spec.minorsPerMajor == 0
            val tickWidth = if (isMajor) 14.dp.toPx() else 7.dp.toPx()
            drawLine(
                color = if (isMajor) Color(0xFF64748B) else Color(0xFFCBD5E1),
                start = Offset(size.width, y),
                end = Offset(size.width - tickWidth, y),
                strokeWidth = 1.dp.toPx(),
            )
            if (isMajor) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = formatRulerValue(value, unit),
                    topLeft = Offset(3.dp.toPx(), y + 2.dp.toPx()),
                    style = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFF475569),
                        fontSize = 9.sp,
                    ),
                )
            }
            value += spec.minorStep
            tickIndex += 1
        }
    }
}



