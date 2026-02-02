package com.snap.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snap.ui.viewmodel.DownloadViewModel

/**
 * DownloadScreen - Exibe progresso do download em tempo real
 * 
 * Features:
 * - Barra de progresso visual
 * - Percentual de conclusão
 * - Tamanho do arquivo (atual/total)
 * - Velocidade de download
 * - Tempo estimado restante
 * - Status do download
 * - Botão cancelar (se aplicável)
 * - Botão abrir arquivo (após conclusão)
 */
@Composable
fun DownloadScreen(
    viewModel: DownloadViewModel,
    onDownloadComplete: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        // Monitor download progress
        viewModel.monitorProgress()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Download em progresso",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Main progress card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { uiState.downloadProgress.percentage.toFloat() / 100f },
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 6.dp
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${uiState.downloadProgress.percentage}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            "Download",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                
                // File size information
                DownloadInfoRow(
                    label = "Tamanho:",
                    value = "${formatFileSize(uiState.downloadProgress.downloadedBytes)} / ${formatFileSize(uiState.downloadProgress.totalBytes)}"
                )
                
                // Speed information
                DownloadInfoRow(
                    label = "Velocidade:",
                    value = formatSpeed(uiState.downloadProgress.speed)
                )
                
                // Time remaining
                if (uiState.downloadProgress.percentage < 100) {
                    DownloadInfoRow(
                        label = "Tempo restante:",
                        value = formatTimeRemaining(uiState.downloadProgress.timeRemaining)
                    )
                }
                
                // Status message
                if (uiState.downloadStatus.isNotEmpty()) {
                    Text(
                        text = uiState.downloadStatus,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action buttons
        when {
            uiState.downloadProgress.percentage >= 100 -> {
                // Download complete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onDownloadComplete() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Complete",
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp)
                        )
                        Text("Concluído", fontWeight = FontWeight.Medium)
                    }
                    
                    Button(
                        onClick = onDownloadComplete,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Abrir arquivo", fontWeight = FontWeight.Medium)
                    }
                }
            }
            uiState.downloadProgress.percentage > 0 -> {
                // Download in progress - show cancel button
                Button(
                    onClick = {
                        viewModel.resetDownload()
                        onBackClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Medium)
                }
            }
            else -> {
                // Starting download
                Button(
                    onClick = { onDownloadComplete() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                    Text("Iniciar Download", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun DownloadInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

private fun formatSpeed(bytesPerSecond: Long): String {
    return when {
        bytesPerSecond < 1024 -> "$bytesPerSecond B/s"
        bytesPerSecond < 1024 * 1024 -> "${bytesPerSecond / 1024} KB/s"
        else -> "${bytesPerSecond / (1024 * 1024)} MB/s"
    }
}

private fun formatTimeRemaining(seconds: Long): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}
