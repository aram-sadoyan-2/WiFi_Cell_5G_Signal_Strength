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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.wificell5gsignalstrength.widget.SignalWidgetReceiver
import com.algorithm.wificell5gsignalstrength.widget.SimInfoWidgetReceiver
import com.algorithm.wificell5gsignalstrength.widget.SpeedTestWidgetReceiver
import com.algorithm.wificell5gsignalstrength.widget.WidgetPinHelper

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
            "This launcher does not support adding widgets directly. Add it from Home Screen → Widgets.",
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

                        onSignalRemoveHelp = { openRemoveDialog("Signal Widget") },
                        onSpeedRemoveHelp = { openRemoveDialog("Speed Test Widget") },
                        onSimRemoveHelp = { openRemoveDialog("SIM Info Widget") },

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

@Composable
private fun SettingsScreen(
    onBackClick: () -> Unit,

    signalSupported: Boolean,
    speedSupported: Boolean,
    simSupported: Boolean,

    signalAdded: Boolean,
    speedAdded: Boolean,
    simAdded: Boolean,

    onAddSignalWidget: () -> Unit,
    onAddSpeedWidget: () -> Unit,
    onAddSimWidget: () -> Unit,

    onSignalRemoveHelp: () -> Unit,
    onSpeedRemoveHelp: () -> Unit,
    onSimRemoveHelp: () -> Unit,

    showRemoveDialog: Boolean,
    removeDialogTitle: String,
    onDismissRemoveDialog: () -> Unit
) {
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = onDismissRemoveDialog,
            title = {
                Text(
                    text = removeDialogTitle,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "To remove this widget from your home screen:",
                        color = Color(0xFF111111),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "1. Go to your phone home screen",
                        color = Color(0xFF60656D),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "2. Long press the widget",
                        color = Color(0xFF60656D),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "3. Drag it to Remove, or tap Remove if your launcher shows that option",
                        color = Color(0xFF60656D),
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissRemoveDialog) {
                    Text(
                        text = "OK",
                        color = Color(0xFF2C62F4),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1))
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
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
            description = if (signalAdded) {
                "This widget is already added to your home screen."
            } else if (signalSupported) {
                "Add the signal widget directly from the app."
            } else {
                "Direct add is not supported on this launcher."
            },
            buttonText = if (signalSupported) "Add Signal Widget" else "Show Manual Steps",
            onClick = onAddSignalWidget,
            showManualSteps = !signalSupported,
            isAlreadyAdded = signalAdded,
            onRemoveHelpClick = onSignalRemoveHelp
        )

        Spacer(modifier = Modifier.height(14.dp))

        WidgetCard(
            title = "Speed Test Widget",
            description = if (speedAdded) {
                "This widget is already added to your home screen."
            } else if (speedSupported) {
                "Add the speed test widget directly from the app."
            } else {
                "Direct add is not supported on this launcher."
            },
            buttonText = if (speedSupported) "Add Speed Widget" else "Show Manual Steps",
            onClick = onAddSpeedWidget,
            showManualSteps = !speedSupported,
            isAlreadyAdded = speedAdded,
            onRemoveHelpClick = onSpeedRemoveHelp
        )

        Spacer(modifier = Modifier.height(14.dp))

        WidgetCard(
            title = "SIM Info Widget",
            description = if (simAdded) {
                "This widget is already added to your home screen."
            } else if (simSupported) {
                "Add the SIM info widget directly from the app."
            } else {
                "Direct add is not supported on this launcher."
            },
            buttonText = if (simSupported) "Add SIM Widget" else "Show Manual Steps",
            onClick = onAddSimWidget,
            showManualSteps = !simSupported,
            isAlreadyAdded = simAdded,
            onRemoveHelpClick = onSimRemoveHelp
        )
    }
}

@Composable
private fun WidgetCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    showManualSteps: Boolean,
    isAlreadyAdded: Boolean,
    onRemoveHelpClick: () -> Unit
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

            if (!isAlreadyAdded) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C62F4)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = buttonText,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    text = "Already added",
                    color = Color(0xFF1E8E3E),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onRemoveHelpClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAEAEA)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "How to remove",
                        color = Color(0xFF111111)
                    )
                }
            }

            if (showManualSteps && !isAlreadyAdded) {
                Text(
                    text = "Manual way:",
                    color = Color(0xFF111111),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "1. Go to your phone home screen",
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
                Text(
                    text = "2. Long press on empty area",
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
                Text(
                    text = "3. Tap Widgets",
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
                Text(
                    text = "4. Find WiFi Cell 5G Signal Strength",
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
                Text(
                    text = "5. Drag the widget to the home screen",
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
            }
        }
    }
}