@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.algorithm.wificell5gsignalstrength.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NetworkWifi
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

private val AppBg = Color(0xFFF1F1F1)
private val HeaderGray = Color(0xFF8A8A8A)
private val CardBg = Color(0xFFF7F7F7)
private val BorderGray = Color(0xFFD7D7D7)
private val DarkText = Color(0xFF111111)
private val MutedText = Color(0xFF60656D)
private val BlueAccent = Color(0xFF2C62F4)
private val GoodGreen = Color(0xFF87F14D)
private val GoodYellow = Color(0xFFF0D93A)
private val GoodOrange = Color(0xFFF3A14B)
private val BadRed = Color(0xFFFFA3A3)
private val SpeedRingGray = Color(0xFF5F6672)
private val SpeedRingDownload = Color(0xFF43D8C7)
private val SpeedRingUpload = Color(0xFFC93EF3)
private val GoBlue = Color(0xFF145EC8)
private val GoBlue2 = Color(0xFF2B75D9)
private val CyanStroke = Color(0xFF1CE5D2)

@Composable
fun WifiCellSignalScreen(
    state: SignalUiState,
    onRefresh: () -> Unit,
    onGoClick: () -> Unit,
    onResetSpeedTest: () -> Unit,
    onSettingsClick: () -> Unit,
    openSettingsFromWidget: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
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
                        .weight(if (compact) 0.37f else 0.40f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    WifiSignalCard(
                        data = state.wifiCard,
                        modifier = Modifier.weight(0.46f),
                        compact = compact
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
                        .weight(if (compact) 0.25f else 0.24f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CellSignalCard(
                        data = state.sim1,
                        modifier = Modifier.weight(1f),
                        compact = compact
                    )
                    CellSignalCard(
                        data = state.sim2,
                        modifier = Modifier.weight(1f),
                        compact = compact
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ChannelInterferenceCard(
                    data = state.channels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(if (compact) 0.38f else 0.36f),
                    compact = compact
                )
            }
        }
    }
}

@Composable
private fun TopActionBar(
    onRefresh: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleGradientIconButton {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Refresh",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = HeaderGray,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun CircleGradientIconButton(
    modifier: Modifier = Modifier,
    icon: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFF7F87), Color(0xFFD82CC9))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
private fun WifiSignalCard(
    data: WifiCardData,
    modifier: Modifier = Modifier,
    compact: Boolean
) {
    NetworkCardFrame(
        modifier = modifier.fillMaxSize(),
        headerTitle = data.carrier,
        headerLeading = {
            Icon(
                imageVector = Icons.Outlined.CellTower,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(if (compact) 14.dp else 15.dp)
            )
        },
        headerCenter = {
            Box(
                modifier = Modifier
                    .size(if (compact) 16.dp else 18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF44E61F))
                    .border(1.dp, Color(0xFF4CAF50), CircleShape)
            )
        },
        headerTrailing = { HeaderInfoCapsuleSmall() }
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.NetworkWifi,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(if (compact) 20.dp else 22.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = data.title,
                color = MutedText,
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = data.band,
                color = BlueAccent,
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        QualityRow(quality = data.quality, compact = true)

        Spacer(modifier = Modifier.height(8.dp))

        CenterStatLineTiny("dBm", data.dbm.toString())
        Spacer(modifier = Modifier.height(2.dp))
        CenterStatLineTiny(
            label = if (data.linkSpeedMbps != null) "link" else "ping",
            value = data.linkSpeedMbps?.let { "$it Mbps" } ?: "—"
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Connected to",
            color = MutedText,
            fontSize = if (compact) 9.sp else 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )

        Text(
            text = data.connectedTo,
            color = HeaderGray,
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CellSignalCard(
    data: CellSignalData,
    modifier: Modifier = Modifier,
    compact: Boolean
) {
    NetworkCardFrame(
        modifier = modifier.fillMaxSize(),
        headerTitle = data.carrier,
        headerLeading = {
            Icon(
                imageVector = Icons.Outlined.CellTower,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(if (compact) 13.dp else 14.dp)
            )
        },
        headerTrailing = { HeaderInfoCapsuleSmall() }
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SignalCellularAlt,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(if (compact) 17.dp else 18.dp)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = data.title,
                color = MutedText,
                fontSize = if (compact) 9.sp else 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "(${data.simLabel})",
                color = DarkText,
                fontSize = if (compact) 9.sp else 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = data.networkType,
                color = BlueAccent,
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        QualityRow(quality = data.quality, compact = true)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBlockTiny("ASU", data.asu.toString())
            StatBlockTiny("dBm", data.dbm.toString())
        }

        Spacer(modifier = Modifier.height(6.dp))

        CenterStatLineTiny("ping", data.pingMs?.let { "$it mSec" } ?: "—")

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = "Connected to cell tower",
            color = MutedText,
            fontSize = if (compact) 8.sp else 9.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )

        Text(
            text = "ID: ${data.towerId}",
            color = HeaderGray,
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SpeedTestPanel(
    state: SpeedCircleState,
    onGoClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean
) {
    if (state is SpeedCircleState.DownloadResult || state is SpeedCircleState.UploadResult) {
        SpeedTestResultPanel(
            state = state,
            onCloseClick = onCloseClick,
            modifier = modifier,
            compact = compact
        )
        return
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SPEEDTEST",
            color = MutedText,
            fontSize = if (compact) 15.sp else 17.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        SpeedCircle(
            progress = when (state) {
                SpeedCircleState.Idle -> 0f
                is SpeedCircleState.Downloading -> 0.45f
                is SpeedCircleState.Uploading -> 0.78f
                is SpeedCircleState.DownloadResult -> 1f
                is SpeedCircleState.UploadResult -> 1f
            },
            state = state,
            compact = compact,
            onGoClick = onGoClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
    }
}

@Composable
private fun SpeedTestResultPanel(
    state: SpeedCircleState,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean
) {
    val downloadValue = when (state) {
        is SpeedCircleState.DownloadResult -> state.downloadMbps
        is SpeedCircleState.UploadResult -> null
        else -> null
    }

    val uploadValue = when (state) {
        is SpeedCircleState.UploadResult -> state.uploadMbps
        else -> null
    }

    val pingValue = when (state) {
        is SpeedCircleState.DownloadResult -> state.pingMs
        is SpeedCircleState.UploadResult -> state.pingMs
        else -> 0
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (compact) 6.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SPEEDTEST",
                color = MutedText,
                fontSize = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Download",
                color = Color(0xFF14A8C6),
                fontSize = if (compact) 11.sp else 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = downloadValue?.format1() ?: "156.7",
                color = DarkText,
                fontSize = if (compact) 34.sp else 40.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = if (compact) 32.sp else 38.sp
            )

            Text(
                text = "↓Mbps",
                color = MutedText,
                fontSize = if (compact) 14.sp else 16.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Upload",
                color = SpeedRingUpload,
                fontSize = if (compact) 11.sp else 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = uploadValue?.format1() ?: "48.9",
                color = DarkText,
                fontSize = if (compact) 30.sp else 34.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = if (compact) 28.sp else 32.sp
            )

            Text(
                text = "↑Mbps",
                color = MutedText,
                fontSize = if (compact) 13.sp else 15.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = MutedText,
                            fontSize = if (compact) 14.sp else 16.sp
                        )
                    ) {
                        append("Ping ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = DarkText,
                            fontSize = if (compact) 16.sp else 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("$pingValue ms")
                    }
                }
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
                text = "×",
                color = Color.Black,
                fontSize = if (compact) 20.sp else 22.sp
            )
        }
    }
}

@Composable
private fun SpeedCircle(
    progress: Float,
    state: SpeedCircleState,
    compact: Boolean,
    onGoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                startAngle = -130f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            drawArc(
                color = ringColor,
                startAngle = 130f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
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
                        .border(4.dp, CyanStroke, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onGoClick,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "GO",
                            color = Color.White,
                            fontSize = if (compact) 22.sp else 28.sp
                        )
                    }
                }
            }

            is SpeedCircleState.Downloading -> {
                SpeedCenterMetric(
                    value = state.downloadMbps.format1(),
                    unit = "↓ Mbps",
                    pingMs = state.pingMs,
                    compact = compact
                )
            }

            is SpeedCircleState.Uploading -> {
                SpeedCenterMetric(
                    value = state.uploadMbps.format1(),
                    unit = "↑ Mbps",
                    pingMs = state.pingMs,
                    compact = compact
                )
            }

            else -> Unit
        }
    }
}

private fun Float.format1(): String = String.format("%.1f", this)

@Composable
private fun SpeedCenterMetric(
    value: String,
    unit: String,
    pingMs: Int,
    compact: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (compact) 12.dp else 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            color = DarkText,
            fontSize = if (compact) 32.sp else 44.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = unit,
            color = MutedText,
            fontSize = if (compact) 13.sp else 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(if (compact) 14.dp else 22.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MutedText,
                        fontSize = if (compact) 13.sp else 17.sp
                    )
                ) { append("Ping ") }
                withStyle(
                    style = SpanStyle(
                        color = DarkText,
                        fontSize = if (compact) 14.sp else 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) { append("$pingMs ms") }
            }
        )
    }
}

@Composable
private fun ChannelInterferenceCard(
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.SignalCellularAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Wi-Fi Channel Interference",
                color = Color.White,
                fontSize = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SectionCard(title = "Current Wi-Fi", compact = compact) {
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
                SectionCard(title = "Interference Channel", compact = compact) {
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

            item {
                SectionCard(title = "Other Networks", compact = compact) {
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

@Composable
private fun SectionCard(
    title: String,
    compact: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = title,
                color = DarkText,
                fontSize = if (compact) 14.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChannelRow(
    row: ChannelRowData,
    compact: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = row.channel,
                color = BlueAccent,
                fontSize = if (compact) 12.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = row.name,
                color = DarkText,
                fontSize = if (compact) 12.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(6.dp))
        QualityRow(quality = row.quality, compact = true)
    }
}

@Composable
private fun NetworkCardFrame(
    modifier: Modifier = Modifier,
    headerTitle: String,
    headerLeading: @Composable (() -> Unit)? = null,
    headerCenter: @Composable (() -> Unit)? = null,
    headerTrailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = HeaderGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (headerLeading != null) {
                headerLeading()
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = headerTitle,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (headerCenter != null) {
                headerCenter()
                Spacer(modifier = Modifier.width(6.dp))
            }

            if (headerTrailing != null) headerTrailing()
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
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )
        }
    }
}

@Composable
private fun HeaderInfoCapsuleSmall() {
    Box(
        modifier = Modifier
            .width(46.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFEDEDED)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun QualityRow(
    quality: SignalQuality,
    compact: Boolean = false
) {
    val textSize = if (compact) 9.sp else 12.sp
    val chipPaddingH = if (compact) 4.dp else 6.dp
    val chipPaddingV = if (compact) 2.dp else 3.dp

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        QualityWord("Poor", quality == SignalQuality.POOR, BadRed, textSize, chipPaddingH, chipPaddingV)
        DotSeparator(textSize)
        QualityWord(
            "Good",
            quality == SignalQuality.GOOD || quality == SignalQuality.OK_ORANGE,
            if (quality == SignalQuality.OK_ORANGE) GoodOrange else GoodYellow,
            textSize,
            chipPaddingH,
            chipPaddingV
        )
        DotSeparator(textSize)
        QualityWord("Excellent", quality == SignalQuality.EXCELLENT, GoodGreen, textSize, chipPaddingH, chipPaddingV)
    }
}

@Composable
private fun QualityWord(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    textSize: TextUnit,
    paddingH: Dp,
    paddingV: Dp
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) selectedColor else Color.Transparent)
            .padding(horizontal = paddingH, vertical = paddingV)
    ) {
        Text(
            text = text,
            color = if (selected) Color.Black else MutedText,
            fontSize = textSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun DotSeparator(textSize: TextUnit) {
    Text(
        text = " · ",
        color = MutedText,
        fontSize = textSize,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun CenterStatLineTiny(
    label: String,
    value: String
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = MutedText, fontSize = 11.sp)) { append("$label ") }
            withStyle(SpanStyle(color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                append(value)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        maxLines = 1
    )
}

@Composable
private fun StatBlockTiny(label: String, value: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = "$label ",
            color = MutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = Color.Black,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}