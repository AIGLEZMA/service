package me.aiglez.service.ui.templates.editor

import me.aiglez.service.domain.models.TemplateElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HistoryManagerTest {
    @Test
    fun addUndoRedoKeepsSelectionAndElementListConsistent() {
        val history = HistoryManager()
        val element = TemplateElement.Rectangle(
            id = "rect-1",
            x = 10f,
            y = 20f,
            width = 100f,
            height = 80f,
        )

        val added = history.execute(EditorDocument(), AddElementCommand(element))
        assertEquals(listOf(element), added.elements)
        assertEquals("rect-1", added.selectedElementId)
        assertTrue(history.canUndo)
        assertFalse(history.canRedo)

        val undone = history.undo(added)
        assertEquals(emptyList(), undone.elements)
        assertEquals(null, undone.selectedElementId)
        assertFalse(history.canUndo)
        assertTrue(history.canRedo)

        val redone = history.redo(undone)
        assertEquals(listOf(element), redone.elements)
        assertEquals("rect-1", redone.selectedElementId)
    }

    @Test
    fun replaceCommandCanRepresentOneCompletedDrag() {
        val history = HistoryManager()
        val before = TemplateElement.Rectangle(
            id = "rect-1",
            x = 10f,
            y = 20f,
            width = 100f,
            height = 80f,
        )
        val after = before.copy(x = 50f, y = 70f)
        val document = EditorDocument(elements = listOf(before), selectedElementIds = listOf(before.id))

        val moved = history.execute(document, ReplaceElementCommand(before, after))
        assertEquals(after, moved.elements.single())

        val undone = history.undo(moved)
        assertEquals(before, undone.elements.single())
    }

    @Test
    fun deleteElementsCommandRemovesMultiSelection() {
        val first = TemplateElement.Rectangle(id = "first", x = 0f, y = 0f)
        val second = TemplateElement.Rectangle(id = "second", x = 20f, y = 20f)
        val document = EditorDocument(
            elements = listOf(first, second),
            selectedElementIds = listOf(first.id, second.id),
        )

        val deleted = DeleteElementsCommand(listOf(first, second)).apply(document)

        assertEquals(emptyList(), deleted.elements)
        assertEquals(emptyList(), deleted.selectedElementIds)
    }
}



