package com.snap.data.repository

import com.snap.data.api.DownloadRequest
import com.snap.data.api.SnapApiService
import com.snap.data.api.VideoInfoRequest
import com.snap.data.models.DownloadHistory
import com.snap.data.models.DownloadProgress
import com.snap.data.models.VideoInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * Repository for video-related operations
 */
class VideoRepository(
    private val apiService: SnapApiService
) {

    /**
     * Fetch video information from a URL
     */
    suspend fun getVideoInfo(url: String): Result<VideoInfo> {
        return try {
            val response = apiService.getVideoInfo(VideoInfoRequest(url))
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(
                    Exception(response.error ?: "Unknown error fetching video info")
                )
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Start downloading a video
     */
    suspend fun startDownload(
        url: String,
        format: String,
        quality: String = "best",
        mode: String = "single",
        selectedChapters: List<Int> = emptyList()
    ): Result<String> {
        return try {
            val request = DownloadRequest(
                url = url,
                format = format,
                quality = quality,
                mode = mode,
                chapters = selectedChapters
            )
            val response = apiService.startDownload(request)
            if (response.success && response.downloadId != null) {
                Result.Success(response.downloadId)
            } else {
                Result.Error(
                    Exception(response.error ?: "Unknown error starting download")
                )
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Monitor download progress with a flow
     */
    fun monitorDownloadProgress(downloadId: String): Flow<Result<DownloadProgress>> = flow {
        var isCompleted = false
        var errorCount = 0
        val maxErrors = 5

        while (!isCompleted && errorCount < maxErrors) {
            try {
                emit(Result.Loading)
                val response = apiService.getDownloadStatus(downloadId)

                if (response.success && response.status != null) {
                    emit(Result.Success(response.status))

                    when (response.status.status) {
                        "completed" -> isCompleted = true
                        "error" -> {
                            emit(Result.Error(Exception(response.status.message)))
                            isCompleted = true
                        }
                        else -> delay(1000) // Poll every second
                    }
                } else {
                    val error = Exception(response.error ?: "Unknown error getting download status")
                    emit(Result.Error(error))
                    errorCount++
                    delay(1000)
                }
            } catch (e: Exception) {
                emit(Result.Error(e))
                errorCount++
                delay(1000)
            }
        }

        if (errorCount >= maxErrors) {
            emit(Result.Error(Exception("Max retries exceeded")))
        }
    }

    /**
     * Get download history
     */
    suspend fun getHistory(): Result<List<DownloadHistory>> {
        return try {
            val response = apiService.getHistory()
            if (response.success) {
                Result.Success(response.history)
            } else {
                Result.Error(
                    Exception(response.error ?: "Unknown error fetching history")
                )
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Clear download history
     */
    suspend fun clearHistory(): Result<Unit> {
        return try {
            val response = apiService.clearHistory()
            if (response.success) {
                Result.Success(Unit)
            } else {
                Result.Error(
                    Exception(response.error ?: "Unknown error clearing history")
                )
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Check if backend is available
     */
    suspend fun checkBackendAvailability(baseUrl: String = "http://localhost:5000"): Result<Boolean> {
        return try {
            apiService.healthCheck()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
