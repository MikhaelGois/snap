package com.snap.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snap.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * StatisticsScreen - Dashboard com estatísticas de downloads
 * 
 * Features:
 * - Total de downloads
 * - Taxa de sucesso/falha
 * - Tamanho total em disco
 * - Velocidade média
 * - Gráficos visuais
 * - Estatísticas por formato
 */
@Composable
fun StatisticsScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val downloads by viewModel.downloads.collectAsState()
    val totalSize by viewModel.totalSize.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    
    // Calcula estatísticas
    val completedCount = downloads.count { it.status == "COMPLETED" }
    val failedCount = downloads.count { it.status == "FAILED" }
    val successRate = if (totalCount > 0) (completedCount * 100) / totalCount else 0
    val avgSpeed = if (completedCount > 0) {
        downloads
            .filter { it.status == "COMPLETED" }
            .map { it.averageSpeed }
            .average()
            .toLong()
    } else 0L
    
    // Agrupa por formato
    val formatStats = downloads
        .filter { it.status == "COMPLETED" }
        .groupingBy { it.format }
        .eachCount()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            "Estatísticas de Downloads",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Principais métricas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatisticCard(
                title = "Total",
                value = totalCount.toString(),
                icon = Icons.Default.CloudDownload,
                modifier = Modifier.weight(1f)
            )
            
            StatisticCard(
                title = "Concluídos",
                value = completedCount.toString(),
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
            
            StatisticCard(
                title = "Falhas",
                value = failedCount.toString(),
                icon = Icons.Default.Error,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Taxa de sucesso
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Taxa de Sucesso",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        "$successRate%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Progress bar
                LinearProgressIndicator(
                    progress = (successRate / 100f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }
        
        // Tamanho total e velocidade
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatisticCard(
                title = "Armazenamento",
                value = viewModel.formatFileSize(totalSize),
                icon = Icons.Default.Storage,
                modifier = Modifier.weight(1f)
            )
            
            StatisticCard(
                title = "Velocidade Média",
                value = viewModel.formatSpeed(avgSpeed),
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Estatísticas por formato
        if (formatStats.isNotEmpty()) {
            Text(
                "Downloads por Formato",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    formatStats.forEach { (format, count) ->
                        FormatStatRow(
                            format = format,
                            count = count,
                            total = completedCount
                        )
                    }
                }
            }
        }
        
        // Resumo
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Resumo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                SummaryRow(
                    label = "Primeiro Download",
                    value = if (downloads.isNotEmpty()) {
                        formatDate(downloads.minByOrNull { it.downloadStartTime }?.downloadStartTime ?: 0)
                    } else {
                        "-"
                    }
                )
                
                SummaryRow(
                    label = "Último Download",
                    value = if (downloads.isNotEmpty()) {
                        formatDate(downloads.maxByOrNull { it.downloadEndTime }?.downloadEndTime ?: 0)
                    } else {
                        "-"
                    }
                )
                
                SummaryRow(
                    label = "Tempo Total de Download",
                    value = if (downloads.isNotEmpty()) {
                        val totalTime = downloads
                            .filter { it.status == "COMPLETED" }
                            .sumOf { it.downloadDuration }
                        viewModel.formatDuration(totalTime / 1000)
                    } else {
                        "-"
                    }
                )
            }
        }
    }
}

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FormatStatRow(
    format: String,
    count: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val percentage = if (total > 0) (count * 100) / total else 0
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                format.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                "$count ($percentage%)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        LinearProgressIndicator(
            progress = (percentage / 100f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.tertiary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(3.dp)
        )
    }
}

@Composable
private fun SummaryRow(
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
            label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "-"
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}
