package com.algorithm.wificellgsignalstrength.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NetworkWifi
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.wificellgsignalstrength.CellInfoPopupData
import com.algorithm.wificellgsignalstrength.NetworkInfoPopupData
import com.algorithm.wificellgsignalstrength.R
import com.algorithm.wificellgsignalstrength.WifiInfoPopupData

@Composable
internal fun NetworkInfoPopup(
    data: NetworkInfoPopupData,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3F3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 20.dp,
                        bottom = 20.dp
                    )
                ) {
                    Text(
                        text = data.title,
                        color = HeaderGray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    when (data) {
                        is WifiInfoPopupData -> WifiInfoPopupContent(data)
                        is CellInfoPopupData -> CellInfoPopupContent(data)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 14.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F3F3))
                    .border(2.dp, Color.Black, CircleShape)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun WifiInfoPopupContent(data: WifiInfoPopupData) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.NetworkWifi,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(38.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = data.wifiName,
                color = DarkText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        InfoLine(stringResource(R.string.access_point), data.accessPoint)
        InfoLine(stringResource(R.string.frequency), stringResource(R.string.frequency_mhz_value, data.frequencyMHz))
        InfoLine(stringResource(R.string.channel), data.channel.toString())
        InfoLine(
            stringResource(R.string.link_speed),
            data.linkSpeedMbps?.let { stringResource(R.string.mbps_value, it) } ?: stringResource(R.string.dash)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.router),
            color = DarkText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        InfoLine(
            stringResource(R.string.band_5ghz),
            if (data.is5GHzSupported) stringResource(R.string.supported) else stringResource(R.string.not_supported)
        )
        InfoLine(stringResource(R.string.ip_address), data.ipAddress)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.mac_address),
            color = DarkText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        InfoLine(stringResource(R.string.gateway), data.gateway)
        InfoLine(stringResource(R.string.router_mac), data.routerMac)
        InfoLine(stringResource(R.string.dns1), data.dns1)
        InfoLine(stringResource(R.string.dns2), data.dns2)
        InfoLine(stringResource(R.string.dhcp_server), data.dhcpServer)
    }
}

@Composable
private fun CellInfoPopupContent(data: CellInfoPopupData) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.SignalCellularAlt,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(R.string.carrier_sim_format, data.carrier, data.simLabel),
                color = DarkText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        InfoLine(stringResource(R.string.network_type), data.networkType)
        InfoLine(stringResource(R.string.signal), stringResource(R.string.dbm_unit_value, data.dbm))
        InfoLine(stringResource(R.string.asu), data.asu.toString())
        InfoLine(stringResource(R.string.quality), data.qualityLabel)
        InfoLine(stringResource(R.string.operator), data.operatorName)
        InfoLine(stringResource(R.string.country), data.countryIso)
        InfoLine(
            stringResource(R.string.roaming),
            if (data.roaming) stringResource(R.string.yes) else stringResource(R.string.no)
        )
    }
}
