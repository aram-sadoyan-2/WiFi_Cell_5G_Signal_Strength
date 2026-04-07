package com.algorithm.wificell5gsignalstrength.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
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

data class WidgetSignalUi(
    val title: String = "Wi-Fi",
    val dbm: String = "-67 dBm",
    val quality: String = "Excellent",
    val band: String = "5 GHz",
    val ping: String = "Ping 21 ms",
    val qualityColor: Int = 0xFF6EEB83.toInt()
)

class SignalWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            SignalWidgetContent(
                data = WidgetSignalUi()
            )
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun SignalWidgetContent(data: WidgetSignalUi) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(0xFF2F3136.toInt()))
            .clickable(actionStartActivity<MainActivity>())
            .padding(14.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_wifi),
                    contentDescription = "Wi-Fi",
                    modifier = GlanceModifier.size(18.dp)
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = data.title,
                    style = TextStyle(
                        color = ColorProvider(0xFFFFFFFF.toInt()),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(16.dp))

            Text(
                text = data.dbm,
                style = TextStyle(
                    color = ColorProvider(0xFFFFFFFF.toInt()),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = data.quality,
                style = TextStyle(
                    color = ColorProvider(data.qualityColor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                Text(
                    text = data.band,
                    style = TextStyle(
                        color = ColorProvider(0xFFC7CBD1.toInt()),
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = GlanceModifier.width(12.dp))

                Text(
                    text = data.ping,
                    style = TextStyle(
                        color = ColorProvider(0xFFC7CBD1.toInt()),
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            Image(
                provider = ImageProvider(R.drawable.ic_widget_refresh),
                contentDescription = "Refresh",
                modifier = GlanceModifier.size(16.dp)
            )
        }
    }
}