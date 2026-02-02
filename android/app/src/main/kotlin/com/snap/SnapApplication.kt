package com.snap

import android.app.Application
import com.snap.data.api.ApiServiceFactory
import com.snap.data.repository.VideoRepository

/**
 * Main application class for Snap
 */
class SnapApplication : Application() {

    companion object {
        lateinit var instance: SnapApplication
            private set
        
        lateinit var videoRepository: VideoRepository
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize API service and repository
        val apiService = ApiServiceFactory.createApiService()
        videoRepository = VideoRepository(apiService)
    }
}
