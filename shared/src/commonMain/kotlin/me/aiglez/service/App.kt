package me.aiglez.service

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.aiglez.service.ui.theme.ServiceTheme

const val GLASSMORPHISM_INTENSITY = 0.6f

@Composable
@Preview
fun App(
    onCloseRequest: () -> Unit = {},
    onMinimizeRequest: () -> Unit = {},
    onMaximizeRequest: () -> Unit = {},
    titleBar: @Composable () -> Unit = {},
) {
    ServiceTheme(darkTheme = false) {

    }
}