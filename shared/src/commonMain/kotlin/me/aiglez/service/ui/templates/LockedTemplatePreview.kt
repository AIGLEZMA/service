package me.aiglez.service.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.min
import me.aiglez.service.ui.templates.editor.TemplateEditorState

/** A fitted, non-interactive template view opened from the dashboard. */
@Composable
internal fun LockedTemplatePreview(
    state: TemplateEditorState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.background(Color(0xFFE5E7EB)),
        contentAlignment = Alignment.Center,
    ) {
        val pageDimensions = templatePageDimensions(state.template?.pageSize)
        val fittedZoom = min(
            maxWidth.value / pageDimensions.width,
            maxHeight.value / pageDimensions.height,
        ).coerceAtLeast(0.01f)
        val previewState = state.copy(
            document = state.document.copy(selectedElementIds = emptyList()),
            canvas = state.canvas.copy(
                zoom = fittedZoom,
                showPageOutline = false,
                showRulers = false,
                showGrid = false,
                showGuides = false,
                showMargins = false,
                showBleed = false,
                showSafeArea = false,
                showPageShadow = false,
            ),
        )

        Box {
            PageCanvas(
                state = previewState,
                navigationPanActive = false,
                paletteDrop = null,
                onConsumePaletteDrop = {},
                onAddElement = { _, _, _ -> },
                onAddDataField = { _, _, _, _, _ -> },
                onSelectElement = {},
                onSetSelection = {},
                onToggleSelection = {},
                onShowInlineEditHint = {},
                onPreviewBounds = { _, _ -> },
                onPreviewBoundsBatch = {},
                onPreviewRotation = { _, _ -> },
                onCommitBounds = { _, _ -> },
                onCommitBoundsBatch = { _, _ -> },
                onCommitRotation = { _, _ -> },
                onPan = {},
                onCopy = {},
                onPaste = {},
                onDuplicate = {},
                onDeleteSelected = {},
                onGroup = {},
                onUngroup = {},
                onLock = {},
                onHide = {},
                onBringToFront = {},
                onSendToBack = {},
                onAlign = {},
                onPageBoundsChanged = {},
                onCursorPagePointChange = {},
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent().changes.forEach { it.consume() }
                            }
                        }
                    },
            )
        }
    }
}
