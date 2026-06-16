package me.aiglez.service.ui.shell

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.ui.navigation.AppScreen
import me.aiglez.service.ui.records.DashboardScreen
import me.aiglez.service.ui.records.RecordCreateScreen
import me.aiglez.service.ui.records.RecordListScreen
import me.aiglez.service.ui.records.SchemaCreateScreen
import me.aiglez.service.ui.records.SchemaManagementScreen
import me.aiglez.service.ui.templates.CompileScreen
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    onExitApplication: () -> Unit,
    sidebarViewModel: SidebarViewModel = koinViewModel(),
) {
    val schemas by sidebarViewModel.schemas.collectAsState()
    val navStack = remember { mutableStateListOf<AppScreen>(AppScreen.Dashboard) }

    fun navigate(screen: AppScreen) {
        if (navStack.lastOrNull() != screen) {
            navStack.add(screen)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Row(modifier = Modifier.fillMaxSize()) {
            AppSidebar(
                schemas = schemas,
                selected = navStack.lastOrNull() ?: AppScreen.Dashboard,
                onNavigate = ::navigate,
            )
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                TopAppBar(
                    title = { Text("Service Workspace") },
                    actions = {
                        IconButton(onClick = onExitApplication) {
                            Icon(Icons.Default.Close, contentDescription = "Close application")
                        }
                    },
                )
                NavDisplay(
                    backStack = navStack,
                    modifier = Modifier.fillMaxSize(),
                    onBack = {
                        if (navStack.size > 1) {
                            navStack.removeAt(navStack.lastIndex)
                        }
                    },
                    entryProvider = entryProvider {
                        entry<AppScreen.Dashboard> {
                            DashboardScreen(
                                onOpenTemplate = { navigate(AppScreen.Compile(it)) },
                                onCreateTemplate = { navigate(AppScreen.Compile("")) },
                            )
                        }
                        entry<AppScreen.SchemaManagement> {
                            SchemaManagementScreen(
                                onEditSchema = { navigate(AppScreen.SchemaEdit(it)) },
                                onOpenRecords = { navigate(AppScreen.RecordList(it)) },
                            )
                        }
                        entry<AppScreen.SchemaCreate> {
                            SchemaCreateScreen(
                                schemaId = null,
                                onSaved = { navigate(AppScreen.RecordList(it)) },
                            )
                        }
                        entry<AppScreen.SchemaEdit> { screen ->
                            SchemaCreateScreen(
                                schemaId = screen.schemaId,
                                onSaved = { navigate(AppScreen.RecordList(it)) },
                            )
                        }
                        entry<AppScreen.RecordList> { screen ->
                            RecordListScreen(
                                schemaId = screen.schemaId,
                                onCreateRecord = { navigate(AppScreen.RecordCreate(screen.schemaId)) },
                            )
                        }
                        entry<AppScreen.RecordCreate> { screen ->
                            RecordCreateScreen(
                                schemaId = screen.schemaId,
                                onSaved = { navigate(AppScreen.RecordList(screen.schemaId)) },
                            )
                        }
                        entry<AppScreen.Compile> { screen ->
                            CompileScreen(templateId = screen.templateId)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun AppSidebar(
    schemas: List<DataSchema>,
    selected: AppScreen,
    onNavigate: (AppScreen) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            .padding(16.dp),
    ) {
        Text(
            text = "Service",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(24.dp))
        SidebarItem(
            label = "Home Dashboard",
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            selected = selected == AppScreen.Dashboard,
            onClick = { onNavigate(AppScreen.Dashboard) },
        )
        SidebarItem(
            label = "PDF Layout Studio",
            icon = { Icon(Icons.Default.Description, contentDescription = null) },
            selected = selected is AppScreen.Compile,
            onClick = { onNavigate(AppScreen.Compile("")) },
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Data Collections",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onNavigate(AppScreen.SchemaCreate) }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Create Schema")
            }
            IconButton(onClick = { onNavigate(AppScreen.SchemaManagement) }) {
                Icon(Icons.Default.MoreHoriz, contentDescription = "Show more")
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize().padding(end = 10.dp),
            ) {
                items(schemas, key = { it.id }) { schema ->
                    SidebarItem(
                        label = schema.name,
                        icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        selected = selected == AppScreen.RecordList(schema.id),
                        onClick = { onNavigate(AppScreen.RecordList(schema.id)) },
                    )
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun SidebarItem(
    label: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val background = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        hovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        else -> Color.Transparent
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .background(background, MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
    ) {
        icon()
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
