package com.algorithm.wificellgsignalstrength.widget

import android.content.Context
import androidx.core.content.edit
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SpeedWidgetUpdater {
    fun update(
        context: Context,
        speed: String,
        unit: String,
        ping: String
    ) {
        val prefs = context.getSharedPreferences("speed_widget_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("speed_value", speed)
                .putString("speed_unit", unit)
                .putString("ping_value", ping)
        }

        CoroutineScope(Dispatchers.Default).launch {
            SpeedTestWidget().updateAll(context)
        }
    }
}
