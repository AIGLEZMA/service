package me.aiglez.service.ui.templates

import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val imageBitmapCache = ConcurrentHashMap<String, TemplateImageBitmap>()

actual suspend fun chooseTemplateImageFile(): TemplateImageFile? {
    val dialog = FileDialog(null as Frame?, "Choose image", FileDialog.LOAD).apply {
        file = "*.png;*.jpg;*.jpeg;*.webp;*.gif;*.bmp"
        isVisible = true
    }
    val directory = dialog.directory ?: return null
    val fileName = dialog.file ?: return null
    val file = File(directory, fileName)
    val image = withContext(Dispatchers.Default) { decodeTemplateImage(file) }
        ?: return TemplateImageFile(path = file.absolutePath, name = file.name, width = 0, height = 0)
    imageBitmapCache[file.absolutePath] = image
    return TemplateImageFile(
        path = file.absolutePath,
        name = file.name,
        width = image.width,
        height = image.height,
    )
}

actual fun loadTemplateImageBitmap(path: String): TemplateImageBitmap? {
    imageBitmapCache[path]?.let { return it }
    val image = decodeTemplateImage(File(path)) ?: return null
    imageBitmapCache[path] = image
    return image
}

private fun decodeTemplateImage(file: File): TemplateImageBitmap? {
    val decoded = decodeImage(file) ?: return null
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
