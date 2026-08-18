package me.aiglez.service.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.aiglez.service.domain.models.DataSchema

@Composable
fun WorkspaceSidebar(
    schemas: List<DataSchema>,
    selectedSchemaId: String?,
    collapsed: Boolean,
    onHomeClick: () -> Unit,
    onHelpClick: () -> Unit,
    onCreateSchemaClick: () -> Unit,
    onSchemaClick: (String) -> Unit,
    onAddRecordClick: (String) -> Unit,
    onEditSchemaClick: (String) -> Unit,
    onManageSchemasClick: () -> Unit,
    onToggleCollapsed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .width(if (collapsed) 80.dp else 320.dp)
            .fillMaxHeight(),
        tonalElevation = 3.dp,
        shadowElevation = 10.dp,
    ) {
        if (collapsed) {
            CompactSidebar(
                schemas = schemas,
                selectedSchemaId = selectedSchemaId,
                onHomeClick = onHomeClick,
                onHelpClick = onHelpClick,
                onCreateSchemaClick = onCreateSchemaClick,
                onSchemaClick = onSchemaClick,
                onManageSchemasClick = onManageSchemasClick,
                onToggleCollapsed = onToggleCollapsed,
            )
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AppBrandHeader(onClick = onHomeClick)
                }
                IconButton(onClick = onToggleCollapsed, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Réduire la barre latérale",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            QuickActionCard(onCreateSchemaClick = onCreateSchemaClick)
            SchemaList(
                schemas = schemas,
                selectedSchemaId = selectedSchemaId,
                onSchemaClick = onSchemaClick,
                onAddRecordClick = onAddRecordClick,
                onEditSchemaClick = onEditSchemaClick,
                onManageSchemasClick = onManageSchemasClick,
                modifier = Modifier.weight(1f),
            )
            HelpNavigationButton(onClick = onHelpClick)
            /*
            Button(
                onClick = onCreateSchemaClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ajouter un schéma de donnée",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
             */
        }
    }
}

@Composable
private fun CompactSidebar(
    schemas: List<DataSchema>,
    selectedSchemaId: String?,
    onHomeClick: () -> Unit,
    onHelpClick: () -> Unit,
    onCreateSchemaClick: () -> Unit,
    onSchemaClick: (String) -> Unit,
    onManageSchemasClick: () -> Unit,
    onToggleCollapsed: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompactSidebarButton(
            icon = Icons.Default.Menu,
            contentDescription = "Ouvrir la barre latérale",
            onClick = onToggleCollapsed,
        )
        CompactSidebarButton(
            icon = Icons.Default.AutoAwesome,
            contentDescription = "Accueil",
            selected = selectedSchemaId == null,
            onClick = onHomeClick,
        )
        CompactSidebarButton(
            icon = Icons.Default.Add,
            contentDescription = "Ajouter un modèle",
            onClick = onCreateSchemaClick,
        )
        CompactSidebarButton(
            icon = Icons.Default.Tune,
            contentDescription = "Gérer les modèles de données",
            onClick = onManageSchemasClick,
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(items = schemas, key = { it.id }) { schema ->
                CompactSidebarButton(
                    icon = Icons.Default.TableRows,
                    contentDescription = schema.name,
                    selected = schema.id == selectedSchemaId,
                    onClick = { onSchemaClick(schema.id) },
                )
            }
        }

        CompactSidebarButton(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = "Raccourcis et aide",
            onClick = onHelpClick,
        )
    }
}

@Composable
private fun CompactSidebarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SchemaList(
    schemas: List<DataSchema>,
    selectedSchemaId: String?,
    onSchemaClick: (String) -> Unit,
    onAddRecordClick: (String) -> Unit,
    onEditSchemaClick: (String) -> Unit,
    onManageSchemasClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Modèles de données",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${schemas.size} actifs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onManageSchemasClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Gérer les modèles de données",
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = schemas, key = { it.id }) { schema ->
                SchemaListItem(
                    schema = schema,
                    selected = schema.id == selectedSchemaId,
                    onClick = { onSchemaClick(schema.id) },
                    onAddRecordClick = { onAddRecordClick(schema.id) },
                    onEditSchemaClick = { onEditSchemaClick(schema.id) },
                )
            }
        }
    }
}

@Composable
private fun SchemaListItem(
    schema: DataSchema,
    selected: Boolean,
    onClick: () -> Unit,
    onAddRecordClick: () -> Unit,
    onEditSchemaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.TableRows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = schema.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${schema.fields.size} champs configurés",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onAddRecordClick,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter une donnée",
                        tint = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp),
                    )
                }
                IconButton(
                    onClick = onEditSchemaClick,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifier le modèle de donnée",
                        tint = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpNavigationButton(
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(4.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Raccourcis et aide",
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun QuickActionCard(
    onCreateSchemaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Dataset,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Bibliothèque de données",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = "Gérez les modèles de données pour une génération cohérente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onCreateSchemaClick,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ajouter un modèle",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
