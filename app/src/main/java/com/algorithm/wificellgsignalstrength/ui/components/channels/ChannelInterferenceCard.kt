package com.algorithm.wificellgsignalstrength.ui.components.channels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.wificellgsignalstrength.R
import com.algorithm.wificellgsignalstrength.ui.model.ChannelSectionData
import com.algorithm.wificellgsignalstrength.ui.theme.BorderGray
import com.algorithm.wificellgsignalstrength.ui.theme.HeaderGray
import com.algorithm.wificellgsignalstrength.ui.theme.MutedText

@Composable
internal fun ChannelInterferenceCard(
    data: ChannelSectionData,
    modifier: Modifier = Modifier,
    compact: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = HeaderGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = stringResource(R.string.wifi_channel_interference),
            color = Color.White,
            fontSize = if (compact) 16.sp else 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SectionCard(title = stringResource(R.string.current_wifi), compact = compact) {
                    data.currentWifi.forEachIndexed { index, row ->
                        ChannelRow(row, compact)
                        if (index != data.currentWifi.lastIndex) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = BorderGray,
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = stringResource(R.string.interference_channel), compact = compact) {
                    if (data.interference.isEmpty()) {
                        EmptySectionText()
                    } else {
                        data.interference.forEachIndexed { index, row ->
                            ChannelRow(row, compact)
                            if (index != data.interference.lastIndex) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = BorderGray,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = stringResource(R.string.other_networks), compact = compact) {
                    if (data.otherNetworks.isEmpty()) {
                        EmptySectionText()
                    } else {
                        data.otherNetworks.forEachIndexed { index, row ->
                            ChannelRow(row, compact)
                            if (index != data.otherNetworks.lastIndex) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = BorderGray,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySectionText() {
    Text(
        text = stringResource(R.string.no_networks_found),
        color = MutedText,
        fontSize = 13.sp
    )
}
