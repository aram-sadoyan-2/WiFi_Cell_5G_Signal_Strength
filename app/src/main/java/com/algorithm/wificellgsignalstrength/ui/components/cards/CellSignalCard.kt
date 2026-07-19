package com.algorithm.wificellgsignalstrength.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.wificellgsignalstrength.R
import com.algorithm.wificellgsignalstrength.ui.components.common.HeaderInfoCapsuleSmall
import com.algorithm.wificellgsignalstrength.ui.components.common.QualityRow
import com.algorithm.wificellgsignalstrength.ui.model.CellSignalData
import com.algorithm.wificellgsignalstrength.ui.theme.BlueAccent
import com.algorithm.wificellgsignalstrength.ui.theme.CardBg
import com.algorithm.wificellgsignalstrength.ui.theme.DarkText
import com.algorithm.wificellgsignalstrength.ui.theme.HeaderGray
import com.algorithm.wificellgsignalstrength.ui.theme.MutedText

@Composable
internal fun CellSignalCard(
    data: CellSignalData,
    modifier: Modifier = Modifier,
    compact: Boolean,
    onInfoClick: () -> Unit
) {
    val showInfo = data.carrier != stringResource(R.string.no_sim)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = HeaderGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CellTower,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = data.carrier,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (showInfo) {
                HeaderInfoCapsuleSmall(onClick = onInfoClick)
            }
        }

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
                    .padding(top = 12.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sim_card),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(if (compact) 24.dp else 28.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = data.title,
                        color = MutedText,
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = stringResource(R.string.sim_label_format, data.simLabel),
                        color = DarkText,
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = data.networkType,
                        color = BlueAccent,
                        fontSize = if (compact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                QualityRow(
                    quality = data.quality,
                    compact = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = stringResource(R.string.asu_label_with_space),
                            color = MutedText,
                            fontSize = if (compact) 11.sp else 12.sp
                        )
                        Text(
                            text = data.asu.toString(),
                            color = DarkText,
                            fontSize = if (compact) 16.sp else 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = stringResource(R.string.dbm_label_with_space),
                            color = MutedText,
                            fontSize = if (compact) 11.sp else 12.sp
                        )
                        Text(
                            text = data.dbm.toString(),
                            color = DarkText,
                            fontSize = if (compact) 16.sp else 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(R.string.ping_label_with_space),
                        color = MutedText,
                        fontSize = if (compact) 11.sp else 12.sp
                    )
                    Text(
                        text = data.pingMs?.let { stringResource(R.string.msec_value, it) }
                            ?: stringResource(R.string.dash),
                        color = DarkText,
                        fontSize = if (compact) 16.sp else 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.connected_to_cell_tower),
                    color = MutedText,
                    fontSize = if (compact) 10.sp else 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Text(
                    text = stringResource(R.string.tower_id, data.towerId),
                    color = HeaderGray,
                    fontSize = if (compact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
