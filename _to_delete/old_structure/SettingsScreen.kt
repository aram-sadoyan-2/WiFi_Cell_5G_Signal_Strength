package com.algorithm.wificellgsignalstrength.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.wificellgsignalstrength.R
import com.algorithm.wificellgsignalstrength.ui.components.RemoveWidgetDialog
import com.algorithm.wificellgsignalstrength.ui.components.WidgetCard

@Composable
internal fun SettingsScreen(
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
        RemoveWidgetDialog(
            title = removeDialogTitle,
            onDismiss = onDismissRemoveDialog
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
