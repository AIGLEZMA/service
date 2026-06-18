package me.aiglez.service.ui.templates

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.aiglez.service.domain.models.DataRecord
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CompileScreen(
    templateId: String,
) {
    val viewModel: CompileViewModel = koinViewModel(
        key = "compile-$templateId",
        parameters = { parametersOf(templateId) },
    )
    val state by viewModel.uiState.collectAsState()
    CompileContent(
        state = state,
        onSelectRecord = viewModel::selectRecord,
        onExportPdf = viewModel::exportPdf,
    )
}

@Composable
private fun CompileContent(
    state: CompileUiState,
    onSelectRecord: (String) -> Unit,
    onExportPdf: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            A4CanvasPreview(state = state, modifier = Modifier.fillMaxHeight(0.92f).aspectRatio(595f / 842f))
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.width(360.dp).fillMaxHeight(),
        ) {
            Text(
                text = state.template?.name ?: "Template Compiler",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            RecordSelector(
                records = state.records,
                selectedRecordId = state.selectedRecordId,
                onSelectRecord = onSelectRecord,
            )
            Text("Token Translations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize().padding(end = 12.dp),
                ) {
                    items(state.translations, key = { it.token }) { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                                .padding(12.dp),
                        ) {
                            Text(
                                item.token,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(item.value.ifBlank { "-" }, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                )
            }
            Button(onClick = onExportPdf, enabled = state.template != null, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Export PDF Document")
            }
        }
    }
}

@Composable
private fun A4CanvasPreview(
    state: CompileUiState,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .background(Color.White)
            .border(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        val scaleX = size.width / 595f
        val scaleY = size.height / 842f
        drawRect(color = Color.White)
        drawRect(color = Color(0xFFCBD5E1), style = Stroke(width = 1.dp.toPx()))
        state.lineBlocks.forEach { line ->
            drawLine(
                color = Color(0xFF111827),
                start = Offset(line.x1 * scaleX, line.y1 * scaleY),
                end = Offset(line.x2 * scaleX, line.y2 * scaleY),
                strokeWidth = line.thickness * ((scaleX + scaleY) / 2f),
            )
        }
        state.textBlocks.forEach { block ->
            drawText(
                textMeasurer = textMeasurer,
                text = block.text,
                topLeft = Offset(block.x * scaleX, block.y * scaleY),
                style = androidx.compose.ui.text.TextStyle(
                    color = Color(0xFF111827),
                    fontSize = 12.sp,
                ),
            )
        }
    }
}

@Composable
private fun RecordSelector(
    records: List<DataRecord>,
    selectedRecordId: String,
    onSelectRecord: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            val selected = records.firstOrNull { it.id == selectedRecordId }
            Text(selected?.values?.values?.firstOrNull().orEmpty().ifBlank { "Select data record" })
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            records.forEach { record ->
                DropdownMenuItem(
                    text = { Text(record.values.values.firstOrNull().orEmpty().ifBlank { record.id }) },
                    onClick = {
                        onSelectRecord(record.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
