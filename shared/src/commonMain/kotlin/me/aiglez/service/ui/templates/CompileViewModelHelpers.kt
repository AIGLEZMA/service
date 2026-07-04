package me.aiglez.service.ui.templates

import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.ui.templates.editor.GeometryService
import me.aiglez.service.ui.templates.editor.PageRect
import me.aiglez.service.ui.templates.editor.TemplateEditorState
import me.aiglez.service.ui.templates.editor.referencedExpressionRoots
import me.aiglez.service.ui.templates.editor.schemaExpressionAliases
import me.aiglez.service.ui.templates.editor.updateCommon

internal fun PageRect.normalized(): PageRect {
    val minSize = GeometryService.MinElementSize
    return copy(
        x = x,
        y = y,
        width = width.coerceAtLeast(minSize),
        height = height.coerceAtLeast(minSize),
    )
}

enum class SelectionAlignment {
    Left,
    Center,
    Right,
    Top,
    Middle,
    Bottom,
}

enum class DistributionAxis {
    Horizontal,
    Vertical,
}

enum class SizeMatchAxis {
    Width,
    Height,
}

internal fun TemplateElement.withCopiedIdentity(id: String, zIndex: Int): TemplateElement {
    val nextName = "$name copy"
    return updateCommon(name = nextName, zIndex = zIndex).let { element ->
        when (element) {
            is TemplateElement.Text -> element.copy(id = id)
            is TemplateElement.Image -> element.copy(id = id)
            is TemplateElement.Circle -> element.copy(id = id)
            is TemplateElement.QRCode -> element.copy(id = id)
            is TemplateElement.Barcode -> element.copy(id = id)
            is TemplateElement.List -> element.copy(id = id)
            is TemplateElement.Rectangle -> element.copy(id = id)
            is TemplateElement.Line -> element.copy(id = id)
        }
    }
}

internal fun List<PageRect>.unionBounds(): PageRect? {
    if (isEmpty()) return null
    val left = minOf { it.x }
    val top = minOf { it.y }
    val right = maxOf { it.right }
    val bottom = maxOf { it.bottom }
    return PageRect(left, top, right - left, bottom - top)
}

internal fun TemplateEditorState.referencedPreviewSchemaIds(): List<String> {
    val schemas = availableSchemas
    if (schemas.isEmpty()) return emptyList()
    val schemaByAlias = schemas
        .flatMap { schema -> schemaExpressionAliases(schema).map { alias -> alias to schema } }
        .toMap()
    val roots = document.elements.flatMap { element -> element.referencedExpressionRoots() }.toSet()
    val schemaIds = roots.mapNotNull { root ->
        when (root) {
            "data" -> schema?.id
            else -> schemaByAlias[root]?.id
        }
    }
    return schemaIds
        .ifEmpty { listOfNotNull(schema?.id ?: template?.targetSchemaId) }
        .distinct()
}

internal fun TemplateElement.referencedExpressionRoots(): Set<String> {
    val roots = mutableSetOf<String>()
    fun addFrom(value: String?) {
        if (!value.isNullOrBlank()) roots += referencedExpressionRoots(value)
    }
    when (this) {
        is TemplateElement.Text -> {
            addFrom(staticText)
            expression.takeIf { it.isNotBlank() }?.let(::addFrom)
            if (!placeholderTag.isNullOrBlank()) roots += "data"
        }
        is TemplateElement.QRCode -> addFrom(text)
        is TemplateElement.Barcode -> addFrom(text)
        is TemplateElement.List -> {
            if (fieldSlug.contains(".")) {
                roots += fieldSlug.substringBefore(".")
            } else if (fieldSlug.isNotBlank()) {
                roots += "data"
            }
        }
        is TemplateElement.Image,
        is TemplateElement.Circle,
        is TemplateElement.Rectangle,
        is TemplateElement.Line -> Unit
    }
    return roots
}

internal fun normalizeRotation(rotation: Float): Float {
    val normalized = rotation % 360f
    return if (normalized < 0f) normalized + 360f else normalized
}



