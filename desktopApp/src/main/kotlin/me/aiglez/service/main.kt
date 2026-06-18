package me.aiglez.service

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import me.aiglez.service.di.initKoin

fun main() = application {
    remember { initKoin() }
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Service",
    ) {
        App()
    }
}
