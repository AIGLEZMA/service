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
import me.aiglez.service.ui.templates.editor.ReplaceElementsCommand
import me.aiglez.service.ui.templates.editor.updateCommon
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

    fun updateSelectedBounds(bounds: PageRect) {
        val element = selectedElement() ?: return
        if (element.locked) return
        val nextElement = element.withBounds(bounds.normalized())
        execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedCommonProperty(property: CommonProperty, value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withCommonProperty(property, value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedText(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withTextValue(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextColor(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withTextColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextFontSize(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withTextFontSize(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
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
        val element = selectedElement() ?: return
        val nextElement = element.withTextFontFamily(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextFontStyle(value: TemplateTextStyle) {
        val element = selectedElement() ?: return
        val nextElement = element.withTextFontStyle(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
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
        val element = selectedElement() ?: return
        val nextElement = element.withTextLineHeight(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextLetterSpacing(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withTextLetterSpacing(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextVerticalAlign(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withTextVerticalAlign(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextAlign(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withTextAlign(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
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
        val element = selectedElement() ?: return
        val nextElement = element.withTextDirection(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextBackground(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withTextBackground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextPadding(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withTextPadding(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextBorderColor(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withTextBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextBorderWidth(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withTextBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextBorderStyle(value: TemplateBorderStyle) {
        val element = selectedElement() ?: return
        val nextElement = element.withTextBorderStyle(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextBorderRadius(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withTextBorderRadius(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedRectangleFill(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withRectangleFill(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedRectangleBorderColor(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withRectangleBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedRectangleBorderWidth(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withRectangleBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedRectangleBorderStyle(value: TemplateBorderStyle) {
        val element = selectedElement() ?: return
        val nextElement = element.withRectangleBorderStyle(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedRectangleBorderRadius(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withRectangleBorderRadius(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedCircleFill(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withCircleFill(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedCircleBorderColor(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withCircleBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedCircleBorderWidth(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withCircleBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedCircleBorderStyle(value: TemplateBorderStyle) {
        val element = selectedElement() ?: return
        val nextElement = element.withCircleBorderStyle(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
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
        val element = selectedElement() ?: return
        val nextElement = element.withImageContentMode(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedImageAlignment(value: TemplateImageAlignment) {
        val element = selectedElement() ?: return
        val nextElement = element.withImageAlignment(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedImageBackground(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withImageBackground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedImageBorderColor(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withImageBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedImageBorderWidth(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withImageBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedImageBorderRadius(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withImageBorderRadius(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedQrText(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withQrText(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedQrForeground(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withQrForeground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedQrBackground(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withQrBackground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedQrQuietZone(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withQrQuietZone(value.roundToInt())
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedQrBorderColor(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withQrBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedQrBorderWidth(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withQrBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeText(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withBarcodeText(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeFormat(value: TemplateBarcodeFormat) {
        val element = selectedElement() ?: return
        val nextElement = element.withBarcodeFormat(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeForeground(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withBarcodeForeground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeBackground(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withBarcodeBackground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeQuietZone(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withBarcodeQuietZone(value.roundToInt())
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeShowText(value: Boolean) {
        val element = selectedElement() ?: return
        val nextElement = element.withBarcodeShowText(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeFontSize(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withBarcodeFontSize(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeBorderColor(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withBarcodeBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeBorderWidth(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withBarcodeBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListFieldSlug(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withListFieldSlug(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListPrefix(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withListPrefix(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListSuffix(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withListSuffix(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListItemSeparator(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withListItemSeparator(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListMaxItems(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withListMaxItems(value.roundToInt())
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListMaxItemLength(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withListMaxItemLength(value.roundToInt())
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListColumns(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withListColumns(value.roundToInt())
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListColumnGap(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withListColumnGap(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListItemSpacing(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withListItemSpacing(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListPadding(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withListPadding(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListFontFamily(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withListFontFamily(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListFontSize(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withListFontSize(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListColor(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withListColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListBackground(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withListBackground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListBorderColor(value: String) {
        val element = selectedElement() ?: return
        val nextElement = element.withListBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListBorderWidth(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withListBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListBorderStyle(value: TemplateBorderStyle) {
        val element = selectedElement() ?: return
        val nextElement = element.withListBorderStyle(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListBorderRadius(value: Float) {
        val element = selectedElement() ?: return
        val nextElement = element.withListBorderRadius(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }
}



