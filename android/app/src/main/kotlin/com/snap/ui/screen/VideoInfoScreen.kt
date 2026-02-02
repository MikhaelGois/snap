package com.snap.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snap.data.models.Chapter
import com.snap.data.models.DownloadFormat
import com.snap.data.models.DownloadMode
import com.snap.data.models.DownloadQuality
import com.snap.data.models.VideoInfo
import com.snap.ui.viewmodel.VideoViewModel

/**
 * VideoInfoScreen - Exibe informações do vídeo e permite configurar o download
 * 
 * Features:
 * - Exibição de metadados do vídeo
 * - Lista de capítulos com seleção
 * - Seleção de qualidade e formato
 * - Modo de download (Single/Separated)
 * - Botão de download
 */
@Composable
fun VideoInfoScreen(
    viewModel: VideoViewModel,
    onDownloadStarted: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.videoInfo == null) {
        // Empty state
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    "Nenhum vídeo carregado",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Button(
                    onClick = onBackClick,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Voltar")
                }
            }
        }
        return
    }
    
    val videoInfo = uiState.videoInfo!!
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back button and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text("← Voltar")
            }
            
            Text(
                "Informações do Vídeo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Video metadata
            item {
                VideoMetadataCard(videoInfo)
            }
            
            // Quality selection
            item {
                QualitySelectionCard(
                    selectedQuality = uiState.quality,
                    onQualitySelected = { viewModel.setQuality(it) }
                )
            }
            
            // Format selection
            item {
                FormatSelectionCard(
                    selectedFormat = uiState.format,
                    onFormatSelected = { viewModel.setFormat(it) }
                )
            }
            
            // Download mode selection
            item {
                DownloadModeCard(
                    selectedMode = uiState.mode,
                    onModeSelected = { viewModel.setDownloadMode(it) }
                )
            }
            
            // Chapters section (if available)
            if (videoInfo.chapters.isNotEmpty()) {
                item {
                    ChaptersHeaderCard(
                        totalChapters = videoInfo.chapters.size,
                        selectedCount = uiState.selectedChapters.size,
                        onSelectAll = { viewModel.selectAllChapters() },
                        onDeselectAll = { viewModel.deselectAllChapters() },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(videoInfo.chapters) { chapter ->
                    ChapterItem(
                        chapter = chapter,
                        isSelected = chapter.id in uiState.selectedChapters,
                        onToggle = { viewModel.toggleChapterSelection(chapter.id) }
                    )
                }
            }
        }
        
        // Download button
        Button(
            onClick = onDownloadStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(top = 16.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Começar Download",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun VideoMetadataCard(
    videoInfo: VideoInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = videoInfo.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Site: ${videoInfo.website}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    "Duração: ${formatDuration(videoInfo.duration)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QualitySelectionCard(
    selectedQuality: DownloadQuality,
    onQualitySelected: (DownloadQuality) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Qualidade",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DownloadQuality.entries.forEach { quality ->
                    QualityOption(
                        quality = quality,
                        isSelected = selectedQuality == quality,
                        onSelect = { onQualitySelected(quality) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QualityOption(
    quality: DownloadQuality,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            modifier = Modifier.padding(end = 12.dp)
        )
        
        Text(
            text = quality.name,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FormatSelectionCard(
    selectedFormat: DownloadFormat,
    onFormatSelected: (DownloadFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Formato",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DownloadFormat.entries.forEach { format ->
                    FormatOption(
                        format = format,
                        isSelected = selectedFormat == format,
                        onSelect = { onFormatSelected(format) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatOption(
    format: DownloadFormat,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            modifier = Modifier.padding(end = 12.dp)
        )
        
        Text(
            text = format.name,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DownloadModeCard(
    selectedMode: DownloadMode,
    onModeSelected: (DownloadMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Modo de Download",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DownloadMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMode == mode,
                            onClick = { onModeSelected(mode) },
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        
                        Text(
                            text = mode.name,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChaptersHeaderCard(
    totalChapters: Int,
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Capítulos ($selectedCount/$totalChapters)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSelectAll,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Select all",
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 4.dp)
                    )
                    Text("Selecionar tudo", fontSize = 12.sp)
                }
                
                Button(
                    onClick = onDeselectAll,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Deselect all",
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 4.dp)
                    )
                    Text("Desselecionar", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: Chapter,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = chapter.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${formatTime(chapter.startTime)} - ${formatTime(chapter.endTime)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    
    return when {
        hours > 0 -> "%02d:%02d:%02d".format(hours, minutes, secs)
        else -> "%02d:%02d".format(minutes, secs)
    }
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(minutes, secs)
}
