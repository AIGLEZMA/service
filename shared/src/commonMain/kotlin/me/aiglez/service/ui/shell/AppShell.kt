package me.aiglez.service.ui.shell

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import me.aiglez.service.GLASSMORPHISM_INTENSITY
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.ui.components.WorkspaceSidebar
import me.aiglez.service.ui.components.WorkspaceTopBar
import me.aiglez.service.ui.navigation.AppScreen
import me.aiglez.service.ui.records.*
import me.aiglez.service.ui.templates.CompileScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppShell(
    sidebarViewModel: SidebarViewModel = koinViewModel(),
) {
    val schemas by sidebarViewModel.schemas.collectAsState()
    val navStack = remember { mutableStateListOf<AppScreen>(AppScreen.Dashboard) }
    val currentScreen = navStack.lastOrNull() ?: AppScreen.Dashboard

    fun navigate(screen: AppScreen) {
        if (navStack.lastOrNull() != screen) {
            navStack.add(screen)
        }
    }

    fun navigateBack() {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.lastIndex)
        }
    }

    WorkspaceScaffold(
        schemas = schemas,
        selectedSchemaId = currentScreen.selectedSchemaId,
        currentRoute = currentScreen.chrome(schemas),
        showFab = currentScreen == AppScreen.Dashboard,
        onBackClick = if (navStack.size > 1) ::navigateBack else null,
        onCreateTemplateClick = { navigate(AppScreen.Compile("")) },
        onHomeClick = { navigate(AppScreen.Dashboard) },
        onCreateSchemaClick = { navigate(AppScreen.SchemaCreate) },
        onSchemaClick = { navigate(AppScreen.RecordList(it)) },
        onAddRecordClick = { navigate(AppScreen.RecordCreate(it)) },
        onEditSchemaClick = { navigate(AppScreen.SchemaEdit(it)) },
        onManageSchemasClick = { navigate(AppScreen.SchemaManagement) },
    ) {
        NavDisplay(
            backStack = navStack,
            modifier = Modifier.fillMaxSize(),
            onBack = { navigateBack() },
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

@Composable
private fun WorkspaceScaffold(
    schemas: List<DataSchema>,
    selectedSchemaId: String?,
    currentRoute: RouteChrome,
    showFab: Boolean,
    onBackClick: (() -> Unit)?,
    onCreateTemplateClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCreateSchemaClick: () -> Unit,
    onSchemaClick: (String) -> Unit,
    onAddRecordClick: (String) -> Unit,
    onEditSchemaClick: (String) -> Unit,
    onManageSchemasClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (showFab) {
                ExtendedFloatingActionButton(
                    text = { Text(text = "Create Template") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                        )
                    },
                    onClick = onCreateTemplateClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            WorkspaceBackdrop(glassmorphismIntensity = GLASSMORPHISM_INTENSITY)

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    WorkspaceSidebar(
                        schemas = schemas,
                        selectedSchemaId = selectedSchemaId,
                        glassmorphismIntensity = GLASSMORPHISM_INTENSITY,
                        onHomeClick = onHomeClick,
                        onCreateSchemaClick = onCreateSchemaClick,
                        onSchemaClick = onSchemaClick,
                        onAddRecordClick = onAddRecordClick,
                        onEditSchemaClick = onEditSchemaClick,
                        onManageSchemasClick = onManageSchemasClick,
                    )

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        //shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 6.dp,
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            WorkspaceTopBar(
                                title = currentRoute.title,
                                description = currentRoute.description,
                                count = schemas.size,
                                countLabel = if (schemas.size == 1) "Schema" else "Schemas",
                                onBackClick = onBackClick,
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 28.dp, vertical = 24.dp),
                                contentAlignment = Alignment.TopStart,
                                content = content,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkspaceBackdrop(glassmorphismIntensity: Float) {
    if (glassmorphismIntensity > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background.copy(
                        alpha = 0.75f + (1f - 0.75f) * (1f - glassmorphismIntensity),
                    ),
                ),
        ) {
            Box(modifier = Modifier.fillMaxSize().blur(100.dp)) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val tertiaryColor = MaterialTheme.colorScheme.tertiary
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.35f * glassmorphismIntensity),
                                Color.Transparent,
                            ),
                            center = Offset(size.width * 0.15f, size.height * 0.25f),
                            radius = size.width * 0.5f,
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.25f),
                        radius = size.width * 0.5f,
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                tertiaryColor.copy(alpha = 0.3f * glassmorphismIntensity),
                                Color.Transparent,
                            ),
                            center = Offset(size.width * 0.85f, size.height * 0.75f),
                            radius = size.width * 0.6f,
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.75f),
                        radius = size.width * 0.6f,
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        )
    }
}

private data class RouteChrome(
    val title: String,
    val description: String?,
)

private val AppScreen.selectedSchemaId: String?
    get() = when (this) {
        is AppScreen.RecordList -> schemaId
        is AppScreen.RecordCreate -> schemaId
        is AppScreen.SchemaEdit -> schemaId
        else -> null
    }

private fun AppScreen.chrome(schemas: List<DataSchema>): RouteChrome = when (this) {
    AppScreen.Dashboard -> RouteChrome(
        title = "Templates",
        description = "Manage document templates and their data sources",
    )

    AppScreen.SchemaManagement -> RouteChrome(
        title = "Data Schemas",
        description = "Review, modify, and archive reusable data models",
    )

    AppScreen.SchemaCreate -> RouteChrome(
        title = "Data Schemas",
        description = "Create a reusable data model for generated documents",
    )

    is AppScreen.SchemaEdit -> RouteChrome(
        title = schemaTitle(schemas, schemaId, fallback = "Schema Blueprint"),
        description = "Modify this schema blueprint",
    )

    is AppScreen.RecordList -> RouteChrome(
        title = schemaTitle(schemas, schemaId, fallback = "Records"),
        description = "Review captured entries for this data model",
    )

    is AppScreen.RecordCreate -> RouteChrome(
        title = schemaTitle(schemas, schemaId, fallback = "New Entry"),
        description = "Capture a new record for this data model",
    )

    is AppScreen.Compile -> RouteChrome(
        title = "Template Compiler",
        description = "Preview token translations and export a PDF document",
    )
}

private fun schemaTitle(
    schemas: List<DataSchema>,
    schemaId: String,
    fallback: String,
): String = schemas.firstOrNull { it.id == schemaId }?.name ?: fallback
