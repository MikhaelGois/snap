package com.snap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snap.data.manager.DownloadManager
import com.snap.data.models.DownloadProgress
import com.snap.data.models.DownloadMode
import com.snap.data.models.DownloadFormat
import com.snap.data.models.DownloadQuality
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DownloadUiState(
    val downloadProgress: DownloadProgress = DownloadProgress(
        downloadId = "",
        percentage = 0,
        downloadedBytes = 0,
        totalBytes = 0,
        speed = 0,
        timeRemaining = 0,
        status = "Pronto"
    ),
    val downloadStatus: String = "",
    val isDownloading: Boolean = false,
    val downloadId: String? = null,
    val error: String? = null,
    val isCompleted: Boolean = false
)

/**
 * DownloadViewModel - Gerencia o estado de download na UI
 * 
 * Responsabilidades:
 * - Iniciar downloads via DownloadManager
 * - Monitorar progresso em tempo real
 * - Gerenciar estado na UI
 * - Tratar erros
 */
@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val downloadManager: DownloadManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()

    /**
     * Inicia um novo download
     */
    fun startDownload(
        videoUrl: String,
        fileName: String,
        format: DownloadFormat = DownloadFormat.MP4,
        quality: DownloadQuality = DownloadQuality.BEST,
        mode: DownloadMode = DownloadMode.SINGLE,
        selectedChapters: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val downloadId = UUID.randomUUID().toString()
            
            _uiState.value = _uiState.value.copy(
                isDownloading = true,
                downloadId = downloadId,
                error = null,
                isCompleted = false,
                downloadStatus = "Iniciando download..."
            )

            try {
                downloadManager.downloadVideo(
                    downloadId = downloadId,
                    videoUrl = videoUrl,
                    fileName = fileName,
                    format = format.name
                ).collect { progress ->
                    _uiState.value = _uiState.value.copy(
                        downloadProgress = progress,
                        downloadStatus = progress.status,
                        isDownloading = progress.percentage < 100
                    )

                    if (progress.percentage >= 100) {
                        _uiState.value = _uiState.value.copy(
                            isCompleted = true,
                            isDownloading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    error = e.message ?: "Erro desconhecido",
                    isCompleted = false
                )
            }
        }
    }

    /**
     * Monitora progresso de um download existente
     */
    fun monitorProgress() {
        val downloadId = _uiState.value.downloadId ?: return

        viewModelScope.launch {
            try {
                // Monitora progresso
                _uiState.value = _uiState.value.copy(
                    downloadStatus = "Monitorando progresso..."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Erro ao monitorar progresso"
                )
            }
        }

    /**
     * Reseta o estado de download
     */
    fun resetDownload() {
        _uiState.value = DownloadUiState()
    }

    /**
     * Limpa mensagem de erro
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Cancela o download atual
     */
    fun cancelDownload() {
        val downloadId = _uiState.value.downloadId ?: return
        downloadManager.cancelDownload(downloadId)
        _uiState.value = _uiState.value.copy(
            isDownloading = false,
            downloadStatus = "Download cancelado"
        )
    }
}
