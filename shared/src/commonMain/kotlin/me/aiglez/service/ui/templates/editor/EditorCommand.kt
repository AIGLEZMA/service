package me.aiglez.service.ui.templates.editor

import me.aiglez.service.domain.models.TemplateElement

data class EditorDocument(
    val elements: List<TemplateElement> = emptyList(),
    val selectedElementIds: List<String> = emptyList(),
) {
    private val elementsById: Map<String, TemplateElement> = elements.associateBy { it.id }

    val selectedElementId: String?
        get() = selectedElementIds.firstOrNull()

    val selectedElement: TemplateElement?
        get() = selectedElementId?.let(elementsById::get)

    val selectedElements: List<TemplateElement>
        get() = selectedElementIds.mapNotNull(elementsById::get)

    fun withSelection(elementIds: List<String>): EditorDocument {
        if (elementIds.isEmpty()) {
            return if (selectedElementIds.isEmpty()) this else copy(selectedElementIds = emptyList())
        }
        val nextSelection = if (elementIds.size == 1) {
            val id = elementIds.first()
            if (id in elementsById) listOf(id) else emptyList()
        } else {
            elementIds.distinct().filter { it in elementsById }
        }
        return if (selectedElementIds == nextSelection) this else copy(selectedElementIds = nextSelection)
    }
}

interface EditorCommand {
    fun apply(document: EditorDocument): EditorDocument
    fun revert(document: EditorDocument): EditorDocument
}

data class AddElementCommand(
    val element: TemplateElement,
) : EditorCommand {
    override fun apply(document: EditorDocument): EditorDocument {
        return document.copy(
            elements = document.elements + element,
            selectedElementIds = listOf(element.id),
        )
    }

    override fun revert(document: EditorDocument): EditorDocument {
        return document.copy(
            elements = document.elements.filterNot { it.id == element.id },
            selectedElementIds = emptyList(),
        )
    }
}

data class AddElementsCommand(
    val elementsToAdd: List<TemplateElement>,
) : EditorCommand {
    override fun apply(document: EditorDocument): EditorDocument {
        return document.copy(
            elements = document.elements + elementsToAdd,
            selectedElementIds = elementsToAdd.map { it.id },
        )
    }

    override fun revert(document: EditorDocument): EditorDocument {
        val addIds = elementsToAdd.map { it.id }.toSet()
        return document.copy(
            elements = document.elements.filterNot { it.id in addIds },
            selectedElementIds = emptyList(),
        )
    }
}

data class DeleteElementCommand(
    val element: TemplateElement,
) : EditorCommand {
    override fun apply(document: EditorDocument): EditorDocument {
        return document.copy(
            elements = document.elements.filterNot { it.id == element.id },
            selectedElementIds = document.selectedElementIds.filterNot { it == element.id },
        )
    }

    override fun revert(document: EditorDocument): EditorDocument {
        return document.copy(
            elements = document.elements + element,
            selectedElementIds = listOf(element.id),
        )
    }
}

data class DeleteElementsCommand(
    val elementsToDelete: List<TemplateElement>,
) : EditorCommand {
    override fun apply(document: EditorDocument): EditorDocument {
        val deleteIds = elementsToDelete.map { it.id }.toSet()
        return document.copy(
            elements = document.elements.filterNot { it.id in deleteIds },
            selectedElementIds = document.selectedElementIds.filterNot { it in deleteIds },
        )
    }

    override fun revert(document: EditorDocument): EditorDocument {
        return document.copy(
            elements = document.elements + elementsToDelete,
            selectedElementIds = elementsToDelete.map { it.id },
        )
    }
}

data class ReplaceElementCommand(
    val before: TemplateElement,
    val after: TemplateElement,
    val selectAfter: Boolean = true,
) : EditorCommand {
    override fun apply(document: EditorDocument): EditorDocument {
        return document.copy(
            elements = document.elements.map { if (it.id == before.id) after else it },
            selectedElementIds = if (selectAfter) listOf(after.id) else document.selectedElementIds.map { if (it == before.id) after.id else it },
        )
    }

    override fun revert(document: EditorDocument): EditorDocument {
        return document.copy(
            elements = document.elements.map { if (it.id == after.id) before else it },
            selectedElementIds = listOf(before.id),
        )
    }
}

data class ReplaceElementsCommand(
    val replacements: List<Pair<TemplateElement, TemplateElement>>,
    val preserveSelection: Boolean = true,
) : EditorCommand {
    override fun apply(document: EditorDocument): EditorDocument {
        val afterById = replacements.associate { it.first.id to it.second }
        return document.copy(
            elements = document.elements.map { afterById[it.id] ?: it },
            selectedElementIds = if (preserveSelection) {
                document.selectedElementIds.map { id -> afterById[id]?.id ?: id }
            } else {
                replacements.map { it.second.id }
            },
        )
    }

    override fun revert(document: EditorDocument): EditorDocument {
        val beforeById = replacements.associate { it.second.id to it.first }
        return document.copy(
            elements = document.elements.map { beforeById[it.id] ?: it },
            selectedElementIds = if (preserveSelection) {
                document.selectedElementIds.map { id -> beforeById[id]?.id ?: id }
            } else {
                replacements.map { it.first.id }
            },
        )
    }
}

class HistoryManager {
    private val undoStack = mutableListOf<EditorCommand>()
    private val redoStack = mutableListOf<EditorCommand>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun execute(document: EditorDocument, command: EditorCommand): EditorDocument {
        val next = command.apply(document)
        undoStack += command
        redoStack.clear()
        return next
    }

    fun undo(document: EditorDocument): EditorDocument {
        val command = undoStack.removeLastOrNull() ?: return document
        redoStack += command
        return command.revert(document)
    }

    fun redo(document: EditorDocument): EditorDocument {
        val command = redoStack.removeLastOrNull() ?: return document
        undoStack += command
        return command.apply(document)
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}



