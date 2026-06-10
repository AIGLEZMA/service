package me.aiglez.service

import androidx.compose.ui.window.Window
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import me.aiglez.service.ui.components.TitleBar

fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Service",
        transparent = true,
        undecorated = true,
    ) {
        App(
            onCloseRequest = ::exitApplication,
            onMinimizeRequest = { windowState.isMinimized = true },
            onMaximizeRequest = {
                windowState.placement = if (windowState.placement == WindowPlacement.Maximized) {
                    WindowPlacement.Floating
                } else {
                    WindowPlacement.Maximized
                }
            },
            titleBar = {
                WindowDraggableArea {
                    TitleBar(
                        onClose = { exitApplication() },
                        onMinimize = { windowState.isMinimized = true },
                        onMaximize = {
                            windowState.placement = if (windowState.placement == WindowPlacement.Maximized) {
                                WindowPlacement.Floating
                            } else {
                                WindowPlacement.Maximized
                            }
                        }
                    )
                }
            }
        )
    }
}
