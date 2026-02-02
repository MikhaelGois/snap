package com.snap.di

import android.content.Context
import com.snap.data.api.ApiServiceFactory
import com.snap.data.api.SnapApiService
import com.snap.data.manager.DownloadManager
import com.snap.data.repository.VideoRepository
import com.snap.util.FileManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AppModule - Define dependências globais da aplicação
 * 
 * Fornece singletons para:
 * - SnapApiService
 * - VideoRepository
 * - DownloadManager
 * - FileManager
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Fornece a instância de SnapApiService
     */
    @Singleton
    @Provides
    fun provideSnapApiService(): SnapApiService {
        return ApiServiceFactory.createApiService()
    }

    /**
     * Fornece a instância de FileManager
     */
    @Singleton
    @Provides
    fun provideFileManager(
        @ApplicationContext context: Context
    ): FileManager {
        return FileManager(context)
    }

    /**
     * Fornece a instância de DownloadManager
     */
    @Singleton
    @Provides
    fun provideDownloadManager(
        apiService: SnapApiService,
        fileManager: FileManager,
        @ApplicationContext context: Context
    ): DownloadManager {
        return DownloadManager(
            apiService = apiService,
            fileManager = fileManager,
            context = context
        )
    }

    /**
     * Fornece a instância de VideoRepository
     */
    @Singleton
    @Provides
    fun provideVideoRepository(
        apiService: SnapApiService
    ): VideoRepository {
        return VideoRepository(apiService)
    }
}
