package com.snap.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.snap.ui.screen.DownloadScreen
import com.snap.ui.screen.HistoryScreen
import com.snap.ui.screen.InputScreen
import com.snap.ui.screen.VideoInfoScreen
import com.snap.ui.screen.StatisticsScreen
import com.snap.ui.screen.FilterScreen
import com.snap.ui.viewmodel.DownloadViewModel
import com.snap.ui.viewmodel.HistoryViewModel
import com.snap.ui.viewmodel.VideoViewModel

/**
 * Navigation - Define as rotas e fluxo de navegação da aplicação
 * 
 * Rotas:
 * - Input: Tela de entrada de URL
 * - VideoInfo: Tela de informações do vídeo
 * - Download: Tela de progresso do download
 * - History: Tela de histórico
 */

@Composable
fun SnapNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = SnapScreen.Input.route
    ) {
        composable(SnapScreen.Input.route) {
            val videoViewModel: VideoViewModel = hiltViewModel()
            
            InputScreen(
                viewModel = videoViewModel,
                onVideoInfoLoaded = {
                    navController.navigate(SnapScreen.VideoInfo.route)
                }
            )
        }
        
        composable(SnapScreen.VideoInfo.route) {
            val videoViewModel: VideoViewModel = hiltViewModel()
            
            VideoInfoScreen(
                viewModel = videoViewModel,
                onDownloadStarted = {
                    navController.navigate(SnapScreen.Download.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(SnapScreen.Download.route) {
            val downloadViewModel: DownloadViewModel = hiltViewModel()
            
            DownloadScreen(
                viewModel = downloadViewModel,
                onDownloadComplete = {
                    navController.navigate(SnapScreen.History.route) {
                        popUpTo(SnapScreen.Input.route) { inclusive = false }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(SnapScreen.History.route) {
            val historyViewModel: HistoryViewModel = hiltViewModel()
            
            HistoryScreen(
                viewModel = historyViewModel,
                onStatisticsClick = {
                    navController.navigate(SnapScreen.Statistics.route)
                },
                onFilterClick = {
                    navController.navigate(SnapScreen.Filter.route)
                }
            )
        }
        
        composable(SnapScreen.Statistics.route) {
            StatisticsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(SnapScreen.Filter.route) {
            FilterScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class SnapScreen(val route: String) {
    data object Input : SnapScreen("input")
    data object VideoInfo : SnapScreen("video_info")
    data object Download : SnapScreen("download")
    data object History : SnapScreen("history")
    data object Statistics : SnapScreen("statistics")
    data object Filter : SnapScreen("filter")
}
