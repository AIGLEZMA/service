package me.aiglez.service.ui.templates

import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual fun chooseTemplateImageFile(): TemplateImageFile? {
    val dialog = FileDialog(null as Frame?, "Choose image", FileDialog.LOAD).apply {
        file = "*.png;*.jpg;*.jpeg;*.webp;*.gif;*.bmp"
        isVisible = true
    }
    val directory = dialog.directory ?: return null
    val fileName = dialog.file ?: return null
    val file = File(directory, fileName)
    val decoded = decodeImage(file) ?: return TemplateImageFile(
        path = file.absolutePath,
        name = file.name,
        width = 0,
        height = 0,
    )
    return TemplateImageFile(
        path = file.absolutePath,
        name = file.name,
        width = decoded.width,
        height = decoded.height,
    )
}

actual fun loadTemplateImageBitmap(path: String): TemplateImageBitmap? {
    val decoded = decodeImage(File(path)) ?: return null
    return TemplateImageBitmap(
        bitmap = decoded.toComposeImageBitmap(),
        width = decoded.width,
        height = decoded.height,
    )
}

private fun decodeImage(file: File): Image? {
    if (!file.isFile) return null
    return runCatching {
        Image.makeFromEncoded(file.readBytes())
    }.getOrNull()
}
