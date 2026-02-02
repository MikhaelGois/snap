package com.snap.data.api

import com.snap.data.models.DownloadInitResponse
import com.snap.data.models.DownloadStatusResponse
import com.snap.data.models.HistoryResponse
import com.snap.data.models.VideoInfoResponse
import kotlinx.serialization.SerialName
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * SnapApiService - Interface para comunicação com o backend Snap
 * 
 * Define todos os endpoints disponíveis:
 * - Informações de vídeo
 * - Iniciar downloads
 * - Status de download
 * - Histórico
 * - Health check
 */
interface SnapApiService {

    /**
     * Buscar informações do vídeo a partir de uma URL
     */
    @POST("/api/video-info")
    suspend fun getVideoInfo(
        @Body request: VideoInfoRequest
    ): VideoInfoResponse

    /**
     * Iniciar um download de vídeo
     */
    @POST("/api/download")
    suspend fun startDownload(
        @Body request: DownloadRequest
    ): DownloadInitResponse

    /**
     * Baixar o arquivo de vídeo
     * 
     * Retorna ResponseBody para streaming do arquivo
     */
    @GET("/api/download/file")
    suspend fun downloadVideo(
        @Query("url") videoUrl: String
    ): Response<ResponseBody>

    /**
     * Obter status de um download
     */
    @GET("/api/download-status/{id}")
    suspend fun getDownloadStatus(
        @Path("id") downloadId: String
    ): DownloadStatusResponse

    /**
     * Obter histórico de downloads
     */
    @GET("/api/history")
    suspend fun getHistory(): HistoryResponse

    /**
     * Limpar histórico de downloads
     */
    @POST("/api/history/clear")
    suspend fun clearHistory(): HistoryResponse

    /**
     * Verificar disponibilidade do servidor
     */
    @GET("/")
    suspend fun healthCheck(): String
}

/**
 * VideoInfoRequest - Modelo para requisição de informações
 */
@kotlinx.serialization.Serializable
data class VideoInfoRequest(
    @SerialName("url")
    val url: String
)

/**
 * DownloadRequest - Modelo para requisição de download
 */
@kotlinx.serialization.Serializable
data class DownloadRequest(
    @SerialName("url")
    val url: String,
    @SerialName("format")
    val format: String,
    @SerialName("quality")
    val quality: String = "best",
    @SerialName("mode")
    val mode: String = "single", // "single" or "chapters"
    @SerialName("chapters")
    val chapters: List<Int> = emptyList() // selected chapter indices if mode is "chapters"
)
