package me.aiglez.service.ui.templates.editor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import me.aiglez.service.domain.models.AnchorX
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.models.TemplateElementType
import me.aiglez.service.domain.models.TemplateTableCell
import me.aiglez.service.domain.models.templateTableCellKey

class TemplateTableMutationsTest {
    @Test
    fun rowUpdatesClampHeaderRowsAndDropOutOfRangeCells() {
        val table = TemplateElement.Table(
            id = "table-1",
            x = 0f,
            y = 0f,
            rows = 5,
            columns = 3,
            headerRows = 4,
            cells = mapOf(
                templateTableCellKey(0, 0) to TemplateTableCell(text = "A"),
                templateTableCellKey(2, 1) to TemplateTableCell(text = "B"),
                templateTableCellKey(4, 2) to TemplateTableCell(text = "C"),
            ),
        )

        val updated = assertIs<TemplateElement.Table>(table.withTableRows(2))

        assertEquals(2, updated.rows)
        assertEquals(2, updated.headerRows)
        assertEquals(
            mapOf(templateTableCellKey(0, 0) to TemplateTableCell(text = "A")),
            updated.cells,
        )
    }

    @Test
    fun columnUpdatesClampAndDropOutOfRangeCells() {
        val table = TemplateElement.Table(
            id = "table-1",
            x = 0f,
            y = 0f,
            rows = 3,
            columns = 5,
            cells = mapOf(
                templateTableCellKey(0, 0) to TemplateTableCell(text = "A"),
                templateTableCellKey(1, 2) to TemplateTableCell(text = "B"),
                templateTableCellKey(2, 4) to TemplateTableCell(text = "C"),
            ),
        )

        val updated = assertIs<TemplateElement.Table>(table.withTableColumns(2))

        assertEquals(2, updated.columns)
        assertEquals(
            mapOf(templateTableCellKey(0, 0) to TemplateTableCell(text = "A")),
            updated.cells,
        )
    }

    @Test
    fun cellUpdatesCreateCellsAndIgnoreInvalidCoordinates() {
        val table = TemplateElement.Table(id = "table-1", x = 0f, y = 0f, rows = 2, columns = 2)

        val withText = assertIs<TemplateElement.Table>(table.withTableCellText(1, 1, "{{ data.total }}"))
        val withPadding = assertIs<TemplateElement.Table>(withText.withTableCellPadding(1, 1, -12f))
        val invalidUpdate = withPadding.withTableCellText(5, 1, "ignored")

        assertEquals("{{ data.total }}", withPadding.cells.getValue(templateTableCellKey(1, 1)).text)
        assertEquals(0f, withPadding.cells.getValue(templateTableCellKey(1, 1)).padding)
        assertEquals(withPadding, invalidUpdate)
    }

    @Test
    fun commonPropertiesAndBoundsApplyToTables() {
        val table = TemplateElement.Table(id = "table-1", x = 0f, y = 0f)

        val moved = assertIs<TemplateElement.Table>(
            table.withBounds(PageRect(x = 12f, y = 18f, width = 240f, height = 120f)),
        )
        val updated = assertIs<TemplateElement.Table>(
            moved.updateCommon(name = "Line items", anchorX = AnchorX.Right, opacity = 0.5f),
        )

        assertEquals(12f, moved.x)
        assertEquals(18f, moved.y)
        assertEquals(240f, moved.width)
        assertEquals(120f, moved.height)
        assertEquals("Line items", updated.name)
        assertEquals(AnchorX.Right, updated.anchorX)
        assertEquals(0.5f, updated.opacity)
    }

    @Test
    fun defaultTableElementUsesTableTypeAndPosition() {
        val element = assertIs<TemplateElement.Table>(
            createDefaultElement(TemplateElementType.Table, id = "table-1", x = 24f, y = 32f, zIndex = 7),
        )

        assertEquals("table-1", element.id)
        assertEquals("Table", element.name)
        assertEquals(24f, element.x)
        assertEquals(32f, element.y)
        assertEquals(7, element.zIndex)
    }
}
