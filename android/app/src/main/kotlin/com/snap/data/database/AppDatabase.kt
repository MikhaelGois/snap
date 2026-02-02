package com.snap.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.snap.data.database.dao.DownloadHistoryDao
import com.snap.data.database.entity.DownloadHistoryEntity

/**
 * AppDatabase - Configuração principal do banco de dados Room
 * 
 * Responsabilidades:
 * - Definir entidades do banco
 * - Gerenciar versão do schema
 * - Fornecer acesso aos DAOs
 */
@Database(
    entities = [
        DownloadHistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Fornece acesso ao DAO de histórico de downloads
     */
    abstract fun downloadHistoryDao(): DownloadHistoryDao
    
    companion object {
        
        private const val DATABASE_NAME = "snap_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Obtém instância singleton do banco de dados
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // Remove dados antigos em caso de mudanças de schema
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
