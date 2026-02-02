package com.snap.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.snap.data.database.entity.DownloadHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DownloadHistoryDao - Data Access Object para gerenciar downloads no banco de dados
 * 
 * Responsabilidades:
 * - Inserir novos downloads
 * - Atualizar status de downloads
 * - Consultar histórico
 * - Deletar histórico
 */
@Dao
interface DownloadHistoryDao {
    
    /**
     * Insere um novo download no histórico
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadHistoryEntity): Long
    
    /**
     * Insere múltiplos downloads
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(downloads: List<DownloadHistoryEntity>)
    
    /**
     * Atualiza um download existente
     */
    @Update
    suspend fun update(download: DownloadHistoryEntity)
    
    /**
     * Deleta um download do histórico
     */
    @Delete
    suspend fun delete(download: DownloadHistoryEntity)
    
    /**
     * Deleta um download pelo ID
     */
    @Query("DELETE FROM download_history WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    /**
     * Deleta um download pelo downloadId (ID único da operação)
     */
    @Query("DELETE FROM download_history WHERE downloadId = :downloadId")
    suspend fun deleteByDownloadId(downloadId: String)
    
    /**
     * Deleta todos os downloads
     */
    @Query("DELETE FROM download_history")
    suspend fun deleteAll()
    
    /**
     * Deleta downloads com status FAILED
     */
    @Query("DELETE FROM download_history WHERE status = 'FAILED'")
    suspend fun deleteAllFailed()
    
    /**
     * Deleta downloads cujos arquivos não existem mais
     */
    @Query("DELETE FROM download_history WHERE fileExists = 0")
    suspend fun deleteNonExistentFiles()
    
    /**
     * Obtém um download pelo ID
     */
    @Query("SELECT * FROM download_history WHERE id = :id")
    suspend fun getById(id: Long): DownloadHistoryEntity?
    
    /**
     * Obtém um download pelo downloadId
     */
    @Query("SELECT * FROM download_history WHERE downloadId = :downloadId")
    suspend fun getByDownloadId(downloadId: String): DownloadHistoryEntity?
    
    /**
     * Obtém todos os downloads (Flow para atualizações em tempo real)
     */
    @Query("SELECT * FROM download_history ORDER BY downloadEndTime DESC")
    fun getAllAsFlow(): Flow<List<DownloadHistoryEntity>>
    
    /**
     * Obtém todos os downloads
     */
    @Query("SELECT * FROM download_history ORDER BY downloadEndTime DESC")
    suspend fun getAll(): List<DownloadHistoryEntity>
    
    /**
     * Obtém últimos N downloads
     */
    @Query("SELECT * FROM download_history ORDER BY downloadEndTime DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<DownloadHistoryEntity>
    
    /**
     * Obtém downloads com status específico
     */
    @Query("SELECT * FROM download_history WHERE status = :status ORDER BY downloadEndTime DESC")
    suspend fun getByStatus(status: String): List<DownloadHistoryEntity>
    
    /**
     * Obtém downloads completos
     */
    @Query("SELECT * FROM download_history WHERE status = 'COMPLETED' ORDER BY downloadEndTime DESC")
    fun getCompletedAsFlow(): Flow<List<DownloadHistoryEntity>>
    
    /**
     * Obtém downloads com erro
     */
    @Query("SELECT * FROM download_history WHERE status = 'FAILED' ORDER BY downloadEndTime DESC")
    fun getFailedAsFlow(): Flow<List<DownloadHistoryEntity>>
    
    /**
     * Obtém downloads com a palavra-chave no título
     */
    @Query("SELECT * FROM download_history WHERE videoTitle LIKE '%' || :keyword || '%' ORDER BY downloadEndTime DESC")
    suspend fun searchByTitle(keyword: String): List<DownloadHistoryEntity>
    
    /**
     * Obtém downloads de uma data específica
     */
    @Query("SELECT * FROM download_history WHERE DATE(datetime(downloadStartTime / 1000, 'unixepoch')) = :date ORDER BY downloadEndTime DESC")
    suspend fun getByDate(date: String): List<DownloadHistoryEntity>
    
    /**
     * Obtém downloads em um intervalo de datas
     */
    @Query("SELECT * FROM download_history WHERE downloadStartTime >= :startTime AND downloadStartTime <= :endTime ORDER BY downloadEndTime DESC")
    suspend fun getByDateRange(startTime: Long, endTime: Long): List<DownloadHistoryEntity>
    
    /**
     * Obtém estatísticas gerais
     */
    @Query("""
        SELECT 
            COUNT(*) as totalDownloads,
            SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completedDownloads,
            SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failedDownloads,
            SUM(fileSize) as totalSize,
            AVG(averageSpeed) as avgSpeed
        FROM download_history
    """)
    suspend fun getStatistics(): DownloadStatistics?
    
    /**
     * Conta total de downloads
     */
    @Query("SELECT COUNT(*) FROM download_history")
    suspend fun getCount(): Int
    
    /**
     * Obtém tamanho total em disco
     */
    @Query("SELECT COALESCE(SUM(fileSize), 0) FROM download_history WHERE fileExists = 1")
    suspend fun getTotalSize(): Long
}

/**
 * Data class para estatísticas de download
 */
data class DownloadStatistics(
    val totalDownloads: Int,
    val completedDownloads: Int,
    val failedDownloads: Int,
    val totalSize: Long,
    val avgSpeed: Long
)
