package com.algorithm.wificellgsignalstrength.ui.components.speed

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.wificellgsignalstrength.R
import com.algorithm.wificellgsignalstrength.ui.model.SpeedCircleState
import com.algorithm.wificellgsignalstrength.ui.theme.DarkText
import com.algorithm.wificellgsignalstrength.ui.theme.MutedText
import com.algorithm.wificellgsignalstrength.ui.theme.SpeedRingUpload
import com.algorithm.wificellgsignalstrength.ui.util.format1

@Composable
internal fun SpeedTestResultPanel(
    state: SpeedCircleState.FinalResult,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.speedtest),
                color = MutedText,
                fontSize = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = stringResource(R.string.download),
                color = Color(0xFF14A8C6),
                fontSize = if (compact) 11.sp else 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = state.downloadMbps.format1(),
                color = DarkText,
                fontSize = if (compact) 34.sp else 40.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.download_mbps_compact),
                color = MutedText,
                fontSize = if (compact) 14.sp else 16.sp
            )

            Text(
                text = stringResource(R.string.upload),
                color = SpeedRingUpload,
                fontSize = if (compact) 11.sp else 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = state.uploadMbps.format1(),
                color = DarkText,
                fontSize = if (compact) 30.sp else 34.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.upload_mbps_compact),
                color = MutedText,
                fontSize = if (compact) 13.sp else 15.sp
            )

            Text(
                text = stringResource(R.string.ping_ms_value, state.pingMs),
                color = DarkText,
                fontSize = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = if (compact) 18.dp else 20.dp, end = 4.dp)
                .size(if (compact) 32.dp else 36.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Black, CircleShape)
                .clickable { onCloseClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.close_symbol),
                color = Color.Black,
                fontSize = if (compact) 20.sp else 22.sp
            )
        }
    }
}
