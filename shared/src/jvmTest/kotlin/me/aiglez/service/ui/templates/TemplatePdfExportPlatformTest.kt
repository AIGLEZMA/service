package me.aiglez.service.ui.templates

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.models.TemplateTableCell
import me.aiglez.service.domain.models.templateTableCellKey
import me.aiglez.service.ui.templates.editor.TemplateExpressionContext
import org.apache.pdfbox.pdmodel.PDDocument

class TemplatePdfExportPlatformTest {
    @Test
    fun writesReadablePdfForCoreTemplateElements() {
        val output = File("build/tmp/pdf-export-smoke/service-template-export.pdf").apply {
            parentFile.mkdirs()
        }
        val elements = listOf(
            TemplateElement.Text(
                id = "title",
                x = 48f,
                y = 48f,
                width = 260f,
                height = 48f,
                staticText = "Invoice {{ data.invoice }}",
                fontSize = 18f,
                fontWeight = 700,
            ),
            TemplateElement.Rectangle(
                id = "panel",
                x = 48f,
                y = 116f,
                width = 220f,
                height = 72f,
            ),
            TemplateElement.QRCode(
                id = "qr",
                x = 392f,
                y = 48f,
                width = 96f,
                height = 96f,
                text = "{{ data.url }}",
            ),
            TemplateElement.Barcode(
                id = "barcode",
                x = 48f,
                y = 220f,
                width = 240f,
                height = 72f,
                text = "{{ data.code }}",
            ),
            TemplateElement.Table(
                id = "table",
                x = 48f,
                y = 320f,
                width = 360f,
                height = 96f,
                rows = 2,
                columns = 2,
                cells = mapOf(
                    templateTableCellKey(0, 0) to TemplateTableCell(text = "Item"),
                    templateTableCellKey(0, 1) to TemplateTableCell(text = "Amount"),
                    templateTableCellKey(1, 0) to TemplateTableCell(text = "{{ data.item }}"),
                    templateTableCellKey(1, 1) to TemplateTableCell(text = "{{ data.amount }}"),
                ),
            ),
        )

        writeTemplatePdf(
            outputFile = output,
            elements = elements,
            expressionContext = TemplateExpressionContext(
                data = mapOf(
                    "invoice" to "A-100",
                    "url" to "https://example.com/A-100",
                    "code" to "ABC100",
                    "item" to "Service visit",
                    "amount" to "$42.00",
                ),
            ),
            resolveExpressions = true,
        )

        assertTrue(output.length() > 1_000)
        PDDocument.load(output).use { document ->
            assertEquals(1, document.numberOfPages)
        }
    }
}
