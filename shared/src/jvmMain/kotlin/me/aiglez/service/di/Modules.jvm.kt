package me.aiglez.service.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import me.aiglez.service.cache.AppDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val platformModule: Module = module {
    single<SqlDriver> {
        val dbFile = File("service.db")
        val dbExists = dbFile.exists()

        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:service.db")

        if (!dbExists) {
            AppDatabase.Schema.create(driver)
        }

        driver
    }
}