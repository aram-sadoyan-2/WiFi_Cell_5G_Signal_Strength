@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.algorithm.wificellgsignalstrength.ui.components.speed

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import com.algorithm.wificellgsignalstrength.R
import com.algorithm.wificellgsignalstrength.ui.model.SpeedCircleState
import com.algorithm.wificellgsignalstrength.ui.theme.CyanStroke
import com.algorithm.wificellgsignalstrength.ui.theme.GoBlue
import com.algorithm.wificellgsignalstrength.ui.theme.GoBlue2
import com.algorithm.wificellgsignalstrength.ui.theme.SpeedRingDownload
import com.algorithm.wificellgsignalstrength.ui.theme.SpeedRingGray
import com.algorithm.wificellgsignalstrength.ui.theme.SpeedRingUpload
import com.algorithm.wificellgsignalstrength.ui.util.format1
import com.algorithm.wificellgsignalstrength.ui.util.speedToProgress

@Composable
internal fun SpeedCircle(
    state: SpeedCircleState,
    compact: Boolean,
    onGoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = when (state) {
        SpeedCircleState.Idle -> 0f
        is SpeedCircleState.Downloading -> speedToProgress(state.downloadMbps)
        is SpeedCircleState.Uploading -> speedToProgress(state.uploadMbps)
        is SpeedCircleState.FinalResult -> 1f
    }

    val ringColor = when (state) {
        is SpeedCircleState.Uploading -> SpeedRingUpload
        else -> SpeedRingDownload
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = min(size.width, size.height) * 0.075f
            val diameter = min(size.width, size.height) - stroke * 1.3f
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = SpeedRingGray,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            if (progress > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }

        when (state) {
            SpeedCircleState.Idle -> {
                Box(
                    modifier = Modifier
                        .size(if (compact) 96.dp else 120.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(GoBlue2, GoBlue)
                            )
                        )
                        .border(4.dp, CyanStroke, CircleShape)
                        .clickable { onGoClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.go),
                        color = Color.White,
                        fontSize = if (compact) 22.sp else 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            is SpeedCircleState.Downloading -> {
                SpeedCenterMetric(
                    label = stringResource(R.string.download_upper),
                    value = state.downloadMbps.format1(),
                    unit = stringResource(R.string.mbps),
                    pingMs = state.pingMs,
                    compact = compact
                )
            }

            is SpeedCircleState.Uploading -> {
                SpeedCenterMetric(
                    label = stringResource(R.string.upload_upper),
                    value = state.uploadMbps.format1(),
                    unit = stringResource(R.string.mbps),
                    pingMs = state.pingMs,
                    compact = compact
                )
            }

            is SpeedCircleState.FinalResult -> Unit
        }
    }
}
