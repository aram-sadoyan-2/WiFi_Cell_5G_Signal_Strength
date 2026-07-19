@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.algorithm.wificellgsignalstrength.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algorithm.wificellgsignalstrength.data.model.NetworkInfoPopupData
import com.algorithm.wificellgsignalstrength.ui.components.cards.CellSignalCard
import com.algorithm.wificellgsignalstrength.ui.components.cards.WifiSignalCard
import com.algorithm.wificellgsignalstrength.ui.components.channels.ChannelInterferenceCard
import com.algorithm.wificellgsignalstrength.ui.components.common.TopActionBar
import com.algorithm.wificellgsignalstrength.ui.components.popup.NetworkInfoPopup
import com.algorithm.wificellgsignalstrength.ui.components.speed.SpeedTestPanel
import com.algorithm.wificellgsignalstrength.ui.model.SignalUiState
import com.algorithm.wificellgsignalstrength.ui.theme.AppBg

@Composable
fun WifiCellSignalScreen(
    state: SignalUiState,
    onRefresh: () -> Unit,
    onGoClick: () -> Unit,
    onResetSpeedTest: () -> Unit,
    onSettingsClick: () -> Unit,
    openSettingsFromWidget: Boolean = false
) {
    var popupData by remember { mutableStateOf<NetworkInfoPopupData?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            TopActionBar(
                onRefresh = onRefresh,
                onSettingsClick = onSettingsClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            BoxWithConstraints(
                modifier = Modifier.weight(1f)
            ) {
                val compact = maxWidth < 360.dp

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (compact) 0.44f else 0.38f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        WifiSignalCard(
                            data = state.wifiCard,
                            modifier = Modifier.weight(0.46f),
                            compact = compact,
                            onInfoClick = { popupData = state.wifiCard.infoPopup }
                        )

                        SpeedTestPanel(
                            state = state.speedTest,
                            onGoClick = onGoClick,
                            onCloseClick = onResetSpeedTest,
                            modifier = Modifier.weight(0.54f),
                            compact = compact
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (compact) 0.44f else 0.38f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CellSignalCard(
                            data = state.sim1,
                            modifier = Modifier.weight(1f),
                            compact = compact,
                            onInfoClick = {
                                popupData = state.sim1.infoPopup
                            }
                        )

                        CellSignalCard(
                            data = state.sim2,
                            modifier = Modifier.weight(1f),
                            compact = compact,
                            onInfoClick = {
                                popupData = state.sim2.infoPopup
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ChannelInterferenceCard(
                        data = state.channels,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (compact) 0.40f else 0.39f),
                        compact = compact
                    )
                }
            }
        }

        popupData?.let {
            NetworkInfoPopup(
                data = it,
                onClose = { popupData = null }
            )
        }
    }
}
