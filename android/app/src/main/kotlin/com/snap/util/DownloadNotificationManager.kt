package com.snap.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.snap.MainActivity
import com.snap.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NotificationManager - Gerencia notificações de download
 * 
 * Responsabilidades:
 * - Criar canais de notificação
 * - Mostrar notificações de progresso
 * - Mostrar notificações de conclusão
 * - Mostrar notificações de erro
 * - Gerenciar ações de notificação
 */
@Singleton
class DownloadNotificationManager @Inject constructor(
    private val context: Context
) {
    
    companion object {
        const val CHANNEL_ID = "download_channel"
        const val CHANNEL_NAME = "Download Notifications"
        const val CHANNEL_DESCRIPTION = "Notificações de progresso de download"
        const val DOWNLOAD_NOTIFICATION_ID = 1001
        const val DOWNLOAD_COMPLETE_ID = 1002
        const val DOWNLOAD_ERROR_ID = 1003
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Cria o canal de notificação para Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(false)
                enableLights(false)
            }
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Mostra notificação de progresso de download
     */
    fun showProgressNotification(
        downloadId: String,
        fileName: String,
        percentage: Int,
        downloadedBytes: Long,
        totalBytes: Long,
        speed: Long
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("Baixando: $fileName")
            .setContentText(
                "${formatFileSize(downloadedBytes)} / ${formatFileSize(totalBytes)} " +
                "• ${formatSpeed(speed)}"
            )
            .setProgress(100, percentage, false)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(
                createPendingIntent()
            )
            .addAction(
                0,
                "Cancelar",
                createCancelPendingIntent(downloadId)
            )
            .build()
        
        notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, notification)
    }
    
    /**
     * Mostra notificação de conclusão de download
     */
    fun showCompletionNotification(
        fileName: String,
        filePath: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download_done)
            .setContentTitle("Download concluído")
            .setContentText(fileName)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(
                createOpenFilePendingIntent(filePath)
            )
            .addAction(
                0,
                "Abrir",
                createOpenFilePendingIntent(filePath)
            )
            .build()
        
        notificationManager.notify(DOWNLOAD_COMPLETE_ID, notification)
        notificationManager.cancel(DOWNLOAD_NOTIFICATION_ID) // Remove progress notification
    }
    
    /**
     * Mostra notificação de erro de download
     */
    fun showErrorNotification(
        fileName: String,
        errorMessage: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download_error)
            .setContentTitle("Erro no download")
            .setContentText("$fileName: $errorMessage")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(
                createPendingIntent()
            )
            .build()
        
        notificationManager.notify(DOWNLOAD_ERROR_ID, notification)
        notificationManager.cancel(DOWNLOAD_NOTIFICATION_ID) // Remove progress notification
    }
    
    /**
     * Cancela notificação de progresso
     */
    fun cancelProgressNotification() {
        notificationManager.cancel(DOWNLOAD_NOTIFICATION_ID)
    }
    
    /**
     * Cancela todas as notificações
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    /**
     * Cria PendingIntent para abrir a app
     */
    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Cria PendingIntent para cancelar download
     */
    private fun createCancelPendingIntent(downloadId: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_CANCEL_DOWNLOAD"
            putExtra("downloadId", downloadId)
        }
        
        return PendingIntent.getActivity(
            context,
            downloadId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Cria PendingIntent para abrir arquivo
     */
    private fun createOpenFilePendingIntent(filePath: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_OPEN_FILE"
            putExtra("filePath", filePath)
        }
        
        return PendingIntent.getActivity(
            context,
            filePath.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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
}
