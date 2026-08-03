package me.aiglez.service.data.csv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField

class DataRecordCsvImporterTest {

    @Test
    fun `imports valid rows using suggested mappings`() {
        val schema = personSchema()
        val source = assertIs<CsvImportReadResult.Success>(
            DataRecordCsvImporter.read(
                fileName = "people.csv",
                content = "Name,Age,Score,Tags\nAda,37,19.5,admin;editor\nBob,42,18,reader",
            ),
        ).source

        val mappings = DataRecordCsvImporter.suggestMappings(schema, source)
        val preview = DataRecordCsvImporter.preview(schema, source, mappings)

        assertTrue(preview.canImport)
        assertEquals(2, preview.records.size)
        assertEquals("Ada", preview.records[0]["name"])
        assertEquals("37", preview.records[0]["age"])
        assertEquals("19.5", preview.records[0]["score"])
        assertEquals("admin\neditor", preview.records[0]["tags"])
    }

    @Test
    fun `detects semicolon and tab delimiters`() {
        val semicolon = assertIs<CsvImportReadResult.Success>(
            DataRecordCsvImporter.read("people.csv", "Name;Age\nAda;37"),
        ).source
        val tab = assertIs<CsvImportReadResult.Success>(
            DataRecordCsvImporter.read("people.csv", "Name\tAge\nAda\t37"),
        ).source

        assertEquals(';', semicolon.delimiter)
        assertEquals('\t', tab.delimiter)
    }

    @Test
    fun `reports invalid numeric values with source row numbers`() {
        val schema = personSchema()
        val source = assertIs<CsvImportReadResult.Success>(
            DataRecordCsvImporter.read(
                "people.csv",
                "Name,Age,Score,Tags\nAda,invalid,not-a-decimal,admin",
            ),
        ).source

        val preview = DataRecordCsvImporter.preview(
            schema,
            source,
            DataRecordCsvImporter.suggestMappings(schema, source),
        )

        assertEquals(false, preview.canImport)
        assertEquals(2, preview.errors.size)
        assertTrue(preview.errors.all { it.rowNumber == 2 })
    }

    @Test
    fun `allows intentionally unmapped fields with a warning`() {
        val schema = personSchema()
        val source = assertIs<CsvImportReadResult.Success>(
            DataRecordCsvImporter.read("people.csv", "Name,Age,Score,Tags\nAda,37,19.5,admin"),
        ).source
        val mappings = DataRecordCsvImporter.suggestMappings(schema, source) + ("field-score" to null)

        val preview = DataRecordCsvImporter.preview(schema, source, mappings)

        assertTrue(preview.canImport)
        assertEquals(1, preview.warnings.size)
        assertEquals(null, preview.records.single()["score"])
    }

    @Test
    fun `rejects duplicate and blank headers`() {
        assertIs<CsvImportReadResult.Failure>(
            DataRecordCsvImporter.read("people.csv", "Name,name\nAda,Lovelace"),
        )
        assertIs<CsvImportReadResult.Failure>(
            DataRecordCsvImporter.read("people.csv", "Name,\nAda,37"),
        )
    }

    private fun personSchema(): DataSchema = DataSchema(
        id = "people",
        name = "People",
        fields = listOf(
            SchemaField("field-name", "Name", "name", FieldType.TEXT),
            SchemaField("field-age", "Age", "age", FieldType.NUMBER),
            SchemaField("field-score", "Score", "score", FieldType.DOUBLE),
            SchemaField("field-tags", "Tags", "tags", FieldType.LIST),
        ),
    )
}
