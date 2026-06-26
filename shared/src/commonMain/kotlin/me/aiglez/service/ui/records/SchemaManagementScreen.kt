package me.aiglez.service.ui.records

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.FieldType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SchemaManagementScreen(
    onEditSchema: (String) -> Unit,
    onOpenRecords: (String) -> Unit,
    onCreateSchema: () -> Unit,
    viewModel: SchemaManagementViewModel = koinViewModel(),
) {
    val schemas by viewModel.schemas.collectAsState()
    var pendingArchive by remember { mutableStateOf<DataSchema?>(null) }

    SchemaManagementContent(
        schemas = schemas,
        onEditSchema = onEditSchema,
        onOpenRecords = onOpenRecords,
        onCreateSchema = onCreateSchema,
        onArchiveRequested = { pendingArchive = it },
    )

    val schema = pendingArchive
    if (schema != null) {
        AlertDialog(
            onDismissRequest = { pendingArchive = null },
            title = { Text("Archiver le modèle ?") },
            text = { Text("L'archivage de \"${schema.name}\" masque ce modèle ainsi que son élément de navigation actif. Les données existantes restent stockées de manière sécurisée.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.archiveSchema(schema.id)
                        pendingArchive = null
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Archiver")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingArchive = null }) {
                    Text("Annuler")
                }
            },
        )
    }
}

@Composable
private fun SchemaManagementContent(
    schemas: List<DataSchema>,
    onEditSchema: (String) -> Unit,
    onOpenRecords: (String) -> Unit,
    onCreateSchema: () -> Unit,
    onArchiveRequested: (DataSchema) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredSchemas = remember(schemas, searchQuery) {
        if (searchQuery.isBlank()) {
            schemas
        } else {
            schemas.filter { schema ->
                schema.name.contains(
                    searchQuery,
                    ignoreCase = true
                ) || schema.fields.any { it.name.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    val totalFields = remember(schemas) {
        schemas.sumOf { it.fields.size }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val listState = rememberLazyListState()
        Box(
            modifier = Modifier.widthIn(max = 920.dp).fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ManagementHeader(
                    schemaCount = schemas.size,
                    totalFields = totalFields,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Rechercher un modèle ou un champ...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                    )

                    Button(
                        onClick = onCreateSchema,
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Nouveau modèle")
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (filteredSchemas.isEmpty()) {
                        EmptyState(
                            isSearchActive = searchQuery.isNotEmpty(),
                            onCreateClick = onCreateSchema,
                            onClearSearch = { searchQuery = "" })
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 2.dp),
                        ) {
                            items(filteredSchemas, key = { it.id }) { schema ->
                                SchemaCard(
                                    schema = schema,
                                    onEditSchema = onEditSchema,
                                    onOpenRecords = onOpenRecords,
                                    onArchiveRequested = onArchiveRequested,
                                )
                            }
                        }
                    }
                }
            }

            if (filteredSchemas.isNotEmpty()) {
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun ManagementHeader(
    schemaCount: Int,
    totalFields: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "REGISTRE DES MODÈLES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Modèles de données",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Consultez, modifiez et gérez les structures de données réutilisables de votre application.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.width(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("$schemaCount modèles") },
                    shape = RoundedCornerShape(4.dp),
                )
                AssistChip(
                    onClick = {},
                    label = { Text("$totalFields champs configurés") },
                    shape = RoundedCornerShape(4.dp),
                )
            }
        }
    }
}

@Composable
private fun SchemaCard(
    schema: DataSchema,
    onEditSchema: (String) -> Unit,
    onOpenRecords: (String) -> Unit,
    onArchiveRequested: (DataSchema) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    Surface(
        modifier = Modifier.fillMaxWidth().hoverable(interactionSource),
        shape = RoundedCornerShape(8.dp),
        color = if (hovered) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (hovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        ),
        tonalElevation = if (hovered) 2.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = schema.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    AssistChip(
                        onClick = {},
                        label = { Text("${schema.fields.size} champs") },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(24.dp)
                    )
                }

                if (schema.fields.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Champs :",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )

                        val maxPreviewFields = 4
                        val previewFields = schema.fields.take(maxPreviewFields)
                        previewFields.forEachIndexed { index, field ->
                            Text(
                                text = "${field.name} (${getTypeLabelAbbrev(field.type)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            if (index < previewFields.size - 1 || schema.fields.size > maxPreviewFields) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                )
                            }
                        }
                        if (schema.fields.size > maxPreviewFields) {
                            Text(
                                text = "+${schema.fields.size - maxPreviewFields} autres",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Aucun champ configuré",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { onOpenRecords(schema.id) },
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Default.TableRows, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Données", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { onEditSchema(schema.id) },
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Modifier", style = MaterialTheme.typography.labelMedium)
                }

                IconButton(
                    onClick = { onArchiveRequested(schema) },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                ) {
                    Icon(Icons.Default.Archive, contentDescription = "Archiver", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    isSearchActive: Boolean,
    onCreateClick: () -> Unit,
    onClearSearch: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (isSearchActive) "Aucun modèle trouvé" else "Aucun modèle de données",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (isSearchActive) {
                    "Aucun modèle ou champ ne correspond à votre recherche actuelle."
                } else {
                    "Commencez par créer un modèle pour structurer vos données de templates."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (isSearchActive) {
                OutlinedButton(
                    onClick = onClearSearch,
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text("Réinitialiser la recherche")
                }
            } else {
                Button(
                    onClick = onCreateClick,
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Créer un modèle")
                }
            }
        }
    }
}

private fun getTypeLabelAbbrev(type: FieldType): String {
    return when (type) {
        FieldType.TEXT -> "Texte"
        FieldType.NUMBER -> "Entier"
        FieldType.DOUBLE -> "Décimal"
        FieldType.REFERENCE -> "Réf"
        FieldType.LIST -> "Liste"
    }
}
