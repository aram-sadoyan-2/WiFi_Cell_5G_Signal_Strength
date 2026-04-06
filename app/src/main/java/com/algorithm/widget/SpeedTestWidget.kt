package com.algorithm.wificell5gsignalstrength.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.algorithm.wificell5gsignalstrength.MainActivity

class SpeedTestWidget : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val prefs = context.getSharedPreferences("speed_widget_prefs", Context.MODE_PRIVATE)

        val speed = prefs.getString("speed_value", "156.7") ?: "156.7"
        val unit = prefs.getString("speed_unit", "Mbps") ?: "Mbps"
        val ping = prefs.getString("ping_value", "4 ms") ?: "4 ms"

        provideContent {
            SpeedTestWidgetContent(
                speed = speed,
                unit = unit,
                ping = ping,
                onClick = actionStartActivity(
                    Intent(context, MainActivity::class.java)
                )
            )
        }
    }
}

class SpeedTestWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SpeedTestWidget()
}

@Composable
private fun SpeedTestWidgetContent(
    speed: String,
    unit: String,
    ping: String,
    onClick: Action
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(28.dp)
            .background(
                ColorProvider(
                    day = Color(0xFFEFEFEF),
                    night = Color(0xFF202020)
                )
            )
            .padding(16.dp)
            .fillMaxWidth()
            .clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SPEEDTEST",
                style = TextStyle(
                    color = ColorProvider(
                        day = Color(0xFF60656D),
                        night = Color(0xFFB8BDC5)
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            Text(
                text = speed,
                style = TextStyle(
                    color = ColorProvider(
                        day = Color(0xFF111111),
                        night = Color(0xFFFFFFFF)
                    ),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = unit,
                style = TextStyle(
                    color = ColorProvider(
                        day = Color(0xFF60656D),
                        night = Color(0xFFB8BDC5)
                    ),
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            Text(
                text = "Ping $ping",
                style = TextStyle(
                    color = ColorProvider(
                        day = Color(0xFF111111),
                        night = Color(0xFFFFFFFF)
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}