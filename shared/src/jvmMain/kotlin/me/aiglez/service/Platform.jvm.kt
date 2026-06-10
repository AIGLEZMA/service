package me.aiglez.service

import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
actual fun PlatformTooltip(
    tooltip: String,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.TooltipArea(
        tooltip = {
            Surface(
                color = MaterialTheme.colorScheme.inverseSurface,
                shape = RoundedCornerShape(4.dp),
                tonalElevation = 4.dp
            ) {
                Text(
                    text = tooltip,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        },
        modifier = modifier,
        delayMillis = 600,
        content = content
    )
}