package me.aiglez.service.ui.templates

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import me.aiglez.service.domain.models.TemplateBarcodeFormat

actual fun generateTemplateBarcodeMatrix(
    text: String,
    format: TemplateBarcodeFormat,
    quietZone: Int,
): TemplateBarcodeMatrix? {
    if (text.isBlank()) return null
    return runCatching {
        val hints = mapOf(EncodeHintType.MARGIN to quietZone.coerceIn(0, 40))
        val matrix = MultiFormatWriter().encode(text, format.toZxingFormat(), 320, 96, hints)
        TemplateBarcodeMatrix(
            width = matrix.width,
            height = matrix.height,
            cells = List(matrix.width * matrix.height) { index ->
                matrix[index % matrix.width, index / matrix.width]
            },
        )
    }.getOrNull()
}

private fun TemplateBarcodeFormat.toZxingFormat(): BarcodeFormat {
    return when (this) {
        TemplateBarcodeFormat.Code128 -> BarcodeFormat.CODE_128
        TemplateBarcodeFormat.Ean13 -> BarcodeFormat.EAN_13
        TemplateBarcodeFormat.Ean8 -> BarcodeFormat.EAN_8
        TemplateBarcodeFormat.UpcA -> BarcodeFormat.UPC_A
        TemplateBarcodeFormat.Itf -> BarcodeFormat.ITF
    }
}
