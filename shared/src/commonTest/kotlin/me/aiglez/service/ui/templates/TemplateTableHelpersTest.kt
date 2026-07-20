package me.aiglez.service.ui.templates

import kotlin.test.Test
import kotlin.test.assertEquals
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.models.TemplateTableCell
import me.aiglez.service.domain.models.templateTableCellKey

class TemplateTableHelpersTest {
    @Test
    fun defaultTableCellTextNamesHeadersAndBodyCells() {
        assertEquals("Header 2", defaultTableCellText(row = 0, column = 1, headerRows = 1))
        assertEquals("Cell 3,2", defaultTableCellText(row = 2, column = 1, headerRows = 1))
    }

    @Test
    fun tableCellsContributeReferencedExpressionRoots() {
        val table = TemplateElement.Table(
            id = "table-1",
            x = 0f,
            y = 0f,
            cells = mapOf(
                templateTableCellKey(0, 0) to TemplateTableCell(text = "{{ data.total }}"),
                templateTableCellKey(0, 1) to TemplateTableCell(text = "{{ CustomerProfile.FullName }}"),
            ),
        )

        assertEquals(setOf("data", "CustomerProfile"), table.referencedExpressionRoots())
    }
}
