package com.algorithm.wificell5gsignalstrength

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppBg
                ) {
                    WifiCellSignalScreen()
                }
            }
        }
    }
}

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
private val SpeedRingGray = Color(0xFF6A7380)
private val SpeedRingStart = Color(0xFF44D9B4)
private val SpeedRingEnd = Color(0xFF34C9F1)
private val GoBlue = Color(0xFF145EC8)
private val GoBlue2 = Color(0xFF2B75D9)
private val CyanStroke = Color(0xFF1CE5D2)
private val Magenta = Color(0xFFCC27F7)

@Composable
fun WifiCellSignalScreen() {
    val wifiCard = remember {
        WifiCardData(
            carrier = "Spectrum",
            title = "WiFi Signal",
            band = "2.4 GHz",
            quality = SignalQuality.POOR,
            dbm = -115,
            pingMs = 210,
            connectedTo = "Spec_2431 2"
        )
    }

    val sim1 = remember {
        CellSignalData(
            carrier = "Verizon",
            title = "LTE Signal",
            simLabel = "SIM 1",
            networkType = "5G",
            quality = SignalQuality.EXCELLENT,
            asu = 25,
            dbm = -89,
            pingMs = 45,
            towerId = "US-2048"
        )
    }

    val sim2 = remember {
        CellSignalData(
            carrier = "T-Mobile",
            title = "LTE Signal",
            simLabel = "SIM 2",
            networkType = "4G",
            quality = SignalQuality.GOOD,
            asu = 30,
            dbm = -110,
            pingMs = 45,
            towerId = "US-2048"
        )
    }

    val interference = remember {
        ChannelSectionData(
            currentWifi = listOf(
                ChannelRowData("Channel 7", "Spec_2431 2", SignalQuality.EXCELLENT),
                ChannelRowData("Channel 36", "RDS_New14", SignalQuality.GOOD),
                ChannelRowData("Channel 1", "Office_24", SignalQuality.GOOD)
            ),
            interference = listOf(
                ChannelRowData("Channel 48", "Baijing tech", SignalQuality.OK_ORANGE),
                ChannelRowData("Channel 11", "HomeNet_2G", SignalQuality.POOR),
                ChannelRowData("Channel 6", "Cafe_wifi", SignalQuality.OK_ORANGE)
            ),
            otherNetworks = listOf(
                ChannelRowData("Channel 2", "Xaomi s20-4", SignalQuality.GOOD),
                ChannelRowData("Channel 149", "Guest_5G", SignalQuality.EXCELLENT),
                ChannelRowData("Channel 44", "MyRouter-5", SignalQuality.GOOD),
                ChannelRowData("Channel 9", "AndroidAP", SignalQuality.POOR),
                ChannelRowData("Channel 3", "TPLink_324", SignalQuality.GOOD),
                ChannelRowData("Channel 13", "Linksys_Test", SignalQuality.OK_ORANGE)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        TopActionBar()

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.40f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            WifiSignalCard(
                data = wifiCard,
                modifier = Modifier.weight(0.46f)
            )

            SpeedTestPanel(
                modifier = Modifier.weight(0.54f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.22f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CellSignalCard(
                data = sim1,
                modifier = Modifier.weight(1f)
            )
            CellSignalCard(
                data = sim2,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        ChannelInterferenceCard(
            data = interference,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.38f)
        )
    }
}

@Composable
private fun TopActionBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleGradientIconButton(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        )

        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = null,
            tint = HeaderGray,
            modifier = Modifier.size(30.dp)
        )
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
    modifier: Modifier = Modifier
) {
    NetworkCardFrame(
        modifier = modifier.fillMaxSize(),
        headerTitle = data.carrier,
        headerLeading = {
            Icon(
                imageVector = Icons.Outlined.CellTower,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        },
        headerCenter = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF44E61F))
                    .border(1.dp, Color(0xFF4CAF50), CircleShape)
            )
        },
        headerTrailing = { HeaderInfoCapsuleSmall() }
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        Icon(
            imageVector = Icons.Outlined.NetworkWifi,
            contentDescription = null,
            tint = MutedText,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = data.title,
            color = MutedText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = data.band,
            color = BlueAccent,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        QualityRow(
            quality = data.quality,
            compact = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        CenterStatLineSmall("dBm", data.dbm.toString())
        Spacer(modifier = Modifier.height(2.dp))
        CenterStatLineSmall("ping", "${data.pingMs} mSec")

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Connected to",
            color = MutedText,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = data.connectedTo,
            color = HeaderGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CellSignalCard(
    data: CellSignalData,
    modifier: Modifier = Modifier
) {
    NetworkCardFrame(
        modifier = modifier.fillMaxSize(),
        headerTitle = data.carrier,
        headerLeading = {
            Icon(
                imageVector = Icons.Outlined.CellTower,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        },
        headerTrailing = { HeaderInfoCapsuleSmall() }
    ) {
        Icon(
            imageVector = Icons.Rounded.SignalCellularAlt,
            contentDescription = null,
            tint = MutedText,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = data.title,
            color = MutedText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "(${data.simLabel})  ${data.networkType}",
            color = BlueAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        QualityRow(
            quality = data.quality,
            compact = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBlockSmall("ASU", data.asu.toString())
            StatBlockSmall("dBm", data.dbm.toString())
        }

        Spacer(modifier = Modifier.height(6.dp))

        CenterStatLineSmall("ping", "${data.pingMs} mSec")

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Tower ID: ${data.towerId}",
            color = HeaderGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SpeedTestPanel(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SPEEDTEST",
            color = MutedText,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        SpeedCircle(
            progress = 0.73f,
            state = SpeedCircleState.Result(
                downloadMbps = 156.7f,
                uploadMbps = 48.9f,
                pingMs = 8
            ),
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = min(size.width, size.height) * 0.07f
            val diameter = min(size.width, size.height) - stroke * 1.2f
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = SpeedRingGray,
                startAngle = -145f,
                sweepAngle = 290f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        SpeedRingStart,
                        SpeedRingEnd,
                        SpeedRingStart
                    ),
                    center = center
                ),
                startAngle = -145f,
                sweepAngle = 290f * progress.coerceIn(0f, 1f),
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
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(GoBlue2, GoBlue)
                            )
                        )
                        .border(4.dp, CyanStroke, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GO",
                        color = Color.White,
                        fontSize = 28.sp
                    )
                }
            }

            is SpeedCircleState.Downloading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.downloadMbps.format1(),
                        color = DarkText,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "↓ Mbps",
                        color = MutedText,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ping ${state.pingMs} ms",
                        color = MutedText,
                        fontSize = 15.sp
                    )
                }
            }

            is SpeedCircleState.Result -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Download",
                        color = Color(0xFF14A8C6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = state.downloadMbps.format1(),
                        color = DarkText,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "↓ Mbps",
                        color = MutedText,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Upload",
                        color = Magenta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = state.uploadMbps.format1(),
                        color = DarkText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "↑ Mbps",
                        color = MutedText,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Ping ${state.pingMs} ms",
                        color = DarkText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelInterferenceCard(
    data: ChannelSectionData,
    modifier: Modifier = Modifier
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
                fontSize = 18.sp,
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
                SectionCard(title = "Current Wi-Fi") {
                    data.currentWifi.forEachIndexed { index, row ->
                        ChannelRow(row)
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
                SectionCard(title = "Interference Channel") {
                    data.interference.forEachIndexed { index, row ->
                        ChannelRow(row)
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
                SectionCard(title = "Other Networks") {
                    Column {
                        data.otherNetworks.forEachIndexed { index, row ->
                            ChannelRow(row)
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
private fun SectionCard(
    title: String,
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
                fontSize = 16.sp,
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
    row: ChannelRowData
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = row.channel,
                color = BlueAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = row.name,
                color = DarkText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        QualityRow(
            quality = row.quality,
            compact = true
        )
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
                modifier = Modifier.weight(1f)
            )

            if (headerCenter != null) {
                headerCenter()
                Spacer(modifier = Modifier.width(6.dp))
            }

            if (headerTrailing != null) {
                headerTrailing()
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
    val textSize = if (compact) 11.sp else 14.sp
    val chipPaddingH = if (compact) 6.dp else 8.dp
    val chipPaddingV = if (compact) 3.dp else 4.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        QualityWord(
            text = "Poor",
            selected = quality == SignalQuality.POOR,
            selectedColor = BadRed,
            textSize = textSize,
            paddingH = chipPaddingH,
            paddingV = chipPaddingV
        )

        DotSeparator(textSize)

        QualityWord(
            text = "Good",
            selected = quality == SignalQuality.GOOD || quality == SignalQuality.OK_ORANGE,
            selectedColor = if (quality == SignalQuality.OK_ORANGE) GoodOrange else GoodYellow,
            textSize = textSize,
            paddingH = chipPaddingH,
            paddingV = chipPaddingV
        )

        DotSeparator(textSize)

        QualityWord(
            text = "Excellent",
            selected = quality == SignalQuality.EXCELLENT,
            selectedColor = GoodGreen,
            textSize = textSize,
            paddingH = chipPaddingH,
            paddingV = chipPaddingV
        )
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
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DotSeparator(
    textSize: TextUnit
) {
    Text(
        text = " · ",
        color = MutedText,
        fontSize = textSize,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun CenterStatLineSmall(
    label: String,
    value: String
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MutedText,
                    fontSize = 13.sp
                )
            ) {
                append("$label ")
            }
            withStyle(
                style = SpanStyle(
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(value)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun StatBlockSmall(
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = "$label ",
            color = MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun Float.format1(): String = String.format("%.1f", this)

@Immutable
data class WifiCardData(
    val carrier: String,
    val title: String,
    val band: String,
    val quality: SignalQuality,
    val dbm: Int,
    val pingMs: Int,
    val connectedTo: String
)

@Immutable
data class CellSignalData(
    val carrier: String,
    val title: String,
    val simLabel: String,
    val networkType: String,
    val quality: SignalQuality,
    val asu: Int,
    val dbm: Int,
    val pingMs: Int,
    val towerId: String
)

@Immutable
data class ChannelRowData(
    val channel: String,
    val name: String,
    val quality: SignalQuality
)

@Immutable
data class ChannelSectionData(
    val currentWifi: List<ChannelRowData>,
    val interference: List<ChannelRowData>,
    val otherNetworks: List<ChannelRowData>
)

enum class SignalQuality {
    POOR,
    GOOD,
    EXCELLENT,
    OK_ORANGE
}

sealed interface SpeedCircleState {
    data object Idle : SpeedCircleState

    data class Downloading(
        val downloadMbps: Float,
        val pingMs: Int
    ) : SpeedCircleState

    data class Result(
        val downloadMbps: Float,
        val uploadMbps: Float,
        val pingMs: Int
    ) : SpeedCircleState
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WifiCellSignalScreenPreview() {
    MaterialTheme {
        Surface(color = AppBg) {
            WifiCellSignalScreen()
        }
    }
}