package com.algorithm.wificellgsignalstrength.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class SignalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: SignalWidget = SignalWidget()
}