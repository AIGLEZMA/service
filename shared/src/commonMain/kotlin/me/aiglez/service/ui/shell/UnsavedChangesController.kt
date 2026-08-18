package me.aiglez.service.ui.shell

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UnsavedChangesState(
    val hasUnsavedChanges: Boolean = false,
    val isSaving: Boolean = false,
)

class UnsavedChangesController {
    private val _state = MutableStateFlow(UnsavedChangesState())
    val state: StateFlow<UnsavedChangesState> = _state.asStateFlow()

    private var owner: Any? = null
    private var saveAction: (((Boolean) -> Unit) -> Unit)? = null

    fun attach(owner: Any, saveAction: ((Boolean) -> Unit) -> Unit) {
        this.owner = owner
        this.saveAction = saveAction
    }

    fun update(owner: Any, hasUnsavedChanges: Boolean, isSaving: Boolean) {
        if (this.owner != owner) return
        _state.value = UnsavedChangesState(hasUnsavedChanges, isSaving)
    }

    fun detach(owner: Any) {
        if (this.owner != owner) return
        this.owner = null
        saveAction = null
        _state.value = UnsavedChangesState()
    }

    fun save(onComplete: (Boolean) -> Unit) {
        val action = saveAction
        if (action == null) {
            onComplete(false)
        } else {
            action(onComplete)
        }
    }

    fun discard() {
        _state.value = UnsavedChangesState()
    }
}
