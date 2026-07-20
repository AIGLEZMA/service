package me.aiglez.service.ui.templates.editor

import me.aiglez.service.domain.models.TemplateBorderStyle
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.models.TemplateTableCell
import me.aiglez.service.domain.models.templateTableCellKey

private const val MaxTableRows = 40
private const val MaxTableColumns = 12

fun TemplateElement.withTableRows(value: Int): TemplateElement {
    return when (this) {
        is TemplateElement.Table -> {
            val nextRows = value.coerceIn(1, MaxTableRows)
            copy(
                rows = nextRows,
                headerRows = headerRows.coerceIn(0, nextRows),
                cells = cells.filterKeys { key -> tableCellCoordinates(key)?.first?.let { it < nextRows } == true },
            )
        }
        else -> this
    }
}

fun TemplateElement.withTableColumns(value: Int): TemplateElement {
    return when (this) {
        is TemplateElement.Table -> {
            val nextColumns = value.coerceIn(1, MaxTableColumns)
            copy(
                columns = nextColumns,
                cells = cells.filterKeys { key -> tableCellCoordinates(key)?.second?.let { it < nextColumns } == true },
            )
        }
        else -> this
    }
}

fun TemplateElement.withTableHeaderRows(value: Int): TemplateElement {
    return when (this) {
        is TemplateElement.Table -> copy(headerRows = value.coerceIn(0, rows))
        else -> this
    }
}

fun TemplateElement.withTableFontFamily(value: String): TemplateElement = updateTable { copy(fontFamily = value) }
fun TemplateElement.withTableFontSize(value: Float): TemplateElement = updateTable { copy(fontSize = value.coerceAtLeast(1f)) }
fun TemplateElement.withTableTextColor(value: String): TemplateElement = updateTable { copy(color = value) }
fun TemplateElement.withTableBackground(value: String): TemplateElement = updateTable { copy(backgroundColor = value) }
fun TemplateElement.withTableHeaderBackground(value: String): TemplateElement = updateTable { copy(headerBackgroundColor = value) }
fun TemplateElement.withTableHeaderColor(value: String): TemplateElement = updateTable { copy(headerColor = value) }
fun TemplateElement.withTableAlternateRowColor(value: String): TemplateElement = updateTable { copy(alternateRowColor = value) }
fun TemplateElement.withTableUseAlternateRows(value: Boolean): TemplateElement = updateTable { copy(useAlternateRows = value) }
fun TemplateElement.withTableTextAlign(value: String): TemplateElement = updateTable { copy(textAlign = value) }
fun TemplateElement.withTableVerticalAlign(value: String): TemplateElement = updateTable { copy(verticalAlign = value) }
fun TemplateElement.withTablePadding(value: Float): TemplateElement = updateTable { copy(padding = value.coerceAtLeast(0f)) }
fun TemplateElement.withTableBorderColor(value: String): TemplateElement = updateTable { copy(borderColor = value) }
fun TemplateElement.withTableBorderWidth(value: Float): TemplateElement = updateTable { copy(borderWidth = value.coerceAtLeast(0f)) }
fun TemplateElement.withTableBorderStyle(value: TemplateBorderStyle): TemplateElement = updateTable { copy(borderStyle = value) }
fun TemplateElement.withTableBorderRadius(value: Float): TemplateElement = updateTable { copy(borderRadius = value.coerceAtLeast(0f)) }
fun TemplateElement.withTableCellBorderColor(value: String): TemplateElement = updateTable { copy(cellBorderColor = value) }
fun TemplateElement.withTableCellBorderWidth(value: Float): TemplateElement = updateTable { copy(cellBorderWidth = value.coerceAtLeast(0f)) }

fun TemplateElement.withTableCellText(row: Int, column: Int, value: String): TemplateElement {
    return updateTableCell(row, column) { copy(text = value) }
}

fun TemplateElement.withTableCellBackground(row: Int, column: Int, value: String): TemplateElement {
    return updateTableCell(row, column) { copy(backgroundColor = value) }
}

fun TemplateElement.withTableCellTextColor(row: Int, column: Int, value: String): TemplateElement {
    return updateTableCell(row, column) { copy(color = value) }
}

fun TemplateElement.withTableCellBorderColor(row: Int, column: Int, value: String): TemplateElement {
    return updateTableCell(row, column) { copy(borderColor = value) }
}

fun TemplateElement.withTableCellBorderWidth(row: Int, column: Int, value: Float): TemplateElement {
    return updateTableCell(row, column) { copy(borderWidth = value.coerceAtLeast(0f)) }
}

fun TemplateElement.withTableCellTextAlign(row: Int, column: Int, value: String): TemplateElement {
    return updateTableCell(row, column) { copy(textAlign = value) }
}

fun TemplateElement.withTableCellVerticalAlign(row: Int, column: Int, value: String): TemplateElement {
    return updateTableCell(row, column) { copy(verticalAlign = value) }
}

fun TemplateElement.withTableCellPadding(row: Int, column: Int, value: Float): TemplateElement {
    return updateTableCell(row, column) { copy(padding = value.coerceAtLeast(0f)) }
}

private fun TemplateElement.updateTable(transform: TemplateElement.Table.() -> TemplateElement.Table): TemplateElement {
    return when (this) {
        is TemplateElement.Table -> transform()
        else -> this
    }
}

private fun TemplateElement.updateTableCell(
    row: Int,
    column: Int,
    transform: TemplateTableCell.() -> TemplateTableCell,
): TemplateElement {
    return when (this) {
        is TemplateElement.Table -> {
            if (row !in 0 until rows || column !in 0 until columns) return this
            val key = templateTableCellKey(row, column)
            copy(cells = cells + (key to (cells[key] ?: TemplateTableCell()).transform()))
        }
        else -> this
    }
}

private fun tableCellCoordinates(key: String): Pair<Int, Int>? {
    val row = key.substringBefore(":").toIntOrNull() ?: return null
    val column = key.substringAfter(":", missingDelimiterValue = "").toIntOrNull() ?: return null
    return row to column
}
