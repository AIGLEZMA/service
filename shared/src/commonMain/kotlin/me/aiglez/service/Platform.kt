package me.aiglez.service

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun PlatformTooltip(
    tooltip: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)