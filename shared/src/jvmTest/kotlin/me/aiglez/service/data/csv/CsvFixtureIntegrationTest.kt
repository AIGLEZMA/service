package me.aiglez.service.data.csv

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField

class CsvFixtureIntegrationTest {

    @Test
    fun `valid delimiter fixtures are importable`() {
        val expectedCounts = mapOf(
            "contacts_valides.csv" to 3,
            "contacts_point_virgule.csv" to 2,
            "contacts_tabulations.csv" to 2,
        )

        expectedCounts.forEach { (fileName, expectedCount) ->
            val source = readFixture(fileName)
            val mappings = DataRecordCsvImporter.suggestMappings(schema, source)
            val preview = DataRecordCsvImporter.preview(schema, source, mappings)

            assertTrue(preview.canImport, "$fileName should be importable: ${preview.errors}")
            assertEquals(expectedCount, preview.records.size, fileName)
        }

        val semicolonSource = readFixture("contacts_point_virgule.csv")
        val semicolonPreview = DataRecordCsvImporter.preview(
            schema,
            semicolonSource,
            DataRecordCsvImporter.suggestMappings(schema, semicolonSource),
        )
        assertEquals("89.5", semicolonPreview.records.first()["score"])
    }

    @Test
    fun `manual mapping fixture becomes importable after column selection`() {
        val source = readFixture("contacts_mapping_manuel.csv")
        val mappings = mapOf(
            "field-csv-full-name" to "Contact",
            "field-csv-email" to "Mail",
            "field-csv-age" to "Years",
            "field-csv-score" to "Rating",
            "field-csv-tags" to "Groups",
            "field-csv-customer-reference" to "Client ID",
        )

        val preview = DataRecordCsvImporter.preview(schema, source, mappings)

        assertTrue(preview.canImport)
        assertEquals(2, preview.records.size)
        assertEquals("Hajar Fassi", preview.records.first()["full_name"])
    }

    @Test
    fun `invalid fixture reports its numeric errors`() {
        val source = readFixture("contacts_erreurs_types.csv")
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
    fun `duplicate header fixture is rejected while reading`() {
        val result = DataRecordCsvImporter.read(
            "contacts_entetes_dupliques.csv",
            fixtureFile("contacts_entetes_dupliques.csv").readText(),
        )

        assertIs<CsvImportReadResult.Failure>(result)
    }

    private fun readFixture(fileName: String): CsvImportSource {
        val result = DataRecordCsvImporter.read(fileName, fixtureFile(fileName).readText())
        return assertIs<CsvImportReadResult.Success>(result).source
    }

    private fun fixtureFile(fileName: String): File {
        val candidates = listOf(
            File("../test/csv/$fileName"),
            File("test/csv/$fileName"),
        )
        return candidates.firstOrNull { it.isFile }
            ?: error("CSV fixture not found: $fileName (working directory: ${File(".").absolutePath})")
    }

    private val schema = DataSchema(
        id = "test_csv_import",
        name = "Test Import CSV",
        fields = listOf(
            SchemaField("field-csv-full-name", "Full Name", "full_name", FieldType.TEXT),
            SchemaField("field-csv-email", "Email", "email", FieldType.TEXT),
            SchemaField("field-csv-age", "Age", "age", FieldType.NUMBER),
            SchemaField("field-csv-score", "Score", "score", FieldType.DOUBLE),
            SchemaField("field-csv-tags", "Tags", "tags", FieldType.LIST),
            SchemaField(
                "field-csv-customer-reference",
                "Customer Reference",
                "customer_reference",
                FieldType.REFERENCE,
                "modele_client",
            ),
        ),
    )
}
