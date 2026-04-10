package com.algorithm.wificellgsignalstrength.widget

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

        val manager = context.getSystemService(AppWidgetManager::class.java)
        val provider = ComponentName(context, receiverClass)

        return manager?.isRequestPinAppWidgetSupported == true &&
                manager.installedProviders.any { it.provider == provider }
    }

    fun requestPin(
        context: Context,
        receiverClass: Class<*>
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false

        val manager = context.getSystemService(AppWidgetManager::class.java)
        val provider = ComponentName(context, receiverClass)

        if (manager?.isRequestPinAppWidgetSupported != true) return false

        return manager.requestPinAppWidget(provider, null, null)
    }
}