package me.aiglez.service.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.TemplateBarcodeFormat
import me.aiglez.service.domain.models.TemplateBorderStyle
import me.aiglez.service.domain.models.Template
import me.aiglez.service.domain.models.TemplateElement
import me.aiglez.service.domain.models.TemplateElementType
import me.aiglez.service.domain.models.TemplateImageAlignment
import me.aiglez.service.domain.models.TemplateImageContentMode
import me.aiglez.service.domain.models.TemplateTextDirection
import me.aiglez.service.domain.models.TemplateTextStyle
import me.aiglez.service.domain.repository.RecordRepository
import me.aiglez.service.domain.repository.TemplateRepository
import me.aiglez.service.ui.templates.editor.AddElementCommand
import me.aiglez.service.ui.templates.editor.AddElementsCommand
import me.aiglez.service.ui.templates.editor.CanvasMetric
import me.aiglez.service.ui.templates.editor.CanvasState
import me.aiglez.service.ui.templates.editor.CommonProperty
import me.aiglez.service.ui.templates.editor.DeleteElementCommand
import me.aiglez.service.ui.templates.editor.DeleteElementsCommand
import me.aiglez.service.ui.templates.editor.EditorDocument
import me.aiglez.service.ui.templates.editor.GeometryService
import me.aiglez.service.ui.templates.editor.HistoryManager
import me.aiglez.service.ui.templates.editor.PageRect
import me.aiglez.service.ui.templates.editor.ReplaceElementCommand
import me.aiglez.service.ui.templates.editor.ReplaceElementsCommand
import me.aiglez.service.ui.templates.editor.TemplateEditorState
import me.aiglez.service.ui.templates.editor.createDefaultElement
import me.aiglez.service.ui.templates.editor.expressionIdentifier
import me.aiglez.service.ui.templates.editor.referencedExpressionRoots
import me.aiglez.service.ui.templates.editor.schemaExpressionAliases
import me.aiglez.service.ui.templates.editor.withBarcodeBackground
import me.aiglez.service.ui.templates.editor.withBarcodeBorderColor
import me.aiglez.service.ui.templates.editor.withBarcodeBorderWidth
import me.aiglez.service.ui.templates.editor.withBarcodeFontSize
import me.aiglez.service.ui.templates.editor.withBarcodeForeground
import me.aiglez.service.ui.templates.editor.withBarcodeFormat
import me.aiglez.service.ui.templates.editor.withBarcodeQuietZone
import me.aiglez.service.ui.templates.editor.withBarcodeShowText
import me.aiglez.service.ui.templates.editor.withBarcodeText
import me.aiglez.service.ui.templates.editor.withBounds
import me.aiglez.service.ui.templates.editor.withCircleBorderColor
import me.aiglez.service.ui.templates.editor.withCircleBorderStyle
import me.aiglez.service.ui.templates.editor.withCircleBorderWidth
import me.aiglez.service.ui.templates.editor.withCircleFill
import me.aiglez.service.ui.templates.editor.withCommonProperty
import me.aiglez.service.ui.templates.editor.withImageAlignment
import me.aiglez.service.ui.templates.editor.withImageBackground
import me.aiglez.service.ui.templates.editor.withImageBorderColor
import me.aiglez.service.ui.templates.editor.withImageBorderRadius
import me.aiglez.service.ui.templates.editor.withImageBorderWidth
import me.aiglez.service.ui.templates.editor.withImageContentMode
import me.aiglez.service.ui.templates.editor.withImageSource
import me.aiglez.service.ui.templates.editor.withListBackground
import me.aiglez.service.ui.templates.editor.withListBorderColor
import me.aiglez.service.ui.templates.editor.withListBorderRadius
import me.aiglez.service.ui.templates.editor.withListBorderStyle
import me.aiglez.service.ui.templates.editor.withListBorderWidth
import me.aiglez.service.ui.templates.editor.withListColor
import me.aiglez.service.ui.templates.editor.withListColumnGap
import me.aiglez.service.ui.templates.editor.withListColumns
import me.aiglez.service.ui.templates.editor.withListFieldSlug
import me.aiglez.service.ui.templates.editor.withListFontFamily
import me.aiglez.service.ui.templates.editor.withListFontSize
import me.aiglez.service.ui.templates.editor.withListItemSeparator
import me.aiglez.service.ui.templates.editor.withListItemSpacing
import me.aiglez.service.ui.templates.editor.withListMaxItemLength
import me.aiglez.service.ui.templates.editor.withListMaxItems
import me.aiglez.service.ui.templates.editor.withListPadding
import me.aiglez.service.ui.templates.editor.withListPrefix
import me.aiglez.service.ui.templates.editor.withListSuffix
import me.aiglez.service.ui.templates.editor.withQrBackground
import me.aiglez.service.ui.templates.editor.withQrBorderColor
import me.aiglez.service.ui.templates.editor.withQrBorderWidth
import me.aiglez.service.ui.templates.editor.withQrForeground
import me.aiglez.service.ui.templates.editor.withQrQuietZone
import me.aiglez.service.ui.templates.editor.withQrText
import me.aiglez.service.ui.templates.editor.withRectangleBorderColor
import me.aiglez.service.ui.templates.editor.withRectangleBorderRadius
import me.aiglez.service.ui.templates.editor.withRectangleBorderStyle
import me.aiglez.service.ui.templates.editor.withRectangleBorderWidth
import me.aiglez.service.ui.templates.editor.withRectangleFill
import me.aiglez.service.ui.templates.editor.withTextBackground
import me.aiglez.service.ui.templates.editor.withTextBorderColor
import me.aiglez.service.ui.templates.editor.withTextBorderRadius
import me.aiglez.service.ui.templates.editor.withTextBorderStyle
import me.aiglez.service.ui.templates.editor.withTextBorderWidth
import me.aiglez.service.ui.templates.editor.withTextColor
import me.aiglez.service.ui.templates.editor.withTextAlign
import me.aiglez.service.ui.templates.editor.withTextDirection
import me.aiglez.service.ui.templates.editor.withTextFontFamily
import me.aiglez.service.ui.templates.editor.withTextFontSize
import me.aiglez.service.ui.templates.editor.withTextFontStyle
import me.aiglez.service.ui.templates.editor.withTextLetterSpacing
import me.aiglez.service.ui.templates.editor.withTextLineHeight
import me.aiglez.service.ui.templates.editor.withTextPadding
import me.aiglez.service.ui.templates.editor.withTextValue
import me.aiglez.service.ui.templates.editor.withTextVerticalAlign
import me.aiglez.service.ui.templates.editor.updateCommon
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class CompileViewModel(
    private val templateId: String,
    private val recordRepository: RecordRepository,
    private val templateRepository: TemplateRepository,
    private val logger: Logger,
) : ViewModel() {

    private val history = HistoryManager()
    private val draftTemplateId = templateId.ifBlank { newId("template") }
    private val _uiState = MutableStateFlow(TemplateEditorState())
    val uiState = _uiState
    private var clipboardElements: List<TemplateElement> = emptyList()
    private val propertyActions = TemplateEditorPropertyActions(
        selectedElement = { _uiState.value.selectedElement },
        executeCommand = ::execute,
        replaceSelection = ::replaceSelected,
    )

    init {
        viewModelScope.launch {
            recordRepository.getActiveSchemas().flatMapLatest { schemas ->
                when {
                    schemas.isEmpty() -> flowOf(Triple(schemas, null, null))
                    templateId.isBlank() -> {
                        val draftSchema = schemas.firstOrNull { it.id == EditorTestSchemaId } ?: schemas.first()
                        flowOf(
                            Triple(
                                schemas,
                                draftSchema,
                                Template(
                                    id = draftTemplateId,
                                    name = "Untitled template",
                                    targetSchemaId = draftSchema.id,
                                    elements = emptyList(),
                                ),
                            )
                        )
                    }
                    else -> combine(schemas.map { schema ->
                        templateRepository.getActiveTemplates(schema.id).map { templates ->
                            schema to templates.firstOrNull { it.id == templateId }
                        }
                    }) { matches ->
                        val match = matches.firstOrNull { it.second != null }
                        Triple(schemas, match?.first, match?.second)
                    }
                }
            }.collect { (schemas, schema, template) ->
                history.clear()
                val current = _uiState.value
                _uiState.value = if (template == null) {
                    TemplateEditorState(
                        availableSchemas = schemas,
                        showSampleData = current.showSampleData,
                        message = "Create a data schema before editing templates.",
                    )
                } else {
                    TemplateEditorState(
                        template = template,
                        schema = schema,
                        availableSchemas = schemas,
                        showSampleData = current.showSampleData,
                        document = EditorDocument(
                            elements = template.elements.ensureStableIds().withEditorTestElementsIfEmpty(),
                        ),
                    )
                }
            }
        }
        viewModelScope.launch {
            _uiState
                .map { state -> state.referencedPreviewSchemaIds() }
                .distinctUntilChanged()
                .flatMapLatest { schemaIds ->
                    if (schemaIds.isEmpty()) {
                        flowOf(emptyMap())
                    } else {
                        combine(schemaIds.map { schemaId ->
                            recordRepository.getActiveRecords(schemaId).map { records -> schemaId to records }
                        }) { entries -> entries.toMap() }
                    }
                }
                .collect { recordsBySchemaId ->
                    _uiState.update { state ->
                        val nextSchemaIds = state.referencedPreviewSchemaIds()
                        val nextSelectedRecordIds = state.selectedPreviewRecordIds.filter { (schemaId, recordId) ->
                            recordsBySchemaId[schemaId].orEmpty().any { it.id == recordId }
                        }
                        state.copy(
                            previewSchemaIds = nextSchemaIds,
                            previewRecordsBySchemaId = recordsBySchemaId,
                            selectedPreviewRecordIds = nextSelectedRecordIds,
                            isPreviewMode = state.isPreviewMode && nextSchemaIds.all { nextSelectedRecordIds[it] != null },
                        )
                    }
                }
        }
    }

    fun openPreviewDialog() {
        val state = _uiState.value
        if (state.previewSchemaIds.isEmpty()) {
            _uiState.update { it.copy(message = "Add a data field before previewing this template.") }
            return
        }
        _uiState.update { it.copy(isPreviewDialogOpen = true, message = null) }
    }

    fun closePreviewDialog() {
        _uiState.update { it.copy(isPreviewDialogOpen = false) }
    }

    fun selectPreviewRecord(schemaId: String, recordId: String) {
        val record = _uiState.value.previewRecordsBySchemaId[schemaId].orEmpty().firstOrNull { it.id == recordId } ?: return
        _uiState.update {
            it.copy(
                selectedPreviewRecordIds = it.selectedPreviewRecordIds + (schemaId to record.id),
                message = null,
            )
        }
    }

    fun togglePreviewMode() {
        val state = _uiState.value
        when {
            state.isPreviewMode -> _uiState.update { it.copy(isPreviewMode = false, message = null) }
            else -> openPreviewDialog()
        }
    }

    fun toggleSampleData() {
        _uiState.update { state ->
            state.copy(
                showSampleData = !state.showSampleData,
                message = if (state.showSampleData) "Showing template bindings." else "Showing sample data.",
            )
        }
    }

    fun showPreview() {
        val state = _uiState.value
        val missingSchema = state.previewSchemas.firstOrNull { schema ->
            state.selectedPreviewRecordIds[schema.id].isNullOrBlank()
        }
        when {
            state.previewSchemaIds.isEmpty() -> _uiState.update { it.copy(message = "Add a data field before previewing this template.") }
            missingSchema != null -> _uiState.update { it.copy(message = "Choose a record for ${missingSchema.name}.") }
            else -> _uiState.update {
                it.copy(
                    isPreviewMode = true,
                    isPreviewDialogOpen = false,
                    message = "Preview mode enabled.",
                )
            }
        }
    }

    fun addElement(type: TemplateElementType, x: Float = 48f, y: Float = 48f) {
        val state = _uiState.value
        val zIndex = (state.document.elements.maxOfOrNull { it.zIndex } ?: 0) + 1
        val element = createDefaultElement(type, newId(type.name.lowercase()), x, y, zIndex)
        execute(AddElementCommand(element))
    }

    fun addDataField(schemaName: String, fieldSlug: String, fieldName: String, x: Float = 64f, y: Float = 64f) {
        val state = _uiState.value
        val zIndex = (state.document.elements.maxOfOrNull { it.zIndex } ?: 0) + 1
        val fieldKey = expressionIdentifier(fieldName).ifBlank { fieldSlug }
        val schemaKey = expressionIdentifier(schemaName)
        val textValue = if (schemaKey.isNotBlank() && fieldKey.isNotBlank()) "{{ $schemaKey.$fieldKey }}" else null
        val element = TemplateElement.Text(
            id = newId("field-${fieldSlug.ifBlank { "data" }}"),
            name = fieldName.ifBlank { fieldSlug.ifBlank { "Data field" } },
            x = x,
            y = y,
            width = 220f,
            height = 44f,
            zIndex = zIndex,
            staticText = textValue,
            placeholderTag = "[DataRecord:$fieldSlug]",
        )
        execute(AddElementCommand(element))
    }

    fun selectElement(elementId: String?) {
        _uiState.update { state ->
            val nextSelection = elementId?.let(::listOf).orEmpty()
            if (state.document.selectedElementIds == nextSelection) {
                state
            } else {
                state.copy(document = state.document.withSelection(nextSelection))
            }
        }
    }

    fun setSelection(elementIds: List<String>) {
        _uiState.update { state ->
            val nextDocument = state.document.withSelection(elementIds)
            if (state.document.selectedElementIds == nextDocument.selectedElementIds) state else state.copy(document = nextDocument)
        }
    }

    fun toggleSelection(elementId: String) {
        _uiState.update { state ->
            val selected = state.document.selectedElementIds
            val nextSelection = if (elementId in selected) selected.filterNot { it == elementId } else selected + elementId
            val nextDocument = state.document.withSelection(nextSelection)
            if (state.document.selectedElementIds == nextDocument.selectedElementIds) state else state.copy(document = nextDocument)
        }
    }

    fun selectSimilarObjects() {
        val state = _uiState.value
        val selected = state.selectedElement ?: return
        val similarIds = state.document.elements
            .filter { it.visible && it.type == selected.type && it::class == selected::class }
            .map { it.id }
        setSelection(similarIds)
    }

    fun showInlineEditHint(elementId: String) {
        val element = _uiState.value.document.elements.firstOrNull { it.id == elementId } ?: return
        val nextMessage = "Editing ${element.name}. Use the inspector text fields for now."
        _uiState.update {
            if (it.message == nextMessage) it else it.copy(message = nextMessage)
        }
    }

    fun showShortcutMessage(message: String) {
        _uiState.update {
            if (it.message == message) it else it.copy(message = message)
        }
    }

    fun deleteSelected() {
        val elements = _uiState.value.selectedElements
        if (elements.isEmpty()) return
        if (elements.size == 1) {
            execute(DeleteElementCommand(elements.first()))
        } else {
            execute(DeleteElementsCommand(elements))
        }
    }

    fun copySelected() {
        clipboardElements = _uiState.value.selectedElements
        if (clipboardElements.isNotEmpty()) {
            _uiState.update { it.copy(message = "Copied ${clipboardElements.size} element${if (clipboardElements.size == 1) "" else "s"}.") }
        }
    }

    fun pasteClipboard() {
        pasteClipboard(offset = 16f)
    }

    fun pasteClipboardInPlace() {
        pasteClipboard(offset = 0f)
    }

    private fun pasteClipboard(offset: Float) {
        val state = _uiState.value
        if (clipboardElements.isEmpty()) return
        val maxZ = state.document.elements.maxOfOrNull { it.zIndex } ?: 0
        val pasted = clipboardElements.mapIndexed { index, element ->
            element
                .withCopiedIdentity(newId("copy-${element.type.name.lowercase()}"), maxZ + index + 1)
                .withBounds(GeometryService.getElementBounds(element).copy(x = element.x + offset, y = element.y + offset))
        }
        execute(AddElementsCommand(pasted))
    }

    fun duplicateSelected() {
        copySelected()
        pasteClipboard()
    }

    fun bringSelectedToFront() {
        val state = _uiState.value
        val selected = state.selectedElements
        if (selected.isEmpty()) return
        val maxZ = state.document.elements.maxOfOrNull { it.zIndex } ?: 0
        replaceSelected { element, index -> element.updateCommon(zIndex = maxZ + index + 1) }
    }

    fun sendSelectedToBack() {
        val selected = _uiState.value.selectedElements
        if (selected.isEmpty()) return
        replaceSelected { element, index -> element.updateCommon(zIndex = -selected.size + index) }
    }

    fun lockSelected() {
        replaceSelected { element, _ -> element.updateCommon(locked = true) }
    }

    fun hideSelected() {
        replaceSelected { element, _ -> element.updateCommon(visible = false) }
    }

    fun groupSelected() {
        _uiState.update { it.copy(message = "Grouping will be enabled when group containers are added.") }
    }

    fun ungroupSelected() {
        _uiState.update { it.copy(message = "Ungroup will be enabled when group containers are added.") }
    }

    fun alignSelected(alignment: SelectionAlignment) {
        val selected = _uiState.value.selectedElements
        if (selected.isEmpty()) return
        val bounds = selected.map(GeometryService::getElementBounds)
        val union = bounds.unionBounds() ?: return
        replaceSelected { element, _ ->
            val current = GeometryService.getElementBounds(element)
            val next = when (alignment) {
                SelectionAlignment.Left -> current.copy(x = union.x)
                SelectionAlignment.Center -> current.copy(x = union.centerX - current.width / 2f)
                SelectionAlignment.Right -> current.copy(x = union.right - current.width)
                SelectionAlignment.Top -> current.copy(y = union.y)
                SelectionAlignment.Middle -> current.copy(y = union.centerY - current.height / 2f)
                SelectionAlignment.Bottom -> current.copy(y = union.bottom - current.height)
            }
            element.withBounds(next)
        }
    }

    fun distributeSelected(axis: DistributionAxis) {
        val selected = _uiState.value.selectedElements
        if (selected.size < 3) return
        val sorted = selected.sortedBy { if (axis == DistributionAxis.Horizontal) it.x else it.y }
        val bounds = sorted.map(GeometryService::getElementBounds)
        val first = bounds.first()
        val last = bounds.last()
        val totalSize = bounds.sumOf { if (axis == DistributionAxis.Horizontal) it.width.toDouble() else it.height.toDouble() }.toFloat()
        val span = if (axis == DistributionAxis.Horizontal) last.right - first.x else last.bottom - first.y
        val gap = ((span - totalSize) / (sorted.size - 1)).coerceAtLeast(0f)
        var cursor = if (axis == DistributionAxis.Horizontal) first.x else first.y
        val replacements = sorted.mapIndexedNotNull { index, element ->
            val current = GeometryService.getElementBounds(element)
            val next = if (axis == DistributionAxis.Horizontal) current.copy(x = cursor) else current.copy(y = cursor)
            cursor += (if (axis == DistributionAxis.Horizontal) current.width else current.height) + gap
            if (index == 0 || index == sorted.lastIndex || next == current) null else element to element.withBounds(next)
        }
        if (replacements.isNotEmpty()) execute(ReplaceElementsCommand(replacements))
    }

    fun matchSelectedSize(axis: SizeMatchAxis) {
        val primary = _uiState.value.selectedElement ?: return
        val primaryBounds = GeometryService.getElementBounds(primary)
        replaceSelected { element, _ ->
            if (element.id == primary.id) {
                element
            } else {
                val current = GeometryService.getElementBounds(element)
                val next = when (axis) {
                    SizeMatchAxis.Width -> current.copy(width = primaryBounds.width)
                    SizeMatchAxis.Height -> current.copy(height = primaryBounds.height)
                }
                element.withBounds(next)
            }
        }
    }

    fun rotateSelectedBy(delta: Float) {
        replaceSelected { element, _ ->
            element.updateCommon(rotation = normalizeRotation(element.rotation + delta))
        }
    }

    fun previewBounds(elementId: String, bounds: PageRect) {
        val state = _uiState.value
        val element = state.document.elements.firstOrNull { it.id == elementId } ?: return
        if (element.locked) return
        val nextElement = element.withBounds(bounds.normalized())
        _uiState.update {
            it.copy(
                document = it.document.copy(
                    elements = it.document.elements.map { item -> if (item.id == elementId) nextElement else item },
                ),
            )
        }
    }

    fun previewBounds(boundsById: Map<String, PageRect>) {
        if (boundsById.isEmpty()) return
        _uiState.update { state ->
            val nextElements = state.document.elements.map { element ->
                val bounds = boundsById[element.id]
                if (bounds == null || element.locked) element else element.withBounds(bounds.normalized())
            }
            state.copy(document = state.document.copy(elements = nextElements))
        }
    }

    fun previewRotation(elementId: String, rotation: Float) {
        _uiState.update { state ->
            val nextElements = state.document.elements.map { element ->
                if (element.id == elementId && !element.locked) {
                    element.updateCommon(rotation = rotation)
                } else {
                    element
                }
            }
            state.copy(document = state.document.copy(elements = nextElements))
        }
    }

    fun commitBounds(originalElement: TemplateElement, bounds: PageRect) {
        if (originalElement.locked) return
        val nextElement = originalElement.withBounds(bounds.normalized())
        if (GeometryService.getElementBounds(originalElement) == GeometryService.getElementBounds(nextElement)) return
        execute(ReplaceElementCommand(originalElement, nextElement))
    }

    fun commitBounds(originalElements: List<TemplateElement>, boundsById: Map<String, PageRect>) {
        val replacements = originalElements.mapNotNull { original ->
            if (original.locked) return@mapNotNull null
            val bounds = boundsById[original.id] ?: return@mapNotNull null
            val nextElement = original.withBounds(bounds.normalized())
            if (nextElement == original) null else original to nextElement
        }
        if (replacements.isEmpty()) return
        if (replacements.size == 1) {
            val (before, after) = replacements.single()
            execute(ReplaceElementCommand(before, after, selectAfter = false))
        } else {
            execute(ReplaceElementsCommand(replacements))
        }
    }

    fun commitRotation(originalElement: TemplateElement, rotation: Float) {
        if (originalElement.locked) return
        val nextElement = originalElement.updateCommon(rotation = rotation)
        if (nextElement == originalElement) return
        execute(ReplaceElementCommand(originalElement, nextElement))
    }

    fun updateSelectedBounds(bounds: PageRect) = propertyActions.updateSelectedBounds(bounds)
    fun updateSelectedCommonProperty(property: CommonProperty, value: String) = propertyActions.updateSelectedCommonProperty(property, value)
    fun updateSelectedText(value: String) = propertyActions.updateSelectedText(value)
    fun updateSelectedTextColor(value: String) = propertyActions.updateSelectedTextColor(value)
    fun updateSelectedTextFontSize(value: Float) = propertyActions.updateSelectedTextFontSize(value)
    fun adjustSelectedTextFontSize(delta: Float) = propertyActions.adjustSelectedTextFontSize(delta)
    fun updateSelectedTextFontFamily(value: String) = propertyActions.updateSelectedTextFontFamily(value)
    fun updateSelectedTextFontStyle(value: TemplateTextStyle) = propertyActions.updateSelectedTextFontStyle(value)
    fun toggleSelectedTextBold() = propertyActions.toggleSelectedTextBold()
    fun toggleSelectedTextItalic() = propertyActions.toggleSelectedTextItalic()
    fun toggleSelectedTextUnderline() = propertyActions.toggleSelectedTextUnderline()
    fun updateSelectedTextLineHeight(value: Float) = propertyActions.updateSelectedTextLineHeight(value)
    fun updateSelectedTextLetterSpacing(value: Float) = propertyActions.updateSelectedTextLetterSpacing(value)
    fun updateSelectedTextVerticalAlign(value: String) = propertyActions.updateSelectedTextVerticalAlign(value)
    fun updateSelectedTextAlign(value: String) = propertyActions.updateSelectedTextAlign(value)
    fun alignSelectedText(value: String) = propertyActions.alignSelectedText(value)
    fun updateSelectedTextDirection(value: TemplateTextDirection) = propertyActions.updateSelectedTextDirection(value)
    fun updateSelectedTextBackground(value: String) = propertyActions.updateSelectedTextBackground(value)
    fun updateSelectedTextPadding(value: Float) = propertyActions.updateSelectedTextPadding(value)
    fun updateSelectedTextBorderColor(value: String) = propertyActions.updateSelectedTextBorderColor(value)
    fun updateSelectedTextBorderWidth(value: Float) = propertyActions.updateSelectedTextBorderWidth(value)
    fun updateSelectedTextBorderStyle(value: TemplateBorderStyle) = propertyActions.updateSelectedTextBorderStyle(value)
    fun updateSelectedTextBorderRadius(value: Float) = propertyActions.updateSelectedTextBorderRadius(value)
    fun updateSelectedRectangleFill(value: String) = propertyActions.updateSelectedRectangleFill(value)
    fun updateSelectedRectangleBorderColor(value: String) = propertyActions.updateSelectedRectangleBorderColor(value)
    fun updateSelectedRectangleBorderWidth(value: Float) = propertyActions.updateSelectedRectangleBorderWidth(value)
    fun updateSelectedRectangleBorderStyle(value: TemplateBorderStyle) = propertyActions.updateSelectedRectangleBorderStyle(value)
    fun updateSelectedRectangleBorderRadius(value: Float) = propertyActions.updateSelectedRectangleBorderRadius(value)
    fun updateSelectedCircleFill(value: String) = propertyActions.updateSelectedCircleFill(value)
    fun updateSelectedCircleBorderColor(value: String) = propertyActions.updateSelectedCircleBorderColor(value)
    fun updateSelectedCircleBorderWidth(value: Float) = propertyActions.updateSelectedCircleBorderWidth(value)
    fun updateSelectedCircleBorderStyle(value: TemplateBorderStyle) = propertyActions.updateSelectedCircleBorderStyle(value)
    fun chooseImageForSelected() = propertyActions.chooseImageForSelected()
    fun resizeSelectedImageToIntrinsic() = propertyActions.resizeSelectedImageToIntrinsic()
    fun fitSelectedImageFrameToAspect() = propertyActions.fitSelectedImageFrameToAspect()
    fun updateSelectedImageContentMode(value: TemplateImageContentMode) = propertyActions.updateSelectedImageContentMode(value)
    fun updateSelectedImageAlignment(value: TemplateImageAlignment) = propertyActions.updateSelectedImageAlignment(value)
    fun updateSelectedImageBackground(value: String) = propertyActions.updateSelectedImageBackground(value)
    fun updateSelectedImageBorderColor(value: String) = propertyActions.updateSelectedImageBorderColor(value)
    fun updateSelectedImageBorderWidth(value: Float) = propertyActions.updateSelectedImageBorderWidth(value)
    fun updateSelectedImageBorderRadius(value: Float) = propertyActions.updateSelectedImageBorderRadius(value)
    fun updateSelectedQrText(value: String) = propertyActions.updateSelectedQrText(value)
    fun updateSelectedQrForeground(value: String) = propertyActions.updateSelectedQrForeground(value)
    fun updateSelectedQrBackground(value: String) = propertyActions.updateSelectedQrBackground(value)
    fun updateSelectedQrQuietZone(value: Float) = propertyActions.updateSelectedQrQuietZone(value)
    fun updateSelectedQrBorderColor(value: String) = propertyActions.updateSelectedQrBorderColor(value)
    fun updateSelectedQrBorderWidth(value: Float) = propertyActions.updateSelectedQrBorderWidth(value)
    fun updateSelectedBarcodeText(value: String) = propertyActions.updateSelectedBarcodeText(value)
    fun updateSelectedBarcodeFormat(value: TemplateBarcodeFormat) = propertyActions.updateSelectedBarcodeFormat(value)
    fun updateSelectedBarcodeForeground(value: String) = propertyActions.updateSelectedBarcodeForeground(value)
    fun updateSelectedBarcodeBackground(value: String) = propertyActions.updateSelectedBarcodeBackground(value)
    fun updateSelectedBarcodeQuietZone(value: Float) = propertyActions.updateSelectedBarcodeQuietZone(value)
    fun updateSelectedBarcodeShowText(value: Boolean) = propertyActions.updateSelectedBarcodeShowText(value)
    fun updateSelectedBarcodeFontSize(value: Float) = propertyActions.updateSelectedBarcodeFontSize(value)
    fun updateSelectedBarcodeBorderColor(value: String) = propertyActions.updateSelectedBarcodeBorderColor(value)
    fun updateSelectedBarcodeBorderWidth(value: Float) = propertyActions.updateSelectedBarcodeBorderWidth(value)
    fun updateSelectedListFieldSlug(value: String) = propertyActions.updateSelectedListFieldSlug(value)
    fun updateSelectedListPrefix(value: String) = propertyActions.updateSelectedListPrefix(value)
    fun updateSelectedListSuffix(value: String) = propertyActions.updateSelectedListSuffix(value)
    fun updateSelectedListItemSeparator(value: String) = propertyActions.updateSelectedListItemSeparator(value)
    fun updateSelectedListMaxItems(value: Float) = propertyActions.updateSelectedListMaxItems(value)
    fun updateSelectedListMaxItemLength(value: Float) = propertyActions.updateSelectedListMaxItemLength(value)
    fun updateSelectedListColumns(value: Float) = propertyActions.updateSelectedListColumns(value)
    fun updateSelectedListColumnGap(value: Float) = propertyActions.updateSelectedListColumnGap(value)
    fun updateSelectedListItemSpacing(value: Float) = propertyActions.updateSelectedListItemSpacing(value)
    fun updateSelectedListPadding(value: Float) = propertyActions.updateSelectedListPadding(value)
    fun updateSelectedListFontFamily(value: String) = propertyActions.updateSelectedListFontFamily(value)
    fun updateSelectedListFontSize(value: Float) = propertyActions.updateSelectedListFontSize(value)
    fun updateSelectedListColor(value: String) = propertyActions.updateSelectedListColor(value)
    fun updateSelectedListBackground(value: String) = propertyActions.updateSelectedListBackground(value)
    fun updateSelectedListBorderColor(value: String) = propertyActions.updateSelectedListBorderColor(value)
    fun updateSelectedListBorderWidth(value: Float) = propertyActions.updateSelectedListBorderWidth(value)
    fun updateSelectedListBorderStyle(value: TemplateBorderStyle) = propertyActions.updateSelectedListBorderStyle(value)
    fun updateSelectedListBorderRadius(value: Float) = propertyActions.updateSelectedListBorderRadius(value)

    fun setZoom(zoom: Float) {
        _uiState.update { state ->
            state.copy(canvas = state.canvas.copy(zoom = zoom.coerceIn(0.25f, 3f)))
        }
    }

    fun setRulerUnit(unit: String) {
        val normalized = unit.lowercase()
        if (normalized !in setOf("px", "mm", "cm", "inch")) return
        _uiState.update { state ->
            state.copy(canvas = state.canvas.copy(rulerUnit = normalized))
        }
    }

    fun toggleSnap() {
        _uiState.update { state ->
            state.copy(canvas = state.canvas.copy(snapEnabled = !state.canvas.snapEnabled))
        }
    }

    fun setNudgeDistance(value: Float) {
        _uiState.update { state ->
            state.copy(canvas = state.canvas.copy(nudgeDistance = value.coerceIn(0.1f, 100f)))
        }
    }

    fun setCanvasMetric(metric: CanvasMetric, value: Float) {
        _uiState.update { state ->
            val pageHeight = 842f
            val pageMaxInset = 421f
            val nextCanvas = when (metric) {
                CanvasMetric.PageMargin -> state.canvas.copy(pageMargin = value.coerceIn(0f, pageMaxInset))
                CanvasMetric.PrintableInset -> state.canvas.copy(printableInset = value.coerceIn(0f, pageMaxInset))
                CanvasMetric.BleedInset -> state.canvas.copy(bleedInset = value.coerceIn(0f, pageMaxInset))
                CanvasMetric.TrimInset -> state.canvas.copy(trimInset = value.coerceIn(0f, pageMaxInset))
                CanvasMetric.SafeAreaInset -> state.canvas.copy(safeAreaInset = value.coerceIn(0f, pageMaxInset))
                CanvasMetric.HeaderGuide -> state.canvas.copy(headerGuide = value.coerceIn(0f, pageHeight))
                CanvasMetric.FooterGuide -> state.canvas.copy(footerGuide = value.coerceIn(0f, pageHeight))
                CanvasMetric.GridSize -> state.canvas.copy(gridSize = value.coerceIn(0f, 200f))
                CanvasMetric.BaselineGrid -> state.canvas.copy(baselineGrid = value.coerceIn(0f, 200f))
                CanvasMetric.DocumentColumns -> state.canvas.copy(documentColumns = value.roundToInt().coerceIn(1, 12))
                CanvasMetric.DocumentRows -> state.canvas.copy(documentRows = value.roundToInt().coerceIn(1, 24))
                CanvasMetric.SnapThreshold -> state.canvas.copy(snapThreshold = value.coerceIn(0f, 48f))
                CanvasMetric.ShowPageOutline -> state.canvas.copy(showPageOutline = value >= 0.5f)
                CanvasMetric.ShowRulers -> state.canvas.copy(showRulers = value >= 0.5f)
                CanvasMetric.ShowGrid -> state.canvas.copy(showGrid = value >= 0.5f)
                CanvasMetric.ShowGuides -> state.canvas.copy(showGuides = value >= 0.5f)
                CanvasMetric.ShowMargins -> state.canvas.copy(showMargins = value >= 0.5f)
                CanvasMetric.ShowBleed -> state.canvas.copy(showBleed = value >= 0.5f)
                CanvasMetric.ShowSafeArea -> state.canvas.copy(showSafeArea = value >= 0.5f)
                CanvasMetric.ShowPageShadow -> state.canvas.copy(showPageShadow = value >= 0.5f)
                CanvasMetric.SnapToGrid -> state.canvas.copy(snapToGrid = value >= 0.5f)
                CanvasMetric.SnapToObjects -> state.canvas.copy(snapToObjects = value >= 0.5f)
                CanvasMetric.SnapToGuides -> state.canvas.copy(snapToGuides = value >= 0.5f)
                CanvasMetric.SnapToMargins -> state.canvas.copy(snapToMargins = value >= 0.5f)
                CanvasMetric.SnapToPageCenter -> state.canvas.copy(snapToPageCenter = value >= 0.5f)
            }
            if (nextCanvas == state.canvas) state else state.copy(canvas = nextCanvas)
        }
    }

    fun nudgeSelected(deltaX: Float, deltaY: Float) {
        val elements = _uiState.value.selectedElements.filterNot { it.locked }
        val replacements = elements.mapNotNull { element ->
            val bounds = GeometryService.getElementBounds(element)
            val nextElement = element.withBounds(
                bounds.copy(x = bounds.x + deltaX, y = bounds.y + deltaY).normalized(),
            )
            if (nextElement == element) null else element to nextElement
        }
        when (replacements.size) {
            0 -> Unit
            1 -> {
                val (before, after) = replacements.single()
                execute(ReplaceElementCommand(before, after, selectAfter = false))
            }
            else -> execute(ReplaceElementsCommand(replacements))
        }
    }

    fun undo() {
        _uiState.update { state ->
            state.copy(
                document = history.undo(state.document),
                isDirty = true,
                canUndo = history.canUndo,
                canRedo = history.canRedo,
            )
        }
    }

    fun redo() {
        _uiState.update { state ->
            state.copy(
                document = history.redo(state.document),
                isDirty = true,
                canUndo = history.canUndo,
                canRedo = history.canRedo,
            )
        }
    }

    fun saveTemplate() {
        val state = _uiState.value
        val template = state.template ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            val nextTemplate = template.copy(elements = _uiState.value.document.elements, templateVersion = 1)
            templateRepository.saveTemplate(nextTemplate)
            logger.i { "Saved template ${nextTemplate.id} with ${nextTemplate.elements.size} elements" }
            _uiState.update {
                it.copy(
                    template = nextTemplate,
                    isDirty = false,
                    isSaving = false,
                    message = "Template saved.",
                )
            }
        }
    }

    fun exportPdf() {
        logger.i { "PDF export is intentionally deferred until the editor model is stable." }
        _uiState.update { it.copy(message = "PDF export will be added after the editor model is stable.") }
    }

    private fun execute(command: me.aiglez.service.ui.templates.editor.EditorCommand) {
        _uiState.update { state ->
            val nextDocument = history.execute(state.document, command)
            state.copy(
                document = nextDocument,
                isDirty = true,
                canUndo = history.canUndo,
                canRedo = history.canRedo,
                message = null,
            )
        }
    }

    private fun replaceSelected(transform: (TemplateElement, Int) -> TemplateElement) {
        val selected = _uiState.value.selectedElements
        val replacements = selected.mapIndexedNotNull { index, element ->
            val next = transform(element, index)
            if (next == element) null else element to next
        }
        if (replacements.isNotEmpty()) execute(ReplaceElementsCommand(replacements))
    }

    private fun List<TemplateElement>.ensureStableIds(): List<TemplateElement> {
        return mapIndexed { index, element ->
            if (element.id.isNotBlank()) {
                element
            } else {
                val id = newId("element-${index + 1}")
                when (element) {
                    is TemplateElement.Text -> element.copy(id = id)
                    is TemplateElement.Image -> element.copy(id = id)
                    is TemplateElement.Circle -> element.copy(id = id)
                    is TemplateElement.QRCode -> element.copy(id = id)
                    is TemplateElement.Barcode -> element.copy(id = id)
                    is TemplateElement.List -> element.copy(id = id)
                    is TemplateElement.Rectangle -> element.copy(id = id)
                    is TemplateElement.Line -> element.copy(id = id)
                }
            }
        }
    }

    private fun newId(prefix: String): String {
        return "$prefix-${Random.nextLong(1_000_000_000L, 9_999_999_999L)}"
    }
}
