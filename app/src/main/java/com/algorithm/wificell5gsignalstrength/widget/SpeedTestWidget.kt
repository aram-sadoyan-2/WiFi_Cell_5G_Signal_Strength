package com.algorithm.wificell5gsignalstrength.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
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
import androidx.glance.unit.ColorProvider
import com.algorithm.wificell5gsignalstrength.MainActivity
import com.algorithm.wificell5gsignalstrength.R

class SpeedTestWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val prefs = context.getSharedPreferences("speed_widget_prefs", Context.MODE_PRIVATE)

        val speed = prefs.getString("speed_value", "14.8") ?: "14.8"
        val unit = prefs.getString("speed_unit", context.getString(R.string.mbps))
            ?: context.getString(R.string.mbps)
        val ping = prefs.getString("ping_value", context.getString(R.string.ms_value_short, 16))
            ?: context.getString(R.string.ms_value_short, 16)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_RUN_SPEED_TEST, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val runTestAction = actionStartActivity(openIntent)

        provideContent {
            SpeedTestWidgetContent(
                speed = speed,
                unit = unit,
                ping = ping,
                onRunTestClick = runTestAction
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
    val context = LocalContext.current

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(28.dp)
            .background(ColorProvider(R.color.widget_bg))
            .clickable(onRunTestClick)
            .padding(14.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_refresh),
                    contentDescription = context.getString(R.string.speedtest),
                    modifier = GlanceModifier.size(18.dp)
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = context.getString(R.string.speedtest),
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_white),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(16.dp))

            Text(
                text = speed,
                style = TextStyle(
                    color = ColorProvider(R.color.widget_white),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = unit,
                style = TextStyle(
                    color = ColorProvider(R.color.widget_quality_good),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = context.getString(R.string.ping_label),
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_secondary_text),
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = ping,
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_white),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(14.dp))

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .cornerRadius(16.dp)
                    .background(ColorProvider(R.color.widget_card_bg))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = context.getString(R.string.run_test),
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_white),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}