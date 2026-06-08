package me.aiglez.service

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(placement = WindowPlacement.Maximized),
        title = "Service",
    ) {
        App()
    }
}
