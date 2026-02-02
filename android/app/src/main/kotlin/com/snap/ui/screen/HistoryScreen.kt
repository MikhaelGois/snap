package com.snap.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snap.data.database.entity.DownloadHistoryEntity
import com.snap.ui.viewmodel.HistoryViewModel
import com.snap.util.ShareManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * HistoryScreen - Exibe histórico de downloads do banco de dados
 * 
 * Features:
 * - Lista de downloads com data/hora
 * - Nome do arquivo e tamanho
 * - Status do download (COMPLETED, FAILED)
 * - Velocidade média de download
 * - Delete individual
 * - Limpar todos os downloads
 * - Search/filter por título
 * - Estatísticas gerais
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onStatisticsClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val downloads by viewModel.downloads.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val totalSize by viewModel.totalSize.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }
    var showBatchShareDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val shareManager = remember { ShareManager(context) }
    val selectedItems by viewModel.selectedItems.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Histórico de Downloads",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                if (totalCount > 0) {
                    Text(
                        "$totalCount downloads (${viewModel.formatFileSize(totalSize)})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (downloads.isNotEmpty()) {
                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear all",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        // Filter and Statistics buttons
        if (downloads.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onFilterClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Search, "Filtros", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filtros")
                }
                
                Button(
                    onClick = onStatisticsClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Estatísticas")
                }
                
                if (selectedItems.isNotEmpty()) {
                    Button(
                        onClick = { showBatchShareDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.Share, "Compartilhar", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartilhar")
                    }
                }
            }
        }
        
        // Search bar
        if (downloads.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.searchDownloads(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Buscar por título...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
        }
        
        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(
                            "Carregando histórico...",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
            downloads.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Empty",
                            modifier = Modifier
                                .size(64.dp)
                                .padding(bottom = 16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        
                        Text(
                            "Nenhum download ainda",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            "Seus downloads aparecerão aqui",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = downloads,
                        key = { it.id }
                    ) { download ->
                        HistoryDownloadItem(
                            download = download,
                            viewModel = viewModel,
                            shareManager = shareManager,
                            onDelete = { viewModel.deleteDownload(download) }
                        )
                    }
                }
            }
        }
        
        // Error message
        if (error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            ) {
                Text(error!!)
            }
        }
    }
    
    // Batch share dialog
    if (showBatchShareDialog) {
        AlertDialog(
            onDismissRequest = { showBatchShareDialog = false },
            title = { Text("Compartilhar ${selectedItems.size} itens?") },
            text = { Text("Escolha como deseja compartilhar os arquivos selecionados.") },
            confirmButton = {
                Button(
                    onClick = {
                        val selected = viewModel.getSelectedDownloads()
                        if (selected.size > 1) {
                            shareManager.shareMultipleFiles(selected)
                        } else if (selected.isNotEmpty()) {
                            shareManager.shareFile(selected.first())
                        }
                        showBatchShareDialog = false
                    }
                ) {
                    Text("Compartilhar Arquivos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchShareDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Limpar histórico?") },
            text = { Text("Todos os downloads serão removidos. Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllDownloads()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Limpar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun HistoryDownloadItem(
    download: DownloadHistoryEntity,
    viewModel: HistoryViewModel,
    shareManager: ShareManager,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected by remember { derivedStateOf { download.id in viewModel.selectedItems.value } }
    
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = if (isSelected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top row: checkbox, filename, and action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { viewModel.toggleSelection(download.id) },
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = download.videoTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                
                IconButton(
                    onClick = { shareManager.shareFile(download) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartilhar",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // File size and format
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Formato: ${download.format}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = viewModel.formatFileSize(download.fileSize),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Download speed and duration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Velocidade: ${viewModel.formatSpeed(download.averageSpeed)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Tempo: ${viewModel.formatDuration(download.downloadDuration / 1000)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Date and status
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDownloadDate(download.downloadEndTime),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                
                StatusBadge(status = download.status)
            }
            
            // Error message if failed
            if (download.status == "FAILED" && download.errorMessage != null) {
                Text(
                    text = "Erro: ${download.errorMessage}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (containerColor, labelColor, label) = when (status) {
        "COMPLETED" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Concluído"
        )
        "FAILED" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Falhou"
        )
        else -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            status
        )
    }
    
    Surface(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = containerColor
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = labelColor,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDownloadDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}
