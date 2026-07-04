package me.aiglez.service.ui.templates

import androidx.compose.ui.graphics.ImageBitmap

data class TemplateImageFile(
    val path: String,
    val name: String,
    val width: Int,
    val height: Int,
)

data class TemplateImageBitmap(
    val bitmap: ImageBitmap,
    val width: Int,
    val height: Int,
)

expect fun chooseTemplateImageFile(): TemplateImageFile?

expect fun loadTemplateImageBitmap(path: String): TemplateImageBitmap?
