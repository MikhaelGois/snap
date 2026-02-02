package com.snap.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FileManager - Gerencia arquivos de download no dispositivo
 * 
 * Responsabilidades:
 * - Criar diretório de downloads
 * - Gerar nomes de arquivo únicos
 * - Mover arquivos para localização final
 * - Deletar arquivos
 * - Obter informações de arquivo
 */
@Singleton
class FileManager @Inject constructor(
    private val context: Context
) {
    
    private val downloadsDir: File by lazy {
        getDownloadsDirectory()
    }
    
    /**
     * Obtém o diretório de downloads da aplicação
     */
    private fun getDownloadsDirectory(): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Snap"
        )
        
        if (!dir.exists()) {
            dir.mkdirs()
        }
        
        return dir
    }
    
    /**
     * Obtém o arquivo temporário para download
     */
    fun getTempDownloadFile(fileName: String): File {
        val tempDir = File(context.cacheDir, "downloads")
        
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        
        return File(tempDir, "$fileName.tmp")
    }
    
    /**
     * Obtém o caminho final do arquivo após download
     */
    fun getFinalDownloadFile(fileName: String, format: String): File {
        val extension = when (format.lowercase()) {
            "mp4", "mkv", "webm" -> format.lowercase()
            "mp3", "m4a", "aac" -> format.lowercase()
            else -> "mp4"
        }
        
        val finalName = if (!fileName.endsWith(".$extension")) {
            "$fileName.$extension"
        } else {
            fileName
        }
        
        return File(downloadsDir, finalName)
    }
    
    /**
     * Move arquivo do temporário para o final
     */
    fun moveDownloadFile(tempFile: File, finalFile: File): Boolean {
        return try {
            if (tempFile.exists()) {
                tempFile.renameTo(finalFile)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Gera um nome de arquivo único baseado no título
     */
    fun generateFileName(videoTitle: String): String {
        val sanitized = videoTitle
            .replace(Regex("[<>:\"/\\|?*]"), "_")
            .replace(Regex("\\s+"), "_")
            .take(100)
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "${sanitized}_${timestamp}"
    }
    
    /**
     * Deleta um arquivo de download
     */
    fun deleteDownloadFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Obtém o tamanho do arquivo em bytes
     */
    fun getFileSize(filePath: String): Long {
        val file = File(filePath)
        return if (file.exists()) file.length() else 0L
    }
    
    /**
     * Verifica se arquivo existe
     */
    fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }
    
    /**
     * Obtém o diretório de downloads público
     */
    fun getPublicDownloadsDirectory(): File {
        return downloadsDir
    }
    
    /**
     * Lista todos os arquivos baixados
     */
    fun listDownloadedFiles(): List<File> {
        return try {
            if (downloadsDir.exists()) {
                downloadsDir.listFiles()?.toList() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Limpa o diretório temporário
     */
    fun cleanTempDirectory() {
        try {
            val tempDir = File(context.cacheDir, "downloads")
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { file ->
                    if (file.isFile) file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
