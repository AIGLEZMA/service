package me.aiglez.service.data.dynamicdata

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DynamicDataCsvImporterTest {

    @Test
    fun `imports valid csv rows with inferred mappings`() {
        val dynamicData = personDynamicData()
        val result = DynamicDataCsvImporter.read(
            fileName = "people.csv",
            content = "Name,Age,Active\nAda,37,yes\nBob,,no",
        )
        val source = assertIs<CsvImportReadResult.Success>(result).source
        val mappings = DynamicDataCsvImporter.suggestMappings(dynamicData, source)

        val preview = DynamicDataCsvImporter.preview(
            dynamicData = dynamicData,
            source = source,
            mappings = mappings,
        )

        assertTrue(preview.canImport)
        assertEquals(2, preview.instances.size)
        assertEquals(DynamicDataValue.Text("Ada"), preview.instances[0].values["Name"])
        assertEquals(DynamicDataValue.Number(37), preview.instances[0].values["Age"])
        assertEquals(DynamicDataValue.Boolean(true), preview.instances[0].values["Active"])
        assertEquals(null, preview.instances[1].values["Age"])
    }

    @Test
    fun `detects semicolon delimited csv`() {
        val result = DynamicDataCsvImporter.read(
            fileName = "people.csv",
            content = "Name;Age;Active\nAda;37;oui",
        )

        val source = assertIs<CsvImportReadResult.Success>(result).source

        assertEquals(';', source.delimiter)
        assertEquals(listOf("Name", "Age", "Active"), source.headers)
    }

    @Test
    fun `blocks import when required field is not mapped`() {
        val dynamicData = personDynamicData()
        val source = assertIs<CsvImportReadResult.Success>(
            DynamicDataCsvImporter.read(
                fileName = "people.csv",
                content = "Name,Age,Active\nAda,37,yes",
            )
        ).source
        val mappings = DynamicDataCsvImporter.suggestMappings(dynamicData, source) + ("Name" to null)

        val preview = DynamicDataCsvImporter.preview(
            dynamicData = dynamicData,
            source = source,
            mappings = mappings,
        )

        assertEquals(false, preview.canImport)
        assertEquals(1, preview.errors.size)
        assertEquals("Name", preview.errors.first().fieldName)
    }

    @Test
    fun `reports row level type errors`() {
        val dynamicData = personDynamicData()
        val source = assertIs<CsvImportReadResult.Success>(
            DynamicDataCsvImporter.read(
                fileName = "people.csv",
                content = "Name,Age,Active\nAda,abc,maybe",
            )
        ).source
        val mappings = DynamicDataCsvImporter.suggestMappings(dynamicData, source)

        val preview = DynamicDataCsvImporter.preview(
            dynamicData = dynamicData,
            source = source,
            mappings = mappings,
        )

        assertEquals(false, preview.canImport)
        assertEquals(2, preview.errors.size)
        assertTrue(preview.errors.all { it.rowNumber == 2 })
    }

    @Test
    fun `rejects duplicate headers`() {
        val result = DynamicDataCsvImporter.read(
            fileName = "people.csv",
            content = "Name,name,Active\nAda,Lovelace,yes",
        )

        assertIs<CsvImportReadResult.Failure>(result)
    }

    private fun personDynamicData(): DynamicData {
        return DynamicData(
            id = 7L,
            name = "People",
            fields = listOf(
                DynamicDataField(
                    name = "Name",
                    type = DynamicDataFieldType.Text,
                ),
                DynamicDataField(
                    name = "Age",
                    optional = true,
                    type = DynamicDataFieldType.Number,
                ),
                DynamicDataField(
                    name = "Active",
                    type = DynamicDataFieldType.Boolean,
                ),
            ),
        )
    }
}
