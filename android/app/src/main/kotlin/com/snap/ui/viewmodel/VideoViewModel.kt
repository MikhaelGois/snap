package com.snap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snap.data.models.Chapter
import com.snap.data.models.DownloadFormat
import com.snap.data.models.DownloadMode
import com.snap.data.models.DownloadQuality
import com.snap.data.models.VideoInfo
import com.snap.data.repository.Result
import com.snap.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VideoInfoUiState(
    val isLoading: Boolean = false,
    val videoInfo: VideoInfo? = null,
    val error: String? = null,
    val selectedChapters: Set<Int> = emptySet(),
    val selectedQuality: DownloadQuality = DownloadQuality.BEST,
    val selectedFormat: DownloadFormat = DownloadFormat.MP4,
    val downloadMode: DownloadMode = DownloadMode.SINGLE_FILE
)

/**
 * ViewModel for video information and download management
 */
class VideoViewModel(
    private val repository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoInfoUiState())
    val uiState: StateFlow<VideoInfoUiState> = _uiState.asStateFlow()

    fun fetchVideoInfo(url: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = repository.getVideoInfo(url)
            _uiState.value = when (result) {
                is Result.Success -> _uiState.value.copy(
                    isLoading = false,
                    videoInfo = result.data,
                    selectedChapters = if (result.data.chapters.isNotEmpty()) {
                        (0 until result.data.chapters.size).toSet()
                    } else {
                        emptySet()
                    }
                )
                is Result.Error -> _uiState.value.copy(
                    isLoading = false,
                    error = result.exception.message ?: "Unknown error"
                )
                is Result.Loading -> _uiState.value.copy(isLoading = true)
            }
        }
    }

    fun toggleChapterSelection(chapterIndex: Int) {
        val currentSelected = _uiState.value.selectedChapters.toMutableSet()
        if (currentSelected.contains(chapterIndex)) {
            currentSelected.remove(chapterIndex)
        } else {
            currentSelected.add(chapterIndex)
        }
        _uiState.value = _uiState.value.copy(selectedChapters = currentSelected)
    }

    fun selectAllChapters() {
        val videoInfo = _uiState.value.videoInfo ?: return
        _uiState.value = _uiState.value.copy(
            selectedChapters = (0 until videoInfo.chapters.size).toSet()
        )
    }

    fun deselectAllChapters() {
        _uiState.value = _uiState.value.copy(selectedChapters = emptySet())
    }

    fun setQuality(quality: DownloadQuality) {
        _uiState.value = _uiState.value.copy(selectedQuality = quality)
    }

    fun setFormat(format: DownloadFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }

    fun setDownloadMode(mode: DownloadMode) {
        _uiState.value = _uiState.value.copy(downloadMode = mode)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        _uiState.value = VideoInfoUiState()
    }
}
