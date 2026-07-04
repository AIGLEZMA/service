package me.aiglez.service.ui.templates.editor

import me.aiglez.service.domain.models.Template
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema

data class CanvasState(
    val zoom: Float = 1.15f,
    val snapEnabled: Boolean = true,
    val rulerUnit: String = "px",
    val nudgeDistance: Float = 1f,
    val snapThreshold: Float = 5f,
    val pageMargin: Float = 36f,
    val printableInset: Float = 24f,
    val bleedInset: Float = 9f,
    val trimInset: Float = 0f,
    val safeAreaInset: Float = 18f,
    val headerGuide: Float = 54f,
    val footerGuide: Float = 788f,
    val gridSize: Float = 10f,
    val documentColumns: Int = 3,
    val documentRows: Int = 4,
    val baselineGrid: Float = 12f,
    val customVerticalGuides: List<Float> = emptyList(),
    val customHorizontalGuides: List<Float> = emptyList(),
    val rulerVerticalGuides: List<Float> = emptyList(),
    val rulerHorizontalGuides: List<Float> = emptyList(),
    val showPageOutline: Boolean = true,
    val showRulers: Boolean = true,
    val showGrid: Boolean = true,
    val showGuides: Boolean = true,
    val showMargins: Boolean = true,
    val showBleed: Boolean = true,
    val showSafeArea: Boolean = true,
    val showPageShadow: Boolean = true,
    val snapToGrid: Boolean = true,
    val snapToObjects: Boolean = true,
    val snapToGuides: Boolean = true,
    val snapToMargins: Boolean = true,
    val snapToPageCenter: Boolean = true,
)

enum class CanvasMetric {
    PageMargin,
    PrintableInset,
    BleedInset,
    TrimInset,
    SafeAreaInset,
    HeaderGuide,
    FooterGuide,
    GridSize,
    BaselineGrid,
    DocumentColumns,
    DocumentRows,
    SnapThreshold,
    ShowPageOutline,
    ShowRulers,
    ShowGrid,
    ShowGuides,
    ShowMargins,
    ShowBleed,
    ShowSafeArea,
    ShowPageShadow,
    SnapToGrid,
    SnapToObjects,
    SnapToGuides,
    SnapToMargins,
    SnapToPageCenter,
}

data class SelectionState(
    val selectedElementIds: List<String> = emptyList(),
) {
    fun selectedElement(elements: List<TemplateElement>): TemplateElement? {
        return elements.firstOrNull { it.id == selectedElementIds.firstOrNull() }
    }
}

data class TemplateEditorState(
    val template: Template? = null,
    val schema: DataSchema? = null,
    val availableSchemas: List<DataSchema> = emptyList(),
    val previewSchemaIds: List<String> = emptyList(),
    val previewRecordsBySchemaId: Map<String, List<DataRecord>> = emptyMap(),
    val selectedPreviewRecordIds: Map<String, String> = emptyMap(),
    val isPreviewDialogOpen: Boolean = false,
    val isPreviewMode: Boolean = false,
    val showSampleData: Boolean = false,
    val document: EditorDocument = EditorDocument(),
    val canvas: CanvasState = CanvasState(),
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val message: String? = null,
) {
    val selectedElement: TemplateElement?
        get() = document.selectedElement

    val selectedElements: List<TemplateElement>
        get() = document.selectedElements

    val previewSchemas: List<DataSchema>
        get() = previewSchemaIds.mapNotNull { schemaId -> availableSchemas.firstOrNull { it.id == schemaId } }

    val selectedPreviewRecords: Map<String, DataRecord>
        get() = previewRecordsBySchemaId.mapNotNull { (schemaId, records) ->
            val recordId = selectedPreviewRecordIds[schemaId] ?: return@mapNotNull null
            val record = records.firstOrNull { it.id == recordId } ?: return@mapNotNull null
            schemaId to record
        }.toMap()
}



