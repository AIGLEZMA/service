package me.aiglez.service.io

import java.awt.FileDialog
import java.awt.Frame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun pickCsvFile(): CsvFileSelection? {
    val dialog = FileDialog(null as Frame?, "Importer un CSV", FileDialog.LOAD).apply {
        filenameFilter = { _, name -> name.endsWith(".csv", ignoreCase = true) }
        isVisible = true
    }
    val file = dialog.files.firstOrNull() ?: return null
    return CsvFileSelection(
        fileName = file.name,
        content = withContext(Dispatchers.IO) { file.readText() },
    )
}
