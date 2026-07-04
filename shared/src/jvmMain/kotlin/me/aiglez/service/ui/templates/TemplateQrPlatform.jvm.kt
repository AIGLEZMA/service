package me.aiglez.service.ui.templates

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

actual fun generateTemplateQrMatrix(text: String, quietZone: Int): TemplateQrMatrix? {
    if (text.isBlank()) return null
    return runCatching {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to quietZone.coerceIn(0, 8),
        )
        val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 1, 1, hints)
        TemplateQrMatrix(
            width = matrix.width,
            height = matrix.height,
            cells = List(matrix.width * matrix.height) { index ->
                matrix[index % matrix.width, index / matrix.width]
            },
        )
    }.getOrNull()
}



