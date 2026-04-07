package com.algorithm.wificell5gsignalstrength.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build

object WidgetPinHelper {

    fun isPinSupported(
        context: Context,
        receiverClass: Class<*>
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false

        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        val provider = ComponentName(context, receiverClass)

        return appWidgetManager?.isRequestPinAppWidgetSupported == true &&
            appWidgetManager.getInstalledProviders().any { it.provider == provider }
    }

    fun requestPin(
        context: Context,
        receiverClass: Class<*>
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false

        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        val provider = ComponentName(context, receiverClass)

        if (appWidgetManager?.isRequestPinAppWidgetSupported != true) return false

        return appWidgetManager.requestPinAppWidget(provider, null, null)
    }
}