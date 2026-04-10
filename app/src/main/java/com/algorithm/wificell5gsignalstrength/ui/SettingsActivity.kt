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
import androidx.compose.ui.res.stringResource
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
                        text = stringResource(R.string.remove_widget_title),
                        color = Color(0xFF111111),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.remove_widget_step_1),
                        color = Color(0xFF60656D),
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(R.string.remove_widget_step_2),
                        color = Color(0xFF60656D),
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(R.string.remove_widget_step_3),
                        color = Color(0xFF60656D),
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissRemoveDialog) {
                    Text(
                        text = stringResource(R.string.ok),
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
                        contentDescription = stringResource(R.string.back),
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = stringResource(R.string.back),
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.settings),
            color = Color(0xFF111111),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        WidgetCard(
            title = stringResource(R.string.signal_widget_title),
            description = when {
                signalAdded -> stringResource(R.string.widget_already_added_description)
                signalSupported -> stringResource(R.string.signal_widget_direct_add_description)
                else -> stringResource(R.string.widget_direct_add_not_supported)
            },
            buttonText = if (signalSupported) {
                stringResource(R.string.add_signal_widget)
            } else {
                stringResource(R.string.show_manual_steps)
            },
            onClick = onAddSignalWidget,
            showManualSteps = !signalSupported,
            isAlreadyAdded = signalAdded,
            onRemoveHelpClick = onSignalRemoveHelp
        )

        Spacer(modifier = Modifier.height(14.dp))

        WidgetCard(
            title = stringResource(R.string.speed_test_widget_title),
            description = when {
                speedAdded -> stringResource(R.string.widget_already_added_description)
                speedSupported -> stringResource(R.string.speed_widget_direct_add_description)
                else -> stringResource(R.string.widget_direct_add_not_supported)
            },
            buttonText = if (speedSupported) {
                stringResource(R.string.add_speed_widget)
            } else {
                stringResource(R.string.show_manual_steps)
            },
            onClick = onAddSpeedWidget,
            showManualSteps = !speedSupported,
            isAlreadyAdded = speedAdded,
            onRemoveHelpClick = onSpeedRemoveHelp
        )

        Spacer(modifier = Modifier.height(14.dp))

        WidgetCard(
            title = stringResource(R.string.sim_info_widget_title),
            description = when {
                simAdded -> stringResource(R.string.widget_already_added_description)
                simSupported -> stringResource(R.string.sim_widget_direct_add_description)
                else -> stringResource(R.string.widget_direct_add_not_supported)
            },
            buttonText = if (simSupported) {
                stringResource(R.string.add_sim_widget)
            } else {
                stringResource(R.string.show_manual_steps)
            },
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
                    text = stringResource(R.string.already_added),
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
                        text = stringResource(R.string.how_to_remove),
                        color = Color(0xFF111111)
                    )
                }
            }

            if (showManualSteps && !isAlreadyAdded) {
                Text(
                    text = stringResource(R.string.manual_way),
                    color = Color(0xFF111111),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.add_widget_step_1),
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.add_widget_step_2),
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.add_widget_step_3),
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.add_widget_step_4),
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.add_widget_step_5_drag),
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
            }
        }
    }
}