package com.algorithm.wificell5gsignalstrength

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.wificell5gsignalstrength.widget.SignalWidgetReceiver
import com.algorithm.wificell5gsignalstrength.widget.SpeedTestWidgetReceiver
import com.algorithm.wificell5gsignalstrength.widget.WidgetPinHelper

class SettingsActivity : ComponentActivity() {

    private fun showManualMessage() {
        Toast.makeText(
            this,
            "This launcher does not support adding widgets directly. Add it from Home Screen → Widgets.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val signalSupported = WidgetPinHelper.isPinSupported(
            context = this,
            receiverClass = SignalWidgetReceiver::class.java
        )

        val speedSupported = WidgetPinHelper.isPinSupported(
            context = this,
            receiverClass = SpeedTestWidgetReceiver::class.java
        )

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF1F1F1)
                ) {
                    SettingsScreen(
                        onBackClick = { finish() },
                        signalSupported = signalSupported,
                        speedSupported = speedSupported,
                        onAddSignalWidget = {
                            val ok = WidgetPinHelper.requestPin(
                                context = this,
                                receiverClass = SignalWidgetReceiver::class.java
                            )
                            if (!ok) showManualMessage()
                        },
                        onAddSpeedWidget = {
                            val ok = WidgetPinHelper.requestPin(
                                context = this,
                                receiverClass = SpeedTestWidgetReceiver::class.java
                            )
                            if (!ok) showManualMessage()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    onBackClick: () -> Unit,
    signalSupported: Boolean,
    speedSupported: Boolean,
    onAddSignalWidget: () -> Unit,
    onAddSpeedWidget: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1))
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.clickable { onBackClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Back",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Settings",
            color = Color(0xFF111111),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        WidgetCard(
            title = "Signal Widget",
            description = if (signalSupported) {
                "Add the signal widget directly from the app."
            } else {
                "Direct add is not supported on this launcher."
            },
            buttonText = if (signalSupported) "Add Signal Widget" else "Show Manual Steps",
            onClick = onAddSignalWidget,
            showManualSteps = !signalSupported
        )

        Spacer(modifier = Modifier.height(14.dp))

        WidgetCard(
            title = "Speed Test Widget",
            description = if (speedSupported) {
                "Add the speed test widget directly from the app."
            } else {
                "Direct add is not supported on this launcher."
            },
            buttonText = if (speedSupported) "Add Speed Widget" else "Show Manual Steps",
            onClick = onAddSpeedWidget,
            showManualSteps = !speedSupported
        )
    }
}

@Composable
private fun WidgetCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    showManualSteps: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Widgets,
                    contentDescription = null,
                    tint = Color(0xFF2C62F4),
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = title,
                    color = Color(0xFF111111),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = description,
                color = Color(0xFF60656D),
                fontSize = 14.sp
            )

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C62F4)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = buttonText, color = Color.White)
            }

            if (showManualSteps) {
                Text(
                    text = "Manual way:",
                    color = Color(0xFF111111),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = "1. Go to your phone home screen", color = Color(0xFF60656D), fontSize = 14.sp)
                Text(text = "2. Long press on empty area", color = Color(0xFF60656D), fontSize = 14.sp)
                Text(text = "3. Tap Widgets", color = Color(0xFF60656D), fontSize = 14.sp)
                Text(text = "4. Find WiFi Cell 5G Signal Strength", color = Color(0xFF60656D), fontSize = 14.sp)
                Text(text = "5. Drag the widget to the home screen", color = Color(0xFF60656D), fontSize = 14.sp)
            }
        }
    }
}