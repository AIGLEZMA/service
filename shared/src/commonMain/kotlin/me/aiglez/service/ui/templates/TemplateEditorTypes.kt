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

internal const val PageWidth = 595f
internal const val PageHeight = 842f
internal const val MinZoom = 0.25f
internal const val MaxZoom = 3f
internal const val WheelZoomSensitivity = 0.0018f
internal const val WheelPanMultiplier = 3f
internal const val ResizeHandleHitSizeDp = 36f
internal const val ResizeHandleVisualSizeDp = 10f
internal const val RotationHandleHitSizeDp = 48f
internal const val RotationHandleOffsetDp = 32f
internal const val RotationHandleRadiusDp = 7f
internal val RulerThickness = 28.dp
internal val WorkspacePadding = 1200.dp
internal val RulerUnits = listOf("px", "mm", "cm", "inch")

internal sealed interface PaletteDrop {
    val windowPosition: Offset
    val sequence: Int

    data class Component(
        val type: TemplateElementType,
        override val windowPosition: Offset,
        override val sequence: Int,
    ) : PaletteDrop

    data class DataField(
        val schemaName: String,
        val slug: String,
        val name: String,
        override val windowPosition: Offset,
        override val sequence: Int,
    ) : PaletteDrop
}

internal enum class PaletteTab {
    Components,
    DataFields,
}

internal enum class ZoomCommand {
    FitPage,
    FitWidth,
    Reset,
    Selection,
}

internal enum class InspectorPage {
    Component,
    PageSetup,
}

internal data class ComponentPaletteItem(
    val label: String,
    val glyph: String,
    val type: TemplateElementType,
    val group: String,
    val enabled: Boolean = true,
)

internal data class InteractionModifiers(
    val shift: Boolean = false,
    val alt: Boolean = false,
    val command: Boolean = false,
)

internal enum class SmartGuideOrientation {
    Vertical,
    Horizontal,
}

internal enum class SmartGuideKind {
    LeftEdge,
    RightEdge,
    TopEdge,
    BottomEdge,
    HorizontalCenter,
    VerticalCenter,
    Baseline,
    EqualSpacing,
    SameWidth,
    SameHeight,
    SameX,
    SameY,
}

internal data class SmartGuide(
    val orientation: SmartGuideOrientation,
    val position: Float,
    val kind: SmartGuideKind,
    val spanStart: Float,
    val spanEnd: Float,
    val label: String,
)

internal data class DistanceLabel(
    val start: PagePoint,
    val end: PagePoint,
    val label: String,
    val horizontal: Boolean,
)

internal data class ViewportFocusRequest(
    val bounds: PageRect,
    val zoom: Float,
    val animate: Boolean = true,
)

internal data class PointerZoomRequest(
    val anchorPagePoint: PagePoint,
    val viewportPoint: Offset,
    val zoom: Float,
)

internal sealed interface PageRenderItem {
    val element: TemplateElement
}

internal data class TextRenderItem(
    override val element: TemplateElement.Text,
    val bounds: PageRect,
    val background: Color,
    val borderColor: Color,
    val textLayout: TextLayoutResult,
    val textTopLeft: Offset,
) : PageRenderItem

internal data class RectangleRenderItem(
    override val element: TemplateElement.Rectangle,
    val bounds: PageRect,
    val fillColor: Color,
    val borderColor: Color,
) : PageRenderItem

internal data class ImageRenderItem(
    override val element: TemplateElement.Image,
    val bounds: PageRect,
    val bitmap: ImageBitmap?,
    val intrinsicWidth: Int,
    val intrinsicHeight: Int,
    val background: Color,
    val borderColor: Color,
) : PageRenderItem

internal data class CircleRenderItem(
    override val element: TemplateElement.Circle,
    val bounds: PageRect,
    val fillColor: Color,
    val borderColor: Color,
) : PageRenderItem

internal data class QRCodeRenderItem(
    override val element: TemplateElement.QRCode,
    val bounds: PageRect,
    val matrix: TemplateQrMatrix?,
    val foregroundColor: Color,
    val backgroundColor: Color,
    val borderColor: Color,
) : PageRenderItem

internal data class BarcodeRenderItem(
    override val element: TemplateElement.Barcode,
    val bounds: PageRect,
    val matrix: TemplateBarcodeMatrix?,
    val foregroundColor: Color,
    val backgroundColor: Color,
    val borderColor: Color,
    val textLayout: TextLayoutResult?,
) : PageRenderItem

internal data class ListRenderItem(
    override val element: TemplateElement.List,
    val bounds: PageRect,
    val background: Color,
    val borderColor: Color,
    val textColor: Color,
    val itemLayouts: kotlin.collections.List<ListItemRenderLayout>,
) : PageRenderItem

internal data class ListItemRenderLayout(
    val layout: TextLayoutResult,
    val topLeft: Offset,
)

internal data class LineRenderItem(
    override val element: TemplateElement.Line,
    val bounds: PageRect,
    val start: Offset,
    val end: Offset,
    val color: Color,
    val strokeWidth: Float,
) : PageRenderItem



