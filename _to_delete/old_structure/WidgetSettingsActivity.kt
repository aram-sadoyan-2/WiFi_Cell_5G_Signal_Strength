package com.algorithm.wificellgsignalstrength.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.algorithm.wificellgsignalstrength.ui.WidgetSettingsScreen

class WidgetSettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF1F1F1)
                ) {
                    WidgetSettingsScreen(
                        onAddWidgetClick = { requestPinSpeedWidget() },
                        onUpdateWidgetClick = {
                            SpeedWidgetUpdater.update(
                                context = this,
                                speed = "156.7",
                                unit = "Mbps",
                                ping = "4 ms"
                            )
                            Toast.makeText(this, "Widget updated", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    private fun requestPinSpeedWidget() {
        val manager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, SpeedTestWidgetReceiver::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.isRequestPinAppWidgetSupported) {
                manager.requestPinAppWidget(provider, null, null)
            } else {
                Toast.makeText(
                    this,
                    "This launcher does not support pin widget request. Add it manually from home screen widgets.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                "Please add the widget manually from the home screen widgets list.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
