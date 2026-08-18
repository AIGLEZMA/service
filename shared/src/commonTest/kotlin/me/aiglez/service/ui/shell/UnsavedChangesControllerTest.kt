package me.aiglez.service.ui.shell

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UnsavedChangesControllerTest {
    @Test
    fun savesThroughAttachedEditor() {
        val controller = UnsavedChangesController()
        val owner = Any()
        var saveCalled = false
        var saveResult = false
        controller.attach(owner) { complete ->
            saveCalled = true
            complete(true)
        }
        controller.update(owner, hasUnsavedChanges = true, isSaving = false)

        controller.save { saveResult = it }

        assertTrue(saveCalled)
        assertTrue(saveResult)
    }

    @Test
    fun ignoresUpdatesAndDetachFromAnotherEditor() {
        val controller = UnsavedChangesController()
        val owner = Any()
        controller.attach(owner) { it(true) }
        controller.update(owner, hasUnsavedChanges = true, isSaving = false)

        controller.update(Any(), hasUnsavedChanges = false, isSaving = false)
        controller.detach(Any())

        assertTrue(controller.state.value.hasUnsavedChanges)
        controller.discard()
        assertFalse(controller.state.value.hasUnsavedChanges)
    }

    @Test
    fun reportsFailedSaveWithoutDiscardingChanges() {
        val controller = UnsavedChangesController()
        val owner = Any()
        var saveResult = true
        controller.attach(owner) { complete -> complete(false) }
        controller.update(owner, hasUnsavedChanges = true, isSaving = false)

        controller.save { saveResult = it }

        assertFalse(saveResult)
        assertTrue(controller.state.value.hasUnsavedChanges)
    }
}
