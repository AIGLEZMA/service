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

    fun updateSelectedBounds(bounds: PageRect) {
        val element = _uiState.value.selectedElement ?: return
        if (element.locked) return
        val nextElement = element.withBounds(bounds.normalized())
        execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedCommonProperty(property: CommonProperty, value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withCommonProperty(property, value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedText(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextValue(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextColor(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextFontSize(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextFontSize(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun adjustSelectedTextFontSize(delta: Float) {
        replaceSelected { element, _ ->
            when (element) {
                is TemplateElement.Text -> element.copy(fontSize = (element.fontSize + delta).coerceAtLeast(1f))
                else -> element
            }
        }
    }

    fun updateSelectedTextFontFamily(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextFontFamily(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextFontStyle(value: TemplateTextStyle) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextFontStyle(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun toggleSelectedTextBold() {
        replaceSelected { element, _ ->
            when (element) {
                is TemplateElement.Text -> element.copy(fontWeight = if (element.fontWeight >= 600) 400 else 700)
                else -> element
            }
        }
    }

    fun toggleSelectedTextItalic() {
        replaceSelected { element, _ ->
            when (element) {
                is TemplateElement.Text -> {
                    val italic = element.fontStyle != TemplateTextStyle.Italic
                    element.copy(
                        fontStyle = if (italic) TemplateTextStyle.Italic else TemplateTextStyle.Normal,
                        italic = italic,
                    )
                }
                else -> element
            }
        }
    }

    fun toggleSelectedTextUnderline() {
        replaceSelected { element, _ ->
            when (element) {
                is TemplateElement.Text -> element.copy(underline = !element.underline)
                else -> element
            }
        }
    }

    fun updateSelectedTextLineHeight(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextLineHeight(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextLetterSpacing(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextLetterSpacing(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextVerticalAlign(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextVerticalAlign(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextAlign(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextAlign(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun alignSelectedText(value: String) {
        replaceSelected { element, _ ->
            when (element) {
                is TemplateElement.Text -> element.copy(textAlign = value)
                else -> element
            }
        }
    }

    fun updateSelectedTextDirection(value: TemplateTextDirection) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextDirection(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextBackground(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextBackground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextPadding(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextPadding(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextBorderColor(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextBorderWidth(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextBorderStyle(value: TemplateBorderStyle) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextBorderStyle(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedTextBorderRadius(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withTextBorderRadius(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedRectangleFill(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withRectangleFill(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedRectangleBorderColor(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withRectangleBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedRectangleBorderWidth(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withRectangleBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedRectangleBorderStyle(value: TemplateBorderStyle) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withRectangleBorderStyle(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedRectangleBorderRadius(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withRectangleBorderRadius(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedCircleFill(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withCircleFill(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedCircleBorderColor(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withCircleBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedCircleBorderWidth(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withCircleBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedCircleBorderStyle(value: TemplateBorderStyle) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withCircleBorderStyle(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun chooseImageForSelected() {
        val element = _uiState.value.selectedElement as? TemplateElement.Image ?: return
        val imageFile = chooseTemplateImageFile() ?: return
        val nextSource = element.withImageSource(
            path = imageFile.path,
            name = imageFile.name,
            intrinsicWidth = imageFile.width,
            intrinsicHeight = imageFile.height,
        )
        val nextElement = if (
            element.sourcePath.isBlank() &&
            imageFile.width > 0 &&
            imageFile.height > 0 &&
            element.width == 180f &&
            element.height == 120f
        ) {
            val aspect = imageFile.width.toFloat() / imageFile.height.toFloat()
            nextSource.withBounds(
                GeometryService.getElementBounds(element).copy(
                    height = (element.width / aspect).coerceAtLeast(GeometryService.MinElementSize),
                ),
            )
        } else {
            nextSource
        }
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun resizeSelectedImageToIntrinsic() {
        val element = _uiState.value.selectedElement as? TemplateElement.Image ?: return
        if (element.intrinsicWidth <= 0 || element.intrinsicHeight <= 0) return
        val nextElement = element.withBounds(
            GeometryService.getElementBounds(element).copy(
                width = element.intrinsicWidth.toFloat(),
                height = element.intrinsicHeight.toFloat(),
            ).normalized(),
        )
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun fitSelectedImageFrameToAspect() {
        val element = _uiState.value.selectedElement as? TemplateElement.Image ?: return
        if (element.intrinsicWidth <= 0 || element.intrinsicHeight <= 0) return
        val aspect = element.intrinsicWidth.toFloat() / element.intrinsicHeight.toFloat()
        val bounds = GeometryService.getElementBounds(element)
        val nextElement = element.withBounds(
            bounds.copy(height = (bounds.width / aspect).coerceAtLeast(GeometryService.MinElementSize)).normalized(),
        )
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedImageContentMode(value: TemplateImageContentMode) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withImageContentMode(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedImageAlignment(value: TemplateImageAlignment) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withImageAlignment(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedImageBackground(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withImageBackground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedImageBorderColor(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withImageBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedImageBorderWidth(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withImageBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedImageBorderRadius(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withImageBorderRadius(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedQrText(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withQrText(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedQrForeground(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withQrForeground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedQrBackground(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withQrBackground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedQrQuietZone(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withQrQuietZone(value.roundToInt())
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedQrBorderColor(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withQrBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedQrBorderWidth(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withQrBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeText(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withBarcodeText(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeFormat(value: TemplateBarcodeFormat) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withBarcodeFormat(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeForeground(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withBarcodeForeground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeBackground(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withBarcodeBackground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeQuietZone(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withBarcodeQuietZone(value.roundToInt())
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeShowText(value: Boolean) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withBarcodeShowText(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeFontSize(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withBarcodeFontSize(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeBorderColor(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withBarcodeBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedBarcodeBorderWidth(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withBarcodeBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListFieldSlug(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListFieldSlug(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListPrefix(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListPrefix(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListSuffix(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListSuffix(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListItemSeparator(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListItemSeparator(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListMaxItems(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListMaxItems(value.roundToInt())
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListMaxItemLength(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListMaxItemLength(value.roundToInt())
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListColumns(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListColumns(value.roundToInt())
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListColumnGap(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListColumnGap(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListItemSpacing(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListItemSpacing(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListPadding(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListPadding(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListFontFamily(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListFontFamily(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListFontSize(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListFontSize(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListColor(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListBackground(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListBackground(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListBorderColor(value: String) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListBorderColor(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListBorderWidth(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListBorderWidth(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListBorderStyle(value: TemplateBorderStyle) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListBorderStyle(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

    fun updateSelectedListBorderRadius(value: Float) {
        val element = _uiState.value.selectedElement ?: return
        val nextElement = element.withListBorderRadius(value)
        if (nextElement != element) execute(ReplaceElementCommand(element, nextElement))
    }

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

    private fun List<TemplateElement>.withEditorTestElementsIfEmpty(): List<TemplateElement> {
        if (isNotEmpty()) return this
        return listOf(
            TemplateElement.Rectangle(
                id = "test-header-band",
                name = "Header band",
                x = 48f,
                y = 48f,
                width = 500f,
                height = 86f,
                zIndex = 1,
                fillColor = "#EFF6FF",
                borderColor = "#93C5FD",
                borderWidth = 1f,
                borderRadius = 8f,
            ),
            TemplateElement.Text(
                id = "test-title",
                name = "Static heading",
                x = 72f,
                y = 66f,
                width = 250f,
                height = 34f,
                zIndex = 2,
                staticText = "Service order test sheet",
                fontSize = 20f,
                fontWeight = 700,
                color = "#1E3A8A",
            ),
            TemplateElement.Text(
                id = "test-order-number",
                name = "Bound order number",
                x = 72f,
                y = 104f,
                width = 250f,
                height = 24f,
                zIndex = 3,
                staticText = "Order {{ data.order_number }}",
                placeholderTag = "[DataRecord:order_number]",
                fontSize = 13f,
                color = "#1F2937",
            ),
            TemplateElement.Text(
                id = "test-total",
                name = "Bound currency total",
                x = 346f,
                y = 66f,
                width = 170f,
                height = 34f,
                zIndex = 4,
                staticText = "{{ currency(data.total_amount, \"USD\") }}",
                placeholderTag = "[DataRecord:total_amount]",
                fontSize = 18f,
                fontWeight = 700,
                textAlign = "right",
                color = "#166534",
            ),
            TemplateElement.Text(
                id = "test-conditional-priority",
                name = "Conditional priority text",
                x = 346f,
                y = 104f,
                width = 170f,
                height = 24f,
                zIndex = 5,
                staticText = "{{ if(gte(data.priority_level, 4), \"Priority order\", \"Standard order\") }}",
                fontSize = 12f,
                fontWeight = 600,
                textAlign = "right",
                color = "#B45309",
            ),
            TemplateElement.Rectangle(
                id = "test-customer-card",
                name = "Customer card shape",
                x = 48f,
                y = 158f,
                width = 240f,
                height = 116f,
                zIndex = 6,
                fillColor = "#F8FAFC",
                borderColor = "#CBD5E1",
                borderWidth = 1f,
                borderRadius = 8f,
            ),
            TemplateElement.Text(
                id = "test-customer-label",
                name = "Customer label",
                x = 68f,
                y = 176f,
                width = 190f,
                height = 20f,
                zIndex = 7,
                staticText = "Customer",
                fontSize = 11f,
                fontWeight = 700,
                color = "#64748B",
            ),
            TemplateElement.Text(
                id = "test-customer-name",
                name = "Bound customer name",
                x = 68f,
                y = 202f,
                width = 190f,
                height = 28f,
                zIndex = 8,
                staticText = "{{ data.customer_name }}",
                placeholderTag = "[DataRecord:customer_name]",
                fontSize = 17f,
                fontWeight = 700,
                color = "#111827",
            ),
            TemplateElement.Text(
                id = "test-customer-email",
                name = "Bound customer email",
                x = 68f,
                y = 236f,
                width = 190f,
                height = 22f,
                zIndex = 9,
                staticText = "{{ lower(data.customer_email) }}",
                placeholderTag = "[DataRecord:customer_email]",
                fontSize = 12f,
                color = "#475569",
            ),
            TemplateElement.Rectangle(
                id = "test-status-pill",
                name = "Conditional status pill",
                x = 328f,
                y = 158f,
                width = 220f,
                height = 52f,
                zIndex = 10,
                fillColor = "#FEF3C7",
                borderColor = "#F59E0B",
                borderWidth = 1f,
                borderRadius = 8f,
            ),
            TemplateElement.Text(
                id = "test-status-text",
                name = "Conditional amount text",
                x = 346f,
                y = 174f,
                width = 184f,
                height = 22f,
                zIndex = 11,
                staticText = "{{ if(gt(data.total_amount, 500), \"High value\", \"Regular value\") }}",
                fontSize = 14f,
                fontWeight = 700,
                textAlign = "center",
                color = "#92400E",
            ),
            TemplateElement.List(
                id = "test-service-items-list",
                name = "Bound service item list",
                x = 48f,
                y = 304f,
                width = 240f,
                height = 138f,
                zIndex = 12,
                fieldSlug = "service_items",
                prefix = "- ",
                itemSeparator = ",",
                maxItems = 8,
                columns = 1,
                itemSpacing = 6f,
                padding = 12f,
                fontSize = 12f,
                color = "#111827",
                backgroundColor = "#F0FDF4",
                borderColor = "#86EFAC",
                borderWidth = 1f,
                borderRadius = 8f,
            ),
            TemplateElement.Circle(
                id = "test-circle-accent",
                name = "Circle accent",
                x = 318f,
                y = 300f,
                width = 70f,
                height = 70f,
                zIndex = 13,
                fillColor = "#FCE7F3",
                borderColor = "#DB2777",
                borderWidth = 2f,
            ),
            TemplateElement.Rectangle(
                id = "test-overlap-front",
                name = "Overlap shape",
                x = 358f,
                y = 334f,
                width = 96f,
                height = 64f,
                zIndex = 14,
                fillColor = "#EDE9FE",
                borderColor = "#7C3AED",
                borderWidth = 2f,
                borderRadius = 6f,
                opacity = 0.9f,
            ),
            TemplateElement.QRCode(
                id = "test-bound-qr",
                name = "Bound QR code",
                x = 48f,
                y = 484f,
                width = 118f,
                height = 118f,
                zIndex = 15,
                text = "{{ data.qr_payload }}",
                borderColor = "#CBD5E1",
                borderWidth = 1f,
            ),
            TemplateElement.Text(
                id = "test-qr-caption",
                name = "QR caption",
                x = 48f,
                y = 610f,
                width = 118f,
                height = 28f,
                zIndex = 16,
                staticText = "QR from record URL",
                fontSize = 10f,
                textAlign = "center",
                color = "#475569",
            ),
            TemplateElement.Barcode(
                id = "test-bound-barcode",
                name = "Bound barcode",
                x = 204f,
                y = 500f,
                width = 260f,
                height = 82f,
                zIndex = 17,
                text = "{{ data.tracking_code }}",
                format = TemplateBarcodeFormat.Code128,
                showText = true,
                borderColor = "#CBD5E1",
                borderWidth = 1f,
            ),
            TemplateElement.Text(
                id = "test-notes",
                name = "Bound notes with fallback",
                x = 48f,
                y = 674f,
                width = 500f,
                height = 54f,
                zIndex = 18,
                staticText = "Notes: {{ default(data.notes, \"No notes supplied\") }}",
                placeholderTag = "[DataRecord:notes]",
                fontSize = 12f,
                color = "#334155",
                backgroundColor = "#F8FAFC",
                padding = 8f,
                borderColor = "#E2E8F0",
                borderWidth = 1f,
                borderRadius = 6f,
            ),
            TemplateElement.Line(
                id = "test-line",
                name = "Footer divider",
                x1 = 48f,
                y1 = 760f,
                x2 = 548f,
                y2 = 760f,
                thickness = 3f,
                zIndex = 19,
            ),
        )
    }

    private fun PageRect.normalized(): PageRect {
        val minSize = GeometryService.MinElementSize
        return copy(
            x = x,
            y = y,
            width = width.coerceAtLeast(minSize),
            height = height.coerceAtLeast(minSize),
        )
    }

    private fun newId(prefix: String): String {
        return "$prefix-${Random.nextLong(1_000_000_000L, 9_999_999_999L)}"
    }
}

private const val EditorTestSchemaId = "test_service_order"

enum class SelectionAlignment {
    Left,
    Center,
    Right,
    Top,
    Middle,
    Bottom,
}

enum class DistributionAxis {
    Horizontal,
    Vertical,
}

enum class SizeMatchAxis {
    Width,
    Height,
}

private fun TemplateElement.withCopiedIdentity(id: String, zIndex: Int): TemplateElement {
    val nextName = "$name copy"
    return updateCommon(name = nextName, zIndex = zIndex).let { element ->
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

private fun List<PageRect>.unionBounds(): PageRect? {
    if (isEmpty()) return null
    val left = minOf { it.x }
    val top = minOf { it.y }
    val right = maxOf { it.right }
    val bottom = maxOf { it.bottom }
    return PageRect(left, top, right - left, bottom - top)
}

private fun TemplateEditorState.referencedPreviewSchemaIds(): List<String> {
    val schemas = availableSchemas
    if (schemas.isEmpty()) return emptyList()
    val schemaByAlias = schemas
        .flatMap { schema -> schemaExpressionAliases(schema).map { alias -> alias to schema } }
        .toMap()
    val roots = document.elements.flatMap { element -> element.referencedExpressionRoots() }.toSet()
    val schemaIds = roots.mapNotNull { root ->
        when (root) {
            "data" -> schema?.id
            else -> schemaByAlias[root]?.id
        }
    }
    return schemaIds
        .ifEmpty { listOfNotNull(schema?.id ?: template?.targetSchemaId) }
        .distinct()
}

private fun TemplateElement.referencedExpressionRoots(): Set<String> {
    val roots = mutableSetOf<String>()
    fun addFrom(value: String?) {
        if (!value.isNullOrBlank()) roots += referencedExpressionRoots(value)
    }
    when (this) {
        is TemplateElement.Text -> {
            addFrom(staticText)
            expression.takeIf { it.isNotBlank() }?.let(::addFrom)
            if (!placeholderTag.isNullOrBlank()) roots += "data"
        }
        is TemplateElement.QRCode -> addFrom(text)
        is TemplateElement.Barcode -> addFrom(text)
        is TemplateElement.List -> {
            if (fieldSlug.contains(".")) {
                roots += fieldSlug.substringBefore(".")
            } else if (fieldSlug.isNotBlank()) {
                roots += "data"
            }
        }
        is TemplateElement.Image,
        is TemplateElement.Circle,
        is TemplateElement.Rectangle,
        is TemplateElement.Line -> Unit
    }
    return roots
}

private fun normalizeRotation(rotation: Float): Float {
    val normalized = rotation % 360f
    return if (normalized < 0f) normalized + 360f else normalized
}
