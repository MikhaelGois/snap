package com.snap.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * DownloadHistoryEntity - Entidade Room para armazenar histórico de downloads
 * 
 * Representa um download concluído com todas as informações relevantes
 */
@Entity(tableName = "download_history")
data class DownloadHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * ID único do download gerado durante a operação
     */
    val downloadId: String,
    
    /**
     * Nome original do vídeo
     */
    val videoTitle: String,
    
    /**
     * URL do vídeo original
     */
    val videoUrl: String,
    
    /**
     * Nome do arquivo salvo
     */
    val fileName: String,
    
    /**
     * Caminho completo do arquivo
     */
    val filePath: String,
    
    /**
     * Formato do arquivo (mp4, webm, etc)
     */
    val format: String,
    
    /**
     * Tamanho do arquivo em bytes
     */
    val fileSize: Long,
    
    /**
     * Duração do download em milissegundos
     */
    val downloadDuration: Long,
    
    /**
     * Velocidade média de download em bytes/segundo
     */
    val averageSpeed: Long,
    
    /**
     * Data/hora de início do download
     */
    val downloadStartTime: Long,
    
    /**
     * Data/hora de conclusão do download
     */
    val downloadEndTime: Long,
    
    /**
     * Status do download (COMPLETED, FAILED, CANCELLED)
     */
    val status: String = "COMPLETED",
    
    /**
     * Mensagem de erro (se houver)
     */
    val errorMessage: String? = null,
    
    /**
     * Indica se o arquivo ainda existe no sistema de arquivos
     */
    val fileExists: Boolean = true,
    
    /**
     * Notas adicionais do usuário
     */
    val notes: String? = null,
    
    /**
     * Timestamp de criação do registro
     */
    val createdAt: Long = System.currentTimeMillis(),
    
    /**
     * Timestamp de última atualização
     */
    val updatedAt: Long = System.currentTimeMillis()
) {
    enum class Status {
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
