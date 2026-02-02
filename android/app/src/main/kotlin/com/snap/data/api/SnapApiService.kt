package com.snap.data.api

import com.snap.data.models.DownloadInitResponse
import com.snap.data.models.DownloadStatusResponse
import com.snap.data.models.HistoryResponse
import com.snap.data.models.VideoInfoResponse
import kotlinx.serialization.SerialName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * API service interface for Snap backend communication
 */
interface SnapApiService {

    /**
     * Fetch video information from a URL
     */
    @POST("/api/video-info")
    suspend fun getVideoInfo(
        @Body request: VideoInfoRequest
    ): VideoInfoResponse

    /**
     * Initiate a video download
     */
    @POST("/api/download")
    suspend fun startDownload(
        @Body request: DownloadRequest
    ): DownloadInitResponse

    /**
     * Get the current status of a download
     */
    @GET("/api/download-status/{id}")
    suspend fun getDownloadStatus(
        @Path("id") downloadId: String
    ): DownloadStatusResponse

    /**
     * Get download history
     */
    @GET("/api/history")
    suspend fun getHistory(): HistoryResponse

    /**
     * Clear download history
     */
    @POST("/api/history/clear")
    suspend fun clearHistory(): HistoryResponse

    /**
     * Check if server is available
     */
    @GET("/")
    suspend fun healthCheck(): String
}

/**
 * Request model for video info endpoint
 */
@kotlinx.serialization.Serializable
data class VideoInfoRequest(
    @SerialName("url")
    val url: String
)

/**
 * Request model for download endpoint
 */
@kotlinx.serialization.Serializable
data class DownloadRequest(
    @SerialName("url")
    val url: String,
    @SerialName("format")
    val format: String, // "best_video", "best_audio", etc.
    @SerialName("quality")
    val quality: String = "best",
    @SerialName("mode")
    val mode: String = "single", // "single" or "chapters"
    @SerialName("chapters")
    val chapters: List<Int> = emptyList() // selected chapter indices if mode is "chapters"
)
