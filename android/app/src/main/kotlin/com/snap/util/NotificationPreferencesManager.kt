package com.snap.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_settings")

/**
 * NotificationPreferencesManager - Gerencia preferências de notificações do usuário
 * 
 * Features:
 * - Enable/Disable por tipo de notificação
 * - Preferências de som, vibração, luz
 * - Configurações persistidas
 */
class NotificationPreferencesManager(private val context: Context) {
    
    companion object {
        private val DOWNLOAD_PROGRESS_KEY = booleanPreferencesKey("download_progress")
        private val DOWNLOAD_SUCCESS_KEY = booleanPreferencesKey("download_success")
        private val DOWNLOAD_ERROR_KEY = booleanPreferencesKey("download_error")
        private val INFO_NOTIFICATIONS_KEY = booleanPreferencesKey("info_notifications")
        
        private val NOTIFICATION_SOUND_KEY = booleanPreferencesKey("notification_sound")
        private val NOTIFICATION_VIBRATION_KEY = booleanPreferencesKey("notification_vibration")
        private val NOTIFICATION_LIGHT_KEY = booleanPreferencesKey("notification_light")
        
        private val SHOW_BADGES_KEY = booleanPreferencesKey("show_badges")
        private val DETAILED_PROGRESS_KEY = booleanPreferencesKey("detailed_progress")
    }
    
    // Download notifications
    val downloadProgressEnabled: Flow<Boolean> = context.notificationDataStore.data.map { preferences ->
        preferences[DOWNLOAD_PROGRESS_KEY] ?: true
    }
    
    val downloadSuccessEnabled: Flow<Boolean> = context.notificationDataStore.data.map { preferences ->
        preferences[DOWNLOAD_SUCCESS_KEY] ?: true
    }
    
    val downloadErrorEnabled: Flow<Boolean> = context.notificationDataStore.data.map { preferences ->
        preferences[DOWNLOAD_ERROR_KEY] ?: true
    }
    
    val infoNotificationsEnabled: Flow<Boolean> = context.notificationDataStore.data.map { preferences ->
        preferences[INFO_NOTIFICATIONS_KEY] ?: true
    }
    
    // Sound, vibration, light
    val soundEnabled: Flow<Boolean> = context.notificationDataStore.data.map { preferences ->
        preferences[NOTIFICATION_SOUND_KEY] ?: true
    }
    
    val vibrationEnabled: Flow<Boolean> = context.notificationDataStore.data.map { preferences ->
        preferences[NOTIFICATION_VIBRATION_KEY] ?: true
    }
    
    val lightEnabled: Flow<Boolean> = context.notificationDataStore.data.map { preferences ->
        preferences[NOTIFICATION_LIGHT_KEY] ?: true
    }
    
    val showBadges: Flow<Boolean> = context.notificationDataStore.data.map { preferences ->
        preferences[SHOW_BADGES_KEY] ?: true
    }
    
    val detailedProgressEnabled: Flow<Boolean> = context.notificationDataStore.data.map { preferences ->
        preferences[DETAILED_PROGRESS_KEY] ?: true
    }
    
    /**
     * Atualiza preferência de notificação de progresso
     */
    suspend fun setDownloadProgressEnabled(enabled: Boolean) {
        context.notificationDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[DOWNLOAD_PROGRESS_KEY] = enabled
            }
        }
    }
    
    /**
     * Atualiza preferência de notificação de sucesso
     */
    suspend fun setDownloadSuccessEnabled(enabled: Boolean) {
        context.notificationDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[DOWNLOAD_SUCCESS_KEY] = enabled
            }
        }
    }
    
    /**
     * Atualiza preferência de notificação de erro
     */
    suspend fun setDownloadErrorEnabled(enabled: Boolean) {
        context.notificationDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[DOWNLOAD_ERROR_KEY] = enabled
            }
        }
    }
    
    /**
     * Atualiza preferência de som
     */
    suspend fun setSoundEnabled(enabled: Boolean) {
        context.notificationDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[NOTIFICATION_SOUND_KEY] = enabled
            }
        }
    }
    
    /**
     * Atualiza preferência de vibração
     */
    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.notificationDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[NOTIFICATION_VIBRATION_KEY] = enabled
            }
        }
    }
    
    /**
     * Atualiza preferência de luz
     */
    suspend fun setLightEnabled(enabled: Boolean) {
        context.notificationDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[NOTIFICATION_LIGHT_KEY] = enabled
            }
        }
    }
}

private suspend fun <T> DataStore<Preferences>.updateData(transform: suspend (Preferences) -> Preferences) {
    this.updateData { currentPreferences ->
        transform(currentPreferences)
    }
}

private fun Preferences.toMutablePreferences(): MutableMap<Preferences.Key<*>, Any> {
    return this.asMap().toMutableMap()
}
