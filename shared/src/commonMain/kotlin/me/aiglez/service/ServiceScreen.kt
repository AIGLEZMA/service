package me.aiglez.service

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import me.aiglez.service.data.dynamicdata.DynamicData
import me.aiglez.service.ui.CreateDynamicDataScreen
import me.aiglez.service.ui.components.*
import me.aiglez.service.ui.state.ServiceViewModel

@Serializable
private sealed interface ServiceRoute : NavKey {
    val title: String
    val description: String
    val showDynamicDataPane: Boolean
    val showFab: Boolean
}

@Serializable
private data object TemplatesHomeRoute : ServiceRoute {
    override val title: String = "Modèles"
    override val description: String = "Gérez vos modèles et leurs données dynamiques"
    override val showDynamicDataPane: Boolean = true
    override val showFab: Boolean = true
}

@Serializable
private data object CreateDynamicDataRoute : ServiceRoute {
    override val title: String = "Données dynamiques"
    override val description: String = "Créez un modèle de données réutilisable dans vos rapports"
    override val showDynamicDataPane: Boolean = true
    override val showFab: Boolean = false
}

@Serializable
private data object CreateTemplateRoute : ServiceRoute {
    override val title: String = "Nouvelle template"
    override val description: String = "Configurez une nouvelle template de rapport"
    override val showDynamicDataPane: Boolean = true
    override val showFab: Boolean = false
}

private val serviceRouteSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(TemplatesHomeRoute::class, TemplatesHomeRoute.serializer())
            subclass(CreateDynamicDataRoute::class, CreateDynamicDataRoute.serializer())
            subclass(CreateTemplateRoute::class, CreateTemplateRoute.serializer())
        }
    }
}

@Composable
fun ServiceApp(
    viewModel: ServiceViewModel = viewModel { ServiceViewModel() },
    glassmorphismIntensity: Float = 0.5f,
    onCloseRequest: () -> Unit = {},
    onMinimizeRequest: () -> Unit = {},
    onMaximizeRequest: () -> Unit = {},
    titleBar: @Composable () -> Unit = {},
) {
    val backStack = rememberNavBackStack(serviceRouteSavedStateConfiguration, TemplatesHomeRoute)
    val uiState by viewModel.uiState.collectAsState()
    val currentRoute = backStack.lastOrNull() as? ServiceRoute ?: TemplatesHomeRoute

    ServiceScaffold(
        dynamicData = uiState.dynamicData,
        selectedDynamicDataId = uiState.selectedDynamicDataId,
        currentRouteTitle = currentRoute.title,
        currentRouteDescription = currentRoute.description,
        glassmorphismIntensity = glassmorphismIntensity,
        onCloseRequest = onCloseRequest,
        onMinimizeRequest = onMinimizeRequest,
        onMaximizeRequest = onMaximizeRequest,
        titleBar = titleBar,
        showFAB = currentRoute.showFab,
        onBackClick = if (backStack.size > 1 || uiState.selectedDynamicDataId != null) {
            {
                if (uiState.selectedDynamicDataId != null) {
                    viewModel.selectDynamicData(null)
                } else if (backStack.size > 1) {
                    backStack.removeAt(backStack.size - 1)
                }
            }
        } else null,
        onCreateDynamicDataClick = {
            backStack.add(CreateDynamicDataRoute)
        },
        onCreateTemplateClick = {
            backStack.add(CreateTemplateRoute)
        },
        onDynamicDataClick = { },
    ) {
        when (currentRoute) {
            TemplatesHomeRoute -> TemplateGrid(dynamicDataCount = uiState.dynamicData.size)
            CreateDynamicDataRoute -> CreateDynamicDataScreen(
                availableDynamicData = uiState.dynamicData,
                onSave = { name, fields ->
                    viewModel.addDynamicData(name, fields)
                    backStack.removeAt(backStack.size - 1)
                }
            )

            CreateTemplateRoute -> EmptyRoutePlaceholder(title = currentRoute.title)
        }
    }
}

@Composable
private fun ServiceScaffold(
    dynamicData: List<DynamicData>,
    selectedDynamicDataId: Long?,
    currentRouteTitle: String,
    currentRouteDescription: String?,
    glassmorphismIntensity: Float,
    showFAB: Boolean,
    onCloseRequest: () -> Unit,
    onMinimizeRequest: () -> Unit,
    onMaximizeRequest: () -> Unit,
    titleBar: @Composable () -> Unit,
    onBackClick: (() -> Unit)?,
    onCreateDynamicDataClick: () -> Unit,
    onCreateTemplateClick: () -> Unit,
    onDynamicDataClick: (Long) -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (showFAB) {
                ExtendedFloatingActionButton(
                    text = { Text(text = "Créer un modèle") },
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
                .clip(RoundedCornerShape(12.dp))
        ) {
            // Glassy Background layer
            if (glassmorphismIntensity > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.75f + (1f - 0.75f) * (1f - glassmorphismIntensity)))
                ) {
                    Box(modifier = Modifier.fillMaxSize().blur(100.dp)) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val tertiaryColor = MaterialTheme.colorScheme.tertiary
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.35f * glassmorphismIntensity),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.15f, size.height * 0.25f),
                                    radius = size.width * 0.5f
                                ),
                                center = Offset(size.width * 0.15f, size.height * 0.25f),
                                radius = size.width * 0.5f
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        tertiaryColor.copy(alpha = 0.3f * glassmorphismIntensity),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.85f, size.height * 0.75f),
                                    radius = size.width * 0.6f
                                ),
                                center = Offset(size.width * 0.85f, size.height * 0.75f),
                                radius = size.width * 0.6f
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                if (titleBar != {}) {
                    titleBar()
                } else {
                    TitleBar(
                        onClose = onCloseRequest,
                        onMinimize = onMinimizeRequest,
                        onMaximize = onMaximizeRequest,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Sidebar(
                        dynamicData = dynamicData,
                        selectedDynamicDataId = selectedDynamicDataId,
                        glassmorphismIntensity = glassmorphismIntensity,
                        onCreateDynamicDataClick = onCreateDynamicDataClick,
                        onDynamicDataClick = onDynamicDataClick,
                    )

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 6.dp,
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            WorkspaceTopBar(
                                title = currentRouteTitle,
                                description = currentRouteDescription,
                                templatesCount = dynamicData.size,
                                onBackClick = onBackClick,
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 28.dp, vertical = 24.dp),
                                contentAlignment = Alignment.Center,
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
private fun TemplateGrid(
    dynamicDataCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MetricCard(
                title = "MODÈLES",
                value = "0",
                detail = null,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "MODÈLES DE DONNÉES",
                value = dynamicDataCount.toString(),
                detail = null,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "RAPPORTS GÉNÉRÉS",
                value = "0",
                detail = null,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TemplateCard(
                title = "Intervention",
                description = "Modèle pour les rapports d'intervention.",
                icon = Icons.Default.Description,
                modifier = Modifier.weight(1f),
            )
            TemplateCard(
                title = "Facture",
                description = "Modèle pour la génération des factures.",
                icon = Icons.Default.Tune,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
