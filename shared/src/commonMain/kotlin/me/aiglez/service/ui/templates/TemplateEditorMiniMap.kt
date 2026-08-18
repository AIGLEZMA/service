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
internal fun MiniMapNavigator(
    elements: List<TemplateElement>,
    viewport: PageRect,
    pageDimensions: TemplatePageDimensions,
    onNavigate: (PagePoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density

    fun pagePointForNavigatorPosition(position: Offset): PagePoint {
        val transform = miniMapTransform(
            width = 132f * density,
            height = 188f * density,
            padding = 10f * density,
            pageWidth = pageDimensions.width,
            pageHeight = pageDimensions.height,
        )
        return PagePoint(
            x = ((position.x - transform.left) / transform.scale).coerceIn(0f, pageDimensions.width),
            y = ((position.y - transform.top) / transform.scale).coerceIn(0f, pageDimensions.height),
        )
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Canvas(
            modifier = Modifier
                .size(132.dp, 188.dp)
                .pointerInput(elements, viewport, pageDimensions) {
                    detectTapGestures { offset ->
                        onNavigate(pagePointForNavigatorPosition(offset))
                    }
                }
                .pointerInput(elements, viewport, pageDimensions) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            onNavigate(pagePointForNavigatorPosition(offset))
                        },
                        onDrag = { change, _ ->
                            onNavigate(pagePointForNavigatorPosition(change.position))
                            change.consume()
                        },
                    )
                },
        ) {
            val transform = miniMapTransform(
                width = size.width,
                height = size.height,
                padding = 10.dp.toPx(),
                pageWidth = pageDimensions.width,
                pageHeight = pageDimensions.height,
            )
            drawRoundRect(
                color = Color(0xFFE5E7EB),
                topLeft = Offset.Zero,
                size = size,
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            )
            drawRect(
                color = Color.White,
                topLeft = Offset(transform.left, transform.top),
                size = Size(pageDimensions.width * transform.scale, pageDimensions.height * transform.scale),
            )
            drawRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(transform.left, transform.top),
                size = Size(pageDimensions.width * transform.scale, pageDimensions.height * transform.scale),
                style = Stroke(width = 1.dp.toPx()),
            )
            elements
                .asSequence()
                .filter { it.visible }
                .sortedBy { it.zIndex }
                .forEach { element ->
                    val bounds = GeometryService.getElementBounds(element)
                    drawRect(
                        color = when (element) {
                            is TemplateElement.Text -> Color(0xFF2563EB).copy(alpha = 0.42f)
                            is TemplateElement.Image -> Color(0xFF0F766E).copy(alpha = 0.42f)
                            is TemplateElement.Circle -> Color(0xFFF59E0B).copy(alpha = 0.46f)
                            is TemplateElement.QRCode -> Color(0xFF111827).copy(alpha = 0.48f)
                            is TemplateElement.Barcode -> Color(0xFF6D28D9).copy(alpha = 0.42f)
                            is TemplateElement.List -> Color(0xFF0891B2).copy(alpha = 0.42f)
                            is TemplateElement.Table -> Color(0xFF0D9488).copy(alpha = 0.44f)
                            is TemplateElement.Rectangle -> Color(0xFF16A34A).copy(alpha = 0.42f)
                            is TemplateElement.Line -> Color(0xFF111827).copy(alpha = 0.52f)
                        },
                        topLeft = Offset(
                            transform.left + bounds.x * transform.scale,
                            transform.top + bounds.y * transform.scale,
                        ),
                        size = Size(
                            (bounds.width * transform.scale).coerceAtLeast(1.dp.toPx()),
                            (bounds.height * transform.scale).coerceAtLeast(1.dp.toPx()),
                        ),
                    )
                }
            val clampedViewport = viewport.clampedToPage(pageDimensions.width, pageDimensions.height)
            drawRect(
                color = Color(0xFFDC2626).copy(alpha = 0.10f),
                topLeft = Offset(
                    transform.left + clampedViewport.x * transform.scale,
                    transform.top + clampedViewport.y * transform.scale,
                ),
                size = Size(
                    clampedViewport.width * transform.scale,
                    clampedViewport.height * transform.scale,
                ),
            )
            drawRect(
                color = Color(0xFFDC2626),
                topLeft = Offset(
                    transform.left + clampedViewport.x * transform.scale,
                    transform.top + clampedViewport.y * transform.scale,
                ),
                size = Size(
                    clampedViewport.width * transform.scale,
                    clampedViewport.height * transform.scale,
                ),
                style = Stroke(width = 1.25.dp.toPx()),
            )
        }
    }
}

@Composable
internal fun ElementContextMenu(
    offset: Offset,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onGroup: () -> Unit,
    onUngroup: () -> Unit,
    onLock: () -> Unit,
    onHide: () -> Unit,
    onAlign: (SelectionAlignment) -> Unit,
) {
    val menuOffset = with(LocalDensity.current) {
        DpOffset(offset.x.toDp(), offset.y.toDp())
    }
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        offset = menuOffset,
    ) {
        MenuCommand("Dupliquer", true, onDuplicate, onDismiss)
        MenuCommand("Copier", true, onCopy, onDismiss)
        MenuCommand("Coller", true, onPaste, onDismiss)
        MenuCommand("Supprimer", true, onDelete, onDismiss)
        HorizontalDivider()
        MenuCommand("Mettre au premier plan", true, onBringToFront, onDismiss)
        MenuCommand("Mettre à l’arrière-plan", true, onSendToBack, onDismiss)
        HorizontalDivider()
        MenuCommand("Grouper", true, onGroup, onDismiss)
        MenuCommand("Dégrouper", true, onUngroup, onDismiss)
        MenuCommand("Verrouiller", true, onLock, onDismiss)
        MenuCommand("Masquer", true, onHide, onDismiss)
        HorizontalDivider()
        MenuCommand("Aligner à gauche", true, { onAlign(SelectionAlignment.Left) }, onDismiss)
        MenuCommand("Centrer horizontalement", true, { onAlign(SelectionAlignment.Center) }, onDismiss)
        MenuCommand("Aligner à droite", true, { onAlign(SelectionAlignment.Right) }, onDismiss)
        MenuCommand("Aligner en haut", true, { onAlign(SelectionAlignment.Top) }, onDismiss)
        MenuCommand("Centrer verticalement", true, { onAlign(SelectionAlignment.Middle) }, onDismiss)
        MenuCommand("Aligner en bas", true, { onAlign(SelectionAlignment.Bottom) }, onDismiss)
    }
}

internal data class MiniMapTransform(
    val left: Float,
    val top: Float,
    val scale: Float,
)

internal fun miniMapTransform(
    width: Float,
    height: Float,
    padding: Float,
    pageWidth: Float = PageWidth,
    pageHeight: Float = PageHeight,
): MiniMapTransform {
    val availableWidth = (width - padding * 2f).coerceAtLeast(1f)
    val availableHeight = (height - padding * 2f).coerceAtLeast(1f)
    val scale = min(availableWidth / pageWidth, availableHeight / pageHeight)
    return MiniMapTransform(
        left = (width - pageWidth * scale) / 2f,
        top = (height - pageHeight * scale) / 2f,
        scale = scale,
    )
}
