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
import androidx.compose.material.icons.outlined.Close
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.wificell5gsignalstrength.CellInfoPopupData
import com.algorithm.wificell5gsignalstrength.NetworkInfoPopupData
import com.algorithm.wificell5gsignalstrength.WifiInfoPopupData
import kotlin.math.min

private val AppBg = Color(0xFFF1F1F1)
private val HeaderGray = Color(0xFF8A8A8A)
private val CardBg = Color(0xFFF7F7F7)
private val BorderGray = Color(0xFFD7D7D7)
private val DarkText = Color(0xFF111111)
private val MutedText = Color(0xFF60656D)
private val BlueAccent = Color(0xFF2C62F4)

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
                            .weight(if (compact) 0.34f else 0.36f),
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
                            .weight(if (compact) 0.26f else 0.25f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CellSignalCard(
                            data = state.sim1,
                            modifier = Modifier.weight(1f),
                            compact = compact,
                            onInfoClick = { popupData = state.sim1.infoPopup }
                        )

                        CellSignalCard(
                            data = state.sim2,
                            modifier = Modifier.weight(1f),
                            compact = compact,
                            onInfoClick = { popupData = state.sim2.infoPopup }
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
                    colors = listOf(
                        Color(0xFFFF7F87),
                        Color(0xFFD82CC9)
                    )
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
    compact: Boolean,
    onInfoClick: () -> Unit
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
            Icon(
                imageVector = Icons.Rounded.SignalCellularAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(if (compact) 15.dp else 17.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = data.carrier,
                color = Color.White,
                fontSize = if (compact) 14.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier
                    .size(if (compact) 18.dp else 20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF44E61F))
                    .border(1.dp, Color(0xFF4CAF50), CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            HeaderInfoCapsuleSmall(onClick = onInfoClick)
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
                    .padding(horizontal = 14.dp).padding( top = 12.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.NetworkWifi,
                    contentDescription = null,
                    tint = MutedText,
                    modifier = Modifier.size(if (compact) 34.dp else 40.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.title,
                        color = MutedText,
                        fontSize = if (compact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = data.band,
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

                Text(
                    text = "dBm ${data.dbm}",
                    color = DarkText,
                    fontSize = if (compact) 13.sp else 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = data.linkSpeedMbps?.let { "link $it Mbps" } ?: "ping —",
                    color = MutedText,
                    fontSize = if (compact) 11.sp else 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Connected to",
                    color = MutedText,
                    fontSize = if (compact) 10.sp else 11.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = data.connectedTo,
                    color = HeaderGray,
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CellSignalCard(
    data: CellSignalData,
    modifier: Modifier = Modifier,
    compact: Boolean,
    onInfoClick: () -> Unit
) {
    NetworkCardFrame(
        modifier = modifier.fillMaxSize(),
        headerTitle = data.carrier,
        headerTrailing = { HeaderInfoCapsuleSmall(onClick = onInfoClick) }
    ) {
        Icon(
            imageVector = Icons.Rounded.SignalCellularAlt,
            contentDescription = null,
            tint = MutedText,
            modifier = Modifier.size(if (compact) 20.dp else 24.dp)
        )

        //Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = data.title,
            color = MutedText,
            fontSize = if (compact) 9.sp else 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )

        Text(
            text = "(${data.simLabel}) ${data.networkType}",
            color = BlueAccent,
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ASU ${data.asu}",
            color = DarkText,
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "dBm ${data.dbm}",
            color = DarkText,
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Tower ID: ${data.towerId}",
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
                        .border(4.dp, CyanStroke, CircleShape)
                        .clickable { onGoClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GO",
                        color = Color.White,
                        fontSize = if (compact) 22.sp else 28.sp,
                        fontWeight = FontWeight.Bold
                    )
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
            text = "Ping $pingMs ms",
            color = DarkText,
            fontSize = if (compact) 14.sp else 18.sp,
            fontWeight = FontWeight.Bold
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
        is SpeedCircleState.DownloadResult -> state.downloadMbps.format1()
        else -> "—"
    }

    val uploadValue = when (state) {
        is SpeedCircleState.UploadResult -> state.uploadMbps.format1()
        else -> "—"
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
                .padding(top = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SPEEDTEST",
                color = MutedText,
                fontSize = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Download",
                color = Color(0xFF14A8C6),
                fontSize = if (compact) 11.sp else 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = downloadValue,
                color = DarkText,
                fontSize = if (compact) 34.sp else 40.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "↓Mbps",
                color = MutedText,
                fontSize = if (compact) 14.sp else 16.sp
            )

            Text(
                text = "Upload",
                color = SpeedRingUpload,
                fontSize = if (compact) 11.sp else 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = uploadValue,
                color = DarkText,
                fontSize = if (compact) 30.sp else 34.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "↑Mbps",
                color = MutedText,
                fontSize = if (compact) 13.sp else 15.sp
            )

            Text(
                text = "Ping $pingValue ms",
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
                text = "×",
                color = Color.Black,
                fontSize = if (compact) 20.sp else 22.sp
            )
        }
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
        Text(
            text = "Wi-Fi Channel Interference",
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
                SectionCard(title = "Other Networks", compact = compact) {
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
        text = "No networks found",
        color = MutedText,
        fontSize = 13.sp
    )
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
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
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

@Composable
private fun ChannelRow(
    row: ChannelRowData,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
    }
}

@Composable
private fun NetworkCardFrame(
    modifier: Modifier = Modifier,
    headerTitle: String,
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
            Text(
                text = headerTitle,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            headerTrailing?.invoke()
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
                    .padding(horizontal = 14.dp).padding(top = 10.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )
        }
    }
}

@Composable
private fun HeaderInfoCapsuleSmall(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(46.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFEDEDED))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Info",
            tint = Color.Black,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun NetworkInfoPopup(
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
                        fontSize = 21.sp,
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
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F3F3))
                    .border(2.dp, Color.Black, CircleShape)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
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

        InfoLine("Access Point", data.accessPoint)
        InfoLine("Frequency", "${data.frequencyMHz} MHz")
        InfoLine("Channel", data.channel.toString())
        InfoLine("Linkspeed", data.linkSpeedMbps?.let { "$it Mbps" } ?: "—")

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Router",
            color = DarkText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        InfoLine("5 GHz Band", if (data.is5GHzSupported) "Supported" else "Not supported")
        InfoLine("IP Address", data.ipAddress)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "MAC Address",
            color = DarkText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        InfoLine("Gateway", data.gateway)
        InfoLine("Router MAC", data.routerMac)
        InfoLine("DNS1", data.dns1)
        InfoLine("DNS2", data.dns2)
        InfoLine("DHCP Server", data.dhcpServer)
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
                text = "${data.carrier} ${data.simLabel}",
                color = DarkText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        InfoLine("Network Type", data.networkType)
        InfoLine("Signal", "${data.dbm} dBm")
        InfoLine("ASU", data.asu.toString())
        InfoLine("Quality", data.qualityLabel)
        InfoLine("Operator", data.operatorName)
        InfoLine("Country", data.countryIso)
        InfoLine("Roaming", if (data.roaming) "Yes" else "No")
    }
}

@Composable
private fun InfoLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label ",
            color = DarkText,
            fontSize = 17.sp
        )

        Text(
            text = value,
            color = DarkText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun QualityRow(
    quality: SignalQuality,
    compact: Boolean = false
) {
    val textSize = if (compact) 11.sp else 14.sp
    val chipPadH = if (compact) 8.dp else 10.dp
    val chipPadV = if (compact) 3.dp else 4.dp
    val normalColor = MutedText

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        QualityItem(
            text = "Poor",
            selected = quality == SignalQuality.POOR,
            selectedColor = Color(0xFFF08E8E),
            textSize = textSize,
            chipPadH = chipPadH,
            chipPadV = chipPadV,
            normalColor = normalColor
        )

        Text(
            text = "·",
            color = normalColor,
            fontSize = textSize,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        QualityItem(
            text = "Good",
            selected = quality == SignalQuality.GOOD || quality == SignalQuality.OK_ORANGE,
            selectedColor = if (quality == SignalQuality.OK_ORANGE) {
                Color(0xFFF3C15A)
            } else {
                Color(0xFFF0D93A)
            },
            textSize = textSize,
            chipPadH = chipPadH,
            chipPadV = chipPadV,
            normalColor = normalColor
        )

        Text(
            text = "·",
            color = normalColor,
            fontSize = textSize,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        QualityItem(
            text = "Excellent",
            selected = quality == SignalQuality.EXCELLENT,
            selectedColor = Color(0xFF8EF15A),
            textSize = textSize,
            chipPadH = chipPadH,
            chipPadV = chipPadV,
            normalColor = normalColor
        )
    }
}

@Composable
private fun QualityItem(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    textSize: TextUnit,
    chipPadH: Dp,
    chipPadV: Dp,
    normalColor: Color
) {
    if (selected) {
        Box(
            modifier = Modifier
                .background(selectedColor, RoundedCornerShape(0.dp))
                .padding(horizontal = chipPadH, vertical = chipPadV),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontSize = textSize,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Text(
            text = text,
            color = normalColor,
            fontSize = textSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun Float.format1(): String = String.format("%.1f", this)