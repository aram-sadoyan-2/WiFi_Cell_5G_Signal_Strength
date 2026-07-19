package com.algorithm.wificellgsignalstrength

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.algorithm.wificellgsignalstrength.ui.SettingsScreen
import com.algorithm.wificellgsignalstrength.widget.SignalWidgetReceiver
import com.algorithm.wificellgsignalstrength.widget.SimInfoWidgetReceiver
import com.algorithm.wificellgsignalstrength.widget.SpeedTestWidgetReceiver
import com.algorithm.wificellgsignalstrength.widget.WidgetPinHelper

class SettingsActivity : ComponentActivity() {

    private var signalAdded by mutableStateOf(false)
    private var speedAdded by mutableStateOf(false)
    private var simAdded by mutableStateOf(false)

    private var signalSupported by mutableStateOf(false)
    private var speedSupported by mutableStateOf(false)
    private var simSupported by mutableStateOf(false)

    private var showRemoveDialog by mutableStateOf(false)
    private var removeDialogTitle by mutableStateOf("")

    private fun showManualMessage() {
        Toast.makeText(
            this,
            getString(R.string.widget_manual_add_toast),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun openRemoveDialog(widgetName: String) {
        removeDialogTitle = widgetName
        showRemoveDialog = true
    }

    private fun refreshWidgetState() {
        signalAdded = WidgetInstallHelper.isWidgetAdded(
            context = this,
            receiverClass = SignalWidgetReceiver::class.java
        )

        speedAdded = WidgetInstallHelper.isWidgetAdded(
            context = this,
            receiverClass = SpeedTestWidgetReceiver::class.java
        )

        simAdded = WidgetInstallHelper.isWidgetAdded(
            context = this,
            receiverClass = SimInfoWidgetReceiver::class.java
        )

        signalSupported = WidgetPinHelper.isPinSupported(
            context = this,
            receiverClass = SignalWidgetReceiver::class.java
        )

        speedSupported = WidgetPinHelper.isPinSupported(
            context = this,
            receiverClass = SpeedTestWidgetReceiver::class.java
        )

        simSupported = WidgetPinHelper.isPinSupported(
            context = this,
            receiverClass = SimInfoWidgetReceiver::class.java
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        refreshWidgetState()

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
                        simSupported = simSupported,
                        signalAdded = signalAdded,
                        speedAdded = speedAdded,
                        simAdded = simAdded,
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
                        },
                        onAddSimWidget = {
                            val ok = WidgetPinHelper.requestPin(
                                context = this,
                                receiverClass = SimInfoWidgetReceiver::class.java
                            )
                            if (!ok) showManualMessage()
                        },
                        onSignalRemoveHelp = {
                            openRemoveDialog(getString(R.string.signal_widget_title))
                        },
                        onSpeedRemoveHelp = {
                            openRemoveDialog(getString(R.string.speed_test_widget_title))
                        },
                        onSimRemoveHelp = {
                            openRemoveDialog(getString(R.string.sim_info_widget_title))
                        },
                        showRemoveDialog = showRemoveDialog,
                        removeDialogTitle = removeDialogTitle,
                        onDismissRemoveDialog = { showRemoveDialog = false }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshWidgetState()
    }
}
