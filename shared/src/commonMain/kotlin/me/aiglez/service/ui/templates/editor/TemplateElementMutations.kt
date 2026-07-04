package me.aiglez.service.ui.templates.editor

import me.aiglez.service.domain.models.AnchorX
import me.aiglez.service.domain.models.AnchorY
import me.aiglez.service.domain.models.BreakInside
import me.aiglez.service.domain.models.InlineAlignment
import me.aiglez.service.domain.models.InlineType
import me.aiglez.service.domain.models.TemplateBarcodeFormat
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.models.TemplateElementType
import me.aiglez.service.domain.models.TemplateImageAlignment
import me.aiglez.service.domain.models.TemplateImageContentMode
import me.aiglez.service.domain.models.TemplateBorderStyle
import me.aiglez.service.domain.models.TemplateTextDirection
import me.aiglez.service.domain.models.TemplateTextStyle

fun TemplateElement.withBounds(bounds: PageRect): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(
            x = bounds.x,
            y = bounds.y,
            width = bounds.width,
            height = bounds.height,
        )
        is TemplateElement.Image -> copy(
            x = bounds.x,
            y = bounds.y,
            width = bounds.width,
            height = bounds.height,
        )
        is TemplateElement.Circle -> copy(
            x = bounds.x,
            y = bounds.y,
            width = bounds.width,
            height = bounds.height,
        )
        is TemplateElement.QRCode -> copy(
            x = bounds.x,
            y = bounds.y,
            width = bounds.width,
            height = bounds.height,
        )
        is TemplateElement.Barcode -> copy(
            x = bounds.x,
            y = bounds.y,
            width = bounds.width,
            height = bounds.height,
        )
        is TemplateElement.List -> copy(
            x = bounds.x,
            y = bounds.y,
            width = bounds.width,
            height = bounds.height,
        )
        is TemplateElement.Rectangle -> copy(
            x = bounds.x,
            y = bounds.y,
            width = bounds.width,
            height = bounds.height,
        )
        is TemplateElement.Line -> {
            val oldBounds = GeometryService.getElementBounds(this)
            val dx = bounds.x - oldBounds.x
            val dy = bounds.y - oldBounds.y
            copy(
                x1 = x1 + dx,
                y1 = y1 + dy,
                x2 = x2 + dx,
                y2 = y2 + dy,
            )
        }
    }
}

fun TemplateElement.withCommonProperty(property: CommonProperty, value: String): TemplateElement {
    return when (property) {
        CommonProperty.Name -> updateCommon(name = value)
        CommonProperty.AnchorX -> updateCommon(anchorX = value.toAnchorXOrNull() ?: anchorX)
        CommonProperty.AnchorY -> updateCommon(anchorY = value.toAnchorYOrNull() ?: anchorY)
        CommonProperty.Rotation -> updateCommon(rotation = value.toFloatOrNull() ?: rotation)
        CommonProperty.Locked -> updateCommon(locked = value.toBooleanStrictOrNull() ?: locked)
        CommonProperty.Visible -> updateCommon(visible = value.toBooleanStrictOrNull() ?: visible)
        CommonProperty.ZIndex -> updateCommon(zIndex = value.toIntOrNull() ?: zIndex)
        CommonProperty.Opacity -> updateCommon(opacity = (value.toFloatOrNull() ?: opacity).coerceIn(0f, 1f))
        CommonProperty.VisibilityExpression -> updateCommon(visibilityExpression = value)
        CommonProperty.BreakInside -> updateCommon(breakInside = value.toBreakInsideOrNull() ?: breakInside)
        CommonProperty.InlineType -> updateCommon(inlineType = value.toInlineTypeOrNull() ?: inlineType)
        CommonProperty.InlineAlignment -> updateCommon(inlineAlignment = value.toInlineAlignmentOrNull() ?: inlineAlignment)
    }
}

fun TemplateElement.updateCommon(
    name: String = this.name,
    anchorX: AnchorX = this.anchorX,
    anchorY: AnchorY = this.anchorY,
    rotation: Float = this.rotation,
    locked: Boolean = this.locked,
    visible: Boolean = this.visible,
    zIndex: Int = this.zIndex,
    opacity: Float = this.opacity,
    visibilityExpression: String = this.visibilityExpression,
    breakInside: BreakInside = this.breakInside,
    inlineType: InlineType = this.inlineType,
    inlineAlignment: InlineAlignment = this.inlineAlignment,
): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(
            name = name,
            anchorX = anchorX,
            anchorY = anchorY,
            rotation = rotation,
            locked = locked,
            visible = visible,
            zIndex = zIndex,
            opacity = opacity,
            visibilityExpression = visibilityExpression,
            breakInside = breakInside,
            inlineType = inlineType,
            inlineAlignment = inlineAlignment,
        )
        is TemplateElement.Image -> copy(
            name = name,
            anchorX = anchorX,
            anchorY = anchorY,
            rotation = rotation,
            locked = locked,
            visible = visible,
            zIndex = zIndex,
            opacity = opacity,
            visibilityExpression = visibilityExpression,
            breakInside = breakInside,
            inlineType = inlineType,
            inlineAlignment = inlineAlignment,
        )
        is TemplateElement.Circle -> copy(
            name = name,
            anchorX = anchorX,
            anchorY = anchorY,
            rotation = rotation,
            locked = locked,
            visible = visible,
            zIndex = zIndex,
            opacity = opacity,
            visibilityExpression = visibilityExpression,
            breakInside = breakInside,
            inlineType = inlineType,
            inlineAlignment = inlineAlignment,
        )
        is TemplateElement.QRCode -> copy(
            name = name,
            anchorX = anchorX,
            anchorY = anchorY,
            rotation = rotation,
            locked = locked,
            visible = visible,
            zIndex = zIndex,
            opacity = opacity,
            visibilityExpression = visibilityExpression,
            breakInside = breakInside,
            inlineType = inlineType,
            inlineAlignment = inlineAlignment,
        )
        is TemplateElement.Barcode -> copy(
            name = name,
            anchorX = anchorX,
            anchorY = anchorY,
            rotation = rotation,
            locked = locked,
            visible = visible,
            zIndex = zIndex,
            opacity = opacity,
            visibilityExpression = visibilityExpression,
            breakInside = breakInside,
            inlineType = inlineType,
            inlineAlignment = inlineAlignment,
        )
        is TemplateElement.List -> copy(
            name = name,
            anchorX = anchorX,
            anchorY = anchorY,
            rotation = rotation,
            locked = locked,
            visible = visible,
            zIndex = zIndex,
            opacity = opacity,
            visibilityExpression = visibilityExpression,
            breakInside = breakInside,
            inlineType = inlineType,
            inlineAlignment = inlineAlignment,
        )
        is TemplateElement.Rectangle -> copy(
            name = name,
            anchorX = anchorX,
            anchorY = anchorY,
            rotation = rotation,
            locked = locked,
            visible = visible,
            zIndex = zIndex,
            opacity = opacity,
            visibilityExpression = visibilityExpression,
            breakInside = breakInside,
            inlineType = inlineType,
            inlineAlignment = inlineAlignment,
        )
        is TemplateElement.Line -> copy(
            name = name,
            anchorX = anchorX,
            anchorY = anchorY,
            rotation = rotation,
            locked = locked,
            visible = visible,
            zIndex = zIndex,
            opacity = opacity,
            visibilityExpression = visibilityExpression,
            breakInside = breakInside,
            inlineType = inlineType,
            inlineAlignment = inlineAlignment,
        )
    }
}

fun TemplateElement.withTextValue(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(staticText = value)
        else -> this
    }
}

fun TemplateElement.withTextColor(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(color = value)
        else -> this
    }
}

fun TemplateElement.withTextFontSize(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(fontSize = value.coerceAtLeast(1f))
        else -> this
    }
}

fun TemplateElement.withTextFontFamily(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(fontFamily = value)
        else -> this
    }
}

fun TemplateElement.withTextFontStyle(value: TemplateTextStyle): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(fontStyle = value, italic = value == TemplateTextStyle.Italic)
        else -> this
    }
}

fun TemplateElement.withTextLineHeight(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(lineHeight = value.coerceAtLeast(0.5f))
        else -> this
    }
}

fun TemplateElement.withTextLetterSpacing(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(letterSpacing = value)
        else -> this
    }
}

fun TemplateElement.withTextVerticalAlign(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(verticalAlign = value)
        else -> this
    }
}

fun TemplateElement.withTextAlign(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(textAlign = value)
        else -> this
    }
}

fun TemplateElement.withTextDirection(value: TemplateTextDirection): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(textDirection = value)
        else -> this
    }
}

fun TemplateElement.withTextBackground(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(backgroundColor = value)
        else -> this
    }
}

fun TemplateElement.withTextPadding(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(padding = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withTextBorderColor(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(borderColor = value)
        else -> this
    }
}

fun TemplateElement.withTextBorderWidth(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(borderWidth = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withTextBorderStyle(value: TemplateBorderStyle): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(borderStyle = value)
        else -> this
    }
}

fun TemplateElement.withTextBorderRadius(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.Text -> copy(borderRadius = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withImageSource(
    path: String,
    name: String,
    intrinsicWidth: Int,
    intrinsicHeight: Int,
): TemplateElement {
    return when (this) {
        is TemplateElement.Image -> copy(
            sourcePath = path,
            sourceName = name,
            intrinsicWidth = intrinsicWidth.coerceAtLeast(0),
            intrinsicHeight = intrinsicHeight.coerceAtLeast(0),
        )
        else -> this
    }
}

fun TemplateElement.withImageContentMode(value: TemplateImageContentMode): TemplateElement {
    return when (this) {
        is TemplateElement.Image -> copy(contentMode = value)
        else -> this
    }
}

fun TemplateElement.withImageAlignment(value: TemplateImageAlignment): TemplateElement {
    return when (this) {
        is TemplateElement.Image -> copy(alignment = value)
        else -> this
    }
}

fun TemplateElement.withImageBackground(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Image -> copy(backgroundColor = value)
        else -> this
    }
}

fun TemplateElement.withImageBorderColor(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Image -> copy(borderColor = value)
        else -> this
    }
}

fun TemplateElement.withImageBorderWidth(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.Image -> copy(borderWidth = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withImageBorderRadius(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.Image -> copy(borderRadius = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withCircleFill(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Circle -> copy(fillColor = value)
        else -> this
    }
}

fun TemplateElement.withCircleBorderColor(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Circle -> copy(borderColor = value)
        else -> this
    }
}

fun TemplateElement.withCircleBorderWidth(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.Circle -> copy(borderWidth = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withCircleBorderStyle(value: TemplateBorderStyle): TemplateElement {
    return when (this) {
        is TemplateElement.Circle -> copy(borderStyle = value)
        else -> this
    }
}

fun TemplateElement.withQrText(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.QRCode -> copy(text = value)
        else -> this
    }
}

fun TemplateElement.withQrForeground(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.QRCode -> copy(foregroundColor = value)
        else -> this
    }
}

fun TemplateElement.withQrBackground(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.QRCode -> copy(backgroundColor = value)
        else -> this
    }
}

fun TemplateElement.withQrQuietZone(value: Int): TemplateElement {
    return when (this) {
        is TemplateElement.QRCode -> copy(quietZone = value.coerceIn(0, 8))
        else -> this
    }
}

fun TemplateElement.withQrBorderColor(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.QRCode -> copy(borderColor = value)
        else -> this
    }
}

fun TemplateElement.withQrBorderWidth(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.QRCode -> copy(borderWidth = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withBarcodeText(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Barcode -> copy(text = value)
        else -> this
    }
}

fun TemplateElement.withBarcodeFormat(value: TemplateBarcodeFormat): TemplateElement {
    return when (this) {
        is TemplateElement.Barcode -> copy(format = value)
        else -> this
    }
}

fun TemplateElement.withBarcodeForeground(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Barcode -> copy(foregroundColor = value)
        else -> this
    }
}

fun TemplateElement.withBarcodeBackground(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Barcode -> copy(backgroundColor = value)
        else -> this
    }
}

fun TemplateElement.withBarcodeQuietZone(value: Int): TemplateElement {
    return when (this) {
        is TemplateElement.Barcode -> copy(quietZone = value.coerceIn(0, 40))
        else -> this
    }
}

fun TemplateElement.withBarcodeShowText(value: Boolean): TemplateElement {
    return when (this) {
        is TemplateElement.Barcode -> copy(showText = value)
        else -> this
    }
}

fun TemplateElement.withBarcodeFontSize(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.Barcode -> copy(fontSize = value.coerceAtLeast(1f))
        else -> this
    }
}

fun TemplateElement.withBarcodeBorderColor(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Barcode -> copy(borderColor = value)
        else -> this
    }
}

fun TemplateElement.withBarcodeBorderWidth(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.Barcode -> copy(borderWidth = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withListFieldSlug(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(fieldSlug = value)
        else -> this
    }
}

fun TemplateElement.withListPrefix(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(prefix = value)
        else -> this
    }
}

fun TemplateElement.withListSuffix(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(suffix = value)
        else -> this
    }
}

fun TemplateElement.withListItemSeparator(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(itemSeparator = value)
        else -> this
    }
}

fun TemplateElement.withListMaxItems(value: Int): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(maxItems = value.coerceIn(1, 200))
        else -> this
    }
}

fun TemplateElement.withListMaxItemLength(value: Int): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(maxItemLength = value.coerceIn(1, 500))
        else -> this
    }
}

fun TemplateElement.withListColumns(value: Int): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(columns = value.coerceIn(1, 6))
        else -> this
    }
}

fun TemplateElement.withListColumnGap(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(columnGap = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withListItemSpacing(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(itemSpacing = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withListPadding(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(padding = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withListFontFamily(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(fontFamily = value)
        else -> this
    }
}

fun TemplateElement.withListFontSize(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(fontSize = value.coerceAtLeast(1f))
        else -> this
    }
}

fun TemplateElement.withListColor(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(color = value)
        else -> this
    }
}

fun TemplateElement.withListBackground(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(backgroundColor = value)
        else -> this
    }
}

fun TemplateElement.withListBorderColor(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(borderColor = value)
        else -> this
    }
}

fun TemplateElement.withListBorderWidth(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(borderWidth = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withListBorderStyle(value: TemplateBorderStyle): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(borderStyle = value)
        else -> this
    }
}

fun TemplateElement.withListBorderRadius(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.List -> copy(borderRadius = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withRectangleFill(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Rectangle -> copy(fillColor = value)
        else -> this
    }
}

fun TemplateElement.withRectangleBorderColor(value: String): TemplateElement {
    return when (this) {
        is TemplateElement.Rectangle -> copy(borderColor = value)
        else -> this
    }
}

fun TemplateElement.withRectangleBorderWidth(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.Rectangle -> copy(borderWidth = value.coerceAtLeast(0f))
        else -> this
    }
}

fun TemplateElement.withRectangleBorderStyle(value: TemplateBorderStyle): TemplateElement {
    return when (this) {
        is TemplateElement.Rectangle -> copy(borderStyle = value)
        else -> this
    }
}

fun TemplateElement.withRectangleBorderRadius(value: Float): TemplateElement {
    return when (this) {
        is TemplateElement.Rectangle -> copy(borderRadius = value.coerceAtLeast(0f))
        else -> this
    }
}

fun createDefaultElement(type: TemplateElementType, id: String, x: Float, y: Float, zIndex: Int): TemplateElement {
    return when (type) {
        TemplateElementType.Text -> TemplateElement.Text(
            id = id,
            name = "Text",
            x = x,
            y = y,
            zIndex = zIndex,
            staticText = "New text",
        )
        TemplateElementType.Image -> TemplateElement.Image(
            id = id,
            name = "Image",
            x = x,
            y = y,
            zIndex = zIndex,
        )
        TemplateElementType.Circle -> TemplateElement.Circle(
            id = id,
            name = "Circle",
            x = x,
            y = y,
            zIndex = zIndex,
        )
        TemplateElementType.QRCode -> TemplateElement.QRCode(
            id = id,
            name = "QR Code",
            x = x,
            y = y,
            zIndex = zIndex,
        )
        TemplateElementType.Barcode -> TemplateElement.Barcode(
            id = id,
            name = "Barcode",
            x = x,
            y = y,
            zIndex = zIndex,
        )
        TemplateElementType.List -> TemplateElement.List(
            id = id,
            name = "List",
            x = x,
            y = y,
            zIndex = zIndex,
        )
        TemplateElementType.Rectangle -> TemplateElement.Rectangle(
            id = id,
            name = "Rectangle",
            x = x,
            y = y,
            zIndex = zIndex,
        )
        TemplateElementType.Line -> TemplateElement.Line(
            id = id,
            name = "Line",
            x1 = x,
            y1 = y,
            x2 = x + 120f,
            y2 = y,
            thickness = 2f,
            zIndex = zIndex,
        )
        else -> TemplateElement.Rectangle(
            id = id,
            name = type.name,
            x = x,
            y = y,
            zIndex = zIndex,
        )
    }
}

enum class CommonProperty {
    Name,
    AnchorX,
    AnchorY,
    Rotation,
    Locked,
    Visible,
    ZIndex,
    Opacity,
    VisibilityExpression,
    BreakInside,
    InlineType,
    InlineAlignment,
}

private fun String.toAnchorXOrNull(): AnchorX? = AnchorX.entries.firstOrNull { it.name == this }
private fun String.toAnchorYOrNull(): AnchorY? = AnchorY.entries.firstOrNull { it.name == this }
private fun String.toBreakInsideOrNull(): BreakInside? = BreakInside.entries.firstOrNull { it.name == this }
private fun String.toInlineTypeOrNull(): InlineType? = InlineType.entries.firstOrNull { it.name == this }
private fun String.toInlineAlignmentOrNull(): InlineAlignment? = InlineAlignment.entries.firstOrNull { it.name == this }



