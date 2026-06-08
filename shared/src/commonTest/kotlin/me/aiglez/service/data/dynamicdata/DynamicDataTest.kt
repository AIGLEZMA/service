package me.aiglez.service.data.dynamicdata

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DynamicDataTest {

    @Test
    fun `can create dynamic data with simple fields`() {
        val dynamicData = DynamicData(
            id = 1L,
            name = "Person",
            fields = listOf(
                DynamicDataField(
                    name = "Name",
                    optional = false,
                    type = DynamicDataFieldType.Text
                ),
                DynamicDataField(
                    name = "Age",
                    optional = true,
                    type = DynamicDataFieldType.Number
                ),
                DynamicDataField(
                    name = "Is Student",
                    optional = false,
                    type = DynamicDataFieldType.Boolean
                )
            )
        )

        assertEquals(1L, dynamicData.id)
        assertEquals("Person", dynamicData.name)
        assertEquals(3, dynamicData.fields.size)

        val nameField = dynamicData.fields[0]
        assertEquals("Name", nameField.name)
        assertFalse(nameField.optional)
        assertEquals(DynamicDataFieldType.Text, nameField.type)

        val ageField = dynamicData.fields[1]
        assertEquals("Age", ageField.name)
        assertTrue(ageField.optional)
        assertEquals(DynamicDataFieldType.Number, ageField.type)
    }

    @Test
    fun `can create list of text field`() {
        val field = DynamicDataField(
            name = "Skills",
            optional = true,
            type = DynamicDataFieldType.ListOf(
                itemType = DynamicDataFieldType.Text
            )
        )

        assertEquals("Skills", field.name)
        assertTrue(field.optional)

        val listType = assertIs<DynamicDataFieldType.ListOf>(field.type)
        assertEquals(DynamicDataFieldType.Text, listType.itemType)
    }

    @Test
    fun `can create list of dynamic data references`() {
        val field = DynamicDataField(
            name = "Addresses",
            optional = true,
            type = DynamicDataFieldType.ListOf(
                itemType = DynamicDataFieldType.DynamicDataRef(
                    dynamicDataId = 2L
                )
            )
        )

        val listType = assertIs<DynamicDataFieldType.ListOf>(field.type)
        val itemType = assertIs<DynamicDataFieldType.DynamicDataRef>(listType.itemType)

        assertEquals(2L, itemType.dynamicDataId)
    }
}