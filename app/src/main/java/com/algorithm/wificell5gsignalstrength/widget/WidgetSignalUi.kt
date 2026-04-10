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
import androidx.glance.LocalContext
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
    val title: String,
    val dbm: String,
    val quality: String,
    val band: String,
    val ping: String,
    val qualityColorRes: Int = R.color.widget_quality_good
)

class SignalWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val prefs = context.getSharedPreferences("signal_widget_prefs", Context.MODE_PRIVATE)

        val dbmInt = prefs.getInt("wifi_dbm", -67)
        val pingInt = prefs.getInt("ping_ms", 21)
        val band = prefs.getString("wifi_band", context.getString(R.string.band_5_ghz))
            ?: context.getString(R.string.band_5_ghz)
        val quality = prefs.getString("wifi_quality", context.getString(R.string.quality_excellent))
            ?: context.getString(R.string.quality_excellent)

        val data = WidgetSignalUi(
            title = context.getString(R.string.wifi_label),
            dbm = context.getString(R.string.widget_dbm_value, dbmInt),
            quality = quality,
            band = band,
            ping = context.getString(R.string.ping_ms_value, pingInt),
            qualityColorRes = R.color.widget_quality_good
        )

        provideContent {
            SignalWidgetContent(data = data)
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun SignalWidgetContent(data: WidgetSignalUi) {
    val context = LocalContext.current

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.widget_bg))
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
                    contentDescription = context.getString(R.string.wifi_label),
                    modifier = GlanceModifier.size(18.dp)
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = data.title,
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_white),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(16.dp))

            Text(
                text = data.dbm,
                style = TextStyle(
                    color = ColorProvider(R.color.widget_white),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = data.quality,
                style = TextStyle(
                    color = ColorProvider(data.qualityColorRes),
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
                        color = ColorProvider(R.color.widget_secondary_text),
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = GlanceModifier.width(12.dp))

                Text(
                    text = data.ping,
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_secondary_text),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}