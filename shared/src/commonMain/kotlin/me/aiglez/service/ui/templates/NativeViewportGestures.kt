package me.aiglez.service.ui.templates

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset

internal data class NativeViewportGesture(
    val sequence: Long,
    val kind: NativeViewportGestureKind,
    val positionInWindow: Offset,
    val panDelta: Offset = Offset.Zero,
    val zoomFactor: Float = 1f,
)

internal enum class NativeViewportGestureKind {
    Pan,
    Zoom,
}

@Composable
internal expect fun rememberNativeViewportGesture(): NativeViewportGesture?
