package me.aiglez.service.ui.shell

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.ui.components.WorkspaceSidebar
import me.aiglez.service.ui.components.WorkspaceTopBar
import me.aiglez.service.ui.help.HelpScreen
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
        showDataChrome = currentScreen !is AppScreen.Compile,
        onBackClick = if (navStack.size > 1) ::navigateBack else null,
        onCreateTemplateClick = { navigate(AppScreen.Compile("")) },
        onHomeClick = { navigate(AppScreen.Dashboard) },
        onHelpClick = { navigate(AppScreen.Help) },
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
                entry<AppScreen.Help> {
                    HelpScreen()
                }
                entry<AppScreen.SchemaManagement> {
                    SchemaManagementScreen(
                        onEditSchema = { navigate(AppScreen.SchemaEdit(it)) },
                        onOpenRecords = { navigate(AppScreen.RecordList(it)) },
                        onCreateSchema = { navigate(AppScreen.SchemaCreate) },
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
    showDataChrome: Boolean,
    onBackClick: (() -> Unit)?,
    onCreateTemplateClick: () -> Unit,
    onHomeClick: () -> Unit,
    onHelpClick: () -> Unit,
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
                    text = { Text(text = "Créer une template") },
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
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    if (showDataChrome) {
                        WorkspaceSidebar(
                            schemas = schemas,
                            selectedSchemaId = selectedSchemaId,
                            onHomeClick = onHomeClick,
                            onHelpClick = onHelpClick,
                            onCreateSchemaClick = onCreateSchemaClick,
                            onSchemaClick = onSchemaClick,
                            onAddRecordClick = onAddRecordClick,
                            onEditSchemaClick = onEditSchemaClick,
                            onManageSchemasClick = onManageSchemasClick,
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 6.dp,
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (showDataChrome) {
                                WorkspaceTopBar(
                                    title = currentRoute.title,
                                    description = currentRoute.description,
                                    count = schemas.size,
                                    countLabel = if (schemas.size == 1) "Modèle de données" else "Modèles de données",
                                    onBackClick = onBackClick,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (showDataChrome) {
                                            Modifier.padding(horizontal = 28.dp, vertical = 24.dp)
                                        } else {
                                            Modifier
                                        }
                                    ),
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
        description = "Gérez les templates et leurs modèles de donnée",
    )

    AppScreen.Help -> RouteChrome(
        title = "Aide",
        description = "Raccourcis clavier, souris et pavé tactile de l'éditeur",
    )

    AppScreen.SchemaManagement -> RouteChrome(
        title = "Modèles de données",
        description = "Consultez, modifiez et archivez les modèles de données réutilisables",
    )

    AppScreen.SchemaCreate -> RouteChrome(
        title = "Modèles de données",
        description = "Créez un modèle de données réutilisable pour les templates",
    )

    is AppScreen.SchemaEdit -> RouteChrome(
        title = schemaTitle(schemas, schemaId, fallback = "Modèle de données"),
        description = "Modifiez ce modèle de données",
    )

    is AppScreen.RecordList -> RouteChrome(
        title = schemaTitle(schemas, schemaId, fallback = "Données"),
        description = "Consultez les données saisies pour ce modèle de données",
    )

    is AppScreen.RecordCreate -> RouteChrome(
        title = schemaTitle(schemas, schemaId, fallback = "Nouvelle donnée"),
        description = "Saisissez une nouvelle donnée pour ce modèle de données",
    )

    is AppScreen.Compile -> RouteChrome(
        title = "Template Editor",
        description = "Composez visuellement le modèle PDF",
    )
}

private fun schemaTitle(
    schemas: List<DataSchema>,
    schemaId: String,
    fallback: String,
): String = schemas.firstOrNull { it.id == schemaId }?.name ?: fallback
