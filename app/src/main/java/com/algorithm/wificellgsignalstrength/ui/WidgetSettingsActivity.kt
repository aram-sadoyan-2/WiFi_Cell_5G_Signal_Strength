package com.algorithm.wificellgsignalstrength.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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

@Composable
private fun WidgetSettingsScreen(
    onAddWidgetClick: () -> Unit,
    onUpdateWidgetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1))
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Widgets",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF111111)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Speed Test Widget",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF111111)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Add the top-right speed panel as a home screen widget and update its values from the app.",
                    color = Color(0xFF60656D)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onAddWidgetClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add widget to home screen")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onUpdateWidgetClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update widget values")
                }
            }
        }
    }
}