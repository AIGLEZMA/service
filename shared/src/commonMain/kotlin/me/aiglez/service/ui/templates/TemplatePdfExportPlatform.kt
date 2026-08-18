package me.aiglez.service.ui.templates

import me.aiglez.service.ui.templates.editor.TemplateEditorState

sealed interface TemplatePdfExportResult {
    data class Exported(val path: String) : TemplatePdfExportResult
    data object Cancelled : TemplatePdfExportResult
    data class Failed(val message: String, val cause: Throwable? = null) : TemplatePdfExportResult
}

expect suspend fun exportTemplatePdf(state: TemplateEditorState): TemplatePdfExportResult
expect fun templatePdfPreflightWarnings(state: TemplateEditorState): List<String>
