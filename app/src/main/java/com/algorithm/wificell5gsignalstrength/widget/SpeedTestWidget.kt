package com.algorithm.wificell5gsignalstrength.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.algorithm.wificell5gsignalstrength.MainActivity
import com.algorithm.wificell5gsignalstrength.R
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.action.actionStartActivity

class SpeedTestWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val prefs = context.getSharedPreferences("speed_widget_prefs", Context.MODE_PRIVATE)

        val speed = prefs.getString("speed_value", "14.8") ?: "14.8"
        val unit = prefs.getString("speed_unit", "Mbps") ?: "Mbps"
        val ping = prefs.getString("ping_value", "16 ms") ?: "16 ms"

        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_RUN_SPEED_TEST, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        provideContent {
            SpeedTestWidgetContent(
                speed = speed,
                unit = unit,
                ping = ping,
                onRunTestClick = actionStartActivity(openIntent)
            )
        }
    }
}

class SpeedTestWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = SpeedTestWidget()
}

@Composable
private fun SpeedTestWidgetContent(
    speed: String,
    unit: String,
    ping: String,
    onRunTestClick: Action
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
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
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

                Spacer(modifier = GlanceModifier.defaultWeight())

                Image(
                    provider = ImageProvider(R.drawable.ic_widget_refresh),
                    contentDescription = "Run speed test",
                    modifier = GlanceModifier
                        .size(22.dp)
                        .clickable(onRunTestClick)
                )
            }

            Spacer(modifier = GlanceModifier.height(16.dp))

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

            Spacer(modifier = GlanceModifier.height(14.dp))

            Text(
                text = "Tap refresh to test",
                style = TextStyle(
                    color = ColorProvider(
                        day = Color(0xFF60656D),
                        night = Color(0xFFB8BDC5)
                    ),
                    fontSize = 12.sp
                )
            )
        }
    }
}