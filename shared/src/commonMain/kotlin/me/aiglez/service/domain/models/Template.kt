package me.aiglez.service.domain.models

import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
enum class TemplateElementType {
    Text,
    Image,
    Line,
    Rectangle,
    Circle,
    QRCode,
    Barcode,
    Table,
    List,
    Area,
}

@Serializable
enum class AnchorX {
    Left,
    Right,
}

@Serializable
enum class AnchorY {
    Top,
    Bottom,
}

@Serializable
enum class InlineType {
    Block,
    Inline,
}

@Serializable
enum class InlineAlignment {
    Start,
    Center,
    End,
}

@Serializable
enum class TemplateTextStyle {
    Normal,
    Italic,
    Oblique,
}

@Serializable
enum class TemplateTextDirection {
    Ltr,
    Rtl,
}

@Serializable
enum class TemplateBorderStyle {
    Solid,
    Dotted,
    Dashed,
    Double,
    Groove,
    Ridge,
    Inset,
    Outset,
}

@Serializable
enum class TemplateImageContentMode {
    Fit,
    Fill,
    Stretch,
}

@Serializable
enum class TemplateImageAlignment {
    Center,
    TopLeft,
    Top,
    TopRight,
    Left,
    Right,
    BottomLeft,
    Bottom,
    BottomRight,
}

@Serializable
enum class TemplateBarcodeFormat {
    Code128,
    Ean13,
    Ean8,
    UpcA,
    Itf,
}

@Serializable
enum class BreakInside {
    Auto,
    Avoid,
}

@Serializable
sealed interface TemplateElement {
    val id: String
    val type: TemplateElementType
    val name: String
    val x: Float
    val y: Float
    val width: Float
    val height: Float
    val anchorX: AnchorX
    val anchorY: AnchorY
    val rotation: Float
    val locked: Boolean
    val visible: Boolean
    val zIndex: Int
    val opacity: Float
    val visibilityExpression: String
    val breakInside: BreakInside
    val inlineType: InlineType
    val inlineAlignment: InlineAlignment

    @Serializable
    data class Text(
        override val id: String = "",
        override val name: String = "Text",
        override val x: Float,
        override val y: Float,
        override val width: Float = 180f,
        override val height: Float = 48f,
        override val anchorX: AnchorX = AnchorX.Left,
        override val anchorY: AnchorY = AnchorY.Top,
        override val rotation: Float = 0f,
        override val locked: Boolean = false,
        override val visible: Boolean = true,
        override val zIndex: Int = 0,
        override val opacity: Float = 1f,
        override val visibilityExpression: String = "",
        override val breakInside: BreakInside = BreakInside.Auto,
        override val inlineType: InlineType = InlineType.Block,
        override val inlineAlignment: InlineAlignment = InlineAlignment.Start,
        val staticText: String? = null,
        val placeholderTag: String? = null, // e.g., "[DataRecord:client_age]"
        val expression: String = "",
        val fontFamily: String = "Inter",
        val fontSize: Float = 12f,
        val fontWeight: Int = 400,
        val italic: Boolean = false,
        val underline: Boolean = false,
        val fontStyle: TemplateTextStyle = TemplateTextStyle.Normal,
        val color: String = "#111827",
        val backgroundColor: String = "#00000000",
        val lineHeight: Float = 1.2f,
        val letterSpacing: Float = 0f,
        val textAlign: String = "left",
        val verticalAlign: String = "top",
        val textDirection: TemplateTextDirection = TemplateTextDirection.Ltr,
        val padding: Float = 0f,
        val borderColor: String = "#00000000",
        val borderWidth: Float = 0f,
        val borderStyle: TemplateBorderStyle = TemplateBorderStyle.Solid,
        val borderRadius: Float = 0f,
    ) : TemplateElement {
        override val type: TemplateElementType = TemplateElementType.Text
    }

    @Serializable
    data class Image(
        override val id: String = "",
        override val name: String = "Image",
        override val x: Float,
        override val y: Float,
        override val width: Float = 180f,
        override val height: Float = 120f,
        override val anchorX: AnchorX = AnchorX.Left,
        override val anchorY: AnchorY = AnchorY.Top,
        override val rotation: Float = 0f,
        override val locked: Boolean = false,
        override val visible: Boolean = true,
        override val zIndex: Int = 0,
        override val opacity: Float = 1f,
        override val visibilityExpression: String = "",
        override val breakInside: BreakInside = BreakInside.Auto,
        override val inlineType: InlineType = InlineType.Block,
        override val inlineAlignment: InlineAlignment = InlineAlignment.Start,
        val sourcePath: String = "",
        val sourceName: String = "",
        val intrinsicWidth: Int = 0,
        val intrinsicHeight: Int = 0,
        val contentMode: TemplateImageContentMode = TemplateImageContentMode.Fit,
        val alignment: TemplateImageAlignment = TemplateImageAlignment.Center,
        val backgroundColor: String = "#F8FAFC",
        val borderColor: String = "#CBD5E1",
        val borderWidth: Float = 1f,
        val borderRadius: Float = 0f,
    ) : TemplateElement {
        override val type: TemplateElementType = TemplateElementType.Image
    }

    @Serializable
    data class Circle(
        override val id: String = "",
        override val name: String = "Circle",
        override val x: Float,
        override val y: Float,
        override val width: Float = 120f,
        override val height: Float = 120f,
        override val anchorX: AnchorX = AnchorX.Left,
        override val anchorY: AnchorY = AnchorY.Top,
        override val rotation: Float = 0f,
        override val locked: Boolean = false,
        override val visible: Boolean = true,
        override val zIndex: Int = 0,
        override val opacity: Float = 1f,
        override val visibilityExpression: String = "",
        override val breakInside: BreakInside = BreakInside.Auto,
        override val inlineType: InlineType = InlineType.Block,
        override val inlineAlignment: InlineAlignment = InlineAlignment.Start,
        val fillColor: String = "#FDE68A",
        val borderColor: String = "#D97706",
        val borderWidth: Float = 1f,
        val borderStyle: TemplateBorderStyle = TemplateBorderStyle.Solid,
    ) : TemplateElement {
        override val type: TemplateElementType = TemplateElementType.Circle
    }

    @Serializable
    data class QRCode(
        override val id: String = "",
        override val name: String = "QR Code",
        override val x: Float,
        override val y: Float,
        override val width: Float = 132f,
        override val height: Float = 132f,
        override val anchorX: AnchorX = AnchorX.Left,
        override val anchorY: AnchorY = AnchorY.Top,
        override val rotation: Float = 0f,
        override val locked: Boolean = false,
        override val visible: Boolean = true,
        override val zIndex: Int = 0,
        override val opacity: Float = 1f,
        override val visibilityExpression: String = "",
        override val breakInside: BreakInside = BreakInside.Auto,
        override val inlineType: InlineType = InlineType.Block,
        override val inlineAlignment: InlineAlignment = InlineAlignment.Start,
        val text: String = "https://example.com",
        val foregroundColor: String = "#111827",
        val backgroundColor: String = "#FFFFFF",
        val quietZone: Int = 4,
        val borderColor: String = "#CBD5E1",
        val borderWidth: Float = 0f,
    ) : TemplateElement {
        override val type: TemplateElementType = TemplateElementType.QRCode
    }

    @Serializable
    data class Barcode(
        override val id: String = "",
        override val name: String = "Barcode",
        override val x: Float,
        override val y: Float,
        override val width: Float = 220f,
        override val height: Float = 72f,
        override val anchorX: AnchorX = AnchorX.Left,
        override val anchorY: AnchorY = AnchorY.Top,
        override val rotation: Float = 0f,
        override val locked: Boolean = false,
        override val visible: Boolean = true,
        override val zIndex: Int = 0,
        override val opacity: Float = 1f,
        override val visibilityExpression: String = "",
        override val breakInside: BreakInside = BreakInside.Auto,
        override val inlineType: InlineType = InlineType.Block,
        override val inlineAlignment: InlineAlignment = InlineAlignment.Start,
        val text: String = "ABC-123456",
        val format: TemplateBarcodeFormat = TemplateBarcodeFormat.Code128,
        val foregroundColor: String = "#111827",
        val backgroundColor: String = "#FFFFFF",
        val quietZone: Int = 10,
        val showText: Boolean = true,
        val fontSize: Float = 10f,
        val borderColor: String = "#CBD5E1",
        val borderWidth: Float = 0f,
    ) : TemplateElement {
        override val type: TemplateElementType = TemplateElementType.Barcode
    }

    @Serializable
    data class List(
        override val id: String = "",
        override val name: String = "List",
        override val x: Float,
        override val y: Float,
        override val width: Float = 240f,
        override val height: Float = 140f,
        override val anchorX: AnchorX = AnchorX.Left,
        override val anchorY: AnchorY = AnchorY.Top,
        override val rotation: Float = 0f,
        override val locked: Boolean = false,
        override val visible: Boolean = true,
        override val zIndex: Int = 0,
        override val opacity: Float = 1f,
        override val visibilityExpression: String = "",
        override val breakInside: BreakInside = BreakInside.Auto,
        override val inlineType: InlineType = InlineType.Block,
        override val inlineAlignment: InlineAlignment = InlineAlignment.Start,
        val fieldSlug: String = "",
        val prefix: String = "- ",
        val suffix: String = "",
        val itemSeparator: String = ",",
        val maxItems: Int = 12,
        val maxItemLength: Int = 80,
        val columns: Int = 1,
        val columnGap: Float = 16f,
        val itemSpacing: Float = 4f,
        val padding: Float = 8f,
        val fontFamily: String = "Inter",
        val fontSize: Float = 12f,
        val fontWeight: Int = 400,
        val color: String = "#111827",
        val backgroundColor: String = "#00000000",
        val borderColor: String = "#00000000",
        val borderWidth: Float = 0f,
        val borderStyle: TemplateBorderStyle = TemplateBorderStyle.Solid,
        val borderRadius: Float = 0f,
    ) : TemplateElement {
        override val type: TemplateElementType = TemplateElementType.List
    }

    @Serializable
    data class Rectangle(
        override val id: String = "",
        override val name: String = "Rectangle",
        override val x: Float,
        override val y: Float,
        override val width: Float = 160f,
        override val height: Float = 96f,
        override val anchorX: AnchorX = AnchorX.Left,
        override val anchorY: AnchorY = AnchorY.Top,
        override val rotation: Float = 0f,
        override val locked: Boolean = false,
        override val visible: Boolean = true,
        override val zIndex: Int = 0,
        override val opacity: Float = 1f,
        override val visibilityExpression: String = "",
        override val breakInside: BreakInside = BreakInside.Auto,
        override val inlineType: InlineType = InlineType.Block,
        override val inlineAlignment: InlineAlignment = InlineAlignment.Start,
        val fillColor: String = "#E0F2FE",
        val outerPadding: Float = 0f,
        val borderColor: String = "#0284C7",
        val borderWidth: Float = 1f,
        val borderStyle: TemplateBorderStyle = TemplateBorderStyle.Solid,
        val borderRadius: Float = 0f,
    ) : TemplateElement {
        override val type: TemplateElementType = TemplateElementType.Rectangle
    }

    @Serializable
    data class Line(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val thickness: Float,
        override val id: String = "",
        override val name: String = "Line",
        override val anchorX: AnchorX = AnchorX.Left,
        override val anchorY: AnchorY = AnchorY.Top,
        override val rotation: Float = 0f,
        override val locked: Boolean = false,
        override val visible: Boolean = true,
        override val zIndex: Int = 0,
        override val opacity: Float = 1f,
        override val visibilityExpression: String = "",
        override val breakInside: BreakInside = BreakInside.Auto,
        override val inlineType: InlineType = InlineType.Block,
        override val inlineAlignment: InlineAlignment = InlineAlignment.Start,
    ) : TemplateElement {
        override val type: TemplateElementType = TemplateElementType.Line
        override val x: Float = minOf(x1, x2)
        override val y: Float = minOf(y1, y2)
        override val width: Float = abs(x2 - x1).coerceAtLeast(thickness)
        override val height: Float = abs(y2 - y1).coerceAtLeast(thickness)
    }
}

@Serializable
data class Template(
    val id: String,
    val name: String,
    val targetSchemaId: String,
    val templateVersion: Int = 1,
    val pageSize: String = "A4",
    val elements: List<TemplateElement>,
    val isArchived: Boolean = false, // Soft delete flag
)
