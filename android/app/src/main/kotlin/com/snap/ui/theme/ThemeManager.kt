package com.snap.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

/**
 * ThemeManager - Gerencia preferências de tema da aplicação
 * 
 * Features:
 * - Suporte a Light/Dark/Auto theme
 * - Material You (Android 12+)
 * - Customização de cores
 * - Persistência de preferências
 */
class ThemeManager(private val context: Context) {
    
    companion object {
        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
        private val USE_DYNAMIC_COLOR_KEY = booleanPreferencesKey("use_dynamic_color")
        private val ACCENT_COLOR_KEY = intPreferencesKey("accent_color")
        private val CONTRAST_LEVEL_KEY = intPreferencesKey("contrast_level")
        
        const val THEME_MODE_AUTO = 0
        const val THEME_MODE_LIGHT = 1
        const val THEME_MODE_DARK = 2
        
        const val CONTRAST_NORMAL = 0
        const val CONTRAST_HIGH = 1
        const val CONTRAST_MEDIUM = 2
    }
    
    // Theme mode flow
    val themeMode: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: THEME_MODE_AUTO
    }
    
    // Dynamic color flow
    val useDynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_DYNAMIC_COLOR_KEY] ?: true
    }
    
    // Accent color flow
    val accentColor: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[ACCENT_COLOR_KEY] ?: 0xFF6200EE.toInt()
    }
    
    // Contrast level flow
    val contrastLevel: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[CONTRAST_LEVEL_KEY] ?: CONTRAST_NORMAL
    }
    
    /**
     * Define o modo de tema
     */
    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }
    
    /**
     * Define se deve usar Material You
     */
    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLOR_KEY] = enabled
        }
    }
    
    /**
     * Define cor de destaque
     */
    suspend fun setAccentColor(color: Int) {
        context.dataStore.edit { preferences ->
            preferences[ACCENT_COLOR_KEY] = color
        }
    }
    
    /**
     * Define nível de contraste
     */
    suspend fun setContrastLevel(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[CONTRAST_LEVEL_KEY] = level
        }
    }
}

private suspend fun DataStore<Preferences>.edit(transform: suspend (MutableMap<Preferences.Key<*>, Any>) -> Unit) {
    this.updateData { currentPreferences ->
        val mutableMap = currentPreferences.asMap().toMutableMap()
        @Suppress("UNCHECKED_CAST")
        transform(mutableMap as MutableMap<Preferences.Key<*>, Any>)
        
        val mutablePreferences = androidx.datastore.preferences.core.MutablePreferences(mutableMap, currentPreferences)
        mutablePreferences
    }
}
