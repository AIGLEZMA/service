package me.aiglez.service.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import me.aiglez.service.data.database.elementsAdapter
import me.aiglez.service.data.database.fieldsAdapter
import me.aiglez.service.data.database.valuesMapAdapter
import me.aiglez.service.database.AppDatabase
import me.aiglez.service.database.RecordEntity
import me.aiglez.service.database.SchemaEntity
import me.aiglez.service.database.TemplateEntity

import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule: Module

val coreModule = module {
    single<Logger> {
        Logger(
            config = StaticConfig(logWriterList = listOf(platformLogWriter())),
            tag = "ServiceApp"
        )
    }
}

val dataModule = module {
    single {
        AppDatabase(
            driver = get(), // Provided by platformModule
            SchemaEntityAdapter = SchemaEntity.Adapter(fieldsAdapter),
            RecordEntityAdapter = RecordEntity.Adapter(valuesMapAdapter),
            TemplateEntityAdapter = TemplateEntity.Adapter(elementsAdapter)
        )
    }
}

fun initKoin(additionalModules: List<Module> = emptyList()) {
    org.koin.core.context.startKoin {
        modules(coreModule, platformModule, dataModule)
        modules(additionalModules)
    }
}