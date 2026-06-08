package me.aiglez.service.cache

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import me.aiglez.service.data.dynamicdata.DynamicData
import me.aiglez.service.data.dynamicdata.DynamicDataField
import me.aiglez.service.data.dynamicdata.DynamicDataFieldType
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
}
