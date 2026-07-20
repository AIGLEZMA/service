package me.aiglez.service.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import me.aiglez.service.data.database.elementsAdapter
import me.aiglez.service.data.database.fieldsAdapter
import me.aiglez.service.data.database.valuesMapAdapter
import me.aiglez.service.data.repository.SeededRecordRepository
import me.aiglez.service.data.repository.SeededTemplateRepository
import me.aiglez.service.data.repository.SqlDelightRecordRepository
import me.aiglez.service.data.repository.SqlDelightTemplateRepository
import me.aiglez.service.database.AppDatabase
import me.aiglez.service.database.RecordEntity
import me.aiglez.service.database.SchemaEntity
import me.aiglez.service.database.TemplateEntity
import me.aiglez.service.domain.repository.RecordRepository
import me.aiglez.service.domain.repository.TemplateRepository
import me.aiglez.service.ui.records.*
import me.aiglez.service.ui.shell.SidebarViewModel
import me.aiglez.service.ui.templates.CompileViewModel
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
    single(createdAtStart = true) {
        AppDatabase(
            driver = get(), // Provided by platformModule
            SchemaEntityAdapter = SchemaEntity.Adapter(fieldsAdapter),
            RecordEntityAdapter = RecordEntity.Adapter(valuesMapAdapter),
            TemplateEntityAdapter = TemplateEntity.Adapter(elementsAdapter)
        )
    }
    single<RecordRepository> { SeededRecordRepository(SqlDelightRecordRepository(get(), get()), get()) }
    single<TemplateRepository> { SeededTemplateRepository(SqlDelightTemplateRepository(get(), get()), get(), get()) }
}

val uiModule = module {
    factory { SidebarViewModel(get()) }
    factory { DashboardViewModel(get(), get()) }
    factory { SchemaManagementViewModel(get()) }
    factory { (schemaId: String) -> SchemaCreateViewModel(schemaId, get()) }
    factory { (schemaId: String) -> RecordListViewModel(schemaId, get(), get()) }
    factory { (schemaId: String) -> RecordCreateViewModel(schemaId, get()) }
    factory { (templateId: String) -> CompileViewModel(templateId, get(), get(), get()) }
}

fun initKoin(additionalModules: List<Module> = emptyList()) {
    org.koin.core.context.startKoin {
        modules(coreModule, platformModule, dataModule, uiModule)
        modules(additionalModules)
    }
}
