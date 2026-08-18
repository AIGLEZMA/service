package me.aiglez.service.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun HelpScreen() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        HelpHeader()
        shortcutSections.forEach { section ->
            ShortcutSection(section)
        }
        ShortcutNotes()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HelpHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.46f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Raccourcis de l'editeur",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Utilisez Ctrl sur Windows/Linux et Cmd sur macOS.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ShortcutSection(section: ShortcutSectionData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = section.shortcutHeader,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        section.items.forEachIndexed { index, item ->
            ShortcutRow(item)
            if (index != section.items.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            }
        }
    }
}

@Composable
private fun ShortcutRow(item: ShortcutItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.action,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = item.shortcut,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(280.dp),
        )
    }
}

@Composable
private fun ShortcutNotes() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Notes rapides",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        shortcutNotes.forEach { note ->
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class ShortcutSectionData(
    val title: String,
    val shortcutHeader: String,
    val items: List<ShortcutItem>,
)

private data class ShortcutItem(
    val action: String,
    val shortcut: String,
)

private val shortcutSections = listOf(
    ShortcutSectionData(
        title = "Selection",
        shortcutHeader = "Shortcut / Mouse",
        items = listOf(
            ShortcutItem("Select element", "Click element"),
            ShortcutItem("Multi-select", "Shift + Click"),
            ShortcutItem("Add/remove from selection", "Ctrl/Cmd + Click"),
            ShortcutItem("Select by rectangle", "Click empty canvas + drag"),
            ShortcutItem("Select element behind another", "Alt + Click / repeated Ctrl/Cmd + Click"),
            ShortcutItem("Deselect all", "Esc or click empty canvas"),
            ShortcutItem("Select all elements", "Ctrl/Cmd + A"),
            ShortcutItem("Select all elements on current page", "Ctrl/Cmd + A"),
            ShortcutItem("Select all similar elements", "Ctrl/Cmd + Shift + A"),
        ),
    ),
    ShortcutSectionData(
        title = "Move And Resize",
        shortcutHeader = "Shortcut / Mouse",
        items = listOf(
            ShortcutItem("Move element", "Drag selected element"),
            ShortcutItem("Move precisely", "Arrow keys"),
            ShortcutItem("Move faster", "Shift + Arrow"),
            ShortcutItem("Move slower / fine nudge", "Alt + Arrow"),
            ShortcutItem("Move only horizontally/vertically", "Shift + Drag"),
            ShortcutItem("Temporarily disable snapping", "Ctrl/Cmd while dragging"),
            ShortcutItem("Duplicate while moving", "Alt + Drag"),
            ShortcutItem("Duplicate with same offset again", "Ctrl/Cmd + D"),
            ShortcutItem("Cancel drag operation", "Esc while dragging"),
            ShortcutItem("Resize", "Drag resize handles"),
            ShortcutItem("Keep aspect ratio", "Shift + Drag corner handle"),
            ShortcutItem("Resize from center", "Alt + Drag handle"),
            ShortcutItem("Keep aspect ratio + center resize", "Shift + Alt + Drag"),
            ShortcutItem("Resize only width/height", "Drag side handles"),
            ShortcutItem("Fine resize", "Alt + Arrow on handle-selected mode"),
            ShortcutItem("Reset size", "Double-click resize handle"),
            ShortcutItem("Auto-fit text box height", "Double-click bottom handle"),
            ShortcutItem("Crop image instead of resize", "Hold Ctrl/Cmd while dragging image handle"),
        ),
    ),
    ShortcutSectionData(
        title = "Rotate And Edit",
        shortcutHeader = "Shortcut / Mouse",
        items = listOf(
            ShortcutItem("Rotate element", "Drag rotation handle"),
            ShortcutItem("Snap rotation angle", "Shift while rotating"),
            ShortcutItem("Reset rotation to 0 deg", "Double-click rotation handle"),
            ShortcutItem("Rotate 90 deg clockwise", "Ctrl/Cmd + ]"),
            ShortcutItem("Rotate 90 deg counter-clockwise", "Ctrl/Cmd + ["),
            ShortcutItem("Copy", "Ctrl/Cmd + C"),
            ShortcutItem("Paste", "Ctrl/Cmd + V"),
            ShortcutItem("Cut", "Ctrl/Cmd + X"),
            ShortcutItem("Duplicate", "Ctrl/Cmd + D"),
            ShortcutItem("Paste in place", "Ctrl/Cmd + Shift + V"),
            ShortcutItem("Copy style", "Ctrl/Cmd + Alt + C"),
            ShortcutItem("Paste style", "Ctrl/Cmd + Alt + V"),
            ShortcutItem("Delete", "Delete / Backspace"),
            ShortcutItem("Undo", "Ctrl/Cmd + Z"),
            ShortcutItem("Redo", "Ctrl/Cmd + Shift + Z"),
            ShortcutItem("Redo alternative", "Ctrl/Cmd + Y"),
            ShortcutItem("Open history panel", "Ctrl/Cmd + Alt + Z"),
            ShortcutItem("Group selected elements", "Ctrl/Cmd + G"),
            ShortcutItem("Ungroup", "Ctrl/Cmd + Shift + G"),
            ShortcutItem("Enter group editing", "Double-click group"),
            ShortcutItem("Exit group editing", "Esc"),
            ShortcutItem("Select inside group", "Ctrl/Cmd + Click"),
        ),
    ),
    ShortcutSectionData(
        title = "View And Canvas",
        shortcutHeader = "Shortcut / Mouse / Touchpad",
        items = listOf(
            ShortcutItem("Zoom in", "Ctrl/Cmd + +"),
            ShortcutItem("Zoom out", "Ctrl/Cmd + -"),
            ShortcutItem("Reset zoom to 100%", "Ctrl/Cmd + 0"),
            ShortcutItem("Fit page", "Ctrl/Cmd + 1"),
            ShortcutItem("Fit width", "Ctrl/Cmd + 2"),
            ShortcutItem("Zoom to selection", "Ctrl/Cmd + 3"),
            ShortcutItem("Mouse wheel zoom", "Ctrl/Cmd + Mouse Wheel"),
            ShortcutItem("Trackpad pinch zoom", "Pinch in/out"),
            ShortcutItem("Show/hide rulers", "Shift + R"),
            ShortcutItem("Show/hide grid", "Shift + G"),
            ShortcutItem("Show/hide guides", "Shift + ;"),
            ShortcutItem("Show/hide margins", "Shift + M"),
            ShortcutItem("Preview mode", "Ctrl/Cmd + P or Ctrl/Cmd + Enter"),
            ShortcutItem("Full screen canvas", "Ctrl/Cmd + Shift + F"),
            ShortcutItem("Pan canvas", "Space + Drag"),
            ShortcutItem("Pan canvas alternative", "Middle mouse drag"),
            ShortcutItem("Pan with trackpad", "Two-finger drag"),
            ShortcutItem("Horizontal scroll", "Shift + Mouse Wheel"),
            ShortcutItem("Scroll vertically", "Mouse Wheel / two-finger scroll"),
            ShortcutItem("Center page", "Ctrl/Cmd + MoveHome"),
            ShortcutItem("Go to next page", "Page Down"),
            ShortcutItem("Go to previous page", "Page Up"),
            ShortcutItem("Go to first page", "MoveHome"),
            ShortcutItem("Go to last page", "MoveEnd"),
        ),
    ),
    ShortcutSectionData(
        title = "Text",
        shortcutHeader = "Shortcut / Mouse",
        items = listOf(
            ShortcutItem("Edit text", "Double-click text"),
            ShortcutItem("Finish text editing", "Esc or Ctrl/Cmd + Enter"),
            ShortcutItem("New line", "Enter"),
            ShortcutItem("Bold", "Ctrl/Cmd + B"),
            ShortcutItem("Italic", "Ctrl/Cmd + I"),
            ShortcutItem("Underline", "Ctrl/Cmd + U"),
            ShortcutItem("Increase font size", "Ctrl/Cmd + Shift + >"),
            ShortcutItem("Decrease font size", "Ctrl/Cmd + Shift + <"),
            ShortcutItem("Align text left", "Ctrl/Cmd + Shift + L"),
            ShortcutItem("Align text center", "Ctrl/Cmd + Shift + E"),
            ShortcutItem("Align text right", "Ctrl/Cmd + Shift + R"),
            ShortcutItem("Justify text", "Ctrl/Cmd + Shift + J"),
        ),
    ),
    ShortcutSectionData(
        title = "Mouse And Touchpad",
        shortcutHeader = "Shortcut / Mouse",
        items = listOf(
            ShortcutItem("Pan canvas vertically/horizontally", "Two-finger scroll"),
            ShortcutItem("Zoom in/out", "Pinch"),
            ShortcutItem("Fit page / reset view", "Double-tap empty canvas"),
            ShortcutItem("Context menu", "Two-finger tap / right click"),
            ShortcutItem("Horizontal scroll", "Shift + two-finger scroll"),
            ShortcutItem("Pan canvas", "Space + one-finger drag"),
            ShortcutItem("Vertical scroll", "Wheel"),
            ShortcutItem("Horizontal scroll", "Shift + Wheel"),
            ShortcutItem("Zoom", "Ctrl/Cmd + Wheel"),
            ShortcutItem("Pan", "Middle mouse drag"),
            ShortcutItem("Context menu", "Right click"),
            ShortcutItem("Duplicate", "Alt + drag"),
            ShortcutItem("Constrain movement/resize", "Shift + drag"),
            ShortcutItem("Disable snapping temporarily", "Ctrl/Cmd + drag"),
        ),
    ),
)

private val shortcutNotes = listOf(
    "Esc cancels the current action, exits text editing, exits group editing, or clears selection.",
    "Delete removes selected elements.",
    "Double-click enters edit mode when an editable element is selected.",
    "Alt + Drag duplicates.",
    "Shift + Drag constrains movement, resize ratio, or rotation angle.",
    "Ctrl/Cmd temporarily disables snapping while dragging.",
    "Arrow keys nudge selected elements.",
    "All Ctrl/Cmd shortcuts work with Ctrl on Windows/Linux and Cmd on macOS.",
)
