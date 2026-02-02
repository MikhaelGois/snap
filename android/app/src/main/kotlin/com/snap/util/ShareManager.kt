package com.snap.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.snap.data.database.entity.DownloadHistoryEntity
import java.io.File

/**
 * ShareManager - Gerencia o compartilhamento de arquivos e informações
 * 
 * Features:
 * - Compartilhar arquivo de download individual
 * - Compartilhar múltiplos arquivos
 * - Compartilhar informações de estatísticas
 * - Compartilhar histórico de downloads
 */
class ShareManager(private val context: Context) {
    
    companion object {
        private const val AUTHORITY = "com.snap.fileprovider"
    }
    
    /**
     * Compartilha um arquivo de download
     */
    fun shareFile(download: DownloadHistoryEntity) {
        try {
            val file = File(download.filePath)
            if (!file.exists()) {
                return
            }
            
            val uri = FileProvider.getUriForFile(context, AUTHORITY, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, download.fileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(
                Intent.createChooser(intent, "Compartilhar ${download.fileName}")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Compartilha múltiplos arquivos
     */
    fun shareMultipleFiles(downloads: List<DownloadHistoryEntity>) {
        try {
            val uris = ArrayList<Uri>()
            
            downloads.forEach { download ->
                val file = File(download.filePath)
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(context, AUTHORITY, file)
                    uris.add(uri)
                }
            }
            
            if (uris.isEmpty()) return
            
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "application/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(
                Intent.createChooser(intent, "Compartilhar ${uris.size} arquivos")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Compartilha estatísticas como texto
     */
    fun shareStatistics(
        totalDownloads: Int,
        completedDownloads: Int,
        failedDownloads: Int,
        totalSize: Long,
        successRate: Float,
        averageSpeed: Float
    ) {
        try {
            val statistics = buildString {
                appendLine("📊 ESTATÍSTICAS DE DOWNLOADS - Snap")
                appendLine("═══════════════════════════════════════")
                appendLine()
                appendLine("📈 Resumo Geral:")
                appendLine("  • Total de Downloads: $totalDownloads")
                appendLine("  • Downloads Concluídos: $completedDownloads ✅")
                appendLine("  • Downloads Falhados: $failedDownloads ❌")
                appendLine("  • Taxa de Sucesso: ${"%.1f".format(successRate)}%")
                appendLine()
                appendLine("💾 Armazenamento:")
                appendLine("  • Tamanho Total: ${formatFileSize(totalSize)}")
                appendLine("  • Velocidade Média: ${"%.2f".format(averageSpeed)} MB/s")
                appendLine()
                appendLine("─────────────────────────────────────")
                appendLine("Gerado por Snap - Video Downloader")
                appendLine("Data: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("pt", "BR")).format(java.util.Date())}")
            }
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Estatísticas de Downloads - Snap")
                putExtra(Intent.EXTRA_TEXT, statistics)
            }
            
            context.startActivity(
                Intent.createChooser(intent, "Compartilhar Estatísticas")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Compartilha histórico de downloads
     */
    fun shareHistory(downloads: List<DownloadHistoryEntity>) {
        try {
            val history = buildString {
                appendLine("📥 HISTÓRICO DE DOWNLOADS - Snap")
                appendLine("═══════════════════════════════════════")
                appendLine()
                
                downloads.forEachIndexed { index, download ->
                    val status = when (download.status) {
                        "COMPLETED" -> "✅"
                        "FAILED" -> "❌"
                        else -> "⏳"
                    }
                    
                    appendLine("${index + 1}. $status ${download.fileName}")
                    appendLine("   Tamanho: ${formatFileSize(download.fileSize)}")
                    appendLine("   Data: ${formatDate(download.downloadDate)}")
                    appendLine("   Status: ${download.status}")
                    appendLine("   Velocidade: ${"%.2f".format(download.averageSpeed)} MB/s")
                    appendLine()
                }
                
                appendLine("─────────────────────────────────────")
                appendLine("Total de Downloads: ${downloads.size}")
                appendLine("Gerado por Snap - Video Downloader")
            }
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Histórico de Downloads - Snap")
                putExtra(Intent.EXTRA_TEXT, history)
            }
            
            context.startActivity(
                Intent.createChooser(intent, "Compartilhar Histórico")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Formata tamanho do arquivo
     */
    private fun formatFileSize(size: Long): String {
        return when {
            size <= 0 -> "0 B"
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format("%.2f KB", size / (1024f))
            size < 1024 * 1024 * 1024 -> String.format("%.2f MB", size / (1024f * 1024))
            else -> String.format("%.2f GB", size / (1024f * 1024 * 1024))
        }
    }
    
    /**
     * Formata data
     */
    private fun formatDate(timestamp: Long): String {
        return java.text.SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            java.util.Locale("pt", "BR")
        ).format(java.util.Date(timestamp))
    }
}
