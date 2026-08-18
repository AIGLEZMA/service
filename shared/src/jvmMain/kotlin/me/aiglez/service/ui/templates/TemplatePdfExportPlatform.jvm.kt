package me.aiglez.service.ui.templates

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import me.aiglez.service.domain.models.TemplateBarcodeFormat
import me.aiglez.service.domain.models.TemplateBorderStyle
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.models.TemplateImageContentMode
import me.aiglez.service.domain.models.TemplateTextDirection
import me.aiglez.service.domain.models.TemplateTextStyle
import me.aiglez.service.domain.models.templateTableCellKey
import me.aiglez.service.ui.templates.editor.GeometryService
import me.aiglez.service.ui.templates.editor.PageRect
import me.aiglez.service.ui.templates.editor.TemplateEditorState
import me.aiglez.service.ui.templates.editor.TemplateExpressionContext
import me.aiglez.service.ui.templates.editor.recordExpressionContext
import me.aiglez.service.ui.templates.editor.renderLegacyPlaceholder
import me.aiglez.service.ui.templates.editor.renderTemplateText
import me.aiglez.service.ui.templates.editor.sampleSchemaExpressionContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.util.Matrix

actual suspend fun exportTemplatePdf(state: TemplateEditorState): TemplatePdfExportResult {
    val template = state.template ?: return TemplatePdfExportResult.Failed("No template is open.")
    val outputFile = choosePdfOutputFile(template.name) ?: return TemplatePdfExportResult.Cancelled
    return withContext(Dispatchers.Default) {
        runCatching {
            val context = state.exportExpressionContext()
            writeTemplatePdf(
                outputFile = outputFile,
                elements = state.document.elements,
                expressionContext = context,
                resolveExpressions = true,
                pageSize = template.pageSize,
            )
            TemplatePdfExportResult.Exported(outputFile.absolutePath)
        }.getOrElse { error ->
            TemplatePdfExportResult.Failed(error.message ?: "Unable to write PDF.", error)
        }
    }
}

actual fun templatePdfPreflightWarnings(state: TemplateEditorState): List<String> {
    val elements = state.document.elements.filter { it.visible }
    val pageDimensions = templatePageDimensions(state.template?.pageSize)
    return buildSet {
        if (elements.filterIsInstance<TemplateElement.Image>().any { !File(it.sourcePath).isFile }) {
            add("Une ou plusieurs images sont introuvables et ne seront pas dessinées.")
        }
        if (elements.any { it.opacity < 0.999f }) {
            add("La transparence partielle n’est pas encore reproduite exactement.")
        }
        if (elements.any { it.visibilityExpression.isNotBlank() }) {
            add("Les conditions de visibilité ne sont pas encore évaluées dans le PDF.")
        }
        if (elements.filterIsInstance<TemplateElement.Text>().any { it.letterSpacing != 0f }) {
            add("L’espacement personnalisé des lettres sera ignoré.")
        }
        if (elements.filterIsInstance<TemplateElement.Text>().any { it.textDirection == TemplateTextDirection.Rtl }) {
            add("La mise en forme de droite à gauche peut différer de l’éditeur.")
        }
        if (elements.any(::usesCustomPdfFont)) {
            add("Les polices personnalisées seront remplacées par une police système compatible PDF.")
        }
        if (elements.any(::hasUnsupportedPdfBorder)) {
            add("Les bordures non pleines et les coins arrondis seront simplifiés.")
        }
        if (elements.filterIsInstance<TemplateElement.Text>().any { textLikelyOverflows(it) }) {
            add("Un ou plusieurs textes risquent d’être tronqués dans leur cadre.")
        }
        if (elements.any { element ->
                val bounds = GeometryService.getElementBounds(element)
                bounds.x < 0f || bounds.y < 0f ||
                    bounds.right > pageDimensions.width || bounds.bottom > pageDimensions.height
            }
        ) {
            add("Un ou plusieurs éléments dépassent de la page et seront coupés.")
        }
    }.take(7)
}

private fun usesCustomPdfFont(element: TemplateElement): Boolean = when (element) {
    is TemplateElement.Text -> !element.fontFamily.equals("Inter", ignoreCase = true)
    is TemplateElement.List -> !element.fontFamily.equals("Inter", ignoreCase = true)
    is TemplateElement.Table -> !element.fontFamily.equals("Inter", ignoreCase = true)
    else -> false
}

private fun hasUnsupportedPdfBorder(element: TemplateElement): Boolean = when (element) {
    is TemplateElement.Text -> element.borderStyle != TemplateBorderStyle.Solid || element.borderRadius > 0f
    is TemplateElement.Image -> element.borderRadius > 0f
    is TemplateElement.List -> element.borderStyle != TemplateBorderStyle.Solid || element.borderRadius > 0f
    is TemplateElement.Table -> element.borderStyle != TemplateBorderStyle.Solid || element.borderRadius > 0f
    is TemplateElement.Rectangle -> element.borderStyle != TemplateBorderStyle.Solid || element.borderRadius > 0f
    is TemplateElement.Circle -> element.borderStyle != TemplateBorderStyle.Solid
    else -> false
}

private fun textLikelyOverflows(element: TemplateElement.Text): Boolean {
    val text = element.staticText ?: return false
    val usableWidth = (element.width - element.padding * 2f).coerceAtLeast(1f)
    val usableHeight = (element.height - element.padding * 2f).coerceAtLeast(1f)
    val charactersPerLine = (usableWidth / (element.fontSize.coerceAtLeast(1f) * 0.55f)).toInt().coerceAtLeast(1)
    val availableLines = (usableHeight / (element.fontSize.coerceAtLeast(1f) * element.lineHeight.coerceAtLeast(1f)))
        .toInt()
        .coerceAtLeast(1)
    val requiredLines = text.lines().sumOf { line -> (line.length + charactersPerLine - 1) / charactersPerLine }
    return requiredLines > availableLines
}

internal fun writeTemplatePdf(
    outputFile: File,
    elements: List<TemplateElement>,
    expressionContext: TemplateExpressionContext,
    resolveExpressions: Boolean,
    pageSize: String = "A4",
) {
    outputFile.parentFile?.mkdirs()
    PDDocument().use { document ->
        val pageRectangle = pdfPageRectangle(pageSize)
        val sourceDimensions = templatePageDimensions(pageSize)
        val page = PDPage(pageRectangle)
        document.addPage(page)
        val fonts = loadPdfFonts(document)
        PDPageContentStream(document, page).use { content ->
            content.drawPageBackground(pageRectangle.width, pageRectangle.height)
            content.saveGraphicsState()
            content.transform(
                Matrix.getScaleInstance(
                    pageRectangle.width / sourceDimensions.width,
                    pageRectangle.height / sourceDimensions.height,
                ),
            )
            val previousPageHeight = pdfCoordinatePageHeight.get()
            pdfCoordinatePageHeight.set(sourceDimensions.height)
            try {
                elements
                    .asSequence()
                    .filter { it.visible }
                    .sortedBy { it.zIndex }
                    .forEach { element ->
                        content.drawTemplateElement(document, fonts, element, expressionContext, resolveExpressions)
                    }
            } finally {
                pdfCoordinatePageHeight.set(previousPageHeight)
            }
            content.restoreGraphicsState()
        }
        document.save(outputFile)
    }
}

private fun TemplateEditorState.exportExpressionContext(): TemplateExpressionContext {
    return if (selectedPreviewRecords.isNotEmpty()) {
        recordExpressionContext(
            schemas = availableSchemas,
            recordsBySchemaId = selectedPreviewRecords,
            primarySchema = schema,
        )
    } else {
        sampleSchemaExpressionContext(
            schemas = availableSchemas,
            primarySchema = schema,
        )
    }
}

private fun choosePdfOutputFile(templateName: String): File? {
    val defaultName = "${templateName.ifBlank { "template" }.safePdfFileName()}.pdf"
    val dialog = FileDialog(null as Frame?, "Export PDF", FileDialog.SAVE).apply {
        file = defaultName
        isVisible = true
    }
    val directory = dialog.directory ?: return null
    val fileName = dialog.file ?: return null
    val normalizedName = if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf"
    return File(directory, normalizedName)
}

private fun String.safePdfFileName(): String {
    return trim()
        .replace(Regex("""[^\p{L}\p{N}._-]+"""), "-")
        .trim('-', '.', '_')
        .ifBlank { "template" }
}

private fun PDPageContentStream.drawPageBackground(width: Float, height: Float) {
    setNonStrokingColor(java.awt.Color.WHITE)
    addRect(0f, 0f, width, height)
    fill()
}

private fun PDPageContentStream.drawTemplateElement(
    document: PDDocument,
    fonts: PdfFonts,
    element: TemplateElement,
    expressionContext: TemplateExpressionContext,
    resolveExpressions: Boolean,
) {
    val bounds = GeometryService.getElementBounds(element)
    when (element) {
        is TemplateElement.Text -> withElementTransform(element, bounds) {
            drawTextElement(element, bounds, expressionContext, resolveExpressions, fonts)
        }
        is TemplateElement.Rectangle -> withElementTransform(element, bounds) {
            drawFilledRect(bounds, parsePdfColor(element.fillColor).withOpacity(element.opacity))
            drawRectBorder(bounds, parsePdfColor(element.borderColor).withOpacity(element.opacity), element.borderWidth)
        }
        is TemplateElement.Circle -> withElementTransform(element, bounds) {
            drawEllipse(bounds, parsePdfColor(element.fillColor).withOpacity(element.opacity), fill = true)
            drawEllipse(bounds, parsePdfColor(element.borderColor).withOpacity(element.opacity), fill = false, strokeWidth = element.borderWidth)
        }
        is TemplateElement.Image -> withElementTransform(element, bounds) {
            drawImageElement(document, element, bounds)
        }
        is TemplateElement.QRCode -> withElementTransform(element, bounds) {
            drawQrElement(element, bounds, expressionContext, resolveExpressions)
        }
        is TemplateElement.Barcode -> withElementTransform(element, bounds) {
            drawBarcodeElement(element, bounds, expressionContext, resolveExpressions, fonts)
        }
        is TemplateElement.List -> withElementTransform(element, bounds) {
            drawListElement(element, bounds, expressionContext, resolveExpressions, fonts)
        }
        is TemplateElement.Table -> withElementTransform(element, bounds) {
            drawTableElement(element, bounds, expressionContext, resolveExpressions, fonts)
        }
        is TemplateElement.Line -> drawLineElement(element)
    }
}

private inline fun PDPageContentStream.withElementTransform(
    element: TemplateElement,
    bounds: PageRect,
    draw: PDPageContentStream.() -> Unit,
) {
    saveGraphicsState()
    if (kotlin.math.abs(element.rotation) > 0.001f) {
        val centerX = bounds.x + bounds.width / 2f
        val centerY = pdfY(bounds.y + bounds.height / 2f)
        transform(Matrix.getTranslateInstance(centerX, centerY))
        transform(Matrix.getRotateInstance(Math.toRadians(-element.rotation.toDouble()), 0f, 0f))
        transform(Matrix.getTranslateInstance(-centerX, -centerY))
    }
    draw()
    restoreGraphicsState()
}

private fun PDPageContentStream.drawTextElement(
    element: TemplateElement.Text,
    bounds: PageRect,
    expressionContext: TemplateExpressionContext,
    resolveExpressions: Boolean,
    fonts: PdfFonts,
) {
    val background = parsePdfColor(element.backgroundColor).withOpacity(element.opacity)
    val border = parsePdfColor(element.borderColor).withOpacity(element.opacity)
    drawFilledRect(bounds, background)
    drawRectBorder(bounds, border, element.borderWidth)
    val rawText = element.staticText
        ?: element.placeholderTag?.let { placeholder ->
            if (resolveExpressions) renderLegacyPlaceholder(placeholder, expressionContext) else placeholder
        }
        ?: ""
    val text = if (resolveExpressions) renderTemplateText(rawText, expressionContext) else rawText
    drawTextBox(
        text = text,
        bounds = bounds.inset(element.padding),
        font = fonts.fontFor(
            weight = element.fontWeight,
            italic = element.italic || element.fontStyle != TemplateTextStyle.Normal,
        ),
        fontSize = element.fontSize,
        color = parsePdfColor(element.color).withOpacity(element.opacity),
        align = element.textAlign,
        verticalAlign = element.verticalAlign,
        lineHeightMultiplier = element.lineHeight,
        underline = element.underline,
    )
}

private fun PDPageContentStream.drawImageElement(
    document: PDDocument,
    element: TemplateElement.Image,
    bounds: PageRect,
) {
    drawFilledRect(bounds, parsePdfColor(element.backgroundColor).withOpacity(element.opacity))
    val file = File(element.sourcePath)
    if (file.isFile) {
        val image = PDImageXObject.createFromFileByContent(file, document)
        val frame = if (element.contentMode == TemplateImageContentMode.Stretch) {
            bounds
        } else {
            imageDestinationRect(bounds, image.width, image.height, element.contentMode, element.alignment)
        }
        saveGraphicsState()
        addRect(bounds.x, pdfY(bounds.bottom), bounds.width, bounds.height)
        clip()
        drawImage(image, frame.x, pdfY(frame.bottom), frame.width, frame.height)
        restoreGraphicsState()
    }
    drawRectBorder(bounds, parsePdfColor(element.borderColor).withOpacity(element.opacity), element.borderWidth)
}

private fun PDPageContentStream.drawQrElement(
    element: TemplateElement.QRCode,
    bounds: PageRect,
    expressionContext: TemplateExpressionContext,
    resolveExpressions: Boolean,
) {
    val text = if (resolveExpressions) renderTemplateText(element.text, expressionContext) else element.text
    val matrix = generateTemplateQrMatrix(text, element.quietZone)
    drawFilledRect(bounds, parsePdfColor(element.backgroundColor).withOpacity(element.opacity))
    if (matrix != null) {
        drawMatrix(matrix.width, matrix.height, matrix.cells, bounds, parsePdfColor(element.foregroundColor).withOpacity(element.opacity))
    }
    drawRectBorder(bounds, parsePdfColor(element.borderColor).withOpacity(element.opacity), element.borderWidth)
}

private fun PDPageContentStream.drawBarcodeElement(
    element: TemplateElement.Barcode,
    bounds: PageRect,
    expressionContext: TemplateExpressionContext,
    resolveExpressions: Boolean,
    fonts: PdfFonts,
) {
    val text = if (resolveExpressions) renderTemplateText(element.text, expressionContext) else element.text
    val matrix = generateTemplateBarcodeMatrix(text, element.format, element.quietZone)
    drawFilledRect(bounds, parsePdfColor(element.backgroundColor).withOpacity(element.opacity))
    val codeHeight = if (element.showText) (bounds.height - element.fontSize - 6f).coerceAtLeast(12f) else bounds.height
    val codeBounds = bounds.copy(height = codeHeight)
    if (matrix != null) {
        drawMatrix(matrix.width, matrix.height, matrix.cells, codeBounds, parsePdfColor(element.foregroundColor).withOpacity(element.opacity))
    }
    if (element.showText) {
        drawTextBox(
            text = text,
            bounds = PageRect(bounds.x, bounds.y + codeHeight + 2f, bounds.width, bounds.height - codeHeight - 2f),
            font = fonts.fontFor(400),
            fontSize = element.fontSize,
            color = parsePdfColor(element.foregroundColor).withOpacity(element.opacity),
            align = "center",
            verticalAlign = "middle",
        )
    }
    drawRectBorder(bounds, parsePdfColor(element.borderColor).withOpacity(element.opacity), element.borderWidth)
}

private fun PDPageContentStream.drawListElement(
    element: TemplateElement.List,
    bounds: PageRect,
    expressionContext: TemplateExpressionContext,
    resolveExpressions: Boolean,
    fonts: PdfFonts,
) {
    drawFilledRect(bounds, parsePdfColor(element.backgroundColor).withOpacity(element.opacity))
    drawRectBorder(bounds, parsePdfColor(element.borderColor).withOpacity(element.opacity), element.borderWidth)
    val items = templateListValues(
        value = if (resolveExpressions) expressionContext.data[element.fieldSlug] else element.fieldSlug.toTemplateListPlaceholder(),
        separator = element.itemSeparator,
    )
        .take(element.maxItems.coerceAtLeast(1))
        .map { item -> element.prefix + truncateListItem(item, element.maxItemLength) + element.suffix }
        .filter { it.isNotBlank() }
    if (items.isEmpty()) return

    val columns = element.columns.coerceIn(1, 6)
    val padding = element.padding.coerceAtLeast(0f)
    val columnGap = element.columnGap.coerceAtLeast(0f)
    val itemSpacing = element.itemSpacing.coerceAtLeast(0f)
    val availableWidth = (bounds.width - padding * 2f - columnGap * (columns - 1)).coerceAtLeast(1f)
    val columnWidth = availableWidth / columns
    val itemsPerColumn = ((items.size + columns - 1) / columns).coerceAtLeast(1)
    val lineHeight = element.fontSize * 1.2f
    val color = parsePdfColor(element.color).withOpacity(element.opacity)

    for (column in 0 until columns) {
        var y = bounds.y + padding
        val x = bounds.x + padding + column * (columnWidth + columnGap)
        val start = column * itemsPerColumn
        val end = min(start + itemsPerColumn, items.size)
        for (index in start until end) {
            if (y + lineHeight > bounds.bottom - padding / 2f) break
            drawTextBox(
                text = items[index],
                bounds = PageRect(x, y, columnWidth, lineHeight),
                font = fonts.fontFor(element.fontWeight),
                fontSize = element.fontSize,
                color = color,
            )
            y += lineHeight + itemSpacing
        }
    }
}

private fun PDPageContentStream.drawTableElement(
    element: TemplateElement.Table,
    bounds: PageRect,
    expressionContext: TemplateExpressionContext,
    resolveExpressions: Boolean,
    fonts: PdfFonts,
) {
    val rows = element.rows.coerceAtLeast(1)
    val columns = element.columns.coerceAtLeast(1)
    val rowHeight = bounds.height / rows
    val columnWidth = bounds.width / columns
    for (row in 0 until rows) {
        for (column in 0 until columns) {
            val key = templateTableCellKey(row, column)
            val cell = element.cells[key]
            val isHeader = row < element.headerRows
            val cellBounds = PageRect(
                x = bounds.x + column * columnWidth,
                y = bounds.y + row * rowHeight,
                width = if (column == columns - 1) bounds.right - (bounds.x + column * columnWidth) else columnWidth,
                height = if (row == rows - 1) bounds.bottom - (bounds.y + row * rowHeight) else rowHeight,
            )
            val background = cell?.backgroundColor
                ?: if (isHeader) element.headerBackgroundColor
                else if (element.useAlternateRows && row % 2 == 1) element.alternateRowColor
                else element.backgroundColor
            val textColor = cell?.color ?: if (isHeader) element.headerColor else element.color
            val rawText = cell?.text ?: defaultTableCellText(row, column, element.headerRows)
            val text = if (resolveExpressions) renderTemplateText(rawText, expressionContext) else rawText
            drawFilledRect(cellBounds, parsePdfColor(background).withOpacity(element.opacity))
            drawRectBorder(
                bounds = cellBounds,
                color = parsePdfColor(cell?.borderColor ?: element.cellBorderColor).withOpacity(element.opacity),
                width = (cell?.borderWidth ?: element.cellBorderWidth).coerceAtLeast(0f),
            )
            drawTextBox(
                text = text,
                bounds = cellBounds.inset((cell?.padding ?: element.padding).coerceAtLeast(0f)),
                font = fonts.fontFor(cell?.fontWeight ?: if (isHeader) 700 else element.fontWeight),
                fontSize = element.fontSize,
                color = parsePdfColor(textColor).withOpacity(element.opacity),
                align = cell?.textAlign ?: element.textAlign,
                verticalAlign = cell?.verticalAlign ?: element.verticalAlign,
            )
        }
    }
    drawRectBorder(bounds, parsePdfColor(element.borderColor).withOpacity(element.opacity), element.borderWidth)
}

private fun PDPageContentStream.drawLineElement(element: TemplateElement.Line) {
    val color = parsePdfColor("#111827").withOpacity(element.opacity)
    if (color.alpha <= 0f || element.thickness <= 0f) return
    setStrokingColor(color.toAwt())
    setLineWidth(element.thickness)
    moveTo(element.x1, pdfY(element.y1))
    lineTo(element.x2, pdfY(element.y2))
    stroke()
}

private fun PDPageContentStream.drawMatrix(
    width: Int,
    height: Int,
    cells: List<Boolean>,
    bounds: PageRect,
    color: PdfColor,
) {
    if (width <= 0 || height <= 0 || color.alpha <= 0f) return
    val cellSize = min(bounds.width / width, bounds.height / height).coerceAtLeast(0.01f)
    val startX = bounds.x + (bounds.width - width * cellSize) / 2f
    val startY = bounds.y + (bounds.height - height * cellSize) / 2f
    setNonStrokingColor(color.toAwt())
    for (row in 0 until height) {
        for (column in 0 until width) {
            if (cells.getOrNull(row * width + column) == true) {
                addRect(startX + column * cellSize, pdfY(startY + (row + 1) * cellSize), cellSize, cellSize)
            }
        }
    }
    fill()
}

private fun PDPageContentStream.drawFilledRect(bounds: PageRect, color: PdfColor) {
    if (color.alpha <= 0f) return
    setNonStrokingColor(color.toAwt())
    addRect(bounds.x, pdfY(bounds.bottom), bounds.width, bounds.height)
    fill()
}

private fun PDPageContentStream.drawRectBorder(bounds: PageRect, color: PdfColor, width: Float) {
    if (color.alpha <= 0f || width <= 0f) return
    setStrokingColor(color.toAwt())
    setLineWidth(width)
    addRect(bounds.x, pdfY(bounds.bottom), bounds.width, bounds.height)
    stroke()
}

private fun PDPageContentStream.drawEllipse(
    bounds: PageRect,
    color: PdfColor,
    fill: Boolean,
    strokeWidth: Float = 1f,
) {
    if (color.alpha <= 0f) return
    if (!fill && strokeWidth <= 0f) return
    val k = 0.5522848f
    val rx = bounds.width / 2f
    val ry = bounds.height / 2f
    val cx = bounds.x + rx
    val cy = pdfY(bounds.y + ry)
    moveTo(cx + rx, cy)
    curveTo(cx + rx, cy + k * ry, cx + k * rx, cy + ry, cx, cy + ry)
    curveTo(cx - k * rx, cy + ry, cx - rx, cy + k * ry, cx - rx, cy)
    curveTo(cx - rx, cy - k * ry, cx - k * rx, cy - ry, cx, cy - ry)
    curveTo(cx + k * rx, cy - ry, cx + rx, cy - k * ry, cx + rx, cy)
    if (fill) {
        setNonStrokingColor(color.toAwt())
        fill()
    } else {
        setStrokingColor(color.toAwt())
        setLineWidth(strokeWidth)
        stroke()
    }
}

private fun PDPageContentStream.drawTextBox(
    text: String,
    bounds: PageRect,
    font: PDFont,
    fontSize: Float,
    color: PdfColor,
    align: String = "left",
    verticalAlign: String = "top",
    lineHeightMultiplier: Float = 1.2f,
    underline: Boolean = false,
) {
    if (text.isBlank() || color.alpha <= 0f || bounds.width <= 0f || bounds.height <= 0f) return
    val safeFontSize = fontSize.coerceAtLeast(1f)
    val lineHeight = safeFontSize * lineHeightMultiplier.coerceAtLeast(1f)
    val lines = wrapText(text, font, safeFontSize, bounds.width)
        .take(max(1, (bounds.height / lineHeight).toInt()))
    if (lines.isEmpty()) return
    val totalTextHeight = lines.size * lineHeight
    val topOffset = when (verticalAlign.lowercase()) {
        "middle", "center" -> ((bounds.height - totalTextHeight) / 2f).coerceAtLeast(0f)
        "bottom" -> (bounds.height - totalTextHeight).coerceAtLeast(0f)
        else -> 0f
    }
    setNonStrokingColor(color.toAwt())
    lines.forEachIndexed { index, line ->
        val lineWidth = textWidth(line, font, safeFontSize)
        val textX = when (align.lowercase()) {
            "center" -> bounds.x + (bounds.width - lineWidth) / 2f
            "right", "end" -> bounds.right - lineWidth
            else -> bounds.x
        }.coerceAtLeast(bounds.x)
        val baselineY = pdfY(bounds.y + topOffset + safeFontSize + index * lineHeight)
        beginText()
        setFont(font, safeFontSize)
        newLineAtOffset(textX, baselineY)
        showText(line.toPdfSafeText(font))
        endText()
        if (underline) {
            setStrokingColor(color.toAwt())
            setLineWidth((safeFontSize / 14f).coerceAtLeast(0.5f))
            moveTo(textX, baselineY - 2f)
            lineTo(textX + lineWidth, baselineY - 2f)
            stroke()
        }
    }
}

private fun wrapText(text: String, font: PDFont, fontSize: Float, maxWidth: Float): List<String> {
    val lines = mutableListOf<String>()
    text.replace("\r\n", "\n").replace('\r', '\n').split('\n').forEach { paragraph ->
        if (paragraph.isBlank()) {
            lines += ""
            return@forEach
        }
        var current = ""
        paragraph.split(Regex("""\s+""")).forEach { word ->
            val candidate = if (current.isBlank()) word else "$current $word"
            if (textWidth(candidate, font, fontSize) <= maxWidth) {
                current = candidate
            } else {
                if (current.isNotBlank()) lines += current
                current = word
                while (textWidth(current, font, fontSize) > maxWidth && current.length > 1) {
                    val split = findFittingPrefix(current, font, fontSize, maxWidth)
                    lines += current.take(split)
                    current = current.drop(split)
                }
            }
        }
        if (current.isNotBlank()) lines += current
    }
    return lines
}

private fun findFittingPrefix(value: String, font: PDFont, fontSize: Float, maxWidth: Float): Int {
    var best = 1
    for (index in 1..value.length) {
        if (textWidth(value.take(index), font, fontSize) <= maxWidth) best = index else break
    }
    return best.coerceAtLeast(1)
}

private fun textWidth(text: String, font: PDFont, fontSize: Float): Float {
    return runCatching { font.getStringWidth(text.toPdfSafeText(font)) / 1000f * fontSize }
        .getOrDefault(text.length * fontSize * 0.55f)
}

private data class PdfFonts(
    val regular: PDFont,
    val bold: PDFont,
    val italic: PDFont,
    val boldItalic: PDFont,
) {
    fun fontFor(weight: Int, italic: Boolean = false): PDFont = when {
        weight >= 600 && italic -> boldItalic
        weight >= 600 -> bold
        italic -> this.italic
        else -> regular
    }
}

private fun PageRect.inset(amount: Float): PageRect {
    val safeAmount = amount.coerceAtLeast(0f)
    return PageRect(
        x = x + safeAmount,
        y = y + safeAmount,
        width = (width - safeAmount * 2f).coerceAtLeast(0f),
        height = (height - safeAmount * 2f).coerceAtLeast(0f),
    )
}

private val pdfCoordinatePageHeight = ThreadLocal.withInitial { PageHeight }

private fun pdfY(topLeftY: Float): Float = pdfCoordinatePageHeight.get() - topLeftY

private data class PdfColor(
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Float,
) {
    fun toAwt(): java.awt.Color = java.awt.Color(red, green, blue)
    fun withOpacity(opacity: Float): PdfColor = copy(alpha = alpha * opacity.coerceIn(0f, 1f))
}

private fun parsePdfColor(value: String): PdfColor {
    val clean = value.trim().removePrefix("#")
    val argb = when (clean.length) {
        6 -> "FF$clean"
        8 -> clean
        else -> "FF111827"
    }
    val parsed = argb.toLongOrNull(16) ?: 0xFF111827
    return PdfColor(
        red = ((parsed shr 16) and 0xFF).toInt(),
        green = ((parsed shr 8) and 0xFF).toInt(),
        blue = (parsed and 0xFF).toInt(),
        alpha = (((parsed shr 24) and 0xFF).toInt() / 255f).coerceIn(0f, 1f),
    )
}

private fun String.toPdfSafeText(font: PDFont): String {
    return map { char ->
        if (char == '\n' || char == '\t' || runCatching { font.getStringWidth(char.toString()) }.isSuccess) char else '?'
    }.joinToString("")
}

private fun pdfPageRectangle(pageSize: String): PDRectangle = when (pageSize.lowercase()) {
    "a5" -> PDRectangle(PDRectangle.A5.width, PDRectangle.A5.height)
    "letter" -> PDRectangle(PDRectangle.LETTER.width, PDRectangle.LETTER.height)
    else -> PDRectangle(PDRectangle.A4.width, PDRectangle.A4.height)
}

private fun loadPdfFonts(document: PDDocument): PdfFonts {
    val regular = loadSystemFont(document, "Arial.ttf", "DejaVuSans.ttf", "NotoSans-Regular.ttf")
        ?: PDType1Font.HELVETICA
    val bold = loadSystemFont(document, "Arial Bold.ttf", "Arialbd.ttf", "DejaVuSans-Bold.ttf", "NotoSans-Bold.ttf")
        ?: PDType1Font.HELVETICA_BOLD
    val italic = loadSystemFont(document, "Arial Italic.ttf", "Ariali.ttf", "DejaVuSans-Oblique.ttf", "NotoSans-Italic.ttf")
        ?: PDType1Font.HELVETICA_OBLIQUE
    val boldItalic = loadSystemFont(
        document,
        "Arial Bold Italic.ttf",
        "Arialbi.ttf",
        "DejaVuSans-BoldOblique.ttf",
        "NotoSans-BoldItalic.ttf",
    ) ?: PDType1Font.HELVETICA_BOLD_OBLIQUE
    return PdfFonts(regular, bold, italic, boldItalic)
}

private fun loadSystemFont(document: PDDocument, vararg names: String): PDFont? {
    val roots = buildList {
        add(File("/System/Library/Fonts/Supplemental"))
        add(File("/Library/Fonts"))
        add(File("/usr/share/fonts/truetype/dejavu"))
        add(File("/usr/share/fonts/truetype/noto"))
        System.getenv("WINDIR")?.let { add(File(it, "Fonts")) }
    }
    names.forEach { name ->
        roots.forEach { root ->
            val file = File(root, name)
            if (file.isFile) {
                runCatching { PDType0Font.load(document, file) }.getOrNull()?.let { return it }
            }
        }
    }
    return null
}
