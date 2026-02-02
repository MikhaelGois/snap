package com.snap.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snap.ui.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    onBackClick: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    var selectedStatus by remember { mutableStateOf("ALL") }
    var selectedFormat by remember { mutableStateOf("ALL") }
    var minSizeSlider by remember { mutableFloatStateOf(0f) }
    var maxSizeSlider by remember { mutableFloatStateOf(1000f) }
    var startDateMillis by remember { mutableStateOf(0L) }
    var endDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    
    val statusOptions = listOf("ALL", "COMPLETED", "FAILED", "IN_PROGRESS")
    val formatOptions = listOf("ALL", "mp3", "mp4", "mkv", "avi", "webm")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header
        TopAppBar(
            title = { Text("Filtros Avançados", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Status Filter
            FilterSection(title = "Status") {
                FilterChipGroup(
                    options = statusOptions,
                    selectedOption = selectedStatus,
                    onOptionSelected = { selectedStatus = it }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Format Filter
            FilterSection(title = "Formato") {
                FilterChipGroup(
                    options = formatOptions,
                    selectedOption = selectedFormat,
                    onOptionSelected = { selectedFormat = it }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Size Filter
            FilterSection(title = "Tamanho do Arquivo") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Mínimo: ${(minSizeSlider * 10).toInt()} MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = minSizeSlider,
                        onValueChange = { minSizeSlider = it },
                        valueRange = 0f..1000f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "Máximo: ${(maxSizeSlider * 10).toInt()} MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = maxSizeSlider,
                        onValueChange = { maxSizeSlider = it },
                        valueRange = 0f..1000f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.resetFilters()
                        onBackClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Limpar")
                }
                
                Button(
                    onClick = {
                        // Apply filters
                        if (selectedStatus != "ALL") {
                            viewModel.filterByStatus(selectedStatus)
                        }
                        if (selectedFormat != "ALL") {
                            viewModel.filterByFormat(selectedFormat)
                        }
                        if (minSizeSlider > 0) {
                            viewModel.filterByMinSize((minSizeSlider * 10 * 1024 * 1024).toLong())
                        }
                        if (maxSizeSlider < 1000) {
                            viewModel.filterByMaxSize((maxSizeSlider * 10 * 1024 * 1024).toLong())
                        }
                        onBackClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Aplicar Filtros")
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun FilterChipGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selectedOption,
                onClick = { onOptionSelected(option) },
                label = { Text(option) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }
}
