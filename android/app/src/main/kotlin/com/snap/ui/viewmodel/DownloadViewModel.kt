package com.snap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snap.data.models.DownloadProgress
import com.snap.data.repository.Result
import com.snap.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DownloadUiState(
    val isDownloading: Boolean = false,
    val downloadId: String? = null,
    val progress: DownloadProgress? = null,
    val error: String? = null,
    val isCompleted: Boolean = false
)

/**
 * ViewModel for managing download progress
 */
class DownloadViewModel(
    private val repository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()

    fun startDownload(
        url: String,
        format: String,
        quality: String = "best",
        mode: String = "single",
        selectedChapters: List<Int> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloading = true,
                error = null,
                isCompleted = false,
                progress = null
            )

            val result = repository.startDownload(
                url = url,
                format = format,
                quality = quality,
                mode = mode,
                selectedChapters = selectedChapters
            )

            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(downloadId = result.data)
                    monitorProgress(result.data)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isDownloading = false,
                        error = result.exception.message ?: "Unknown error",
                        isCompleted = false
                    )
                }
                is Result.Loading -> {
                    // Already set above
                }
            }
        }
    }

    private fun monitorProgress(downloadId: String) {
        viewModelScope.launch {
            repository.monitorDownloadProgress(downloadId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            progress = result.data,
                            isDownloading = result.data.status != "completed"
                        )

                        if (result.data.status == "completed") {
                            _uiState.value = _uiState.value.copy(isCompleted = true)
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isDownloading = false,
                            error = result.exception.message ?: "Unknown error",
                            isCompleted = false
                        )
                    }
                    is Result.Loading -> {
                        // Progress update pending
                    }
                }
            }
        }
    }

    fun resetDownload() {
        _uiState.value = DownloadUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
