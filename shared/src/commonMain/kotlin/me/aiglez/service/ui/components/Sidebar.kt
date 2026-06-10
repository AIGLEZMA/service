package me.aiglez.service.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.aiglez.service.data.dynamicdata.DynamicData

@Composable
fun Sidebar(
    dynamicData: List<DynamicData>,
    selectedDynamicDataId: Long?,
    glassmorphismIntensity: Float,
    onHomeClick: () -> Unit,
    onCreateDynamicDataClick: () -> Unit,
    onDynamicDataClick: (Long) -> Unit,
    onAddDataClick: (Long) -> Unit,
    onDeleteDynamicDataClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (glassmorphismIntensity > 0f) {
        val baseAlpha = 0.28f
        // Scale alpha from baseAlpha up to 1.0 based on intensity
        // Lower intensity = higher alpha (more solid)
        val alpha = baseAlpha + (1f - baseAlpha) * (1f - glassmorphismIntensity)
        MaterialTheme.colorScheme.surface.copy(alpha = alpha)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderModifier = if (glassmorphismIntensity > 0f) {
        Modifier.border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.35f * glassmorphismIntensity),
                    Color.White.copy(alpha = 0.1f * glassmorphismIntensity),
                )
            ),
            shape = RoundedCornerShape(12.dp)
        )
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight()
            .then(borderModifier),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        tonalElevation = if (glassmorphismIntensity > 0f) 0.dp else 3.dp,
        shadowElevation = if (glassmorphismIntensity > 0f) 0.dp else 10.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            AppBrandHeader(onClick = onHomeClick)
            QuickActionCard(onCreateDynamicDataClick = onCreateDynamicDataClick)
            DynamicDataList(
                items = dynamicData,
                selectedItemId = selectedDynamicDataId,
                onItemClick = onDynamicDataClick,
                onAddDataClick = onAddDataClick,
                onDeleteClick = onDeleteDynamicDataClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
