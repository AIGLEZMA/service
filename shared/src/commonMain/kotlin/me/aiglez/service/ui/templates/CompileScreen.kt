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
fun CompileScreen(
    templateId: String,
) {
    val viewModel: CompileViewModel = koinViewModel(
        key = "template-editor-$templateId",
        parameters = { parametersOf(templateId) },
    )
    val state by viewModel.uiState.collectAsState()
    TemplateEditorContent(
        state = state,
        onAddElement = viewModel::addElement,
        onAddDataField = viewModel::addDataField,
        onSelectElement = viewModel::selectElement,
        onSetSelection = viewModel::setSelection,
        onToggleSelection = viewModel::toggleSelection,
        onSelectSimilar = viewModel::selectSimilarObjects,
        onShowInlineEditHint = viewModel::showInlineEditHint,
        onShowShortcutMessage = viewModel::showShortcutMessage,
        onPreviewBounds = viewModel::previewBounds,
        onPreviewBoundsBatch = viewModel::previewBounds,
        onPreviewRotation = viewModel::previewRotation,
        onCommitBounds = viewModel::commitBounds,
        onCommitBoundsBatch = viewModel::commitBounds,
        onCommitRotation = viewModel::commitRotation,
        onUpdateBounds = viewModel::updateSelectedBounds,
        onUpdateCommon = viewModel::updateSelectedCommonProperty,
        onUpdateText = viewModel::updateSelectedText,
        onUpdateTextColor = viewModel::updateSelectedTextColor,
        onUpdateTextFontSize = viewModel::updateSelectedTextFontSize,
        onUpdateTextFontFamily = viewModel::updateSelectedTextFontFamily,
        onUpdateTextFontStyle = viewModel::updateSelectedTextFontStyle,
        onUpdateTextLineHeight = viewModel::updateSelectedTextLineHeight,
        onUpdateTextLetterSpacing = viewModel::updateSelectedTextLetterSpacing,
        onUpdateTextAlign = viewModel::updateSelectedTextAlign,
        onUpdateTextVerticalAlign = viewModel::updateSelectedTextVerticalAlign,
        onUpdateTextDirection = viewModel::updateSelectedTextDirection,
        onUpdateTextBackground = viewModel::updateSelectedTextBackground,
        onUpdateTextPadding = viewModel::updateSelectedTextPadding,
        onUpdateTextBorderColor = viewModel::updateSelectedTextBorderColor,
        onUpdateTextBorderWidth = viewModel::updateSelectedTextBorderWidth,
        onUpdateTextBorderStyle = viewModel::updateSelectedTextBorderStyle,
        onUpdateTextBorderRadius = viewModel::updateSelectedTextBorderRadius,
        onUpdateRectangleFill = viewModel::updateSelectedRectangleFill,
        onUpdateRectangleBorderColor = viewModel::updateSelectedRectangleBorderColor,
        onUpdateRectangleBorderWidth = viewModel::updateSelectedRectangleBorderWidth,
        onUpdateRectangleBorderStyle = viewModel::updateSelectedRectangleBorderStyle,
        onUpdateRectangleBorderRadius = viewModel::updateSelectedRectangleBorderRadius,
        onUpdateCircleFill = viewModel::updateSelectedCircleFill,
        onUpdateCircleBorderColor = viewModel::updateSelectedCircleBorderColor,
        onUpdateCircleBorderWidth = viewModel::updateSelectedCircleBorderWidth,
        onUpdateCircleBorderStyle = viewModel::updateSelectedCircleBorderStyle,
        onChooseImage = viewModel::chooseImageForSelected,
        onResizeImageToIntrinsic = viewModel::resizeSelectedImageToIntrinsic,
        onFitImageFrameToAspect = viewModel::fitSelectedImageFrameToAspect,
        onUpdateImageContentMode = viewModel::updateSelectedImageContentMode,
        onUpdateImageAlignment = viewModel::updateSelectedImageAlignment,
        onUpdateImageBackground = viewModel::updateSelectedImageBackground,
        onUpdateImageBorderColor = viewModel::updateSelectedImageBorderColor,
        onUpdateImageBorderWidth = viewModel::updateSelectedImageBorderWidth,
        onUpdateImageBorderRadius = viewModel::updateSelectedImageBorderRadius,
        onUpdateQrText = viewModel::updateSelectedQrText,
        onUpdateQrForeground = viewModel::updateSelectedQrForeground,
        onUpdateQrBackground = viewModel::updateSelectedQrBackground,
        onUpdateQrQuietZone = viewModel::updateSelectedQrQuietZone,
        onUpdateQrBorderColor = viewModel::updateSelectedQrBorderColor,
        onUpdateQrBorderWidth = viewModel::updateSelectedQrBorderWidth,
        onUpdateBarcodeText = viewModel::updateSelectedBarcodeText,
        onUpdateBarcodeFormat = viewModel::updateSelectedBarcodeFormat,
        onUpdateBarcodeForeground = viewModel::updateSelectedBarcodeForeground,
        onUpdateBarcodeBackground = viewModel::updateSelectedBarcodeBackground,
        onUpdateBarcodeQuietZone = viewModel::updateSelectedBarcodeQuietZone,
        onUpdateBarcodeShowText = viewModel::updateSelectedBarcodeShowText,
        onUpdateBarcodeFontSize = viewModel::updateSelectedBarcodeFontSize,
        onUpdateBarcodeBorderColor = viewModel::updateSelectedBarcodeBorderColor,
        onUpdateBarcodeBorderWidth = viewModel::updateSelectedBarcodeBorderWidth,
        onUpdateListFieldSlug = viewModel::updateSelectedListFieldSlug,
        onUpdateListPrefix = viewModel::updateSelectedListPrefix,
        onUpdateListSuffix = viewModel::updateSelectedListSuffix,
        onUpdateListItemSeparator = viewModel::updateSelectedListItemSeparator,
        onUpdateListMaxItems = viewModel::updateSelectedListMaxItems,
        onUpdateListMaxItemLength = viewModel::updateSelectedListMaxItemLength,
        onUpdateListColumns = viewModel::updateSelectedListColumns,
        onUpdateListColumnGap = viewModel::updateSelectedListColumnGap,
        onUpdateListItemSpacing = viewModel::updateSelectedListItemSpacing,
        onUpdateListPadding = viewModel::updateSelectedListPadding,
        onUpdateListFontFamily = viewModel::updateSelectedListFontFamily,
        onUpdateListFontSize = viewModel::updateSelectedListFontSize,
        onUpdateListColor = viewModel::updateSelectedListColor,
        onUpdateListBackground = viewModel::updateSelectedListBackground,
        onUpdateListBorderColor = viewModel::updateSelectedListBorderColor,
        onUpdateListBorderWidth = viewModel::updateSelectedListBorderWidth,
        onUpdateListBorderStyle = viewModel::updateSelectedListBorderStyle,
        onUpdateListBorderRadius = viewModel::updateSelectedListBorderRadius,
        onUpdateTableRows = viewModel::updateSelectedTableRows,
        onUpdateTableColumns = viewModel::updateSelectedTableColumns,
        onUpdateTableHeaderRows = viewModel::updateSelectedTableHeaderRows,
        onUpdateTableFontFamily = viewModel::updateSelectedTableFontFamily,
        onUpdateTableFontSize = viewModel::updateSelectedTableFontSize,
        onUpdateTableTextColor = viewModel::updateSelectedTableTextColor,
        onUpdateTableBackground = viewModel::updateSelectedTableBackground,
        onUpdateTableHeaderBackground = viewModel::updateSelectedTableHeaderBackground,
        onUpdateTableHeaderColor = viewModel::updateSelectedTableHeaderColor,
        onUpdateTableAlternateRowColor = viewModel::updateSelectedTableAlternateRowColor,
        onUpdateTableUseAlternateRows = viewModel::updateSelectedTableUseAlternateRows,
        onUpdateTableTextAlign = viewModel::updateSelectedTableTextAlign,
        onUpdateTableVerticalAlign = viewModel::updateSelectedTableVerticalAlign,
        onUpdateTablePadding = viewModel::updateSelectedTablePadding,
        onUpdateTableBorderColor = viewModel::updateSelectedTableBorderColor,
        onUpdateTableBorderWidth = viewModel::updateSelectedTableBorderWidth,
        onUpdateTableBorderStyle = viewModel::updateSelectedTableBorderStyle,
        onUpdateTableBorderRadius = viewModel::updateSelectedTableBorderRadius,
        onUpdateTableGridBorderColor = viewModel::updateSelectedTableGridBorderColor,
        onUpdateTableGridBorderWidth = viewModel::updateSelectedTableGridBorderWidth,
        onUpdateTableCellText = viewModel::updateSelectedTableCellText,
        onUpdateTableCellBackground = viewModel::updateSelectedTableCellBackground,
        onUpdateTableCellTextColor = viewModel::updateSelectedTableCellTextColor,
        onUpdateTableCellBorderColor = viewModel::updateSelectedTableCellBorderColor,
        onUpdateTableCellBorderWidth = viewModel::updateSelectedTableCellBorderWidth,
        onUpdateTableCellTextAlign = viewModel::updateSelectedTableCellTextAlign,
        onUpdateTableCellVerticalAlign = viewModel::updateSelectedTableCellVerticalAlign,
        onUpdateTableCellPadding = viewModel::updateSelectedTableCellPadding,
        onDeleteSelected = viewModel::deleteSelected,
        onCopy = viewModel::copySelected,
        onPaste = viewModel::pasteClipboard,
        onPasteInPlace = viewModel::pasteClipboardInPlace,
        onDuplicate = viewModel::duplicateSelected,
        onGroup = viewModel::groupSelected,
        onUngroup = viewModel::ungroupSelected,
        onLock = viewModel::lockSelected,
        onHide = viewModel::hideSelected,
        onBringToFront = viewModel::bringSelectedToFront,
        onSendToBack = viewModel::sendSelectedToBack,
        onAlign = viewModel::alignSelected,
        onDistribute = viewModel::distributeSelected,
        onMatchSize = viewModel::matchSelectedSize,
        onUndo = viewModel::undo,
        onRedo = viewModel::redo,
        onSave = viewModel::saveTemplate,
        onExportPdf = viewModel::exportPdf,
        onPreviewButtonClick = viewModel::togglePreviewMode,
        onToggleSampleData = viewModel::toggleSampleData,
        onPreviewDialogDismiss = viewModel::closePreviewDialog,
        onPreviewRecordSelected = viewModel::selectPreviewRecord,
        onShowPreview = viewModel::showPreview,
        onSetZoom = viewModel::setZoom,
        onSetRulerUnit = viewModel::setRulerUnit,
        onToggleSnap = viewModel::toggleSnap,
        onSetNudgeDistance = viewModel::setNudgeDistance,
        onSetCanvasMetric = viewModel::setCanvasMetric,
        onNudgeSelected = viewModel::nudgeSelected,
        onRotateSelected = viewModel::rotateSelectedBy,
        onToggleSelectedTextBold = viewModel::toggleSelectedTextBold,
        onToggleSelectedTextItalic = viewModel::toggleSelectedTextItalic,
        onToggleSelectedTextUnderline = viewModel::toggleSelectedTextUnderline,
        onAdjustSelectedTextFontSize = viewModel::adjustSelectedTextFontSize,
        onAlignSelectedText = viewModel::alignSelectedText,
    )
}

@Composable
internal fun TemplateEditorContent(
    state: TemplateEditorState,
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
    onDistribute: (DistributionAxis) -> Unit,
    onMatchSize: (SizeMatchAxis) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onExportPdf: () -> Unit,
    onPreviewButtonClick: () -> Unit,
    onToggleSampleData: () -> Unit,
    onPreviewDialogDismiss: () -> Unit,
    onPreviewRecordSelected: (String, String) -> Unit,
    onShowPreview: () -> Unit,
    onSetZoom: (Float) -> Unit,
    onSetRulerUnit: (String) -> Unit,
    onToggleSnap: () -> Unit,
    onSetNudgeDistance: (Float) -> Unit,
    onSetCanvasMetric: (CanvasMetric, Float) -> Unit,
    onNudgeSelected: (Float, Float) -> Unit,
    onRotateSelected: (Float) -> Unit,
    onToggleSelectedTextBold: () -> Unit,
    onToggleSelectedTextItalic: () -> Unit,
    onToggleSelectedTextUnderline: () -> Unit,
    onAdjustSelectedTextFontSize: (Float) -> Unit,
    onAlignSelectedText: (String) -> Unit,
) {
    var paletteDrop by remember { mutableStateOf<PaletteDrop?>(null) }
    var dropSequence by remember { mutableStateOf(0) }
    var zoomCommand by remember { mutableStateOf<ZoomCommand?>(null) }
    Column(modifier = Modifier.fillMaxSize()) {
        TopEditorToolbar(
            state = state,
            onUndo = onUndo,
            onRedo = onRedo,
            onSave = onSave,
            onExportPdf = onExportPdf,
            onPreviewButtonClick = onPreviewButtonClick,
            onToggleSampleData = onToggleSampleData,
            onSetZoom = onSetZoom,
            onSetRulerUnit = onSetRulerUnit,
            onToggleSnap = onToggleSnap,
            onSetNudgeDistance = onSetNudgeDistance,
            onSetCanvasMetric = onSetCanvasMetric,
            onZoomCommand = { zoomCommand = it },
            onSelectSimilar = onSelectSimilar,
            onCopy = onCopy,
            onPaste = onPaste,
            onDuplicate = onDuplicate,
            onDeleteSelected = onDeleteSelected,
            onGroup = onGroup,
            onUngroup = onUngroup,
            onLock = onLock,
            onHide = onHide,
            onAlign = onAlign,
            onDistribute = onDistribute,
            onMatchSize = onMatchSize,
        )
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LeftComponentPalette(
                state = state,
                onAddElement = onAddElement,
                onAddDataField = onAddDataField,
                onDropElement = { type, windowPosition ->
                    dropSequence += 1
                    paletteDrop = PaletteDrop.Component(type, windowPosition, dropSequence)
                },
                onDropDataField = { schemaName, slug, name, windowPosition ->
                    dropSequence += 1
                    paletteDrop = PaletteDrop.DataField(schemaName, slug, name, windowPosition, dropSequence)
                },
            )
            CanvasWorkspace(
                state = state,
                zoomCommand = zoomCommand,
                onConsumeZoomCommand = { zoomCommand = null },
                paletteDrop = paletteDrop,
                onConsumePaletteDrop = { paletteDrop = null },
                onAddElement = onAddElement,
                onAddDataField = onAddDataField,
                onSelectElement = onSelectElement,
                onSetSelection = onSetSelection,
                onToggleSelection = onToggleSelection,
                onSelectSimilar = onSelectSimilar,
                onShowInlineEditHint = onShowInlineEditHint,
                onShowShortcutMessage = onShowShortcutMessage,
                onPreviewBounds = onPreviewBounds,
                onPreviewBoundsBatch = onPreviewBoundsBatch,
                onPreviewRotation = onPreviewRotation,
                onCommitBounds = onCommitBounds,
                onCommitBoundsBatch = onCommitBoundsBatch,
                onCommitRotation = onCommitRotation,
                onDeleteSelected = onDeleteSelected,
                onCopy = onCopy,
                onPaste = onPaste,
                onPasteInPlace = onPasteInPlace,
                onDuplicate = onDuplicate,
                onGroup = onGroup,
                onUngroup = onUngroup,
                onLock = onLock,
                onHide = onHide,
                onBringToFront = onBringToFront,
                onSendToBack = onSendToBack,
                onAlign = onAlign,
                onUndo = onUndo,
                onRedo = onRedo,
                onTogglePreviewMode = onPreviewButtonClick,
                onSetZoom = onSetZoom,
                onSetCanvasMetric = onSetCanvasMetric,
                onZoomCommand = { zoomCommand = it },
                onNudgeSelected = onNudgeSelected,
                onRotateSelected = onRotateSelected,
                onToggleSelectedTextBold = onToggleSelectedTextBold,
                onToggleSelectedTextItalic = onToggleSelectedTextItalic,
                onToggleSelectedTextUnderline = onToggleSelectedTextUnderline,
                onAdjustSelectedTextFontSize = onAdjustSelectedTextFontSize,
                onAlignSelectedText = onAlignSelectedText,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            RightInspectorPanel(
                state = state,
                onSetCanvasMetric = onSetCanvasMetric,
                onUpdateBounds = onUpdateBounds,
                onUpdateCommon = onUpdateCommon,
                onUpdateText = onUpdateText,
                onUpdateTextColor = onUpdateTextColor,
                onUpdateTextFontSize = onUpdateTextFontSize,
                onUpdateTextFontFamily = onUpdateTextFontFamily,
                onUpdateTextFontStyle = onUpdateTextFontStyle,
                onUpdateTextLineHeight = onUpdateTextLineHeight,
                onUpdateTextLetterSpacing = onUpdateTextLetterSpacing,
                onUpdateTextAlign = onUpdateTextAlign,
                onUpdateTextVerticalAlign = onUpdateTextVerticalAlign,
                onUpdateTextDirection = onUpdateTextDirection,
                onUpdateTextBackground = onUpdateTextBackground,
                onUpdateTextPadding = onUpdateTextPadding,
                onUpdateTextBorderColor = onUpdateTextBorderColor,
                onUpdateTextBorderWidth = onUpdateTextBorderWidth,
                onUpdateTextBorderStyle = onUpdateTextBorderStyle,
                onUpdateTextBorderRadius = onUpdateTextBorderRadius,
                onUpdateRectangleFill = onUpdateRectangleFill,
                onUpdateRectangleBorderColor = onUpdateRectangleBorderColor,
                onUpdateRectangleBorderWidth = onUpdateRectangleBorderWidth,
                onUpdateRectangleBorderStyle = onUpdateRectangleBorderStyle,
                onUpdateRectangleBorderRadius = onUpdateRectangleBorderRadius,
                onUpdateCircleFill = onUpdateCircleFill,
                onUpdateCircleBorderColor = onUpdateCircleBorderColor,
                onUpdateCircleBorderWidth = onUpdateCircleBorderWidth,
                onUpdateCircleBorderStyle = onUpdateCircleBorderStyle,
                onChooseImage = onChooseImage,
                onResizeImageToIntrinsic = onResizeImageToIntrinsic,
                onFitImageFrameToAspect = onFitImageFrameToAspect,
                onUpdateImageContentMode = onUpdateImageContentMode,
                onUpdateImageAlignment = onUpdateImageAlignment,
                onUpdateImageBackground = onUpdateImageBackground,
                onUpdateImageBorderColor = onUpdateImageBorderColor,
                onUpdateImageBorderWidth = onUpdateImageBorderWidth,
                onUpdateImageBorderRadius = onUpdateImageBorderRadius,
                onUpdateQrText = onUpdateQrText,
                onUpdateQrForeground = onUpdateQrForeground,
                onUpdateQrBackground = onUpdateQrBackground,
                onUpdateQrQuietZone = onUpdateQrQuietZone,
                onUpdateQrBorderColor = onUpdateQrBorderColor,
                onUpdateQrBorderWidth = onUpdateQrBorderWidth,
                onUpdateBarcodeText = onUpdateBarcodeText,
                onUpdateBarcodeFormat = onUpdateBarcodeFormat,
                onUpdateBarcodeForeground = onUpdateBarcodeForeground,
                onUpdateBarcodeBackground = onUpdateBarcodeBackground,
                onUpdateBarcodeQuietZone = onUpdateBarcodeQuietZone,
                onUpdateBarcodeShowText = onUpdateBarcodeShowText,
                onUpdateBarcodeFontSize = onUpdateBarcodeFontSize,
                onUpdateBarcodeBorderColor = onUpdateBarcodeBorderColor,
                onUpdateBarcodeBorderWidth = onUpdateBarcodeBorderWidth,
                onUpdateListFieldSlug = onUpdateListFieldSlug,
                onUpdateListPrefix = onUpdateListPrefix,
                onUpdateListSuffix = onUpdateListSuffix,
                onUpdateListItemSeparator = onUpdateListItemSeparator,
                onUpdateListMaxItems = onUpdateListMaxItems,
                onUpdateListMaxItemLength = onUpdateListMaxItemLength,
                onUpdateListColumns = onUpdateListColumns,
                onUpdateListColumnGap = onUpdateListColumnGap,
                onUpdateListItemSpacing = onUpdateListItemSpacing,
                onUpdateListPadding = onUpdateListPadding,
                onUpdateListFontFamily = onUpdateListFontFamily,
                onUpdateListFontSize = onUpdateListFontSize,
                onUpdateListColor = onUpdateListColor,
                onUpdateListBackground = onUpdateListBackground,
                onUpdateListBorderColor = onUpdateListBorderColor,
                onUpdateListBorderWidth = onUpdateListBorderWidth,
                onUpdateListBorderStyle = onUpdateListBorderStyle,
                onUpdateListBorderRadius = onUpdateListBorderRadius,
                onUpdateTableRows = onUpdateTableRows,
                onUpdateTableColumns = onUpdateTableColumns,
                onUpdateTableHeaderRows = onUpdateTableHeaderRows,
                onUpdateTableFontFamily = onUpdateTableFontFamily,
                onUpdateTableFontSize = onUpdateTableFontSize,
                onUpdateTableTextColor = onUpdateTableTextColor,
                onUpdateTableBackground = onUpdateTableBackground,
                onUpdateTableHeaderBackground = onUpdateTableHeaderBackground,
                onUpdateTableHeaderColor = onUpdateTableHeaderColor,
                onUpdateTableAlternateRowColor = onUpdateTableAlternateRowColor,
                onUpdateTableUseAlternateRows = onUpdateTableUseAlternateRows,
                onUpdateTableTextAlign = onUpdateTableTextAlign,
                onUpdateTableVerticalAlign = onUpdateTableVerticalAlign,
                onUpdateTablePadding = onUpdateTablePadding,
                onUpdateTableBorderColor = onUpdateTableBorderColor,
                onUpdateTableBorderWidth = onUpdateTableBorderWidth,
                onUpdateTableBorderStyle = onUpdateTableBorderStyle,
                onUpdateTableBorderRadius = onUpdateTableBorderRadius,
                onUpdateTableGridBorderColor = onUpdateTableGridBorderColor,
                onUpdateTableGridBorderWidth = onUpdateTableGridBorderWidth,
                onUpdateTableCellText = onUpdateTableCellText,
                onUpdateTableCellBackground = onUpdateTableCellBackground,
                onUpdateTableCellTextColor = onUpdateTableCellTextColor,
                onUpdateTableCellBorderColor = onUpdateTableCellBorderColor,
                onUpdateTableCellBorderWidth = onUpdateTableCellBorderWidth,
                onUpdateTableCellTextAlign = onUpdateTableCellTextAlign,
                onUpdateTableCellVerticalAlign = onUpdateTableCellVerticalAlign,
                onUpdateTableCellPadding = onUpdateTableCellPadding,
                onDeleteSelected = onDeleteSelected,
            )
        }
        EditorStatusBar(state = state)
    }
    if (state.isPreviewDialogOpen) {
        PreviewRecordDialog(
            state = state,
            onDismiss = onPreviewDialogDismiss,
            onRecordSelected = onPreviewRecordSelected,
            onShowPreview = onShowPreview,
        )
    }
}

