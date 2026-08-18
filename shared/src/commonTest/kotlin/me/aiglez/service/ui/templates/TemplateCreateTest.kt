package me.aiglez.service.ui.templates

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField
import me.aiglez.service.domain.models.TemplateElement

class TemplateCreateTest {
    private val schema = DataSchema(
        id = "customer-profile",
        name = "Customer Profile",
        fields = listOf(
            SchemaField("field-name", "Full Name", "full_name", FieldType.TEXT),
            SchemaField("field-email", "Email", "email", FieldType.TEXT),
        ),
    )

    @Test
    fun createsBlankTemplateWithExplicitSettings() {
        val template = createTemplateDraft(
            name = "  Customer card  ",
            schema = schema,
            pageSize = "Letter",
            startingPoint = TemplateStartingPoint.Blank,
            idFactory = { prefix -> "$prefix-id" },
        )

        assertEquals("Customer card", template.name)
        assertEquals(schema.id, template.targetSchemaId)
        assertEquals("Letter", template.pageSize)
        assertTrue(template.elements.isEmpty())
    }

    @Test
    fun createsSimpleStarterFromSelectedSchema() {
        var nextId = 0
        val template = createTemplateDraft(
            name = "Customer card",
            schema = schema,
            pageSize = "A4",
            startingPoint = TemplateStartingPoint.Simple,
            idFactory = { prefix -> "$prefix-${nextId++}" },
        )

        assertEquals(4, template.elements.size)
        assertEquals(template.elements.size, template.elements.map { it.id }.distinct().size)
        val fieldTexts = template.elements.filterIsInstance<TemplateElement.Text>().mapNotNull { it.staticText }
        assertTrue(fieldTexts.any { "{{ customerprofile.full_name }}" in it })
        assertTrue(fieldTexts.any { "{{ customerprofile.email }}" in it })
    }

    @Test
    fun usesTheSelectedCanvasDimensions() {
        assertEquals(420f, templatePageDimensions("A5").width)
        assertEquals(595f, templatePageDimensions("A5").height)
        assertEquals(612f, templatePageDimensions("Letter").width)
        assertEquals(792f, templatePageDimensions("Letter").height)
    }
}
