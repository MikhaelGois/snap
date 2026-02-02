package com.snap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snap.data.models.DownloadHistory
import com.snap.data.repository.Result
import com.snap.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = false,
    val history: List<DownloadHistory> = emptyList(),
    val error: String? = null,
    val isClearing: Boolean = false,
    val clearMessage: String? = null
)

/**
 * ViewModel for managing download history
 */
class HistoryViewModel(
    private val repository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = repository.getHistory()
            _uiState.value = when (result) {
                is Result.Success -> _uiState.value.copy(
                    isLoading = false,
                    history = result.data.sortedByDescending { it.downloadedAt }
                )
                is Result.Error -> _uiState.value.copy(
                    isLoading = false,
                    error = result.exception.message ?: "Unknown error"
                )
                is Result.Loading -> _uiState.value.copy(isLoading = true)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isClearing = true)

            val result = repository.clearHistory()
            _uiState.value = when (result) {
                is Result.Success -> _uiState.value.copy(
                    isClearing = false,
                    history = emptyList(),
                    clearMessage = "Histórico limpo com sucesso"
                )
                is Result.Error -> _uiState.value.copy(
                    isClearing = false,
                    error = result.exception.message ?: "Erro ao limpar histórico"
                )
                is Result.Loading -> _uiState.value.copy(isClearing = true)
            }
        }
    }

    fun dismissClearMessage() {
        _uiState.value = _uiState.value.copy(clearMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
