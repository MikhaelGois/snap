package com.snap.data.manager

import android.content.Context
import com.snap.data.api.SnapApiService
import com.snap.data.models.DownloadProgress
import com.snap.util.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DownloadManager - Orquestra os downloads de vídeo
 * 
 * Responsabilidades:
 * - Iniciar downloads
 * - Monitorar progresso em tempo real
 * - Salvar arquivos
 * - Tratar erros
 * - Cancelar downloads
 */
@Singleton
class DownloadManager @Inject constructor(
    private val apiService: SnapApiService,
    private val fileManager: FileManager,
    private val context: Context
) {
    
    private var currentDownloadJob: Job? = null
    private val downloadJobs = mutableMapOf<String, Job>()
    
    /**
     * Inicia um download de vídeo
     */
    fun downloadVideo(
        downloadId: String,
        videoUrl: String,
        fileName: String,
        format: String
    ): Flow<DownloadProgress> = flow {
        try {
            // Gera nome do arquivo
            val generatedFileName = fileManager.generateFileName(fileName)
            val tempFile = fileManager.getTempDownloadFile(generatedFileName)
            val finalFile = fileManager.getFinalDownloadFile(generatedFileName, format)
            
            // Inicia download na API
            val response = apiService.downloadVideo(videoUrl)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    downloadFile(
                        body = body,
                        tempFile = tempFile,
                        finalFile = finalFile,
                        downloadId = downloadId
                    ).collect { progress ->
                        emit(progress)
                    }
                } else {
                    emit(
                        DownloadProgress(
                            downloadId = downloadId,
                            percentage = 0,
                            downloadedBytes = 0,
                            totalBytes = 0,
                            speed = 0,
                            timeRemaining = 0,
                            status = "Erro: Resposta vazia"
                        )
                    )
                }
            } else {
                emit(
                    DownloadProgress(
                        downloadId = downloadId,
                        percentage = 0,
                        downloadedBytes = 0,
                        totalBytes = 0,
                        speed = 0,
                        timeRemaining = 0,
                        status = "Erro: ${response.code()} - ${response.message()}"
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                DownloadProgress(
                    downloadId = downloadId,
                    percentage = 0,
                    downloadedBytes = 0,
                    totalBytes = 0,
                    speed = 0,
                    timeRemaining = 0,
                    status = "Erro: ${e.message}"
                )
            )
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Salva o arquivo de resposta
     */
    private suspend fun downloadFile(
        body: ResponseBody,
        tempFile: File,
        finalFile: File,
        downloadId: String
    ): Flow<DownloadProgress> = flow {
        try {
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L
            var startTime = System.currentTimeMillis()
            
            body.byteStream().use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    val buffer = ByteArray(8192) // 8KB buffer
                    var bytesRead: Int
                    
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        
                        // Calcula progresso
                        val percentage = if (totalBytes > 0) {
                            (downloadedBytes * 100 / totalBytes).toInt()
                        } else {
                            0
                        }
                        
                        // Calcula velocidade
                        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                        val speed = if (elapsedSeconds > 0) {
                            (downloadedBytes / elapsedSeconds).toLong()
                        } else {
                            0L
                        }
                        
                        // Calcula tempo restante
                        val timeRemaining = if (speed > 0) {
                            ((totalBytes - downloadedBytes) / speed)
                        } else {
                            0L
                        }
                        
                        emit(
                            DownloadProgress(
                                downloadId = downloadId,
                                percentage = percentage,
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes,
                                speed = speed,
                                timeRemaining = timeRemaining,
                                status = "Baixando..."
                            )
                        )
                    }
                }
            }
            
            // Move arquivo para destino final
            val moved = fileManager.moveDownloadFile(tempFile, finalFile)
            
            if (moved) {
                emit(
                    DownloadProgress(
                        downloadId = downloadId,
                        percentage = 100,
                        downloadedBytes = totalBytes,
                        totalBytes = totalBytes,
                        speed = 0,
                        timeRemaining = 0,
                        status = "Concluído!"
                    )
                )
            } else {
                emit(
                    DownloadProgress(
                        downloadId = downloadId,
                        percentage = 100,
                        downloadedBytes = totalBytes,
                        totalBytes = totalBytes,
                        speed = 0,
                        timeRemaining = 0,
                        status = "Erro ao salvar arquivo"
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                DownloadProgress(
                    downloadId = downloadId,
                    percentage = 0,
                    downloadedBytes = 0,
                    totalBytes = 0,
                    speed = 0,
                    timeRemaining = 0,
                    status = "Erro: ${e.message}"
                )
            )
            
            // Limpa arquivo temporário
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }
    
    /**
     * Cancela um download
     */
    fun cancelDownload(downloadId: String) {
        downloadJobs[downloadId]?.cancel()
        downloadJobs.remove(downloadId)
    }
    
    /**
     * Cancela todos os downloads
     */
    fun cancelAllDownloads() {
        downloadJobs.forEach { (_, job) ->
            job.cancel()
        }
        downloadJobs.clear()
    }
}

// Placeholder para Job (será substituído por coroutine Job real)
typealias Job = kotlinx.coroutines.Job
