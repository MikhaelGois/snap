package com.snap.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snap.ui.theme.ThemeManager
import kotlinx.coroutines.launch

/**
 * SettingsScreen - Tela de preferências e customização
 * 
 * Features:
 * - Seleção de tema (Light/Dark/Auto)
 * - Material You (Dynamic Colors)
 * - Nível de contraste
 * - Informações do app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeManager: ThemeManager,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val themeMode by themeManager.themeMode.collectAsState(initial = ThemeManager.THEME_MODE_AUTO)
    val useDynamicColor by themeManager.useDynamicColor.collectAsState(initial = true)
    val contrastLevel by themeManager.contrastLevel.collectAsState(initial = ThemeManager.CONTRAST_NORMAL)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Configurações", fontWeight = FontWeight.Bold) },
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
            // Theme Settings
            SettingsSectionHeader("Aparência")
            
            // Theme Mode
            SettingItem(
                title = "Tema",
                description = when (themeMode) {
                    ThemeManager.THEME_MODE_LIGHT -> "Claro"
                    ThemeManager.THEME_MODE_DARK -> "Escuro"
                    else -> "Automático"
                }
            ) {
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.width(200.dp)
                ) {
                    OutlinedTextField(
                        value = when (themeMode) {
                            ThemeManager.THEME_MODE_LIGHT -> "Claro"
                            ThemeManager.THEME_MODE_DARK -> "Escuro"
                            else -> "Automático"
                        },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf(
                            "Automático" to ThemeManager.THEME_MODE_AUTO,
                            "Claro" to ThemeManager.THEME_MODE_LIGHT,
                            "Escuro" to ThemeManager.THEME_MODE_DARK
                        ).forEach { (label, value) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    scope.launch {
                                        themeManager.setThemeMode(value)
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dynamic Color
            SettingItem(
                title = "Material You",
                description = if (useDynamicColor) "Ativado" else "Desativado",
                trailing = {
                    Switch(
                        checked = useDynamicColor,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                themeManager.setDynamicColor(enabled)
                            }
                        }
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Contrast Level
            SettingItem(
                title = "Contraste",
                description = when (contrastLevel) {
                    ThemeManager.CONTRAST_HIGH -> "Alto"
                    ThemeManager.CONTRAST_MEDIUM -> "Médio"
                    else -> "Normal"
                }
            ) {
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.width(200.dp)
                ) {
                    OutlinedTextField(
                        value = when (contrastLevel) {
                            ThemeManager.CONTRAST_HIGH -> "Alto"
                            ThemeManager.CONTRAST_MEDIUM -> "Médio"
                            else -> "Normal"
                        },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf(
                            "Normal" to ThemeManager.CONTRAST_NORMAL,
                            "Médio" to ThemeManager.CONTRAST_MEDIUM,
                            "Alto" to ThemeManager.CONTRAST_HIGH
                        ).forEach { (label, value) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    scope.launch {
                                        themeManager.setContrastLevel(value)
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Information
            SettingsSectionHeader("Sobre")
            
            SettingItem(
                title = "Versão",
                description = "1.0.0"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SettingItem(
                title = "Desenvolvedor",
                description = "Snap Team"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SettingItem(
                title = "Construído com",
                description = "Kotlin + Jetpack Compose"
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    trailing: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                trailing?.invoke()
            }
            
            if (content != null) {
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}
