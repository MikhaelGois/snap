package com.snap.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.snap.MainActivity
import com.snap.R

/**
 * AdvancedNotificationBuilder - Construtor avançado de notificações
 * 
 * Features:
 * - Notificações com barra de progresso
 * - Ações customizadas
 * - Estilos BigText e BigPicture
 * - Prioridades configuráveis
 * - Grupos de notificações
 */
class AdvancedNotificationBuilder(private val context: Context) {
    
    /**
     * Cria notificação de download em progresso com barra
     */
    fun buildDownloadProgressNotification(
        title: String,
        subtitle: String,
        progress: Int,
        max: Int = 100,
        channelId: String = NotificationChannelManager.CHANNEL_DOWNLOAD
    ): NotificationCompat.Builder {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setProgress(max, progress, false)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setShowWhen(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
    }
    
    /**
     * Cria notificação de sucesso com ações
     */
    fun buildSuccessNotification(
        title: String,
        message: String,
        channelId: String = NotificationChannelManager.CHANNEL_SUCCESS,
        onShareClick: (() -> Unit)? = null,
        onOpenClick: (() -> Unit)? = null
    ): NotificationCompat.Builder {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setBigContentTitle(title)
            )
        
        return notification
    }
    
    /**
     * Cria notificação de erro com ações de retry
     */
    fun buildErrorNotification(
        title: String,
        errorMessage: String,
        channelId: String = NotificationChannelManager.CHANNEL_ERROR
    ): NotificationCompat.Builder {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(errorMessage)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(errorMessage)
                    .setBigContentTitle(title)
            )
            .setColor(0xFFFF5252.toInt())
    }
    
    /**
     * Cria notificação de resumo com múltiplos downloads
     */
    fun buildSummaryNotification(
        title: String,
        summary: String,
        count: Int,
        channelId: String = NotificationChannelManager.CHANNEL_INFO
    ): NotificationCompat.Builder {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(summary)
            .setNumber(count)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setGroupSummary(true)
            .setGroup("downloads")
    }
    
    /**
     * Cria notificação com barra de progresso detalhada
     */
    fun buildDetailedProgressNotification(
        title: String,
        fileName: String,
        downloadedMB: Float,
        totalMB: Float,
        speed: Float,
        timeRemaining: String,
        channelId: String = NotificationChannelManager.CHANNEL_PROGRESS
    ): NotificationCompat.Builder {
        val progress = ((downloadedMB / totalMB) * 100).toInt().coerceIn(0, 100)
        
        val detailText = buildString {
            append("${"%.1f".format(downloadedMB)}MB / ${"%.1f".format(totalMB)}MB ")
            append("| ${"%.2f".format(speed)}MB/s ")
            append("| ETA: $timeRemaining")
        }
        
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(fileName)
            .setSubText(detailText)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setShowWhen(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
    }
    
    /**
     * Cria notificação de pausado
     */
    fun buildPausedNotification(
        title: String,
        fileName: String,
        channelId: String = NotificationChannelManager.CHANNEL_DOWNLOAD
    ): NotificationCompat.Builder {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("Pausado: $fileName")
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
    }
}
