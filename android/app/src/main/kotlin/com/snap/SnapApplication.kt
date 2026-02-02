package com.snap

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * SnapApplication - Classe principal da aplicação
 * 
 * Responsabilidades:
 * - Inicializar Hilt para injeção de dependências
 * - Configurar aplicação global
 * - Setup de contexto da aplicação
 */
@HiltAndroidApp
class SnapApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Hilt initialization is automatic with @HiltAndroidApp
    }
}
