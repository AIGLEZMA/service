package me.aiglez.service.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField
import me.aiglez.service.domain.repository.RecordRepository

class SeededRecordRepository(
    private val delegate: RecordRepository,
    private val logger: Logger,
) : RecordRepository by delegate {

    private val seedMutex = Mutex()
    private var hasSeeded = false

    override fun getActiveSchemas(): Flow<List<DataSchema>> {
        return delegate.getActiveSchemas().onStart { seedIfNeeded() }
    }

    override fun getActiveRecords(schemaId: String): Flow<List<DataRecord>> {
        return delegate.getActiveRecords(schemaId).onStart { seedIfNeeded() }
    }

    private suspend fun seedIfNeeded() = withContext(Dispatchers.Default) {
        seedMutex.withLock {
            if (hasSeeded) return@withLock

            val activeSchemas = delegate.getActiveSchemas().first()
            val activeSchemaIds = activeSchemas.map { it.id }.toSet()
            if (DeprecatedInterventionSchemaId in activeSchemaIds) {
                delegate.archiveSchema(DeprecatedInterventionSchemaId)
            }
            demoSchemas.filterNot { it.id in activeSchemaIds }.forEach { schema ->
                delegate.saveSchema(schema)
            }

            demoRecordsBySchemaId.forEach { (schemaId, records) ->
                val activeRecordIds = delegate.getActiveRecords(schemaId).first().map { it.id }.toSet()
                records.filterNot { it.id in activeRecordIds }.forEach { record ->
                    delegate.saveRecord(record)
                }
            }

            hasSeeded = true
            logger.i { "Seeded template editor test data." }
        }
    }
}

private const val ServiceOrderSchemaId = "test_service_order"
private const val CustomerProfileSchemaId = "test_customer_profile"
private const val ProductCatalogSchemaId = "test_product_catalog"
private const val CsvImportTestSchemaId = "test_csv_import"
internal const val ClientSchemaId = "modele_client"
internal const val IntervenantSchemaId = "modele_intervenant"
private const val DeprecatedInterventionSchemaId = "modele_intervention"

private val demoSchemas = listOf(
    DataSchema(
        id = ClientSchemaId,
        name = "Client",
        fields = listOf(
            SchemaField("field-client-nom", "Nom", "nom", FieldType.TEXT),
            SchemaField("field-client-numero", "Numéro", "numero", FieldType.TEXT),
        ),
    ),
    DataSchema(
        id = IntervenantSchemaId,
        name = "Intervenant",
        fields = listOf(
            SchemaField("field-intervenant-nom", "Nom", "nom", FieldType.TEXT),
            SchemaField("field-intervenant-prenom", "Prénom", "prenom", FieldType.TEXT),
        ),
    ),
    DataSchema(
        id = ServiceOrderSchemaId,
        name = "Test Service Order",
        fields = listOf(
            SchemaField("field-order-number", "Order Number", "order_number", FieldType.TEXT),
            SchemaField("field-customer-name", "Customer Name", "customer_name", FieldType.TEXT),
            SchemaField("field-customer-email", "Customer Email", "customer_email", FieldType.TEXT),
            SchemaField("field-priority-level", "Priority Level", "priority_level", FieldType.NUMBER),
            SchemaField("field-total-amount", "Total Amount", "total_amount", FieldType.DOUBLE),
            SchemaField("field-service-items", "Service Items", "service_items", FieldType.LIST),
            SchemaField("field-tracking-code", "Tracking Code", "tracking_code", FieldType.TEXT),
            SchemaField("field-qr-payload", "QR Payload", "qr_payload", FieldType.TEXT),
            SchemaField("field-customer-ref", "Customer Reference", "customer_ref", FieldType.REFERENCE, CustomerProfileSchemaId),
            SchemaField("field-product-ref", "Product Reference", "product_ref", FieldType.REFERENCE, ProductCatalogSchemaId),
            SchemaField("field-notes", "Notes", "notes", FieldType.TEXT),
        ),
    ),
    DataSchema(
        id = CustomerProfileSchemaId,
        name = "Test Customer Profile",
        fields = listOf(
            SchemaField("field-full-name", "Full Name", "full_name", FieldType.TEXT),
            SchemaField("field-email", "Email", "email", FieldType.TEXT),
            SchemaField("field-loyalty-points", "Loyalty Points", "loyalty_points", FieldType.NUMBER),
            SchemaField("field-segment", "Segment", "segment", FieldType.TEXT),
        ),
    ),
    DataSchema(
        id = ProductCatalogSchemaId,
        name = "Test Product Catalog",
        fields = listOf(
            SchemaField("field-sku", "SKU", "sku", FieldType.TEXT),
            SchemaField("field-product-name", "Product Name", "product_name", FieldType.TEXT),
            SchemaField("field-unit-price", "Unit Price", "unit_price", FieldType.DOUBLE),
            SchemaField("field-stock-count", "Stock Count", "stock_count", FieldType.NUMBER),
            SchemaField("field-features", "Features", "features", FieldType.LIST),
        ),
    ),
    DataSchema(
        id = CsvImportTestSchemaId,
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
                ClientSchemaId,
            ),
        ),
    ),
)

private val demoRecordsBySchemaId = mapOf(
    ClientSchemaId to listOf(
        DataRecord(
            id = "client_dupont",
            schemaId = ClientSchemaId,
            values = mapOf(
                "nom" to "Dupont",
                "numero" to "CL-001",
            ),
        ),
        DataRecord(
            id = "client_martin",
            schemaId = ClientSchemaId,
            values = mapOf(
                "nom" to "Martin",
                "numero" to "CL-002",
            ),
        ),
    ),
    IntervenantSchemaId to listOf(
        DataRecord(
            id = "intervenant_karim_benali",
            schemaId = IntervenantSchemaId,
            values = mapOf(
                "nom" to "Benali",
                "prenom" to "Karim",
            ),
        ),
        DataRecord(
            id = "intervenant_sara_alaoui",
            schemaId = IntervenantSchemaId,
            values = mapOf(
                "nom" to "Alaoui",
                "prenom" to "Sara",
            ),
        ),
    ),
    CustomerProfileSchemaId to listOf(
        DataRecord(
            id = "test_customer_ada",
            schemaId = CustomerProfileSchemaId,
            values = mapOf(
                "full_name" to "Ada Lovelace",
                "email" to "ada@example.test",
                "loyalty_points" to "1280",
                "segment" to "Enterprise",
            ),
        ),
        DataRecord(
            id = "test_customer_grace",
            schemaId = CustomerProfileSchemaId,
            values = mapOf(
                "full_name" to "Grace Hopper",
                "email" to "grace@example.test",
                "loyalty_points" to "640",
                "segment" to "Research",
            ),
        ),
        DataRecord(
            id = "test_customer_katherine",
            schemaId = CustomerProfileSchemaId,
            values = mapOf(
                "full_name" to "Katherine Johnson",
                "email" to "katherine@example.test",
                "loyalty_points" to "920",
                "segment" to "Priority",
            ),
        ),
    ),
    ProductCatalogSchemaId to listOf(
        DataRecord(
            id = "test_product_audit",
            schemaId = ProductCatalogSchemaId,
            values = mapOf(
                "sku" to "SVC-AUDIT",
                "product_name" to "Workflow Audit",
                "unit_price" to "249.95",
                "stock_count" to "24",
                "features" to "Discovery\nProcess map\nRecommendations",
            ),
        ),
        DataRecord(
            id = "test_product_launch",
            schemaId = ProductCatalogSchemaId,
            values = mapOf(
                "sku" to "SVC-LAUNCH",
                "product_name" to "Launch Package",
                "unit_price" to "899.00",
                "stock_count" to "8",
                "features" to "Setup\nTraining\nGo-live support",
            ),
        ),
        DataRecord(
            id = "test_product_care",
            schemaId = ProductCatalogSchemaId,
            values = mapOf(
                "sku" to "SVC-CARE",
                "product_name" to "Care Plan",
                "unit_price" to "149.50",
                "stock_count" to "50",
                "features" to "Monitoring\nMonthly review\nPriority queue",
            ),
        ),
    ),
    ServiceOrderSchemaId to listOf(
        DataRecord(
            id = "test_order_001",
            schemaId = ServiceOrderSchemaId,
            values = mapOf(
                "order_number" to "SO-2026-001",
                "customer_name" to "Ada Lovelace",
                "customer_email" to "ada@example.test",
                "priority_level" to "5",
                "total_amount" to "1249.75",
                "service_items" to "Workflow audit\nAutomation blueprint\nLaunch support",
                "tracking_code" to "SO2026001",
                "qr_payload" to "https://example.test/orders/SO-2026-001",
                "customer_ref" to "test_customer_ada",
                "product_ref" to "test_product_launch",
                "notes" to "Requires priority handling and executive summary.",
            ),
        ),
        DataRecord(
            id = "test_order_002",
            schemaId = ServiceOrderSchemaId,
            values = mapOf(
                "order_number" to "SO-2026-002",
                "customer_name" to "Grace Hopper",
                "customer_email" to "grace@example.test",
                "priority_level" to "2",
                "total_amount" to "349.45",
                "service_items" to "Care plan\nMonthly review\nTraining add-on",
                "tracking_code" to "SO2026002",
                "qr_payload" to "https://example.test/orders/SO-2026-002",
                "customer_ref" to "test_customer_grace",
                "product_ref" to "test_product_care",
                "notes" to "Standard fulfillment window.",
            ),
        ),
        DataRecord(
            id = "test_order_003",
            schemaId = ServiceOrderSchemaId,
            values = mapOf(
                "order_number" to "SO-2026-003",
                "customer_name" to "Katherine Johnson",
                "customer_email" to "katherine@example.test",
                "priority_level" to "4",
                "total_amount" to "599.90",
                "service_items" to "Discovery session\nProcess map\nRecommendations",
                "tracking_code" to "SO2026003",
                "qr_payload" to "https://example.test/orders/SO-2026-003",
                "customer_ref" to "test_customer_katherine",
                "product_ref" to "test_product_audit",
                "notes" to "Include printed checklist with delivery.",
            ),
        ),
    ),
)
