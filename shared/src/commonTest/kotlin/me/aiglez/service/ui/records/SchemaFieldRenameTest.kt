package me.aiglez.service.ui.records

import kotlin.test.Test
import kotlin.test.assertEquals
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField

class SchemaFieldRenameTest {
    @Test
    fun persistedFieldKeepsItsSlugAndPreviousName() {
        val field = schemaField(name = "Full Name", slug = "full_name")

        val renamed = renameSchemaField(
            field = field,
            name = "Customer Name",
            persistedName = "Full Name",
        )

        assertEquals("Customer Name", renamed.name)
        assertEquals("full_name", renamed.slug)
        assertEquals(listOf("Full Name"), renamed.aliases)
    }

    @Test
    fun draftFieldUpdatesItsSlug() {
        val field = schemaField(name = "Champ 1", slug = "champ_1")

        val renamed = renameSchemaField(
            field = field,
            name = "Customer Name",
            persistedName = null,
        )

        assertEquals("customer_name", renamed.slug)
        assertEquals(emptyList(), renamed.aliases)
    }

    @Test
    fun repeatedEditsOnlyKeepSavedNamesAsAliases() {
        val field = schemaField(
            name = "Customer Name",
            slug = "full_name",
            aliases = listOf("Full Name"),
        )

        val firstEdit = renameSchemaField(field, "Billing", persistedName = "Customer Name")
        val secondEdit = renameSchemaField(firstEdit, "Billing Name", persistedName = "Customer Name")

        assertEquals(listOf("Full Name", "Customer Name"), secondEdit.aliases)
    }

    private fun schemaField(
        name: String,
        slug: String,
        aliases: List<String> = emptyList(),
    ) = SchemaField(
        id = "field-name",
        name = name,
        slug = slug,
        type = FieldType.TEXT,
        aliases = aliases,
    )
}
