package com.snap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snap.data.database.dao.DownloadHistoryDao
import com.snap.data.database.entity.DownloadHistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HistoryViewModel - Gerencia o estado da tela de histórico
 * 
 * Responsabilidades:
 * - Carregar histórico do banco de dados
 * - Filtrar downloads
 * - Deletar downloads
 * - Fornecer estatísticas
 * - Gerenciar seleção em batch
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val downloadHistoryDao: DownloadHistoryDao
) : ViewModel() {
    
    private val _downloads = MutableStateFlow<List<DownloadHistoryEntity>>(emptyList())
    val downloads: StateFlow<List<DownloadHistoryEntity>> = _downloads.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _totalSize = MutableStateFlow(0L)
    val totalSize: StateFlow<Long> = _totalSize.asStateFlow()
    
    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()
    
    private val _selectedItems = MutableStateFlow<Set<Long>>(emptySet())
    val selectedItems: StateFlow<Set<Long>> = _selectedItems.asStateFlow()
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()
    
    init {
        loadHistory()
        loadStatistics()
    }
    
    /**
     * Carrega histórico de downloads do banco
     */
    fun loadHistory() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val history = downloadHistoryDao.getAll()
                _downloads.value = history
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao carregar histórico: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Carrega estatísticas de downloads
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                val totalSize = downloadHistoryDao.getTotalSize()
                val count = downloadHistoryDao.getCount()
                _totalSize.value = totalSize
                _totalCount.value = count
            } catch (e: Exception) {
                // Silently fail for stats
            }
        }
    }
    
    /**
     * Deleta um download do histórico
     */
    fun deleteDownload(download: DownloadHistoryEntity) {
        viewModelScope.launch {
            try {
                downloadHistoryDao.delete(download)
                loadHistory()
                loadStatistics()
            } catch (e: Exception) {
                _error.value = "Erro ao deletar download: ${e.message}"
            }
        }
    }
    
    /**
     * Deleta todos os downloads
     */
    fun deleteAllDownloads() {
        viewModelScope.launch {
            try {
                downloadHistoryDao.deleteAll()
                loadHistory()
                loadStatistics()
            } catch (e: Exception) {
                _error.value = "Erro ao limpar histórico: ${e.message}"
            }
        }
    }
    
    /**
     * Deleta downloads com erro
     */
    fun deleteFailedDownloads() {
        viewModelScope.launch {
            try {
                downloadHistoryDao.deleteAllFailed()
                loadHistory()
                loadStatistics()
            } catch (e: Exception) {
                _error.value = "Erro ao deletar downloads com erro: ${e.message}"
            }
        }
    }
    
    /**
     * Busca downloads por título
     */
    fun searchDownloads(keyword: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val results = if (keyword.isBlank()) {
                    downloadHistoryDao.getAll()
                } else {
                    downloadHistoryDao.searchByTitle(keyword)
                }
                _downloads.value = results
            } catch (e: Exception) {
                _error.value = "Erro ao buscar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Limpa mensagem de erro
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Formata tamanho do arquivo para legibilidade
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
            bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
    
    /**
     * Formata duração para legibilidade (hh:mm:ss)
     */
    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
    
    /**
     * Formata velocidade de download
     */
    fun formatSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond >= 1024 * 1024 -> String.format("%.2f MB/s", bytesPerSecond / (1024.0 * 1024))
            bytesPerSecond >= 1024 -> String.format("%.2f KB/s", bytesPerSecond / 1024.0)
            else -> "$bytesPerSecond B/s"
        }
    }
    
    /**
     * Alterna seleção de um item
     */
    fun toggleSelection(id: Long) {
        val current = _selectedItems.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedItems.value = current
    }
    
    /**
     * Seleciona todos os itens
     */
    fun selectAll() {
        _selectedItems.value = downloads.value.map { it.id }.toSet()
    }
    
    /**
     * Limpa seleção
     */
    fun clearSelection() {
        _selectedItems.value = emptySet()
    }
    
    /**
     * Deleta items selecionados
     */
    fun deleteSelected() {
        viewModelScope.launch {
            try {
                downloads.value
                    .filter { it.id in _selectedItems.value }
                    .forEach { downloadHistoryDao.delete(it) }
                
                clearSelection()
                loadHistory()
                loadStatistics()
            } catch (e: Exception) {
                _error.value = "Erro ao deletar items: ${e.message}"
            }
        }
    }
    
    /**
     * Filtra por status
     */
    fun filterByStatus(status: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val results = if (status == "ALL") {
                    downloadHistoryDao.getAll()
                } else {
                    downloadHistoryDao.getByStatus(status)
                }
                _downloads.value = results
            } catch (e: Exception) {
                _error.value = "Erro ao filtrar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Filtra por intervalo de datas
     */
    fun filterByDateRange(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val results = downloadHistoryDao.getByDateRange(startTime, endTime)
                _downloads.value = results
            } catch (e: Exception) {
                _error.value = "Erro ao filtrar por data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Filtra por tamanho mínimo
     */
    fun filterByMinSize(minSize: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val results = downloadHistoryDao.getAll()
                    .filter { it.fileSize >= minSize }
                _downloads.value = results
            } catch (e: Exception) {
                _error.value = "Erro ao filtrar por tamanho: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Filtra por tamanho máximo
     */
    fun filterByMaxSize(maxSize: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val results = downloadHistoryDao.getAll()
                    .filter { it.fileSize <= maxSize }
                _downloads.value = results
            } catch (e: Exception) {
                _error.value = "Erro ao filtrar por tamanho: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Filtra por formato
     */
    fun filterByFormat(format: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val results = if (format == "ALL") {
                    downloadHistoryDao.getAll()
                } else {
                    downloadHistoryDao.getAll()
                        .filter { it.format.equals(format, ignoreCase = true) }
                }
                _downloads.value = results
            } catch (e: Exception) {
                _error.value = "Erro ao filtrar por formato: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Reseta filtros (carrega todos)
     */
    fun resetFilters() {
        loadHistory()
        clearSelection()
    }
}
