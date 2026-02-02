package com.snap.widget

import android.appwidget.AppWidget
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.snap.R

/**
 * QuickDownloadWidget - Widget para downloads rápidos na home screen
 * 
 * Features:
 * - Acesso rápido ao app
 * - Mostrar status dos últimos downloads
 * - Abrir tela de input diretamente
 * - Widget 2x2 ou maior
 */
class QuickDownloadWidget : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Atualizar cada widget
        appWidgetIds.forEach { widgetId ->
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        
        if (intent?.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            
            if (appWidgetIds != null) {
                appWidgetIds.forEach { widgetId ->
                    if (context != null) {
                        updateAppWidget(context, appWidgetManager, widgetId)
                    }
                }
            }
        }
    }
    
    companion object {
        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Criar RemoteViews
            val views = RemoteViews(context.packageName, R.layout.widget_quick_download)
            
            // Atualizar widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
