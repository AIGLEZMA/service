package me.aiglez.service.data.database

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import me.aiglez.service.domain.models.TemplateElement

class AdaptersTest {
    @Test
    fun templateElementsRoundTripWithoutTypeDiscriminatorCollision() {
        val elements = listOf<TemplateElement>(
            TemplateElement.Text(
                id = "title",
                x = 12f,
                y = 24f,
                staticText = "Hello",
            ),
            TemplateElement.Rectangle(
                id = "panel",
                x = 8f,
                y = 16f,
            ),
        )

        val encoded = elementsAdapter.encode(elements)
        val decoded = elementsAdapter.decode(encoded)

        assertEquals(elements, decoded)
        assertIs<TemplateElement.Text>(decoded.first())
    }

    @Test
    fun schemaFieldsWithoutAliasesRemainReadable() {
        val decoded = fieldsAdapter.decode(
            """[{"id":"field-name","name":"Name","slug":"name","type":"TEXT"}]""",
        )

        assertEquals(emptyList(), decoded.single().aliases)
    }
}
