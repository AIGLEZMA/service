package me.aiglez.service.cache

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import me.aiglez.service.data.dynamicdata.DynamicData
import me.aiglez.service.data.dynamicdata.DynamicDataField
import me.aiglez.service.data.dynamicdata.DynamicDataFieldType
import me.aiglez.service.data.dynamicdata.DynamicDataInstance
import me.aiglez.service.data.dynamicdata.DynamicDataValue
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DatabaseTest {

    private lateinit var database: Database

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(
            url = JdbcSqliteDriver.IN_MEMORY,
            schema = AppDatabase.Schema
        )
        database = Database(driver)
    }

    @Test
    fun `can save and read dynamic data`() {
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
                    name = "Addresses",
                    optional = true,
                    type = DynamicDataFieldType.ListOf(
                        itemType = DynamicDataFieldType.DynamicDataRef(dynamicDataId = 2L)
                    )
                )
            )
        )

        database.saveDynamicData(dynamicData)

        assertEquals(dynamicData, database.getDynamicData(1L))
        assertEquals(listOf(dynamicData), database.getAllDynamicData())
    }

    @Test
    fun `can update dynamic data and replace fields`() {
        database.saveDynamicData(
            DynamicData(
                id = 1L,
                name = "Person",
                fields = listOf(
                    DynamicDataField(
                        name = "Name",
                        type = DynamicDataFieldType.Text
                    )
                )
            )
        )

        val updatedDynamicData = DynamicData(
            id = 1L,
            name = "Customer",
            fields = listOf(
                DynamicDataField(
                    name = "Balance",
                    optional = true,
                    type = DynamicDataFieldType.Decimal
                )
            )
        )

        database.saveDynamicData(updatedDynamicData)

        assertEquals(updatedDynamicData, database.getDynamicData(1L))
    }

    @Test
    fun `can delete dynamic data`() {
        database.saveDynamicData(
            DynamicData(
                id = 1L,
                name = "Person",
                fields = listOf(
                    DynamicDataField(
                        name = "Name",
                        type = DynamicDataFieldType.Text
                    )
                )
            )
        )

        database.deleteDynamicData(1L)

        assertNull(database.getDynamicData(1L))
    }

    @Test
    fun `can save and read dynamic data instance`() {
        val instance = DynamicDataInstance(
            id = 1L,
            dynamicDataId = 10L,
            values = mapOf(
                "Name" to DynamicDataValue.Text("Ada"),
                "Age" to DynamicDataValue.Number(37L),
                "Balance" to DynamicDataValue.Decimal(124.50),
                "Active" to DynamicDataValue.Boolean(true),
                "Manager" to DynamicDataValue.DynamicDataRef(5L),
                "Tags" to DynamicDataValue.ListOf(
                    listOf(
                        DynamicDataValue.Text("priority"),
                        null,
                        DynamicDataValue.Number(2L),
                    )
                ),
                "Optional" to null,
            )
        )

        database.saveDynamicDataInstance(instance)

        assertEquals(instance, database.getDynamicDataInstance(1L))
        assertEquals(listOf(instance), database.getDynamicDataInstances(10L))
    }

    @Test
    fun `can update dynamic data instance and replace values`() {
        database.saveDynamicDataInstance(
            DynamicDataInstance(
                id = 1L,
                dynamicDataId = 10L,
                values = mapOf(
                    "Name" to DynamicDataValue.Text("Ada"),
                    "Age" to DynamicDataValue.Number(37L),
                )
            )
        )

        val updatedInstance = DynamicDataInstance(
            id = 1L,
            dynamicDataId = 10L,
            values = mapOf(
                "Name" to DynamicDataValue.Text("Grace"),
                "Active" to DynamicDataValue.Boolean(false),
            )
        )

        database.saveDynamicDataInstance(updatedInstance)

        assertEquals(updatedInstance, database.getDynamicDataInstance(1L))
    }

    @Test
    fun `can delete dynamic data instance`() {
        database.saveDynamicDataInstance(
            DynamicDataInstance(
                id = 1L,
                dynamicDataId = 10L,
                values = mapOf("Name" to DynamicDataValue.Text("Ada"))
            )
        )

        database.deleteDynamicDataInstance(1L)

        assertNull(database.getDynamicDataInstance(1L))
        assertEquals(emptyList(), database.getDynamicDataInstances(10L))
    }

    @Test
    fun `deleting dynamic data deletes its instances`() {
        database.saveDynamicData(
            DynamicData(
                id = 1L,
                name = "Person",
                fields = listOf(DynamicDataField(name = "Name", type = DynamicDataFieldType.Text))
            )
        )
        database.saveDynamicDataInstance(
            DynamicDataInstance(
                id = 1L,
                dynamicDataId = 1L,
                values = mapOf("Name" to DynamicDataValue.Text("Ada"))
            )
        )

        database.deleteDynamicData(1L)

        assertEquals(emptyList(), database.getDynamicDataInstances(1L))
    }
}
