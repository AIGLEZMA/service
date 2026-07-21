package me.aiglez.service.ui.templates

import me.aiglez.service.domain.models.TemplateElement

internal fun ensureStableElementIds(
    elements: List<TemplateElement>,
    newId: (String) -> String,
): List<TemplateElement> = elements.mapIndexed { index, element ->
    if (element.id.isNotBlank()) {
        element
    } else {
        val id = newId("element-${index + 1}")
        when (element) {
            is TemplateElement.Text -> element.copy(id = id)
            is TemplateElement.Image -> element.copy(id = id)
            is TemplateElement.Circle -> element.copy(id = id)
            is TemplateElement.QRCode -> element.copy(id = id)
            is TemplateElement.Barcode -> element.copy(id = id)
            is TemplateElement.List -> element.copy(id = id)
            is TemplateElement.Table -> element.copy(id = id)
            is TemplateElement.Rectangle -> element.copy(id = id)
            is TemplateElement.Line -> element.copy(id = id)
        }
    }
}
