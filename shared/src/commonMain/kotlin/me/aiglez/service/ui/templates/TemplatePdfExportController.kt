package me.aiglez.service.ui.templates

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.aiglez.service.ui.templates.editor.TemplateEditorState

internal class TemplatePdfExportController(
    private val scope: CoroutineScope,
    private val state: () -> TemplateEditorState,
    private val updateState: ((TemplateEditorState) -> TemplateEditorState) -> Unit,
    private val logger: Logger,
) {
    fun export() {
        val current = state()
        if (current.template == null || current.isExporting) return
        val warnings = templatePdfPreflightWarnings(current)
        if (warnings.isNotEmpty()) {
            updateState { it.copy(pdfExportWarnings = warnings) }
            return
        }
        performExport()
    }

    fun confirmExport() {
        updateState { it.copy(pdfExportWarnings = emptyList()) }
        performExport()
    }

    fun cancelExport() {
        updateState { it.copy(pdfExportWarnings = emptyList(), message = "Export PDF annulé.") }
    }

    fun exportPreview() {
        val current = state()
        when {
            current.previewSchemaIds.isEmpty() -> updateState {
                it.copy(message = "Ce modèle ne contient aucun champ de données. Modifiez-le avant de générer un PDF.")
            }
            !current.canGeneratePreviewPdf -> updateState {
                it.copy(message = "Choisissez toutes les données requises avant de générer le PDF.")
            }
            else -> export()
        }
    }

    private fun performExport() {
        val current = state()
        if (current.template == null || current.isExporting) return
        scope.launch {
            updateState { it.copy(isExporting = true, message = "Préparation de l’export PDF…") }
            try {
                when (val result = exportTemplatePdf(current)) {
                    TemplatePdfExportResult.Cancelled -> updateState { it.copy(message = "Export PDF annulé.") }
                    is TemplatePdfExportResult.Exported -> {
                        logger.i { "Exported PDF to ${result.path}" }
                        updateState { it.copy(message = "PDF exporté : ${result.path}") }
                    }
                    is TemplatePdfExportResult.Failed -> {
                        logger.e(result.cause) { "PDF export failed" }
                        updateState { it.copy(message = "Échec de l’export PDF : ${result.message}") }
                    }
                }
            } finally {
                updateState { it.copy(isExporting = false) }
            }
        }
    }
}
