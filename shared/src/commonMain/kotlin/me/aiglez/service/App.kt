package me.aiglez.service

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.aiglez.service.ui.ServiceApp
import me.aiglez.service.ui.theme.ServiceTheme

@Composable
@Preview
fun App() {
    ServiceTheme {
        ServiceApp()
    }
}