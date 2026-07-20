package me.aiglez.service.ui.templates

import kotlin.math.roundToInt
import me.aiglez.service.domain.models.TemplateBarcodeFormat
import me.aiglez.service.domain.models.TemplateBorderStyle
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.models.TemplateImageAlignment
import me.aiglez.service.domain.models.TemplateImageContentMode
import me.aiglez.service.domain.models.TemplateTextDirection
import me.aiglez.service.domain.models.TemplateTextStyle
import me.aiglez.service.ui.templates.editor.CommonProperty
import me.aiglez.service.ui.templates.editor.EditorCommand
import me.aiglez.service.ui.templates.editor.GeometryService
import me.aiglez.service.ui.templates.editor.PageRect
import me.aiglez.service.ui.templates.editor.ReplaceElementCommand
import me.aiglez.service.ui.templates.editor.withBarcodeBackground
import me.aiglez.service.ui.templates.editor.withBarcodeBorderColor
import me.aiglez.service.ui.templates.editor.withBarcodeBorderWidth
import me.aiglez.service.ui.templates.editor.withBarcodeFontSize
import me.aiglez.service.ui.templates.editor.withBarcodeForeground
import me.aiglez.service.ui.templates.editor.withBarcodeFormat
import me.aiglez.service.ui.templates.editor.withBarcodeQuietZone
import me.aiglez.service.ui.templates.editor.withBarcodeShowText
import me.aiglez.service.ui.templates.editor.withBarcodeText
import me.aiglez.service.ui.templates.editor.withBounds
import me.aiglez.service.ui.templates.editor.withCircleBorderColor
import me.aiglez.service.ui.templates.editor.withCircleBorderStyle
import me.aiglez.service.ui.templates.editor.withCircleBorderWidth
import me.aiglez.service.ui.templates.editor.withCircleFill
import me.aiglez.service.ui.templates.editor.withCommonProperty
import me.aiglez.service.ui.templates.editor.withImageAlignment
import me.aiglez.service.ui.templates.editor.withImageBackground
import me.aiglez.service.ui.templates.editor.withImageBorderColor
import me.aiglez.service.ui.templates.editor.withImageBorderRadius
import me.aiglez.service.ui.templates.editor.withImageBorderWidth
import me.aiglez.service.ui.templates.editor.withImageContentMode
import me.aiglez.service.ui.templates.editor.withImageSource
import me.aiglez.service.ui.templates.editor.withListBackground
import me.aiglez.service.ui.templates.editor.withListBorderColor
import me.aiglez.service.ui.templates.editor.withListBorderRadius
import me.aiglez.service.ui.templates.editor.withListBorderStyle
import me.aiglez.service.ui.templates.editor.withListBorderWidth
import me.aiglez.service.ui.templates.editor.withListColor
import me.aiglez.service.ui.templates.editor.withListColumnGap
import me.aiglez.service.ui.templates.editor.withListColumns
import me.aiglez.service.ui.templates.editor.withListFieldSlug
import me.aiglez.service.ui.templates.editor.withListFontFamily
import me.aiglez.service.ui.templates.editor.withListFontSize
import me.aiglez.service.ui.templates.editor.withListItemSeparator
import me.aiglez.service.ui.templates.editor.withListItemSpacing
import me.aiglez.service.ui.templates.editor.withListMaxItemLength
import me.aiglez.service.ui.templates.editor.withListMaxItems
import me.aiglez.service.ui.templates.editor.withListPadding
import me.aiglez.service.ui.templates.editor.withListPrefix
import me.aiglez.service.ui.templates.editor.withListSuffix
import me.aiglez.service.ui.templates.editor.withQrBackground
import me.aiglez.service.ui.templates.editor.withQrBorderColor
import me.aiglez.service.ui.templates.editor.withQrBorderWidth
import me.aiglez.service.ui.templates.editor.withQrForeground
import me.aiglez.service.ui.templates.editor.withQrQuietZone
import me.aiglez.service.ui.templates.editor.withQrText
import me.aiglez.service.ui.templates.editor.withRectangleBorderColor
import me.aiglez.service.ui.templates.editor.withRectangleBorderRadius
import me.aiglez.service.ui.templates.editor.withRectangleBorderStyle
import me.aiglez.service.ui.templates.editor.withRectangleBorderWidth
import me.aiglez.service.ui.templates.editor.withRectangleFill
import me.aiglez.service.ui.templates.editor.withTableAlternateRowColor
import me.aiglez.service.ui.templates.editor.withTableBackground
import me.aiglez.service.ui.templates.editor.withTableBorderColor
import me.aiglez.service.ui.templates.editor.withTableBorderRadius
import me.aiglez.service.ui.templates.editor.withTableBorderStyle
import me.aiglez.service.ui.templates.editor.withTableBorderWidth
import me.aiglez.service.ui.templates.editor.withTableCellBackground
import me.aiglez.service.ui.templates.editor.withTableCellBorderColor
import me.aiglez.service.ui.templates.editor.withTableCellBorderWidth
import me.aiglez.service.ui.templates.editor.withTableCellPadding
import me.aiglez.service.ui.templates.editor.withTableCellText
import me.aiglez.service.ui.templates.editor.withTableCellTextAlign
import me.aiglez.service.ui.templates.editor.withTableCellTextColor
import me.aiglez.service.ui.templates.editor.withTableCellVerticalAlign
import me.aiglez.service.ui.templates.editor.withTableColumns
import me.aiglez.service.ui.templates.editor.withTableFontFamily
import me.aiglez.service.ui.templates.editor.withTableFontSize
import me.aiglez.service.ui.templates.editor.withTableHeaderBackground
import me.aiglez.service.ui.templates.editor.withTableHeaderColor
import me.aiglez.service.ui.templates.editor.withTableHeaderRows
import me.aiglez.service.ui.templates.editor.withTablePadding
import me.aiglez.service.ui.templates.editor.withTableRows
import me.aiglez.service.ui.templates.editor.withTableTextAlign
import me.aiglez.service.ui.templates.editor.withTableTextColor
import me.aiglez.service.ui.templates.editor.withTableUseAlternateRows
import me.aiglez.service.ui.templates.editor.withTableVerticalAlign
import me.aiglez.service.ui.templates.editor.withTextAlign
import me.aiglez.service.ui.templates.editor.withTextBackground
import me.aiglez.service.ui.templates.editor.withTextBorderColor
import me.aiglez.service.ui.templates.editor.withTextBorderRadius
import me.aiglez.service.ui.templates.editor.withTextBorderStyle
import me.aiglez.service.ui.templates.editor.withTextBorderWidth
import me.aiglez.service.ui.templates.editor.withTextColor
import me.aiglez.service.ui.templates.editor.withTextDirection
import me.aiglez.service.ui.templates.editor.withTextFontFamily
import me.aiglez.service.ui.templates.editor.withTextFontSize
import me.aiglez.service.ui.templates.editor.withTextFontStyle
import me.aiglez.service.ui.templates.editor.withTextLetterSpacing
import me.aiglez.service.ui.templates.editor.withTextLineHeight
import me.aiglez.service.ui.templates.editor.withTextPadding
import me.aiglez.service.ui.templates.editor.withTextValue
import me.aiglez.service.ui.templates.editor.withTextVerticalAlign

internal class TemplateEditorPropertyActions(
    private val selectedElement: () -> TemplateElement?,
    private val executeCommand: (EditorCommand) -> Unit,
    private val replaceSelection: ((TemplateElement, Int) -> TemplateElement) -> Unit,
) {
    private fun execute(command: EditorCommand) = executeCommand(command)

    private fun replaceSelected(transform: (TemplateElement, Int) -> TemplateElement) {
        replaceSelection(transform)
    }

    private fun replaceSelectedIfChanged(transform: (TemplateElement) -> TemplateElement) {
        val element = selectedElement() ?: return
        val nextElement = transform(element)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBounds(bounds: PageRect) {
        val element = selectedElement() ?: return
        if (element.locked) return
        val nextElement = element.withBounds(bounds.normalized())
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedCommonProperty(property: CommonProperty, value: String) {
        replaceSelectedIfChanged { it.withCommonProperty(property, value) }
    }

    fun updateSelectedText(value: String) {
        replaceSelectedIfChanged { it.withTextValue(value) }
    }

    fun updateSelectedTextColor(value: String) {
        replaceSelectedIfChanged { it.withTextColor(value) }
    }

    fun updateSelectedTextFontSize(value: Float) {
        replaceSelectedIfChanged { it.withTextFontSize(value) }
    }

    fun adjustSelectedTextFontSize(delta: Float) {
        replaceSelected { element, _ ->
            when (element) {
                is TemplateElement.Text -> element.copy(fontSize = (element.fontSize + delta).coerceAtLeast(1f))
                else -> element
            }
        }
    }

    fun updateSelectedTextFontFamily(value: String) {
        replaceSelectedIfChanged { it.withTextFontFamily(value) }
    }

    fun updateSelectedTextFontStyle(value: TemplateTextStyle) {
        replaceSelectedIfChanged { it.withTextFontStyle(value) }
    }

    fun toggleSelectedTextBold() {
        replaceSelected { element, _ ->
            when (element) {
                is TemplateElement.Text -> element.copy(fontWeight = if (element.fontWeight >= 600) 400 else 700)
                else -> element
            }
        }
    }

    fun toggleSelectedTextItalic() {
        replaceSelected { element, _ ->
            when (element) {
                is TemplateElement.Text -> {
                    val italic = element.fontStyle != TemplateTextStyle.Italic
                    element.copy(
                        fontStyle = if (italic) TemplateTextStyle.Italic else TemplateTextStyle.Normal,
                        italic = italic,
                    )
                }
                else -> element
            }
        }
    }

    fun toggleSelectedTextUnderline() {
        replaceSelected { element, _ ->
            when (element) {
                is TemplateElement.Text -> element.copy(underline = !element.underline)
                else -> element
            }
        }
    }

    fun updateSelectedTextLineHeight(value: Float) {
        replaceSelectedIfChanged { it.withTextLineHeight(value) }
    }

    fun updateSelectedTextLetterSpacing(value: Float) {
        replaceSelectedIfChanged { it.withTextLetterSpacing(value) }
    }

    fun updateSelectedTextVerticalAlign(value: String) {
        replaceSelectedIfChanged { it.withTextVerticalAlign(value) }
    }

    fun updateSelectedTextAlign(value: String) {
        replaceSelectedIfChanged { it.withTextAlign(value) }
    }

    fun alignSelectedText(value: String) {
        replaceSelected { element, _ ->
            when (element) {
                is TemplateElement.Text -> element.copy(textAlign = value)
                else -> element
            }
        }
    }

    fun updateSelectedTextDirection(value: TemplateTextDirection) {
        replaceSelectedIfChanged { it.withTextDirection(value) }
    }

    fun updateSelectedTextBackground(value: String) {
        replaceSelectedIfChanged { it.withTextBackground(value) }
    }

    fun updateSelectedTextPadding(value: Float) {
        replaceSelectedIfChanged { it.withTextPadding(value) }
    }

    fun updateSelectedTextBorderColor(value: String) {
        replaceSelectedIfChanged { it.withTextBorderColor(value) }
    }

    fun updateSelectedTextBorderWidth(value: Float) {
        replaceSelectedIfChanged { it.withTextBorderWidth(value) }
    }

    fun updateSelectedTextBorderStyle(value: TemplateBorderStyle) {
        replaceSelectedIfChanged { it.withTextBorderStyle(value) }
    }

    fun updateSelectedTextBorderRadius(value: Float) {
        replaceSelectedIfChanged { it.withTextBorderRadius(value) }
    }

    fun updateSelectedRectangleFill(value: String) {
        replaceSelectedIfChanged { it.withRectangleFill(value) }
    }

    fun updateSelectedRectangleBorderColor(value: String) {
        replaceSelectedIfChanged { it.withRectangleBorderColor(value) }
    }

    fun updateSelectedRectangleBorderWidth(value: Float) {
        replaceSelectedIfChanged { it.withRectangleBorderWidth(value) }
    }

    fun updateSelectedRectangleBorderStyle(value: TemplateBorderStyle) {
        replaceSelectedIfChanged { it.withRectangleBorderStyle(value) }
    }

    fun updateSelectedRectangleBorderRadius(value: Float) {
        replaceSelectedIfChanged { it.withRectangleBorderRadius(value) }
    }

    fun updateSelectedCircleFill(value: String) {
        replaceSelectedIfChanged { it.withCircleFill(value) }
    }

    fun updateSelectedCircleBorderColor(value: String) {
        replaceSelectedIfChanged { it.withCircleBorderColor(value) }
    }

    fun updateSelectedCircleBorderWidth(value: Float) {
        replaceSelectedIfChanged { it.withCircleBorderWidth(value) }
    }

    fun updateSelectedCircleBorderStyle(value: TemplateBorderStyle) {
        replaceSelectedIfChanged { it.withCircleBorderStyle(value) }
    }

    fun chooseImageForSelected() {
        val element = selectedElement() as? TemplateElement.Image ?: return
        val imageFile = chooseTemplateImageFile() ?: return
        val nextSource = element.withImageSource(
            path = imageFile.path,
            name = imageFile.name,
            intrinsicWidth = imageFile.width,
            intrinsicHeight = imageFile.height,
        )
        val nextElement = if (
            element.sourcePath.isBlank() &&
            imageFile.width > 0 &&
            imageFile.height > 0 &&
            element.width == 180f &&
            element.height == 120f
        ) {
            val aspect = imageFile.width.toFloat() / imageFile.height.toFloat()
            nextSource.withBounds(
                GeometryService.getElementBounds(element).copy(
                    height = (element.width / aspect).coerceAtLeast(GeometryService.MinElementSize),
                ),
            )
        } else {
            nextSource
        }
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun resizeSelectedImageToIntrinsic() {
        val element = selectedElement() as? TemplateElement.Image ?: return
        if (element.intrinsicWidth <= 0 || element.intrinsicHeight <= 0) return
        val nextElement = element.withBounds(
            GeometryService.getElementBounds(element).copy(
                width = element.intrinsicWidth.toFloat(),
                height = element.intrinsicHeight.toFloat(),
            ).normalized(),
        )
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun fitSelectedImageFrameToAspect() {
        val element = selectedElement() as? TemplateElement.Image ?: return
        if (element.intrinsicWidth <= 0 || element.intrinsicHeight <= 0) return
        val aspect = element.intrinsicWidth.toFloat() / element.intrinsicHeight.toFloat()
        val bounds = GeometryService.getElementBounds(element)
        val nextElement = element.withBounds(
            bounds.copy(height = (bounds.width / aspect).coerceAtLeast(GeometryService.MinElementSize)).normalized(),
        )
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedImageContentMode(value: TemplateImageContentMode) {
        replaceSelectedIfChanged { it.withImageContentMode(value) }
    }

    fun updateSelectedImageAlignment(value: TemplateImageAlignment) {
        replaceSelectedIfChanged { it.withImageAlignment(value) }
    }

    fun updateSelectedImageBackground(value: String) {
        replaceSelectedIfChanged { it.withImageBackground(value) }
    }

    fun updateSelectedImageBorderColor(value: String) {
        replaceSelectedIfChanged { it.withImageBorderColor(value) }
    }

    fun updateSelectedImageBorderWidth(value: Float) {
        replaceSelectedIfChanged { it.withImageBorderWidth(value) }
    }

    fun updateSelectedImageBorderRadius(value: Float) {
        replaceSelectedIfChanged { it.withImageBorderRadius(value) }
    }

    fun updateSelectedQrText(value: String) {
        replaceSelectedIfChanged { it.withQrText(value) }
    }

    fun updateSelectedQrForeground(value: String) {
        replaceSelectedIfChanged { it.withQrForeground(value) }
    }

    fun updateSelectedQrBackground(value: String) {
        replaceSelectedIfChanged { it.withQrBackground(value) }
    }

    fun updateSelectedQrQuietZone(value: Float) {
        replaceSelectedIfChanged { it.withQrQuietZone(value.roundToInt()) }
    }

    fun updateSelectedQrBorderColor(value: String) {
        replaceSelectedIfChanged { it.withQrBorderColor(value) }
    }

    fun updateSelectedQrBorderWidth(value: Float) {
        replaceSelectedIfChanged { it.withQrBorderWidth(value) }
    }

    fun updateSelectedBarcodeText(value: String) {
        replaceSelectedIfChanged { it.withBarcodeText(value) }
    }

    fun updateSelectedBarcodeFormat(value: TemplateBarcodeFormat) {
        replaceSelectedIfChanged { it.withBarcodeFormat(value) }
    }

    fun updateSelectedBarcodeForeground(value: String) {
        replaceSelectedIfChanged { it.withBarcodeForeground(value) }
    }

    fun updateSelectedBarcodeBackground(value: String) {
        replaceSelectedIfChanged { it.withBarcodeBackground(value) }
    }

    fun updateSelectedBarcodeQuietZone(value: Float) {
        replaceSelectedIfChanged { it.withBarcodeQuietZone(value.roundToInt()) }
    }

    fun updateSelectedBarcodeShowText(value: Boolean) {
        replaceSelectedIfChanged { it.withBarcodeShowText(value) }
    }

    fun updateSelectedBarcodeFontSize(value: Float) {
        replaceSelectedIfChanged { it.withBarcodeFontSize(value) }
    }

    fun updateSelectedBarcodeBorderColor(value: String) {
        replaceSelectedIfChanged { it.withBarcodeBorderColor(value) }
    }

    fun updateSelectedBarcodeBorderWidth(value: Float) {
        replaceSelectedIfChanged { it.withBarcodeBorderWidth(value) }
    }

    fun updateSelectedListFieldSlug(value: String) {
        replaceSelectedIfChanged { it.withListFieldSlug(value) }
    }

    fun updateSelectedListPrefix(value: String) {
        replaceSelectedIfChanged { it.withListPrefix(value) }
    }

    fun updateSelectedListSuffix(value: String) {
        replaceSelectedIfChanged { it.withListSuffix(value) }
    }

    fun updateSelectedListItemSeparator(value: String) {
        replaceSelectedIfChanged { it.withListItemSeparator(value) }
    }

    fun updateSelectedListMaxItems(value: Float) {
        replaceSelectedIfChanged { it.withListMaxItems(value.roundToInt()) }
    }

    fun updateSelectedListMaxItemLength(value: Float) {
        replaceSelectedIfChanged { it.withListMaxItemLength(value.roundToInt()) }
    }

    fun updateSelectedListColumns(value: Float) {
        replaceSelectedIfChanged { it.withListColumns(value.roundToInt()) }
    }

    fun updateSelectedListColumnGap(value: Float) {
        replaceSelectedIfChanged { it.withListColumnGap(value) }
    }

    fun updateSelectedListItemSpacing(value: Float) {
        replaceSelectedIfChanged { it.withListItemSpacing(value) }
    }

    fun updateSelectedListPadding(value: Float) {
        replaceSelectedIfChanged { it.withListPadding(value) }
    }

    fun updateSelectedListFontFamily(value: String) {
        replaceSelectedIfChanged { it.withListFontFamily(value) }
    }

    fun updateSelectedListFontSize(value: Float) {
        replaceSelectedIfChanged { it.withListFontSize(value) }
    }

    fun updateSelectedListColor(value: String) {
        replaceSelectedIfChanged { it.withListColor(value) }
    }

    fun updateSelectedListBackground(value: String) {
        replaceSelectedIfChanged { it.withListBackground(value) }
    }

    fun updateSelectedListBorderColor(value: String) {
        replaceSelectedIfChanged { it.withListBorderColor(value) }
    }

    fun updateSelectedListBorderWidth(value: Float) {
        replaceSelectedIfChanged { it.withListBorderWidth(value) }
    }

    fun updateSelectedListBorderStyle(value: TemplateBorderStyle) {
        replaceSelectedIfChanged { it.withListBorderStyle(value) }
    }

    fun updateSelectedListBorderRadius(value: Float) {
        replaceSelectedIfChanged { it.withListBorderRadius(value) }
    }

    fun updateSelectedTableRows(value: Float) {
        replaceSelectedIfChanged { it.withTableRows(value.roundToInt()) }
    }

    fun updateSelectedTableColumns(value: Float) {
        replaceSelectedIfChanged { it.withTableColumns(value.roundToInt()) }
    }

    fun updateSelectedTableHeaderRows(value: Float) {
        replaceSelectedIfChanged { it.withTableHeaderRows(value.roundToInt()) }
    }

    fun updateSelectedTableFontFamily(value: String) {
        replaceSelectedIfChanged { it.withTableFontFamily(value) }
    }

    fun updateSelectedTableFontSize(value: Float) {
        replaceSelectedIfChanged { it.withTableFontSize(value) }
    }

    fun updateSelectedTableTextColor(value: String) {
        replaceSelectedIfChanged { it.withTableTextColor(value) }
    }

    fun updateSelectedTableBackground(value: String) {
        replaceSelectedIfChanged { it.withTableBackground(value) }
    }

    fun updateSelectedTableHeaderBackground(value: String) {
        replaceSelectedIfChanged { it.withTableHeaderBackground(value) }
    }

    fun updateSelectedTableHeaderColor(value: String) {
        replaceSelectedIfChanged { it.withTableHeaderColor(value) }
    }

    fun updateSelectedTableAlternateRowColor(value: String) {
        replaceSelectedIfChanged { it.withTableAlternateRowColor(value) }
    }

    fun updateSelectedTableUseAlternateRows(value: Boolean) {
        replaceSelectedIfChanged { it.withTableUseAlternateRows(value) }
    }

    fun updateSelectedTableTextAlign(value: String) {
        replaceSelectedIfChanged { it.withTableTextAlign(value) }
    }

    fun updateSelectedTableVerticalAlign(value: String) {
        replaceSelectedIfChanged { it.withTableVerticalAlign(value) }
    }

    fun updateSelectedTablePadding(value: Float) {
        replaceSelectedIfChanged { it.withTablePadding(value) }
    }

    fun updateSelectedTableBorderColor(value: String) {
        replaceSelectedIfChanged { it.withTableBorderColor(value) }
    }

    fun updateSelectedTableBorderWidth(value: Float) {
        replaceSelectedIfChanged { it.withTableBorderWidth(value) }
    }

    fun updateSelectedTableBorderStyle(value: TemplateBorderStyle) {
        replaceSelectedIfChanged { it.withTableBorderStyle(value) }
    }

    fun updateSelectedTableBorderRadius(value: Float) {
        replaceSelectedIfChanged { it.withTableBorderRadius(value) }
    }

    fun updateSelectedTableGridBorderColor(value: String) {
        replaceSelectedIfChanged { it.withTableCellBorderColor(value) }
    }

    fun updateSelectedTableGridBorderWidth(value: Float) {
        replaceSelectedIfChanged { it.withTableCellBorderWidth(value) }
    }

    fun updateSelectedTableCellText(row: Int, column: Int, value: String) {
        replaceSelectedIfChanged { it.withTableCellText(row, column, value) }
    }

    fun updateSelectedTableCellBackground(row: Int, column: Int, value: String) {
        replaceSelectedIfChanged { it.withTableCellBackground(row, column, value) }
    }

    fun updateSelectedTableCellTextColor(row: Int, column: Int, value: String) {
        replaceSelectedIfChanged { it.withTableCellTextColor(row, column, value) }
    }

    fun updateSelectedTableCellBorderColor(row: Int, column: Int, value: String) {
        replaceSelectedIfChanged { it.withTableCellBorderColor(row, column, value) }
    }

    fun updateSelectedTableCellBorderWidth(row: Int, column: Int, value: Float) {
        replaceSelectedIfChanged { it.withTableCellBorderWidth(row, column, value) }
    }

    fun updateSelectedTableCellTextAlign(row: Int, column: Int, value: String) {
        replaceSelectedIfChanged { it.withTableCellTextAlign(row, column, value) }
    }

    fun updateSelectedTableCellVerticalAlign(row: Int, column: Int, value: String) {
        replaceSelectedIfChanged { it.withTableCellVerticalAlign(row, column, value) }
    }

    fun updateSelectedTableCellPadding(row: Int, column: Int, value: Float) {
        replaceSelectedIfChanged { it.withTableCellPadding(row, column, value) }
    }
}
