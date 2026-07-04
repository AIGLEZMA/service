package me.aiglez.service.ui.templates.editor

import kotlin.test.Test
import kotlin.test.assertEquals
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField

class TemplateExpressionsTest {
    @Test
    fun rendersVariablesAndFallbacks() {
        val context = TemplateExpressionContext(
            data = mapOf(
                "title" to "Invoice",
                "empty" to "",
                "stock" to 0,
            ),
        )

        assertEquals("Header: Invoice", renderTemplateText("Header: {{ data.title }}", context))
        assertEquals("Header: N/A", renderTemplateText("Header: {{ default(data.empty, \"N/A\") }}", context))
        assertEquals("Stock: 0", renderTemplateText("Stock: {{ default(data.stock, \"N/A\") }}", context))
    }

    @Test
    fun rendersNestedConditionalsAndComparisons() {
        val context = TemplateExpressionContext(
            data = mapOf(
                "status" to "paid",
                "amount" to "125.50",
            ),
        )

        assertEquals("Paid", renderTemplateText("{{ if(eq(data.status, \"paid\"), \"Paid\", \"Pending\") }}", context))
        assertEquals("High", renderTemplateText("{{ if(gt(data.amount, 100), \"High\", \"Standard\") }}", context))
    }

    @Test
    fun rendersFormattingHelpers() {
        val context = TemplateExpressionContext(
            data = mapOf(
                "name" to "  ada  ",
                "total" to 42,
                "discount" to 0.15,
                "tags" to listOf("alpha", "beta"),
            ),
        )

        assertEquals("ADA", renderTemplateText("{{ upper(trim(data.name)) }}", context))
        assertEquals("\$42.00", renderTemplateText("{{ currency(data.total, \"USD\") }}", context))
        assertEquals("15%", renderTemplateText("{{ percent(data.discount) }}", context))
        assertEquals("alpha / beta", renderTemplateText("{{ join(data.tags, \" / \") }}", context))
    }

    @Test
    fun keepsUnknownFunctionsVisible() {
        assertEquals("{{ missing(data.title) }}", renderTemplateText("{{ missing(data.title) }}"))
    }

    @Test
    fun buildsContextFromRegisteredRecordValues() {
        val schema = DataSchema(
            id = "schema-1",
            name = "Invoice",
            fields = listOf(
                SchemaField(id = "field-title", name = "Title", slug = "title", type = FieldType.TEXT),
                SchemaField(id = "field-total", name = "Total", slug = "total", type = FieldType.DOUBLE),
                SchemaField(id = "field-items", name = "Items", slug = "items", type = FieldType.LIST),
            ),
        )
        val record = DataRecord(
            id = "record-1",
            schemaId = schema.id,
            values = mapOf(
                "title" to "July invoice",
                "total" to "125.50",
                "items" to "Design\nBuild, Ship",
            ),
        )

        val context = recordExpressionContext(schema, record)

        assertEquals("July invoice", renderTemplateText("{{ data.title }}", context))
        assertEquals("\$125.50", renderTemplateText("{{ currency(data.total, \"USD\") }}", context))
        assertEquals("Design / Build / Ship", renderTemplateText("{{ join(data.items, \" / \") }}", context))
    }

    @Test
    fun rendersSchemaQualifiedFieldsFromRegisteredRecords() {
        val customerSchema = DataSchema(
            id = "schema-customer",
            name = "Customer Profile",
            fields = listOf(
                SchemaField(id = "field-name", name = "Full Name", slug = "full_name", type = FieldType.TEXT),
            ),
        )
        val orderSchema = DataSchema(
            id = "schema-order",
            name = "Order",
            fields = listOf(
                SchemaField(id = "field-total", name = "Total", slug = "total", type = FieldType.DOUBLE),
            ),
        )
        val context = recordExpressionContext(
            schemas = listOf(customerSchema, orderSchema),
            recordsBySchemaId = mapOf(
                customerSchema.id to DataRecord(
                    id = "customer-1",
                    schemaId = customerSchema.id,
                    values = mapOf("full_name" to "Ada Lovelace"),
                ),
                orderSchema.id to DataRecord(
                    id = "order-1",
                    schemaId = orderSchema.id,
                    values = mapOf("total" to "250"),
                ),
            ),
            primarySchema = customerSchema,
        )

        assertEquals("Ada Lovelace", renderTemplateText("{{ CustomerProfile.FullName }}", context))
        assertEquals("\$250.00", renderTemplateText("{{ currency(Order.Total, \"USD\") }}", context))
    }

    @Test
    fun findsSchemaRootsInTemplateExpressions() {
        assertEquals(
            setOf("CustomerProfile", "Order"),
            referencedExpressionRoots("Hello {{ CustomerProfile.FullName }} {{ currency(Order.Total, \"USD\") }}"),
        )
    }
}



