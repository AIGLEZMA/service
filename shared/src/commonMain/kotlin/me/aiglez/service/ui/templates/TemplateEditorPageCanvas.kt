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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun PageCanvas(
    state: TemplateEditorState,
    navigationPanActive: Boolean,
    paletteDrop: PaletteDrop?,
    onConsumePaletteDrop: () -> Unit,
    onAddElement: (TemplateElementType, Float, Float) -> Unit,
    onAddDataField: (String, String, String, Float, Float) -> Unit,
    onSelectElement: (String?) -> Unit,
    onSetSelection: (List<String>) -> Unit,
    onToggleSelection: (String) -> Unit,
    onShowInlineEditHint: (String) -> Unit,
    onPreviewBounds: (String, PageRect) -> Unit,
    onPreviewBoundsBatch: (Map<String, PageRect>) -> Unit,
    onPreviewRotation: (String, Float) -> Unit,
    onCommitBounds: (TemplateElement, PageRect) -> Unit,
    onCommitBoundsBatch: (List<TemplateElement>, Map<String, PageRect>) -> Unit,
    onCommitRotation: (TemplateElement, Float) -> Unit,
    onPan: (Offset) -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onDuplicate: () -> Unit,
    onDeleteSelected: () -> Unit,
    onGroup: () -> Unit,
    onUngroup: () -> Unit,
    onLock: () -> Unit,
    onHide: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onAlign: (SelectionAlignment) -> Unit,
    onPageBoundsChanged: (Rect) -> Unit,
    onCursorPagePointChange: (PagePoint?) -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()
    val zoom = state.canvas.zoom
    val density = LocalDensity.current.density
    val pageScale = zoom * density
    val latestState by rememberUpdatedState(state)
    val latestNavigationPanActive by rememberUpdatedState(navigationPanActive)
    val latestOnSelectElement by rememberUpdatedState(onSelectElement)
    val latestOnSetSelection by rememberUpdatedState(onSetSelection)
    val latestOnToggleSelection by rememberUpdatedState(onToggleSelection)
    val latestOnShowInlineEditHint by rememberUpdatedState(onShowInlineEditHint)
    val latestOnPreviewBounds by rememberUpdatedState(onPreviewBounds)
    val latestOnPreviewBoundsBatch by rememberUpdatedState(onPreviewBoundsBatch)
    val latestOnPreviewRotation by rememberUpdatedState(onPreviewRotation)
    val latestOnCommitBounds by rememberUpdatedState(onCommitBounds)
    val latestOnCommitBoundsBatch by rememberUpdatedState(onCommitBoundsBatch)
    val latestOnCommitRotation by rememberUpdatedState(onCommitRotation)
    val latestOnPan by rememberUpdatedState(onPan)
    val latestOnCursorPagePointChange by rememberUpdatedState(onCursorPagePointChange)
    val pageDimensions = templatePageDimensions(state.template?.pageSize)
    val guideSet = state.canvas.toSnapGuideSet(pageDimensions)
    val latestGuideSet by rememberUpdatedState(guideSet)
    val effectiveSnapThreshold = effectiveSnapThreshold(state.canvas.snapThreshold, zoom)
    val latestEffectiveSnapThreshold by rememberUpdatedState(effectiveSnapThreshold)
    val expressionContext = remember(
        state.availableSchemas,
        state.schema,
        state.selectedPreviewRecords,
        state.isPreviewMode,
    ) {
        if (state.isPreviewMode) {
            recordExpressionContext(
                schemas = state.availableSchemas,
                recordsBySchemaId = state.selectedPreviewRecords,
                primarySchema = state.schema,
            )
        } else {
            sampleSchemaExpressionContext(
                schemas = state.availableSchemas,
                primarySchema = state.schema,
            )
        }
    }
    val resolveTemplateExpressions = state.isPreviewMode || state.showSampleData
    val renderItems = remember(state.document.elements, pageScale, zoom, textMeasurer, expressionContext, resolveTemplateExpressions) {
        buildPageRenderItems(
            elements = state.document.elements,
            pageScale = pageScale,
            zoom = zoom,
            textMeasurer = textMeasurer,
            expressionContext = expressionContext,
            resolveExpressions = resolveTemplateExpressions,
        )
    }
    val pageModifier = Modifier
        .size((pageDimensions.width * zoom).dp, (pageDimensions.height * zoom).dp)
        .then(if (state.canvas.showPageShadow) Modifier.shadow(18.dp) else Modifier)
        .background(Color.White)
        .then(
            if (state.canvas.showPageOutline) {
                Modifier.border(1.dp, Color(0xFFCBD5E1))
            } else {
                Modifier
            },
        )

    var dragOriginalElement by remember { mutableStateOf<TemplateElement?>(null) }
    var dragOriginalElements by remember { mutableStateOf(emptyList<TemplateElement>()) }
    var dragOriginalBoundsById by remember { mutableStateOf(emptyMap<String, PageRect>()) }
    var dragOriginalBounds by remember { mutableStateOf<PageRect?>(null) }
    var dragLatestBounds by remember { mutableStateOf<PageRect?>(null) }
    var dragLatestBoundsById by remember { mutableStateOf(emptyMap<String, PageRect>()) }
    var dragStartPage by remember { mutableStateOf<PagePoint?>(null) }
    var dragStartAngle by remember { mutableStateOf(0f) }
    var dragHandle by remember { mutableStateOf<ResizeHandle?>(null) }
    var dragOriginalRotation by remember { mutableStateOf(0f) }
    var dragLatestRotation by remember { mutableStateOf<Float?>(null) }
    var isRotatingElement by remember { mutableStateOf(false) }
    var isPanningPage by remember { mutableStateOf(false) }
    var isSelectingArea by remember { mutableStateOf(false) }
    var selectionStartPage by remember { mutableStateOf<PagePoint?>(null) }
    var selectionRect by remember { mutableStateOf<PageRect?>(null) }
    var pageWindowBounds by remember { mutableStateOf<Rect?>(null) }
    var pointerModifiers by remember { mutableStateOf(InteractionModifiers()) }
    val latestPointerModifiers by rememberUpdatedState(pointerModifiers)
    var activeSnapGuide by remember { mutableStateOf<Pair<Float?, Float?>?>(null) }
    var activeSmartGuides by remember { mutableStateOf(emptyList<SmartGuide>()) }
    var activeDistanceLabels by remember { mutableStateOf(emptyList<DistanceLabel>()) }
    var activePreSnapBounds by remember { mutableStateOf<PageRect?>(null) }
    var activeTargetElementIds by remember { mutableStateOf(emptySet<String>()) }
    var activeMeasurementLabel by remember { mutableStateOf<DistanceLabel?>(null) }
    var contextMenuOffset by remember { mutableStateOf<Offset?>(null) }
    var snapPulse by remember { mutableStateOf(0f) }

    LaunchedEffect(activeSnapGuide) {
        if (activeSnapGuide == null) {
            snapPulse = 0f
        } else {
            snapPulse = 1f
            repeat(8) { step ->
                delay(16)
                snapPulse = 1f - (step + 1) / 8f
            }
        }
    }

    LaunchedEffect(paletteDrop, pageWindowBounds, pageScale) {
        val drop = paletteDrop ?: return@LaunchedEffect
        val bounds = pageWindowBounds ?: return@LaunchedEffect
        if (drop.windowPosition.x in bounds.left..bounds.right && drop.windowPosition.y in bounds.top..bounds.bottom) {
            val x = (drop.windowPosition.x - bounds.left) / pageScale
            val y = (drop.windowPosition.y - bounds.top) / pageScale
            when (drop) {
                is PaletteDrop.Component -> onAddElement(drop.type, x, y)
                is PaletteDrop.DataField -> onAddDataField(drop.schemaName, drop.slug, drop.name, x, y)
            }
        }
        onConsumePaletteDrop()
    }

    Box(
        modifier = pageModifier
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInWindow()
                pageWindowBounds = bounds
                onPageBoundsChanged(bounds)
            }
            .onPointerEvent(PointerEventType.Move) { event ->
                pointerModifiers = event.toInteractionModifiers()
                val position = event.changes.firstOrNull()?.position
                latestOnCursorPagePointChange(
                    position
                        ?.takeIf {
                            it.x in 0f..(pageDimensions.width * pageScale) &&
                                it.y in 0f..(pageDimensions.height * pageScale)
                        }
                        ?.toPagePoint(pageScale),
                )
            }
            .onPointerEvent(PointerEventType.Press) { event ->
                pointerModifiers = event.toInteractionModifiers()
                if (event.button?.isSecondary == true) {
                    val position = event.changes.firstOrNull()?.position ?: return@onPointerEvent
                    val point = position.toPagePoint(pageScale)
                    val hit = GeometryService.hitTestElement(latestState.document.elements, point)
                    if (hit != null && hit.id !in latestState.document.selectedElementIds) {
                        latestOnSelectElement(hit.id)
                    }
                    if (hit != null) {
                        contextMenuOffset = position
                    }
                    event.changes.forEach { it.consume() }
                }
            }
            .onPointerEvent(PointerEventType.Release) { event ->
                pointerModifiers = event.toInteractionModifiers()
            }
            .onPointerEvent(PointerEventType.Exit) {
                latestOnCursorPagePointChange(null)
            }
            .pointerInput(pageScale) {
                detectTapGestures(
                    onPress = { offset ->
                        val point = offset.toPagePoint(pageScale)
                        val editorState = latestState
                        val modifiers = latestPointerModifiers
                        if (modifiers.alt) {
                            val hits = hitTestElementsAt(editorState.document.elements, point)
                            if (hits.isNotEmpty()) {
                                val currentId = editorState.document.selectedElementId
                                val currentIndex = hits.indexOfFirst { it.id == currentId }
                                val next = hits[(currentIndex + 1).floorMod(hits.size)]
                                latestOnSelectElement(next.id)
                            }
                        } else {
                            val hit = GeometryService.hitTestElement(editorState.document.elements, point)
                            val selectionControlHit = editorState.selectedElement?.let { selected ->
                                hitTestSelectionControl(
                                    element = selected,
                                    point = point,
                                    density = density,
                                    pageScale = pageScale,
                                )
                            }
                            if (selectionControlHit != null) {
                                return@detectTapGestures
                            }
                            if (modifiers.command && hit != null) {
                                val hits = hitTestElementsAt(editorState.document.elements, point)
                                if (hits.size > 1 && hit.id in editorState.document.selectedElementIds) {
                                    val currentIndex = hits.indexOfFirst { it.id == editorState.document.selectedElementId }
                                    val next = hits[(currentIndex + 1).floorMod(hits.size)]
                                    latestOnSelectElement(next.id)
                                } else {
                                    latestOnToggleSelection(hit.id)
                                }
                            } else if (modifiers.shift && hit != null) {
                                latestOnToggleSelection(hit.id)
                            } else if (hit == null || hit.id !in editorState.document.selectedElementIds) {
                                latestOnSelectElement(hit?.id)
                            } else {
                                // Keep multi-selection intact when starting a drag from one selected element.
                            }
                        }
                    },
                    onDoubleTap = { offset ->
                        val point = offset.toPagePoint(pageScale)
                        val hit = GeometryService.hitTestElement(latestState.document.elements, point)
                        if (hit != null) {
                            latestOnSelectElement(hit.id)
                            latestOnShowInlineEditHint(hit.id)
                        }
                    },
                )
            }
            .pointerInput(pageScale) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val point = offset.toPagePoint(pageScale)
                        val editorState = latestState
                        if (latestNavigationPanActive) {
                            dragOriginalElement = null
                            dragOriginalElements = emptyList()
                            dragOriginalBoundsById = emptyMap()
                            dragOriginalBounds = null
                            dragLatestBounds = null
                            dragLatestBoundsById = emptyMap()
                            dragStartPage = null
                            dragStartAngle = 0f
                            dragHandle = null
                            dragOriginalRotation = 0f
                            dragLatestRotation = null
                            isRotatingElement = false
                            isPanningPage = true
                            isSelectingArea = false
                            return@detectDragGestures
                        }
                        val selected = editorState.selectedElement
                        val selectionControlHit = selected?.let { element ->
                            hitTestSelectionControl(
                                element = element,
                                point = point,
                                density = density,
                                pageScale = pageScale,
                            )
                        }
	                        val rotationElement = selected.takeIf { selectionControlHit?.isRotation == true }
	                        val handle = selectionControlHit?.resizeHandle
	                        val element = if (rotationElement != null) {
	                            rotationElement
	                        } else if (handle != null) {
	                            selected
	                        } else {
	                            GeometryService.hitTestElement(editorState.document.elements, point)
	                        }
                        if (element != null) {
                            if (!latestPointerModifiers.shift && element.id !in editorState.document.selectedElementIds) {
                                latestOnSelectElement(element.id)
	                    }
                            val draggedElements = if (handle == null && element.id in editorState.document.selectedElementIds) {
                                editorState.selectedElements.filterNot { it.locked }
                            } else {
                                listOf(element).filterNot { it.locked }
                            }
                            dragOriginalElement = element
                            dragOriginalElements = draggedElements
                            dragOriginalBoundsById = draggedElements.associate { it.id to GeometryService.getElementBounds(it) }
                            dragOriginalBounds = GeometryService.getElementBounds(element)
                            dragLatestBounds = GeometryService.getElementBounds(element)
                            dragLatestBoundsById = emptyMap()
                            dragStartPage = point
                            dragStartAngle = angleFromCenter(GeometryService.getElementBounds(element).centerPagePoint(), point)
                            dragHandle = handle
                            dragOriginalRotation = element.rotation
                            dragLatestRotation = null
                            isRotatingElement = rotationElement != null
                            isPanningPage = false
                            isSelectingArea = false
                        } else {
                            if (!latestPointerModifiers.shift) {
                                latestOnSelectElement(null)
                            }
                            dragOriginalElement = null
                            dragOriginalElements = emptyList()
                            dragOriginalBoundsById = emptyMap()
                            dragOriginalBounds = null
                            dragLatestBounds = null
                            dragLatestBoundsById = emptyMap()
                            dragStartPage = null
                            dragStartAngle = 0f
                            dragHandle = null
                            dragOriginalRotation = 0f
                            dragLatestRotation = null
                            isRotatingElement = false
                            isPanningPage = false
                            isSelectingArea = true
                            selectionStartPage = point
                            selectionRect = PageRect(point.x, point.y, 0f, 0f)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        if (isSelectingArea) {
                            val start = selectionStartPage ?: return@detectDragGestures
                            val current = change.position.toPagePoint(pageScale)
                            selectionRect = selectionBounds(start, current)
                            change.consume()
                            return@detectDragGestures
                        }
                        if (isPanningPage) {
                            latestOnPan(dragAmount)
                            change.consume()
                            return@detectDragGestures
                        }
                        val element = dragOriginalElement ?: return@detectDragGestures
                        if (element.locked) return@detectDragGestures
                        val originalBounds = dragOriginalBounds ?: return@detectDragGestures
                        val start = dragStartPage ?: return@detectDragGestures
                        val current = change.position.toPagePoint(pageScale)
                        val modifiers = latestPointerModifiers
                        if (isRotatingElement) {
                            val angle = angleFromCenter(originalBounds.centerPagePoint(), current)
                            val rawRotation = normalizeEditorRotation(dragOriginalRotation + angle - dragStartAngle)
                            val nextRotation = if (modifiers.shift) GeometryService.snapRotation(rawRotation, increment = 15f, threshold = 7f) else rawRotation
                            dragLatestRotation = nextRotation
                            activeMeasurementLabel = DistanceLabel(
                                start = PagePoint(originalBounds.centerX, originalBounds.y),
                                end = PagePoint(originalBounds.centerX, originalBounds.y),
                                label = "R ${formatCompact(nextRotation)}",
                                horizontal = true,
                            )
                            latestOnPreviewRotation(element.id, nextRotation)
                            change.consume()
                            return@detectDragGestures
                        }
                        val nextBounds = dragHandle?.let { handle ->
                            GeometryService.resizeElement(
                                originalBounds = originalBounds,
                                handle = handle,
                                dragStartPage = start,
                                currentPage = current,
                                constrainProportions = modifiers.shift,
                                resizeFromCenter = modifiers.alt,
                            )
                        } ?: if (modifiers.shift) {
                            GeometryService.moveElementConstrained(originalBounds, start, current)
                        } else {
                            GeometryService.moveElement(originalBounds, start, current)
                        }
                        val snapped = if (latestState.canvas.snapEnabled && !modifiers.command) {
                            GeometryService.snapBounds(
                                bounds = nextBounds,
                                guideSet = latestGuideSet,
                                otherElements = if (latestState.canvas.snapToObjects) {
                                    latestState.document.elements.filterNot { it.id == element.id }
                                } else {
                                    emptyList()
                                },
                                threshold = latestEffectiveSnapThreshold,
                                mode = dragHandle,
                                resizeFromCenter = modifiers.alt,
                            )
                        } else {
                            activeSnapGuide = null
                            null
                        }
                        val previewBounds = snapped?.bounds ?: nextBounds
                        activeSnapGuide = snapped?.let { it.verticalGuide to it.horizontalGuide }
                        val otherElements = latestState.document.elements.filterNot { it.id == element.id }
                        activePreSnapBounds = snapped
                            ?.bounds
                            ?.takeIf { abs(it.x - nextBounds.x) > 0.01f || abs(it.y - nextBounds.y) > 0.01f || abs(it.width - nextBounds.width) > 0.01f || abs(it.height - nextBounds.height) > 0.01f }
                            ?.let { nextBounds }
                        val nextBoundsById = if (dragHandle == null && dragOriginalElements.size > 1) {
                            val dx = previewBounds.x - originalBounds.x
                            val dy = previewBounds.y - originalBounds.y
                            dragOriginalElements.associate { item ->
                                val itemBounds = dragOriginalBoundsById.getValue(item.id)
                                item.id to itemBounds.copy(x = itemBounds.x + dx, y = itemBounds.y + dy)
                            }
                        } else {
                            mapOf(element.id to previewBounds)
                        }
                        activeSmartGuides = buildSmartGuides(
                            activeElement = element,
                            activeBounds = previewBounds,
                            otherElements = otherElements,
                            threshold = latestEffectiveSnapThreshold,
                        )
                        activeTargetElementIds = buildSnapTargetElementIds(
                            activeBounds = previewBounds,
                            otherElements = otherElements,
                            threshold = latestEffectiveSnapThreshold,
                        )
                        activeDistanceLabels = if (dragHandle == null) {
                            buildDistanceLabels(previewBounds, otherElements)
                        } else {
                            emptyList()
                        }
                        activeMeasurementLabel = if (dragHandle == null) {
                            DistanceLabel(
                                start = PagePoint(previewBounds.x, previewBounds.y),
                                end = PagePoint(previewBounds.x, previewBounds.y),
                                label = "X ${formatCompact(previewBounds.x)}  Y ${formatCompact(previewBounds.y)}",
                                horizontal = true,
                            )
                        } else {
                            DistanceLabel(
                                start = PagePoint(previewBounds.right, previewBounds.bottom),
                                end = PagePoint(previewBounds.right, previewBounds.bottom),
                                label = "W ${formatCompact(previewBounds.width)}  H ${formatCompact(previewBounds.height)}",
                                horizontal = true,
                            )
                        }
                        dragLatestBounds = previewBounds
                        dragLatestBoundsById = nextBoundsById
                        if (nextBoundsById.size == 1) {
                            val (id, bounds) = nextBoundsById.entries.single()
                            latestOnPreviewBounds(id, bounds)
                        } else {
                            latestOnPreviewBoundsBatch(nextBoundsById)
                        }
                        change.consume()
                    },
                    onDragEnd = {
                        if (isSelectingArea) {
                            val rect = selectionRect
                            if (rect != null) {
                                val selectedIds = latestState.document.elements
                                    .filter { it.visible && rect.intersects(GeometryService.getElementBounds(it)) }
                                    .map { it.id }
                                val nextSelection = if (latestPointerModifiers.shift) {
                                    latestState.document.selectedElementIds + selectedIds
                                } else {
                                    selectedIds
                                }
                                latestOnSetSelection(nextSelection)
                            }
                            selectionStartPage = null
                            selectionRect = null
                            isSelectingArea = false
                            return@detectDragGestures
                        }
                        val element = dragOriginalElement
                        if (element != null && isRotatingElement) {
                            dragLatestRotation?.let { rotation ->
                                latestOnCommitRotation(element, rotation)
                            }
                        } else if (element != null && dragLatestBoundsById.isNotEmpty()) {
                            if (dragOriginalElements.size == 1) {
                                dragLatestBoundsById[dragOriginalElements.first().id]?.let { bounds ->
                                    latestOnCommitBounds(dragOriginalElements.first(), bounds)
                                }
                            } else {
                                latestOnCommitBoundsBatch(dragOriginalElements, dragLatestBoundsById)
                            }
                        }
                        activeSnapGuide = null
                        activeSmartGuides = emptyList()
                        activeDistanceLabels = emptyList()
                        activePreSnapBounds = null
                        activeTargetElementIds = emptySet()
                        activeMeasurementLabel = null
                        dragOriginalElement = null
                        dragOriginalElements = emptyList()
                        dragOriginalBoundsById = emptyMap()
                        dragOriginalBounds = null
                        dragLatestBounds = null
                        dragLatestBoundsById = emptyMap()
                        dragStartPage = null
                        dragStartAngle = 0f
                        dragHandle = null
                        dragOriginalRotation = 0f
                        dragLatestRotation = null
                        isRotatingElement = false
                        isPanningPage = false
                    },
                    onDragCancel = {
                        val element = dragOriginalElement
                        if (element != null && isRotatingElement) {
                            latestOnPreviewRotation(element.id, dragOriginalRotation)
                        } else if (element != null && dragOriginalBoundsById.isNotEmpty()) {
                            if (dragOriginalBoundsById.size == 1) {
                                val (id, originalBounds) = dragOriginalBoundsById.entries.single()
                                latestOnPreviewBounds(id, originalBounds)
                            } else {
                                latestOnPreviewBoundsBatch(dragOriginalBoundsById)
                            }
                        }
                        activeSnapGuide = null
                        activeSmartGuides = emptyList()
                        activeDistanceLabels = emptyList()
                        activePreSnapBounds = null
                        activeTargetElementIds = emptySet()
                        activeMeasurementLabel = null
                        dragOriginalElement = null
                        dragOriginalElements = emptyList()
                        dragOriginalBoundsById = emptyMap()
                        dragOriginalBounds = null
                        dragLatestBounds = null
                        dragLatestBoundsById = emptyMap()
                        dragStartPage = null
                        dragStartAngle = 0f
                        dragHandle = null
                        dragOriginalRotation = 0f
                        dragLatestRotation = null
                        isRotatingElement = false
                        isPanningPage = false
                        isSelectingArea = false
                        selectionStartPage = null
                        selectionRect = null
                    },
                )
            },
    ) {
        PageBaseCanvas(
            guideSet = guideSet,
            canvas = state.canvas,
            isPreviewMode = state.isPreviewMode,
            zoom = zoom,
            pageScale = pageScale,
            renderItems = renderItems,
            modifier = Modifier.fillMaxSize(),
        )
        if (!state.isPreviewMode) {
            PageOverlayCanvas(
                selectedElements = state.selectedElements,
                primarySelectedElementId = state.document.selectedElementId,
                selectionRect = selectionRect,
                activeVerticalGuide = activeSnapGuide?.first,
                activeHorizontalGuide = activeSnapGuide?.second,
                activeSmartGuides = activeSmartGuides,
                activeDistanceLabels = activeDistanceLabels,
                activeMeasurementLabel = activeMeasurementLabel,
                activePreSnapBounds = activePreSnapBounds,
                activeTargetElements = state.document.elements.filter { it.id in activeTargetElementIds },
                snapPulse = snapPulse,
                guideSet = guideSet,
                pageScale = pageScale,
                textMeasurer = textMeasurer,
                modifier = Modifier.fillMaxSize(),
            )
        }
        contextMenuOffset?.let { offset ->
            ElementContextMenu(
                offset = offset,
                onDismiss = { contextMenuOffset = null },
                onCopy = onCopy,
                onPaste = onPaste,
                onDuplicate = onDuplicate,
                onDelete = onDeleteSelected,
                onBringToFront = onBringToFront,
                onSendToBack = onSendToBack,
                onGroup = onGroup,
                onUngroup = onUngroup,
                onLock = onLock,
                onHide = onHide,
                onAlign = onAlign,
            )
        }
    }
}
