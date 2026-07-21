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
import androidx.compose.ui.text.input.KeyboardType
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
import me.aiglez.service.domain.models.InlineAlignment
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.models.TemplateElementType
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
import me.aiglez.service.ui.templates.editor.TemplateEditorState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun CanvasWorkspace(
    state: TemplateEditorState,
    zoomCommand: ZoomCommand?,
    onConsumeZoomCommand: () -> Unit,
    paletteDrop: PaletteDrop?,
    onConsumePaletteDrop: () -> Unit,
    onAddElement: (TemplateElementType, Float, Float) -> Unit,
    onAddDataField: (String, String, String, Float, Float) -> Unit,
    onSelectElement: (String?) -> Unit,
    onSetSelection: (List<String>) -> Unit,
    onToggleSelection: (String) -> Unit,
    onSelectSimilar: () -> Unit,
    onShowInlineEditHint: (String) -> Unit,
    onShowShortcutMessage: (String) -> Unit,
    onPreviewBounds: (String, PageRect) -> Unit,
    onPreviewBoundsBatch: (Map<String, PageRect>) -> Unit,
    onPreviewRotation: (String, Float) -> Unit,
    onCommitBounds: (TemplateElement, PageRect) -> Unit,
    onCommitBoundsBatch: (List<TemplateElement>, Map<String, PageRect>) -> Unit,
    onCommitRotation: (TemplateElement, Float) -> Unit,
    onDeleteSelected: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onPasteInPlace: () -> Unit,
    onDuplicate: () -> Unit,
    onGroup: () -> Unit,
    onUngroup: () -> Unit,
    onLock: () -> Unit,
    onHide: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onAlign: (SelectionAlignment) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onTogglePreviewMode: () -> Unit,
    onSetZoom: (Float) -> Unit,
    onSetCanvasMetric: (CanvasMetric, Float) -> Unit,
    onZoomCommand: (ZoomCommand) -> Unit,
    onNudgeSelected: (Float, Float) -> Unit,
    onRotateSelected: (Float) -> Unit,
    onToggleSelectedTextBold: () -> Unit,
    onToggleSelectedTextItalic: () -> Unit,
    onToggleSelectedTextUnderline: () -> Unit,
    onAdjustSelectedTextFontSize: (Float) -> Unit,
    onAlignSelectedText: (String) -> Unit,
    workspaceColor: Color = Color(0xFFE5E7EB),
    modifier: Modifier = Modifier,
) {
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current.density
    val pageScale = state.canvas.zoom * density
    var pageWindowBounds by remember { mutableStateOf<Rect?>(null) }
    var workspaceWindowBounds by remember { mutableStateOf<Rect?>(null) }
    var cursorPagePoint by remember { mutableStateOf<PagePoint?>(null) }
    var isSpacePressed by remember { mutableStateOf(false) }
    var isMiddleMousePressed by remember { mutableStateOf(false) }
    var didInitialCenter by remember { mutableStateOf(false) }
    var pendingViewportFocus by remember { mutableStateOf<ViewportFocusRequest?>(null) }
    var pendingPointerZoom by remember { mutableStateOf<PointerZoomRequest?>(null) }
    val nativeViewportGesture = rememberNativeViewportGesture()

    fun applyScrollDelta(delta: Offset) {
        horizontalScroll.dispatchRawDelta(delta.x)
        verticalScroll.dispatchRawDelta(delta.y)
    }

    fun panByDrag(dragAmount: Offset) {
        applyScrollDelta(Offset(-dragAmount.x, -dragAmount.y))
    }

    fun panByWheel(scrollDelta: Offset) {
        applyScrollDelta(scrollDelta * WheelPanMultiplier)
    }

    fun toggleCanvasMetric(metric: CanvasMetric, enabled: Boolean) {
        onSetCanvasMetric(metric, if (enabled) 0f else 1f)
    }

    BoxWithConstraints(
        modifier = modifier
            .background(workspaceColor)
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.key == Key.Spacebar) {
                    isSpacePressed = event.type == KeyEventType.KeyDown
                    return@onPreviewKeyEvent true
                }
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val isCommand = event.isCtrlPressed || event.isMetaPressed
                val nudgeMultiplier = when {
                    event.isAltPressed -> 0.25f
                    event.isShiftPressed -> 10f
                    else -> 1f
                }
                val nudge = state.canvas.nudgeDistance * nudgeMultiplier
                when {
                    event.key == Key.Escape -> {
                        onSelectElement(null)
                        true
                    }
                    event.key in listOf(Key.DirectionLeft, Key.DirectionRight, Key.DirectionUp, Key.DirectionDown) &&
                        state.selectedElement == null -> false
                    event.key == Key.Delete || event.key == Key.Backspace -> {
                        if (state.selectedElement == null) false else {
                            onDeleteSelected()
                            true
                        }
                    }
                    isCommand && event.isAltPressed && event.key == Key.Z -> {
                        onShowShortcutMessage("History panel is not available yet.")
                        true
                    }
                    isCommand && event.isShiftPressed && event.key == Key.Z -> {
                        onRedo()
                        true
                    }
                    isCommand && event.key == Key.Z -> {
                        onUndo()
                        true
                    }
                    isCommand && event.key == Key.Y -> {
                        onRedo()
                        true
                    }
                    isCommand && event.key == Key.A -> {
                        if (event.isShiftPressed) {
                            onSelectSimilar()
                        } else {
                            onSetSelection(state.document.elements.filter { it.visible }.map { it.id })
                        }
                        true
                    }
                    isCommand && event.isAltPressed && event.key in listOf(Key.C, Key.V) -> {
                        onShowShortcutMessage("Style clipboard shortcuts will be available with reusable style presets.")
                        true
                    }
                    isCommand && event.key == Key.C -> {
                        onCopy()
                        true
                    }
                    isCommand && event.key == Key.X -> {
                        onCopy()
                        onDeleteSelected()
                        true
                    }
                    isCommand && event.key == Key.V -> {
                        if (event.isShiftPressed) {
                            onPasteInPlace()
                        } else {
                            onPaste()
                        }
                        true
                    }
                    isCommand && event.key == Key.D -> {
                        onDuplicate()
                        true
                    }
                    isCommand && event.isShiftPressed && event.key == Key.G -> {
                        onUngroup()
                        true
                    }
                    isCommand && event.key == Key.G -> {
                        onGroup()
                        true
                    }
                    isCommand && event.key == Key.RightBracket -> {
                        onRotateSelected(90f)
                        true
                    }
                    isCommand && event.key == Key.LeftBracket -> {
                        onRotateSelected(-90f)
                        true
                    }
                    isCommand && event.key in listOf(Key.Plus, Key.Equals, Key.NumPadAdd) -> {
                        onSetZoom((state.canvas.zoom + 0.1f).coerceIn(MinZoom, MaxZoom))
                        true
                    }
                    isCommand && event.key in listOf(Key.Minus, Key.NumPadSubtract) -> {
                        onSetZoom((state.canvas.zoom - 0.1f).coerceIn(MinZoom, MaxZoom))
                        true
                    }
                    isCommand && event.key in listOf(Key.Zero, Key.NumPad0) -> {
                        onZoomCommand(ZoomCommand.Reset)
                        true
                    }
                    isCommand && event.key in listOf(Key.One, Key.NumPad1) -> {
                        onZoomCommand(ZoomCommand.FitPage)
                        true
                    }
                    isCommand && event.key in listOf(Key.Two, Key.NumPad2) -> {
                        onZoomCommand(ZoomCommand.FitWidth)
                        true
                    }
                    isCommand && event.key in listOf(Key.Three, Key.NumPad3) -> {
                        onZoomCommand(ZoomCommand.Selection)
                        true
                    }
                    isCommand && event.key in listOf(Key.P, Key.Enter, Key.NumPadEnter) -> {
                        onTogglePreviewMode()
                        true
                    }
                    isCommand && event.isShiftPressed && event.key == Key.F -> {
                        onShowShortcutMessage("Full screen canvas is not available yet.")
                        true
                    }
                    isCommand && event.key == Key.B -> {
                        onToggleSelectedTextBold()
                        true
                    }
                    isCommand && event.key == Key.I -> {
                        onToggleSelectedTextItalic()
                        true
                    }
                    isCommand && event.key == Key.U -> {
                        onToggleSelectedTextUnderline()
                        true
                    }
                    isCommand && event.isShiftPressed && event.key == Key.Period -> {
                        onAdjustSelectedTextFontSize(1f)
                        true
                    }
                    isCommand && event.isShiftPressed && event.key == Key.Comma -> {
                        onAdjustSelectedTextFontSize(-1f)
                        true
                    }
                    isCommand && event.isShiftPressed && event.key == Key.L -> {
                        onAlignSelectedText("left")
                        true
                    }
                    isCommand && event.isShiftPressed && event.key == Key.E -> {
                        onAlignSelectedText("center")
                        true
                    }
                    isCommand && event.isShiftPressed && event.key == Key.R -> {
                        onAlignSelectedText("right")
                        true
                    }
                    isCommand && event.isShiftPressed && event.key == Key.J -> {
                        onAlignSelectedText("justify")
                        true
                    }
                    !isCommand && event.isShiftPressed && event.key == Key.R -> {
                        toggleCanvasMetric(CanvasMetric.ShowRulers, state.canvas.showRulers)
                        true
                    }
                    !isCommand && event.isShiftPressed && event.key == Key.G -> {
                        toggleCanvasMetric(CanvasMetric.ShowGrid, state.canvas.showGrid)
                        true
                    }
                    !isCommand && event.isShiftPressed && event.key == Key.Semicolon -> {
                        toggleCanvasMetric(CanvasMetric.ShowGuides, state.canvas.showGuides)
                        true
                    }
                    !isCommand && event.isShiftPressed && event.key == Key.M -> {
                        toggleCanvasMetric(CanvasMetric.ShowMargins, state.canvas.showMargins)
                        true
                    }
                    isCommand && event.key in listOf(Key.MoveHome, Key.NumPadMoveHome) -> {
                        onZoomCommand(ZoomCommand.FitPage)
                        true
                    }
                    event.key in listOf(Key.PageDown, Key.NumPadPageDown, Key.PageUp, Key.NumPadPageUp, Key.MoveHome, Key.NumPadMoveHome, Key.MoveEnd, Key.NumPadMoveEnd) -> {
                        onShowShortcutMessage("This editor currently has one page.")
                        true
                    }
                    event.key == Key.DirectionLeft -> {
                        onNudgeSelected(-nudge, 0f)
                        true
                    }
                    event.key == Key.DirectionRight -> {
                        onNudgeSelected(nudge, 0f)
                        true
                    }
                    event.key == Key.DirectionUp -> {
                        onNudgeSelected(0f, -nudge)
                        true
                    }
                    event.key == Key.DirectionDown -> {
                        onNudgeSelected(0f, nudge)
                        true
                    }
                    else -> false
                }
            }
            .onPointerEvent(PointerEventType.Press) {
                focusRequester.requestFocus()
                isMiddleMousePressed = it.button?.isTertiary == true
            }
            .onPointerEvent(PointerEventType.Release) {
                isMiddleMousePressed = false
            }
            .onGloballyPositioned { coordinates ->
                workspaceWindowBounds = coordinates.boundsInWindow()
            },
    ) {
        val rulerThickness = if (state.canvas.showRulers) RulerThickness else 0.dp
        val contentWidth = rulerThickness + (PageWidth * state.canvas.zoom).dp
        val contentHeight = rulerThickness + (PageHeight * state.canvas.zoom).dp
        val centeredHorizontalPadding = if (maxWidth > contentWidth) (maxWidth - contentWidth) / 2f else 72.dp
        val centeredVerticalPadding = if (maxHeight > contentHeight) (maxHeight - contentHeight) / 2f else 48.dp
        val horizontalPadding = if (centeredHorizontalPadding > WorkspacePadding) centeredHorizontalPadding else WorkspacePadding
        val verticalPadding = if (centeredVerticalPadding > WorkspacePadding) centeredVerticalPadding else WorkspacePadding
        val viewportWidthPx = maxWidth.value * density
        val viewportHeightPx = maxHeight.value * density
        val rulerPx = rulerThickness.value * density
        val horizontalPaddingPx = horizontalPadding.value * density
        val verticalPaddingPx = verticalPadding.value * density

        fun requestPointerZoom(viewportPoint: Offset, targetZoom: Float) {
            val pendingZoom = pendingPointerZoom
            val effectiveZoom = pendingZoom?.zoom ?: state.canvas.zoom
            val effectivePageScale = effectiveZoom * density
            val effectiveScrollX = pendingZoom
                ?.let { horizontalPaddingPx + rulerPx + it.anchorPagePoint.x * effectivePageScale - it.viewportPoint.x }
                ?: horizontalScroll.value.toFloat()
            val effectiveScrollY = pendingZoom
                ?.let { verticalPaddingPx + rulerPx + it.anchorPagePoint.y * effectivePageScale - it.viewportPoint.y }
                ?: verticalScroll.value.toFloat()
            val clampedZoom = targetZoom.coerceIn(MinZoom, MaxZoom)
            if (abs(clampedZoom - effectiveZoom) < 0.001f) return

            val anchorPagePoint = PagePoint(
                x = (effectiveScrollX + viewportPoint.x - horizontalPaddingPx - rulerPx) / effectivePageScale,
                y = (effectiveScrollY + viewportPoint.y - verticalPaddingPx - rulerPx) / effectivePageScale,
            )
            pendingViewportFocus = null
            pendingPointerZoom = PointerZoomRequest(
                anchorPagePoint = anchorPagePoint,
                viewportPoint = viewportPoint,
                zoom = clampedZoom,
            )
            onSetZoom(clampedZoom)
        }

        fun requestPointerZoomBy(viewportPoint: Offset, zoomFactor: Float) {
            val baseZoom = pendingPointerZoom?.zoom ?: state.canvas.zoom
            requestPointerZoom(viewportPoint, baseZoom * zoomFactor)
        }

        fun zoomForBounds(bounds: PageRect, viewportFill: Float): Float {
            val availableWidth = (maxWidth.value - rulerThickness.value - 48f).coerceAtLeast(120f)
            val availableHeight = (maxHeight.value - rulerThickness.value - 48f).coerceAtLeast(120f)
            val widthZoom = availableWidth / bounds.width.coerceAtLeast(1f)
            val heightZoom = availableHeight / bounds.height.coerceAtLeast(1f)
            return (min(widthZoom, heightZoom) * viewportFill).coerceIn(MinZoom, MaxZoom)
        }

        fun requestViewportFocus(bounds: PageRect, targetZoom: Float, animate: Boolean = true) {
            pendingViewportFocus = ViewportFocusRequest(bounds = bounds, zoom = targetZoom, animate = animate)
            onSetZoom(targetZoom)
        }

        fun scrollToPagePoint(point: PagePoint, targetZoom: Float) {
            requestViewportFocus(PageRect(point.x, point.y, 1f, 1f), targetZoom)
        }

        fun applyZoomToBounds(bounds: PageRect, viewportFill: Float) {
            val zoom = zoomForBounds(bounds, viewportFill)
            requestViewportFocus(bounds, zoom)
        }

        fun pageBounds(): PageRect = PageRect(0f, 0f, PageWidth, PageHeight)

        fun selectionBounds(): PageRect? {
            return state.selectedElements
                .takeIf { it.isNotEmpty() }
                ?.map(GeometryService::getElementBounds)
                ?.renderUnionBounds()
                ?.expanded(48f)
                ?.clampedToPage()
        }

        val latestRequestedZoom by rememberUpdatedState(pendingPointerZoom?.zoom ?: state.canvas.zoom)
        val latestPointerZoomRequest by rememberUpdatedState(::requestPointerZoom)

        LaunchedEffect(nativeViewportGesture?.sequence, workspaceWindowBounds) {
            val gesture = nativeViewportGesture ?: return@LaunchedEffect
            val workspaceBounds = workspaceWindowBounds ?: return@LaunchedEffect
            if (!workspaceBounds.contains(gesture.positionInWindow)) return@LaunchedEffect
            val viewportPoint = gesture.positionInWindow - workspaceBounds.topLeft
            when (gesture.kind) {
                NativeViewportGestureKind.Zoom -> requestPointerZoomBy(
                    viewportPoint = viewportPoint,
                    zoomFactor = gesture.zoomFactor,
                )
                NativeViewportGestureKind.Pan -> panByWheel(gesture.panDelta)
            }
        }

        LaunchedEffect(zoomCommand, maxWidth, maxHeight) {
            when (zoomCommand) {
                ZoomCommand.FitPage -> applyZoomToBounds(pageBounds(), 0.92f)
                ZoomCommand.FitWidth -> applyZoomToBounds(PageRect(0f, 0f, PageWidth, 1f), 0.96f)
                ZoomCommand.Reset -> requestViewportFocus(selectionBounds() ?: pageBounds(), 1f)
                ZoomCommand.Selection -> applyZoomToBounds(selectionBounds() ?: pageBounds(), 0.86f)
                null -> Unit
            }
            if (zoomCommand != null) onConsumeZoomCommand()
        }

        LaunchedEffect(
            pendingViewportFocus,
            state.canvas.zoom,
            horizontalScroll.maxValue,
            verticalScroll.maxValue,
            horizontalPaddingPx,
            verticalPaddingPx,
            viewportWidthPx,
            viewportHeightPx,
        ) {
            val request = pendingViewportFocus ?: return@LaunchedEffect
            if (horizontalScroll.maxValue <= 0 || verticalScroll.maxValue <= 0) return@LaunchedEffect
            if (abs(state.canvas.zoom - request.zoom) > 0.001f) return@LaunchedEffect

            val centerX = horizontalPaddingPx + rulerPx + request.bounds.centerX * pageScale
            val centerY = verticalPaddingPx + rulerPx + request.bounds.centerY * pageScale
            val targetX = (centerX - viewportWidthPx / 2f).roundToInt().coerceIn(0, horizontalScroll.maxValue)
            val targetY = (centerY - viewportHeightPx / 2f).roundToInt().coerceIn(0, verticalScroll.maxValue)
            if (request.animate) {
                horizontalScroll.animateScrollTo(targetX)
                verticalScroll.animateScrollTo(targetY)
            } else {
                horizontalScroll.scrollTo(targetX)
                verticalScroll.scrollTo(targetY)
            }
            pendingViewportFocus = null
        }

        LaunchedEffect(
            pendingPointerZoom,
            state.canvas.zoom,
            horizontalScroll.maxValue,
            verticalScroll.maxValue,
            horizontalPaddingPx,
            verticalPaddingPx,
            viewportWidthPx,
            viewportHeightPx,
        ) {
            val request = pendingPointerZoom ?: return@LaunchedEffect
            if (horizontalScroll.maxValue <= 0 || verticalScroll.maxValue <= 0) return@LaunchedEffect
            if (abs(state.canvas.zoom - request.zoom) > 0.001f) return@LaunchedEffect

            val centerX = horizontalPaddingPx + rulerPx + request.anchorPagePoint.x * pageScale
            val centerY = verticalPaddingPx + rulerPx + request.anchorPagePoint.y * pageScale
            val targetX = (centerX - request.viewportPoint.x).roundToInt().coerceIn(0, horizontalScroll.maxValue)
            val targetY = (centerY - request.viewportPoint.y).roundToInt().coerceIn(0, verticalScroll.maxValue)
            horizontalScroll.scrollTo(targetX)
            verticalScroll.scrollTo(targetY)
            pendingPointerZoom = null
        }

        LaunchedEffect(maxWidth, maxHeight, horizontalScroll.maxValue, verticalScroll.maxValue) {
            if (
                !didInitialCenter &&
                maxWidth.value > 0f &&
                maxHeight.value > 0f &&
                horizontalScroll.maxValue > 0 &&
                verticalScroll.maxValue > 0
            ) {
                didInitialCenter = true
                requestViewportFocus(pageBounds(), state.canvas.zoom, animate = false)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val scrollDelta = event.changes.fold(Offset.Zero) { total, change ->
                    total + change.scrollDelta
                }
                if (scrollDelta == Offset.Zero) return@onPointerEvent
                val anchorPosition = event.changes.firstOrNull()?.position ?: Offset.Zero
                val modifiers = event.toInteractionModifiers()
                when {
                    modifiers.command -> {
                        val zoomFactor = exp((-scrollDelta.y * WheelZoomSensitivity).coerceIn(-0.35f, 0.35f))
                        requestPointerZoomBy(anchorPosition, zoomFactor)
                    }
                    modifiers.shift && scrollDelta.x == 0f -> {
                        panByWheel(Offset(scrollDelta.y, 0f))
                    }
                    else -> panByWheel(scrollDelta)
                }
                event.changes.forEach { it.consume() }
            }
            .pointerInput(Unit) {
                var startedOnPage = false
                detectDragGestures(
                    onDragStart = { offset ->
                        val workspaceBounds = workspaceWindowBounds
                        val pageBounds = pageWindowBounds
                        val windowPoint = if (workspaceBounds != null) workspaceBounds.topLeft + offset else offset
                        startedOnPage = pageBounds?.contains(windowPoint) == true
                    },
                    onDrag = { change, dragAmount ->
                        if (!startedOnPage) {
                            panByDrag(dragAmount)
                            change.consume()
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var accumulatedZoom = 1f
                    var pastTouchSlop = false
                    val touchSlop = viewConfiguration.touchSlop

                    do {
                        val event = awaitPointerEvent()
                        val pointerCount = event.changes.count { it.pressed && it.previousPressed }
                        if (pointerCount >= 2) {
                            val zoomChange = event.calculateZoom()
                            if (!pastTouchSlop) {
                                accumulatedZoom *= zoomChange
                                val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                val zoomMotion = abs(1f - accumulatedZoom) * centroidSize
                                pastTouchSlop = zoomMotion > touchSlop
                            }

                            if (pastTouchSlop && zoomChange != 1f) {
                                val centroid = event.calculateCentroid(useCurrent = false)
                                if (centroid != Offset.Unspecified) {
                                    latestPointerZoomRequest(centroid, latestRequestedZoom * zoomChange)
                                }
                                event.changes.forEach { change ->
                                    if (change.positionChanged()) change.consume()
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .horizontalScroll(horizontalScroll, enabled = false)
            .verticalScroll(verticalScroll, enabled = false),
        ) {
            Box(
                modifier = Modifier.padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = verticalPadding,
                    bottom = verticalPadding,
                ),
                contentAlignment = Alignment.TopStart,
            ) {
                Row {
                    if (state.canvas.showRulers) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(RulerThickness)
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFCBD5E1))
                            )
                            VerticalRuler(height = PageHeight, zoom = state.canvas.zoom, pageScale = pageScale, unit = state.canvas.rulerUnit)
                        }
                    }
                    Column {
                        if (state.canvas.showRulers) {
                            HorizontalRuler(width = PageWidth, zoom = state.canvas.zoom, pageScale = pageScale, unit = state.canvas.rulerUnit)
                        }
                        PageCanvas(
                            state = state,
                            navigationPanActive = isSpacePressed || isMiddleMousePressed,
                            paletteDrop = paletteDrop,
                            onConsumePaletteDrop = onConsumePaletteDrop,
                            onAddElement = onAddElement,
                            onAddDataField = onAddDataField,
                            onSelectElement = onSelectElement,
                            onSetSelection = onSetSelection,
                            onToggleSelection = onToggleSelection,
                            onShowInlineEditHint = onShowInlineEditHint,
                            onPreviewBounds = onPreviewBounds,
                            onPreviewBoundsBatch = onPreviewBoundsBatch,
                            onPreviewRotation = onPreviewRotation,
                            onCommitBounds = onCommitBounds,
                            onCommitBoundsBatch = onCommitBoundsBatch,
                            onCommitRotation = onCommitRotation,
                            onPan = ::panByDrag,
                            onCopy = onCopy,
                            onPaste = onPaste,
                            onDuplicate = onDuplicate,
                            onDeleteSelected = onDeleteSelected,
                            onGroup = onGroup,
                            onUngroup = onUngroup,
                            onLock = onLock,
                            onHide = onHide,
                            onBringToFront = onBringToFront,
                            onSendToBack = onSendToBack,
                            onAlign = onAlign,
                            onPageBoundsChanged = { pageWindowBounds = it },
                            onCursorPagePointChange = { cursorPagePoint = it },
                        )
                    }
                }
            }
        }
        MiniMapNavigator(
            elements = state.document.elements,
            viewport = PageRect(
                x = (horizontalScroll.value - horizontalPaddingPx - rulerPx) / pageScale,
                y = (verticalScroll.value - verticalPaddingPx - rulerPx) / pageScale,
                width = viewportWidthPx / pageScale,
                height = viewportHeightPx / pageScale,
            ),
            onNavigate = { point -> scrollToPagePoint(point, state.canvas.zoom) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
        )
        Surface(
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            tonalElevation = 1.dp,
            shadowElevation = 1.dp,
            shape = MaterialTheme.shapes.extraSmall,
        ) {
            Text(
                text = cursorPagePoint?.let { point ->
                    "X ${formatPageValue(point.x, state.canvas.rulerUnit)}  Y ${formatPageValue(point.y, state.canvas.rulerUnit)}"
                } ?: "Outside page",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


