package com.algorithm.wificell5gsignalstrength

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetInstallHelper {

    fun isWidgetAdded(
        context: Context,
        receiverClass: Class<*>
    ): Boolean {
        val manager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(context, receiverClass)
        val ids = manager.getAppWidgetIds(provider)
        return ids.isNotEmpty()
    }
}