package com.algorithm.wificell5gsignalstrength.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class SignalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: SignalWidget = SignalWidget()
}