package me.aiglez.service.ui.templates

data class TemplateQrMatrix(
    val width: Int,
    val height: Int,
    val cells: List<Boolean>,
) {
    fun isDark(x: Int, y: Int): Boolean {
        if (x !in 0 until width || y !in 0 until height) return false
        return cells[y * width + x]
    }
}

expect fun generateTemplateQrMatrix(text: String, quietZone: Int): TemplateQrMatrix?
