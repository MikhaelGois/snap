package com.snap.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * NotificationChannelManager - Gerencia canais de notificação do Android
 * 
 * Canais:
 * - DOWNLOAD: Para downloads em andamento
 * - SUCCESS: Para downloads concluídos
 * - ERROR: Para erros de download
 * - INFO: Para informações gerais
 * - PROGRESS: Para progresso detalhado
 */
class NotificationChannelManager(private val context: Context) {
    
    companion object {
        const val CHANNEL_DOWNLOAD = "download_progress"
        const val CHANNEL_SUCCESS = "download_success"
        const val CHANNEL_ERROR = "download_error"
        const val CHANNEL_INFO = "app_info"
        const val CHANNEL_PROGRESS = "download_detailed"
        
        // Notification IDs
        const val NOTIFICATION_DOWNLOAD_ID = 1
        const val NOTIFICATION_SUCCESS_ID = 2
        const val NOTIFICATION_ERROR_ID = 3
        const val NOTIFICATION_PROGRESS_ID = 4
    }
    
    private val notificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    /**
     * Cria canais de notificação (Android 8.0+)
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal de Download em Andamento
            val downloadChannel = NotificationChannel(
                CHANNEL_DOWNLOAD,
                "Downloads em Progresso",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações de downloads em andamento"
                enableLights(true)
                lightColor = 0xFF6200EE.toInt()
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 100, 100)
                setShowBadge(true)
            }
            
            // Canal de Sucesso
            val successChannel = NotificationChannel(
                CHANNEL_SUCCESS,
                "Baixar Completo",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações quando downloads são concluídos"
                enableLights(true)
                lightColor = 0xFF4CAF50.toInt()
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 100, 100)
                setShowBadge(true)
                setSound(
                    Uri.parse("android.resource://${context.packageName}/raw/success"),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
            
            // Canal de Erro
            val errorChannel = NotificationChannel(
                CHANNEL_ERROR,
                "Erros de Download",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações quando downloads falham"
                enableLights(true)
                lightColor = 0xFFFF5252.toInt()
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                setShowBadge(true)
                setSound(
                    Uri.parse("android.resource://${context.packageName}/raw/error"),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
            
            // Canal de Informação
            val infoChannel = NotificationChannel(
                CHANNEL_INFO,
                "Informações do App",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Informações gerais do aplicativo"
                enableVibration(false)
                setShowBadge(false)
            }
            
            // Canal de Progresso Detalhado
            val progressChannel = NotificationChannel(
                CHANNEL_PROGRESS,
                "Progresso Detalhado",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificações com barra de progresso detalhada"
                enableVibration(false)
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannels(
                listOf(
                    downloadChannel,
                    successChannel,
                    errorChannel,
                    infoChannel,
                    progressChannel
                )
            )
        }
    }
    
    /**
     * Deleta um canal de notificação
     */
    fun deleteNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(channelId)
        }
    }
    
    /**
     * Obtém prioridade de notificação para API < 26
     */
    fun getNotificationPriority(channelId: String): Int {
        return when (channelId) {
            CHANNEL_ERROR -> NotificationCompat.PRIORITY_HIGH
            CHANNEL_SUCCESS -> NotificationCompat.PRIORITY_DEFAULT
            CHANNEL_DOWNLOAD -> NotificationCompat.PRIORITY_DEFAULT
            CHANNEL_PROGRESS -> NotificationCompat.PRIORITY_LOW
            CHANNEL_INFO -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }
}
