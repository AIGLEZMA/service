package me.aiglez.service.ui.templates

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.aiglez.service.domain.models.Template
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.models.TemplateTableCell
import me.aiglez.service.domain.models.TemplateTextDirection
import me.aiglez.service.domain.models.templateTableCellKey
import me.aiglez.service.ui.templates.editor.EditorDocument
import me.aiglez.service.ui.templates.editor.TemplateEditorState
import me.aiglez.service.ui.templates.editor.TemplateExpressionContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.text.PDFTextStripper

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
                staticText = "Facture été – {{ data.invoice }}",
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
            assertEquals(PDRectangle.A4.width, document.getPage(0).mediaBox.width, 0.1f)
            assertTrue("Facture été" in PDFTextStripper().getText(document))
        }
    }

    @Test
    fun writesSelectedPageFormat() {
        val formats = mapOf(
            "A5" to PDRectangle.A5,
            "Letter" to PDRectangle.LETTER,
        )

        formats.forEach { (pageSize, expected) ->
            val output = File("build/tmp/pdf-export-smoke/page-$pageSize.pdf")
            writeTemplatePdf(
                outputFile = output,
                elements = emptyList(),
                expressionContext = TemplateExpressionContext(),
                resolveExpressions = false,
                pageSize = pageSize,
            )

            PDDocument.load(output).use { document ->
                assertEquals(expected.width, document.getPage(0).mediaBox.width, 0.1f)
                assertEquals(expected.height, document.getPage(0).mediaBox.height, 0.1f)
            }
        }
    }

    @Test
    fun reportsUnsupportedAndMissingContentBeforeExport() {
        val text = TemplateElement.Text(
            id = "text",
            x = 10f,
            y = 10f,
            width = 40f,
            height = 10f,
            staticText = "A very long text that cannot fit",
            letterSpacing = 2f,
            textDirection = TemplateTextDirection.Rtl,
        )
        val image = TemplateElement.Image(
            id = "image",
            x = 10f,
            y = 40f,
            sourcePath = "build/does-not-exist.png",
        )
        val state = TemplateEditorState(
            template = Template("template", "Test", "schema", elements = listOf(text, image)),
            document = EditorDocument(elements = listOf(text, image)),
        )

        val warnings = templatePdfPreflightWarnings(state)

        assertTrue(warnings.any { "images" in it })
        assertTrue(warnings.any { "lettres" in it })
        assertTrue(warnings.any { "droite à gauche" in it })
        assertTrue(warnings.any { "tronqués" in it })
    }
}
