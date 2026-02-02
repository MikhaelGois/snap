package com.snap.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents video metadata retrieved from the backend
 */
@Serializable
data class VideoInfo(
    @SerialName("url")
    val url: String,
    @SerialName("title")
    val title: String,
    @SerialName("duration")
    val duration: Int, // in seconds
    @SerialName("thumbnail_url")
    val thumbnailUrl: String,
    @SerialName("site")
    val site: String,
    @SerialName("formats")
    val formats: List<Format> = emptyList(),
    @SerialName("chapters")
    val chapters: List<Chapter> = emptyList()
) {
    @Serializable
    data class Format(
        @SerialName("format_id")
        val formatId: String,
        @SerialName("ext")
        val extension: String,
        @SerialName("format")
        val format: String,
        @SerialName("height")
        val height: Int? = null,
        @SerialName("fps")
        val fps: Int? = null
    )
}

/**
 * Represents a chapter in a video
 */
@Serializable
data class Chapter(
    @SerialName("title")
    val title: String,
    @SerialName("start_time")
    val startTime: Double, // in seconds
    @SerialName("end_time")
    val endTime: Double // in seconds
) {
    val durationSeconds: Double
        get() = endTime - startTime

    fun formatStartTime(): String = formatTime(startTime)
    fun formatEndTime(): String = formatTime(endTime)
    fun formatDuration(): String = formatTime(durationSeconds)

    private fun formatTime(seconds: Double): String {
        val totalSeconds = seconds.toInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, secs)
            else -> String.format("%d:%02d", minutes, secs)
        }
    }
}

/**
 * Download quality options
 */
enum class DownloadQuality(val label: String) {
    BEST("Melhor"),
    P2160("4K (2160p)"),
    P1440("1440p"),
    P1080("1080p"),
    P720("720p"),
    P480("480p"),
    P360("360p")
}

/**
 * Download mode options
 */
enum class DownloadMode {
    SINGLE_FILE,
    SEPARATED_CHAPTERS
}

/**
 * Download format options
 */
enum class DownloadFormat(val label: String) {
    MP4("MP4 (Vídeo)"),
    MKV("Matroska (Vídeo)"),
    MP3("MP3 (Áudio)"),
    M4A("M4A (Áudio)"),
    WAV("WAV (Áudio)"),
    OPUS("Opus (Áudio)")
}

/**
 * Represents the progress of a download
 */
@Serializable
data class DownloadProgress(
    @SerialName("id")
    val id: String,
    @SerialName("status")
    val status: String, // "downloading", "completed", "error"
    @SerialName("percentage")
    val percentage: Int,
    @SerialName("current_size")
    val currentSize: String,
    @SerialName("total_size")
    val totalSize: String,
    @SerialName("speed")
    val speed: String,
    @SerialName("message")
    val message: String
)

/**
 * Database entity for download history
 */
@Entity(tableName = "download_history")
data class DownloadHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val url: String,
    val title: String,
    val filename: String,
    val format: String,
    val quality: String,
    val mode: String,
    val downloadedAt: Long = System.currentTimeMillis()
)

/**
 * UI model for download history
 */
data class DownloadHistory(
    val id: Int,
    val url: String,
    val title: String,
    val filename: String,
    val format: String,
    val quality: String,
    val mode: String,
    val downloadedAt: Long
)

/**
 * API response for video info
 */
@Serializable
data class VideoInfoResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("data")
    val data: VideoInfo? = null,
    @SerialName("error")
    val error: String? = null
)

/**
 * API response for download initiation
 */
@Serializable
data class DownloadInitResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("download_id")
    val downloadId: String? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("error")
    val error: String? = null
)

/**
 * API response for download status
 */
@Serializable
data class DownloadStatusResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("status")
    val status: DownloadProgress? = null,
    @SerialName("error")
    val error: String? = null
)

/**
 * API response for download history
 */
@Serializable
data class HistoryResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("history")
    val history: List<DownloadHistory> = emptyList(),
    @SerialName("error")
    val error: String? = null
)
