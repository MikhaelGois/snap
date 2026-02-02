package com.snap.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snap.ui.viewmodel.VideoViewModel

/**
 * InputScreen - Permite o usuário inserir URLs de vídeos
 * 
 * Features:
 * - Modo Single: Inserir um link por vez
 * - Modo Multiple: Inserir múltiplos links (um por linha)
 * - Validação de entrada
 * - Feedback de carregamento
 * - Tratamento de erros
 */
@Composable
fun InputScreen(
    viewModel: VideoViewModel,
    onVideoInfoLoaded: () -> Unit,
    modifier: Modifier = Modifier
) {
    var url by remember { mutableStateOf("") }
    var inputMode by remember { mutableStateOf(InputMode.SINGLE) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 32.dp, bottom = 16.dp)
    ) {
        // Header
        Text(
            text = "Snap",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Baixar vídeos de múltiplas plataformas",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Mode Selection
        Text(
            text = "Modo de entrada",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Single mode button
            Button(
                onClick = { 
                    inputMode = InputMode.SINGLE
                    url = "" // Clear input when switching modes
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (inputMode == InputMode.SINGLE) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text(
                    "1 Link",
                    fontWeight = FontWeight.Medium,
                    color = if (inputMode == InputMode.SINGLE)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Multiple mode button
            Button(
                onClick = { 
                    inputMode = InputMode.MULTIPLE
                    url = "" // Clear input when switching modes
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (inputMode == InputMode.MULTIPLE) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text(
                    "Múltiplos",
                    fontWeight = FontWeight.Medium,
                    color = if (inputMode == InputMode.MULTIPLE)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Input field
        when (inputMode) {
            InputMode.SINGLE -> {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL do vídeo (YouTube, TikTok, Instagram...)") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    enabled = !uiState.isLoading,
                    singleLine = true
                )
            }
            InputMode.MULTIPLE -> {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URLs (uma por linha)") },
                    placeholder = { Text("https://...\nhttps://...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(bottom = 24.dp),
                    enabled = !uiState.isLoading,
                    maxLines = 10
                )
            }
        }
        
        // Fetch button
        Button(
            onClick = {
                if (url.isNotBlank()) {
                    viewModel.fetchVideoInfo(url)
                    onVideoInfoLoaded()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(bottom = 16.dp),
            enabled = url.isNotBlank() && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Buscar Informações",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Error message
        if (uiState.error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Erro: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
        }
        
        // Help text
        Spacer(modifier = Modifier.weight(1f))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Plataformas suportadas:",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "• YouTube\n• TikTok\n• Instagram\n• Twitter/X\n• Facebook",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

enum class InputMode {
    SINGLE,
    MULTIPLE
}
