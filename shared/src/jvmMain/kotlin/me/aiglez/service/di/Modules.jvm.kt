package me.aiglez.service.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<SqlDriver> {
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:service.db")
        ensureDatabaseSchema(driver)
        driver
    }
}

private fun ensureDatabaseSchema(driver: SqlDriver) {
    driver.execute(null, "PRAGMA foreign_keys = ON", 0)
    driver.execute(
        identifier = null,
        sql = """
            CREATE TABLE IF NOT EXISTS SchemaEntity (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                fields TEXT NOT NULL,
                isArchived INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent(),
        parameters = 0,
    )
    driver.execute(
        identifier = null,
        sql = """
            CREATE TABLE IF NOT EXISTS RecordEntity (
                id TEXT NOT NULL PRIMARY KEY,
                schemaId TEXT NOT NULL,
                valuesMap TEXT NOT NULL,
                isArchived INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(schemaId) REFERENCES SchemaEntity(id) ON DELETE RESTRICT
            )
        """.trimIndent(),
        parameters = 0,
    )
    driver.execute(
        identifier = null,
        sql = """
            CREATE TABLE IF NOT EXISTS TemplateEntity (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                targetSchemaId TEXT NOT NULL,
                pageSize TEXT NOT NULL DEFAULT 'A4',
                elements TEXT NOT NULL,
                isArchived INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent(),
        parameters = 0,
    )
}
