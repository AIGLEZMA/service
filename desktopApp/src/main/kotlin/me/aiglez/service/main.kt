package me.aiglez.service

import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import me.aiglez.service.di.initKoin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

fun main() = application {
    var isReady by remember { mutableStateOf(false) }
    var showWaitingScreen by remember { mutableStateOf(false) }
    var exitRequested by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) { initKoin() }
        isReady = true
    }
    LaunchedEffect(Unit) {
        delay(600)
        if (!isReady) showWaitingScreen = true
    }

    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    Window(
        onCloseRequest = {
            if (isReady) {
                exitRequested = true
            } else {
                exitApplication()
            }
        },
        state = windowState,
        title = "Service",
    ) {
        if (isReady) {
            App(
                exitRequested = exitRequested,
                onExitCancelled = { exitRequested = false },
                onExitConfirmed = ::exitApplication,
            )
        } else {
            StartupScreen(showWaiting = showWaitingScreen)
        }
    }
}
