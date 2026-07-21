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
import me.aiglez.service.domain.models.templateTableCellKey
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
internal fun RightInspectorPanel(
    state: TemplateEditorState,
    onSetCanvasMetric: (CanvasMetric, Float) -> Unit,
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
) {
    val element = state.selectedElement
    val selectedCount = state.selectedElements.size
    var inspectorPage by remember { mutableStateOf(InspectorPage.Component) }
    Surface(
        modifier = Modifier.width(336.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Inspecteur", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = inspectorPage == InspectorPage.Component,
                    onClick = { inspectorPage = InspectorPage.Component },
                    label = { Text("Composant") },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = inspectorPage == InspectorPage.PageSetup,
                    onClick = { inspectorPage = InspectorPage.PageSetup },
                    label = { Text("Mise en page") },
                    modifier = Modifier.weight(1f),
                )
            }
            if (inspectorPage == InspectorPage.PageSetup) {
                PageSetupSection(
                    canvas = state.canvas,
                    onSetCanvasMetric = onSetCanvasMetric,
                )
                return@Column
            }
            if (element == null) {
                Text("Aucune sélection", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                return@Column
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(element.type.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(
                    if (selectedCount > 1) "$selectedCount selected" else "#${element.zIndex}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            InspectorSection("Élément")
            TextFieldRow("Nom", element.name) { onUpdateCommon(CommonProperty.Name, it) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ToggleRow("Verrouillé", element.locked, { onUpdateCommon(CommonProperty.Locked, it.toString()) }, Modifier.weight(1f))
                ToggleRow("Visible", element.visible, { onUpdateCommon(CommonProperty.Visible, it.toString()) }, Modifier.weight(1f))
            }

            InspectorSection("Position")
            NumericGridRow(
                "X" to element.x,
                "Y" to element.y,
                onFirstChange = { onUpdateBounds(GeometryService.getElementBounds(element).copy(x = it)) },
                onSecondChange = { onUpdateBounds(GeometryService.getElementBounds(element).copy(y = it)) },
            )
            NumericGridRow(
                "Largeur" to element.width,
                "Hauteur" to element.height,
                onFirstChange = { onUpdateBounds(GeometryService.getElementBounds(element).copy(width = it)) },
                onSecondChange = { onUpdateBounds(GeometryService.getElementBounds(element).copy(height = it)) },
            )
            NumericGridRow(
                "Droite" to element.x + element.width,
                "Bas" to element.y + element.height,
                onFirstChange = { onUpdateBounds(GeometryService.getElementBounds(element).copy(width = it - element.x)) },
                onSecondChange = { onUpdateBounds(GeometryService.getElementBounds(element).copy(height = it - element.y)) },
            )

            InspectorSection("Disposition")
            RotationRow(
                value = element.rotation,
                onValueChange = { onUpdateCommon(CommonProperty.Rotation, it.toString()) },
            )
            SliderRow("Opacité", element.opacity, 0f..1f, { onUpdateCommon(CommonProperty.Opacity, it.toString()) })
            NumericFieldRow("Ordre d’empilement", element.zIndex.toFloat()) { onUpdateCommon(CommonProperty.ZIndex, it.toInt().toString()) }
            HorizontalDivider()
            when (element) {
                is TemplateElement.Text -> {
                    InspectorSection("Texte")
                    ExpressionTextEditor(
                        value = element.staticText.orEmpty(),
                        schema = state.schema,
                        legacyPlaceholder = element.placeholderTag,
                        onValueChange = onUpdateText,
                    )
                    DropdownRow("Police", listOf("Inter", "Sans", "Serif", "Monospace"), element.fontFamily, onUpdateTextFontFamily)
                    EnumRow("Style", TemplateTextStyle.entries.map { it.name }, element.fontStyle.name) {
                        onUpdateTextFontStyle(TemplateTextStyle.valueOf(it))
                    }
                    SliderRow("Taille de police", element.fontSize, 6f..96f, onUpdateTextFontSize)
                    SliderRow("Hauteur de ligne", element.lineHeight, 0.8f..3f, onUpdateTextLineHeight)
                    SliderRow("Espacement des lettres", element.letterSpacing, -2f..12f, onUpdateTextLetterSpacing)
                    EnumRow("Alignement du texte", listOf("left", "center", "right", "justify"), element.textAlign) {
                        onUpdateTextAlign(it)
                    }
                    EnumRow("Alignement vertical", listOf("top", "middle", "bottom"), element.verticalAlign) {
                        onUpdateTextVerticalAlign(it)
                    }
                    EnumRow("Direction", TemplateTextDirection.entries.map { it.name }, element.textDirection.name) {
                        onUpdateTextDirection(TemplateTextDirection.valueOf(it))
                    }
                    ColorFieldRow("Couleur du texte", element.color, onUpdateTextColor)
                    ColorFieldRow("Arrière-plan", element.backgroundColor, onUpdateTextBackground)
                    SliderRow("Marge intérieure", element.padding, 0f..48f, onUpdateTextPadding)
                    InspectorSection("Bordure")
                    ColorFieldRow("Couleur de bordure", element.borderColor, onUpdateTextBorderColor)
                    BorderStyleRow("Style de bordure", element.borderStyle, onUpdateTextBorderStyle)
                    SliderRow("Épaisseur de bordure", element.borderWidth, 0f..12f, onUpdateTextBorderWidth)
                    SliderRow("Rayon de bordure", element.borderRadius, 0f..48f, onUpdateTextBorderRadius)
                }
                is TemplateElement.Image -> {
                    InspectorSection("Image")
                    Button(
                        onClick = onChooseImage,
                        enabled = !state.isChoosingImage,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            when {
                                state.isChoosingImage -> "Chargement…"
                                element.sourcePath.isBlank() -> "Choisir un fichier"
                                else -> "Remplacer le fichier"
                            }
                        )
                    }
                    if (element.sourceName.isNotBlank()) {
                        Text(
                            element.sourceName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (element.intrinsicWidth > 0 && element.intrinsicHeight > 0) {
                        Text(
                            "${element.intrinsicWidth} x ${element.intrinsicHeight}px",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    EnumRow("Redimensionnement", TemplateImageContentMode.entries.map { it.name }, element.contentMode.name) {
                        onUpdateImageContentMode(TemplateImageContentMode.valueOf(it))
                    }
                    DropdownRow(
                        label = "Alignement",
                        options = TemplateImageAlignment.entries.map { it.name },
                        selected = element.alignment.name,
                        onSelect = { onUpdateImageAlignment(TemplateImageAlignment.valueOf(it)) },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onFitImageFrameToAspect,
                            enabled = element.intrinsicWidth > 0 && element.intrinsicHeight > 0,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Adapter aux proportions")
                        }
                        OutlinedButton(
                            onClick = onResizeImageToIntrinsic,
                            enabled = element.intrinsicWidth > 0 && element.intrinsicHeight > 0,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Taille d’origine")
                        }
                    }
                    ColorFieldRow("Arrière-plan", element.backgroundColor, onUpdateImageBackground)
                    InspectorSection("Bordure")
                    ColorFieldRow("Couleur de bordure", element.borderColor, onUpdateImageBorderColor)
                    SliderRow("Épaisseur de bordure", element.borderWidth, 0f..12f, onUpdateImageBorderWidth)
                    SliderRow("Rayon de bordure", element.borderRadius, 0f..72f, onUpdateImageBorderRadius)
                }
                is TemplateElement.Circle -> {
                    InspectorSection("Remplissage")
                    ColorFieldRow("Couleur de remplissage", element.fillColor, onUpdateCircleFill)
                    InspectorSection("Bordure")
                    ColorFieldRow("Couleur de bordure", element.borderColor, onUpdateCircleBorderColor)
                    BorderStyleRow("Style de bordure", element.borderStyle, onUpdateCircleBorderStyle)
                    SliderRow("Épaisseur de bordure", element.borderWidth, 0f..12f, onUpdateCircleBorderWidth)
                }
                is TemplateElement.QRCode -> {
                    InspectorSection("QR code")
                    OutlinedTextField(
                        value = element.text,
                        onValueChange = onUpdateQrText,
                        label = { Text("Texte") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 3,
                        maxLines = 5,
                    )
                    SliderRow("Zone de silence", element.quietZone.toFloat(), 0f..8f, onUpdateQrQuietZone)
                    ColorFieldRow("Premier plan", element.foregroundColor, onUpdateQrForeground)
                    ColorFieldRow("Arrière-plan", element.backgroundColor, onUpdateQrBackground)
                    InspectorSection("Bordure")
                    ColorFieldRow("Couleur de bordure", element.borderColor, onUpdateQrBorderColor)
                    SliderRow("Épaisseur de bordure", element.borderWidth, 0f..12f, onUpdateQrBorderWidth)
                }
                is TemplateElement.Barcode -> {
                    InspectorSection("Code-barres")
                    OutlinedTextField(
                        value = element.text,
                        onValueChange = onUpdateBarcodeText,
                        label = { Text("Texte") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    EnumRow("Format", TemplateBarcodeFormat.entries.map { it.name }, element.format.name) {
                        onUpdateBarcodeFormat(TemplateBarcodeFormat.valueOf(it))
                    }
                    SliderRow("Zone de silence", element.quietZone.toFloat(), 0f..40f, onUpdateBarcodeQuietZone)
                    ToggleRow("Afficher le texte", element.showText, onUpdateBarcodeShowText)
                    SliderRow("Taille du texte", element.fontSize, 6f..32f, onUpdateBarcodeFontSize)
                    ColorFieldRow("Premier plan", element.foregroundColor, onUpdateBarcodeForeground)
                    ColorFieldRow("Arrière-plan", element.backgroundColor, onUpdateBarcodeBackground)
                    InspectorSection("Bordure")
                    ColorFieldRow("Couleur de bordure", element.borderColor, onUpdateBarcodeBorderColor)
                    SliderRow("Épaisseur de bordure", element.borderWidth, 0f..12f, onUpdateBarcodeBorderWidth)
                }
                is TemplateElement.List -> {
                    val listFieldSlugs = state.schema?.fields
                        .orEmpty()
                        .filter { it.type == FieldType.LIST }
                        .map { it.slug }
                        .filter { it.isNotBlank() }
                    val fieldOptions = (listOf("Choisir un champ") + listFieldSlugs + element.fieldSlug.takeIf { it.isNotBlank() && it !in listFieldSlugs }.orEmpty())
                        .distinct()
                    InspectorSection("Données de la liste")
                    DropdownRow(
                        label = "Champ",
                        options = fieldOptions,
                        selected = element.fieldSlug.ifBlank { "Choisir un champ" },
                        onSelect = { value -> onUpdateListFieldSlug(if (value == "Choisir un champ") "" else value) },
                    )
                    if (listFieldSlugs.isEmpty()) {
                        Text(
                            "Aucun champ LIST dans ce modèle de données.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextFieldRow("Préfixe", element.prefix, onValueChange = onUpdateListPrefix)
                    TextFieldRow("Suffixe", element.suffix, onValueChange = onUpdateListSuffix)
                    TextFieldRow("Séparateur", element.itemSeparator, onValueChange = onUpdateListItemSeparator)
                    SliderRow("Nombre maximal d’éléments", element.maxItems.toFloat(), 1f..80f, onUpdateListMaxItems)
                    SliderRow("Longueur maximale", element.maxItemLength.toFloat(), 1f..240f, onUpdateListMaxItemLength)
                    InspectorSection("Mise en page")
                    SliderRow("Colonnes", element.columns.toFloat(), 1f..6f, onUpdateListColumns)
                    SliderRow("Écart entre les colonnes", element.columnGap, 0f..80f, onUpdateListColumnGap)
                    SliderRow("Espacement des éléments", element.itemSpacing, 0f..32f, onUpdateListItemSpacing)
                    SliderRow("Marge intérieure", element.padding, 0f..48f, onUpdateListPadding)
                    InspectorSection("Texte")
                    DropdownRow("Police", listOf("Inter", "Sans", "Serif", "Monospace"), element.fontFamily, onUpdateListFontFamily)
                    SliderRow("Taille de police", element.fontSize, 6f..48f, onUpdateListFontSize)
                    ColorFieldRow("Couleur du texte", element.color, onUpdateListColor)
                    ColorFieldRow("Arrière-plan", element.backgroundColor, onUpdateListBackground)
                    InspectorSection("Bordure")
                    ColorFieldRow("Couleur de bordure", element.borderColor, onUpdateListBorderColor)
                    BorderStyleRow("Style de bordure", element.borderStyle, onUpdateListBorderStyle)
                    SliderRow("Épaisseur de bordure", element.borderWidth, 0f..12f, onUpdateListBorderWidth)
                    SliderRow("Rayon de bordure", element.borderRadius, 0f..48f, onUpdateListBorderRadius)
                }
                is TemplateElement.Table -> {
                    var selectedRow by remember(element.id, element.rows) { mutableStateOf(0) }
                    var selectedColumn by remember(element.id, element.columns) { mutableStateOf(0) }
                    selectedRow = selectedRow.coerceIn(0, (element.rows - 1).coerceAtLeast(0))
                    selectedColumn = selectedColumn.coerceIn(0, (element.columns - 1).coerceAtLeast(0))
                    val cell = element.cells[templateTableCellKey(selectedRow, selectedColumn)]
                    val isHeaderCell = selectedRow < element.headerRows
                    val cellText = cell?.text ?: defaultTableCellText(selectedRow, selectedColumn, element.headerRows)
                    val cellBackground = cell?.backgroundColor
                        ?: if (isHeaderCell) element.headerBackgroundColor
                        else if (element.useAlternateRows && selectedRow % 2 == 1) element.alternateRowColor
                        else element.backgroundColor
                    val cellTextColor = cell?.color ?: if (isHeaderCell) element.headerColor else element.color

                    InspectorSection("Tableau")
                    SliderRow("Lignes", element.rows.toFloat(), 1f..40f, onUpdateTableRows)
                    SliderRow("Colonnes", element.columns.toFloat(), 1f..12f, onUpdateTableColumns)
                    SliderRow("Lignes d’en-tête", element.headerRows.toFloat(), 0f..element.rows.toFloat().coerceAtLeast(1f), onUpdateTableHeaderRows)
                    ToggleRow("Alterner les lignes", element.useAlternateRows, onUpdateTableUseAlternateRows)
                    InspectorSection("Texte")
                    DropdownRow("Police", listOf("Inter", "Sans", "Serif", "Monospace"), element.fontFamily, onUpdateTableFontFamily)
                    SliderRow("Taille de police", element.fontSize, 6f..48f, onUpdateTableFontSize)
                    EnumRow("Alignement du texte", listOf("left", "center", "right"), element.textAlign, onUpdateTableTextAlign)
                    EnumRow("Alignement vertical", listOf("top", "middle", "bottom"), element.verticalAlign, onUpdateTableVerticalAlign)
                    SliderRow("Marge intérieure", element.padding, 0f..32f, onUpdateTablePadding)
                    ColorFieldRow("Couleur du texte", element.color, onUpdateTableTextColor)
                    ColorFieldRow("Arrière-plan", element.backgroundColor, onUpdateTableBackground)
                    ColorFieldRow("Arrière-plan de l’en-tête", element.headerBackgroundColor, onUpdateTableHeaderBackground)
                    ColorFieldRow("Texte de l’en-tête", element.headerColor, onUpdateTableHeaderColor)
                    ColorFieldRow("Ligne alternée", element.alternateRowColor, onUpdateTableAlternateRowColor)
                    InspectorSection("Bordure")
                    ColorFieldRow("Couleur de bordure", element.borderColor, onUpdateTableBorderColor)
                    BorderStyleRow("Style de bordure", element.borderStyle, onUpdateTableBorderStyle)
                    SliderRow("Épaisseur de bordure", element.borderWidth, 0f..12f, onUpdateTableBorderWidth)
                    SliderRow("Rayon de bordure", element.borderRadius, 0f..48f, onUpdateTableBorderRadius)
                    ColorFieldRow("Bordure de cellule", element.cellBorderColor, onUpdateTableGridBorderColor)
                    SliderRow("Épaisseur de bordure de cellule", element.cellBorderWidth, 0f..8f, onUpdateTableGridBorderWidth)
                    InspectorSection("Cellule sélectionnée")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        DropdownRow(
                            label = "Ligne",
                            options = (0 until element.rows).map { (it + 1).toString() },
                            selected = (selectedRow + 1).toString(),
                            onSelect = { selectedRow = it.toIntOrNull()?.minus(1)?.coerceIn(0, element.rows - 1) ?: selectedRow },
                            modifier = Modifier.weight(1f),
                        )
                        DropdownRow(
                            label = "Colonne",
                            options = (0 until element.columns).map { (it + 1).toString() },
                            selected = (selectedColumn + 1).toString(),
                            onSelect = { selectedColumn = it.toIntOrNull()?.minus(1)?.coerceIn(0, element.columns - 1) ?: selectedColumn },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    ExpressionTextEditor(
                        value = cellText,
                        schema = state.schema,
                        legacyPlaceholder = null,
                        onValueChange = { onUpdateTableCellText(selectedRow, selectedColumn, it) },
                    )
                    ColorFieldRow("Arrière-plan de la cellule", cellBackground) { onUpdateTableCellBackground(selectedRow, selectedColumn, it) }
                    ColorFieldRow("Texte de la cellule", cellTextColor) { onUpdateTableCellTextColor(selectedRow, selectedColumn, it) }
                    ColorFieldRow("Bordure de la cellule", cell?.borderColor ?: element.cellBorderColor) {
                        onUpdateTableCellBorderColor(selectedRow, selectedColumn, it)
                    }
                    SliderRow("Épaisseur de bordure de la cellule", cell?.borderWidth ?: element.cellBorderWidth, 0f..8f) {
                        onUpdateTableCellBorderWidth(selectedRow, selectedColumn, it)
                    }
                    EnumRow("Alignement de la cellule", listOf("left", "center", "right"), cell?.textAlign ?: element.textAlign) {
                        onUpdateTableCellTextAlign(selectedRow, selectedColumn, it)
                    }
                    EnumRow("Alignement vertical de la cellule", listOf("top", "middle", "bottom"), cell?.verticalAlign ?: element.verticalAlign) {
                        onUpdateTableCellVerticalAlign(selectedRow, selectedColumn, it)
                    }
                    SliderRow("Marge intérieure de la cellule", cell?.padding ?: element.padding, 0f..32f) {
                        onUpdateTableCellPadding(selectedRow, selectedColumn, it)
                    }
                }
                is TemplateElement.Rectangle -> {
                    InspectorSection("Remplissage")
                    ColorFieldRow("Couleur de remplissage", element.fillColor, onUpdateRectangleFill)
                    InspectorSection("Bordure")
                    ColorFieldRow("Couleur de bordure", element.borderColor, onUpdateRectangleBorderColor)
                    BorderStyleRow("Style de bordure", element.borderStyle, onUpdateRectangleBorderStyle)
                    SliderRow("Épaisseur de bordure", element.borderWidth, 0f..12f, onUpdateRectangleBorderWidth)
                    SliderRow("Rayon de bordure", element.borderRadius, 0f..72f, onUpdateRectangleBorderRadius)
                }
                is TemplateElement.Line -> Unit
            }
            Button(
                onClick = onDeleteSelected,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Supprimer")
            }
        }
    }
}
