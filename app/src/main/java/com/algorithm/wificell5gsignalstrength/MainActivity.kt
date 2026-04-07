package com.algorithm.wificell5gsignalstrength

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.platform.LocalContext
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.algorithm.wificell5gsignalstrength.settings.SettingsActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

class MainActivity : ComponentActivity() {

    private val wifiManager by lazy {
        applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val telephonyManager by lazy {
        getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    private val connectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private var uiState by mutableStateOf(SignalUiState())
    private var speedTestState by mutableStateOf<SpeedCircleState>(SpeedCircleState.Idle)

    private var wifiReceiver: BroadcastReceiver? = null
    private var telephonyCallback: TelephonyCallback? = null
    private var refreshJob: Job? = null
    private var latestSignalStrength: SignalStrength? = null
    private var speedTestJob: Job? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshAll()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNeededPermissions()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppBg
                ) {
                    WifiCellSignalScreen(
                        state = uiState,
                        onRefresh = { refreshAll() },
                        onGoClick = { runFakeSpeedTest() },
                        onResetSpeedTest = { resetSpeedTest() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerWifiReceiver()
        registerTelephony()
        startRefreshLoop()
        refreshAll()
    }

    override fun onStop() {
        super.onStop()
        unregisterWifiReceiver()
        unregisterTelephony()
        refreshJob?.cancel()
        refreshJob = null
        speedTestJob?.cancel()
        speedTestJob = null
    }

    private fun requestNeededPermissions() {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_WIFI_STATE)
            add(Manifest.permission.ACCESS_NETWORK_STATE)
            add(Manifest.permission.READ_PHONE_STATE)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }.distinct()

        val missing = permissions.filterNot { hasPermission(it) }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun startRefreshLoop() {
        refreshJob?.cancel()
        refreshJob = lifecycleScope.launch {
            while (isActive) {
                refreshAll()
                delay(5000)
            }
        }
    }

    private fun refreshAll() {
        if (hasWifiScanPermission()) {
            runCatching { wifiManager.startScan() }
        }
        uiState = buildSignalUiState()
    }

    private fun runFakeSpeedTest() {
        speedTestJob?.cancel()
        speedTestJob = lifecycleScope.launch {
            speedTestState = SpeedCircleState.Downloading(
                downloadMbps = 32.4f,
                pingMs = 18
            )
            uiState = buildSignalUiState()

            delay(1200)

            speedTestState = SpeedCircleState.Uploading(
                uploadMbps = 14.8f,
                pingMs = 16
            )
            uiState = buildSignalUiState()

            delay(1200)

            speedTestState = SpeedCircleState.Result(
                downloadMbps = 32.4f,
                uploadMbps = 14.8f,
                pingMs = 16
            )
            uiState = buildSignalUiState()
        }
    }

    private fun resetSpeedTest() {
        speedTestJob?.cancel()
        speedTestJob = null
        speedTestState = SpeedCircleState.Idle
        uiState = buildSignalUiState()
    }

    private fun hasWifiScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun registerWifiReceiver() {
        if (wifiReceiver != null) return

        wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                uiState = buildSignalUiState()
            }
        }

        ContextCompat.registerReceiver(
            this,
            wifiReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun unregisterWifiReceiver() {
        wifiReceiver?.let {
            runCatching { unregisterReceiver(it) }
        }
        wifiReceiver = null
    }

    private fun registerTelephony() {
        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            latestSignalStrength = runCatching { telephonyManager.signalStrength }.getOrNull()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (telephonyCallback != null) return

            val callback = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    latestSignalStrength = signalStrength
                    uiState = buildSignalUiState()
                }
            }

            telephonyCallback = callback

            runCatching {
                telephonyManager.registerTelephonyCallback(mainExecutor, callback)
            }

            latestSignalStrength = runCatching { telephonyManager.signalStrength }.getOrNull()
        } else {
            latestSignalStrength = runCatching { telephonyManager.signalStrength }.getOrNull()
        }
    }

    private fun unregisterTelephony() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let {
                runCatching { telephonyManager.unregisterTelephonyCallback(it) }
            }
        }
        telephonyCallback = null
    }

    @Suppress("DEPRECATION")
    private fun buildSignalUiState(): SignalUiState {
        val wifiInfo = runCatching { wifiManager.connectionInfo }.getOrNull()
        val scanResults = if (hasWifiScanPermission()) {
            runCatching { wifiManager.scanResults }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        val activeNetwork = connectivityManager.activeNetwork
        val caps = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }

        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isCell = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

        val ssid = wifiInfo?.ssid
            ?.removePrefix("\"")
            ?.removeSuffix("\"")
            ?.takeUnless { it.isNullOrBlank() || it == "<unknown ssid>" }
            ?: "Not connected"

        val wifiRssi = wifiInfo?.rssi ?: -127
        val wifiFrequency = wifiInfo?.frequency ?: 0
        val wifiBand = when {
            wifiFrequency in 2400..2500 -> "2.4 GHz"
            wifiFrequency in 4900..5900 -> "5 GHz"
            wifiFrequency in 5925..7125 -> "6 GHz"
            else -> "Unknown"
        }

        val wifiLevel = WifiManager.calculateSignalLevel(wifiRssi, 5)
        val wifiQuality = when (wifiLevel) {
            0, 1 -> SignalQuality.POOR
            2, 3 -> SignalQuality.GOOD
            else -> SignalQuality.EXCELLENT
        }

        val linkSpeed = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && wifiInfo != null -> {
                wifiInfo.rxLinkSpeedMbps.takeIf { it > 0 } ?: wifiInfo.linkSpeed
            }
            else -> wifiInfo?.linkSpeed ?: 0
        }.takeIf { it > 0 }

        val cellSignal = latestSignalStrength
            ?.cellSignalStrengths
            ?.firstOrNull()

        val cellDbm = cellSignal?.dbm ?: 0
        val cellAsu = cellSignal?.asuLevel ?: 0
        val cellQuality = when (cellSignal?.level ?: 0) {
            0 -> SignalQuality.POOR
            1, 2 -> SignalQuality.GOOD
            else -> SignalQuality.EXCELLENT
        }

        val carrierName = runCatching {
            telephonyManager.simOperatorName
        }.getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "Cellular"

        val networkType = getSafeNetworkTypeLabel()

        val sortedScans = scanResults
            .sortedByDescending { it.level }
            .take(20)

        val currentWifiRows = sortedScans
            .filter { sameSsid(it.SSID, ssid) }
            .map { it.toChannelRow() }

        val interferenceRows = sortedScans
            .filterNot { sameSsid(it.SSID, ssid) }
            .filter { isOverlappingChannel(it.frequency, wifiFrequency) }
            .map { it.toChannelRow() }

        val otherRows = sortedScans
            .filterNot { sameSsid(it.SSID, ssid) }
            .filterNot { isOverlappingChannel(it.frequency, wifiFrequency) }
            .map { it.toChannelRow() }

        return SignalUiState(
            wifiCard = WifiCardData(
                carrier = if (isWifi) "Connected" else "Wi-Fi",
                title = "WiFi Signal",
                band = wifiBand,
                quality = wifiQuality,
                dbm = wifiRssi,
                pingMs = null,
                connectedTo = ssid,
                linkSpeedMbps = linkSpeed
            ),
            sim1 = CellSignalData(
                carrier = carrierName,
                title = "Cell Signal",
                simLabel = "SIM 1",
                networkType = networkType,
                quality = cellQuality,
                asu = cellAsu,
                dbm = cellDbm,
                pingMs = null,
                towerId = "—"
            ),
            sim2 = CellSignalData(
                carrier = "SIM 2",
                title = "Cell Signal",
                simLabel = "SIM 2",
                networkType = "—",
                quality = SignalQuality.POOR,
                asu = 0,
                dbm = 0,
                pingMs = null,
                towerId = "—"
            ),
            speedTest = speedTestState,
            channels = ChannelSectionData(
                currentWifi = if (currentWifiRows.isNotEmpty()) {
                    currentWifiRows
                } else {
                    listOf(ChannelRowData("Current", ssid, wifiQuality))
                },
                interference = interferenceRows,
                otherNetworks = otherRows
            ),
            activeTransportLabel = when {
                isWifi -> "Wi-Fi"
                isCell -> "Cellular"
                else -> "Offline"
            }
        )
    }

    private fun getSafeNetworkTypeLabel(): String {
        val type = runCatching {
            telephonyManager.dataNetworkType
        }.getOrElse {
            runCatching { telephonyManager.networkType }.getOrDefault(0)
        }
        return networkTypeLabel(type)
    }

    private fun sameSsid(a: String?, b: String?): Boolean {
        return !a.isNullOrBlank() && !b.isNullOrBlank() && a == b
    }

    private fun isOverlappingChannel(otherFreq: Int, currentFreq: Int): Boolean {
        if (otherFreq == 0 || currentFreq == 0) return false
        return abs(otherFreq - currentFreq) <= 25
    }

    private fun networkTypeLabel(type: Int): String {
        return when (type) {
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
            TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_GSM -> "2G"
            else -> "Unknown"
        }
    }
}

private fun ScanResult.toChannelRow(): ChannelRowData {
    val quality = when {
        level <= -85 -> SignalQuality.POOR
        level <= -70 -> SignalQuality.OK_ORANGE
        level <= -60 -> SignalQuality.GOOD
        else -> SignalQuality.EXCELLENT
    }

    return ChannelRowData(
        channel = "Channel ${frequencyToChannel(frequency)}",
        name = SSID.ifBlank { "<hidden>" },
        quality = quality
    )
}

private fun frequencyToChannel(freq: Int): Int {
    return when {
        freq in 2412..2484 -> ((freq - 2412) / 5) + 1
        freq in 5170..5895 -> (freq - 5000) / 5
        freq in 5955..7115 -> (freq - 5950) / 5
        else -> 0
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
    onResetSpeedTest: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .safeDrawingPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        TopActionBar(
            onRefresh = onRefresh,
            onSettingsClick = {
                context.startActivity(Intent(context, SettingsActivity::class.java))
            }
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
                        .weight(if (compact) 0.40f else 0.38f),
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
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = null,
            tint = HeaderGray,
            modifier = Modifier
                .size(30.dp)
                .clickable { onSettingsClick() }
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
    if (state is SpeedCircleState.Result) {
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

                Spacer(modifier = Modifier.height(0.dp))

                Text(
                    text = "Download",
                    color = Color(0xFF14A8C6),
                    fontSize = if (compact) 11.sp else 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = state.downloadMbps.format1(),
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

                Spacer(modifier = Modifier.height(0.dp))

                Text(
                    text = "Upload",
                    color = SpeedRingUpload,
                    fontSize = if (compact) 11.sp else 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = state.uploadMbps.format1(),
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

                Spacer(modifier = Modifier.height(0.dp))

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
                            append("${state.pingMs} ms")
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
                is SpeedCircleState.Result -> 1f
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
        is SpeedCircleState.Uploading,
        is SpeedCircleState.Result -> SpeedRingUpload
        else -> SpeedRingDownload
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
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
                        fontSize = if (compact) 22.sp else 28.sp
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

            is SpeedCircleState.Result -> Unit
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
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MutedText,
                        fontSize = if (compact) 13.sp else 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                ) {
                    append("Ping ")
                }
                withStyle(
                    style = SpanStyle(
                        color = DarkText,
                        fontSize = if (compact) 14.sp else 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("$pingMs ms")
                }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChannelRow(
    row: ChannelRowData,
    compact: Boolean
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
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
    val textSize = if (compact) 9.sp else 12.sp
    val chipPaddingH = if (compact) 4.dp else 6.dp
    val chipPaddingV = if (compact) 2.dp else 3.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
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
            withStyle(
                style = SpanStyle(
                    color = MutedText,
                    fontSize = 11.sp
                )
            ) {
                append("$label ")
            }
            withStyle(
                style = SpanStyle(
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(value)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        maxLines = 1
    )
}

@Composable
private fun StatBlockTiny(
    label: String,
    value: String
) {
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

private fun Float.format1(): String = String.format("%.1f", this)

@Immutable
data class SignalUiState(
    val wifiCard: WifiCardData = WifiCardData(
        carrier = "Wi-Fi",
        title = "WiFi Signal",
        band = "Unknown",
        quality = SignalQuality.POOR,
        dbm = 0,
        pingMs = null,
        connectedTo = "Not connected",
        linkSpeedMbps = null
    ),
    val sim1: CellSignalData = CellSignalData(
        carrier = "Cellular",
        title = "Cell Signal",
        simLabel = "SIM 1",
        networkType = "Unknown",
        quality = SignalQuality.POOR,
        asu = 0,
        dbm = 0,
        pingMs = null,
        towerId = "—"
    ),
    val sim2: CellSignalData = CellSignalData(
        carrier = "SIM 2",
        title = "Cell Signal",
        simLabel = "SIM 2",
        networkType = "—",
        quality = SignalQuality.POOR,
        asu = 0,
        dbm = 0,
        pingMs = null,
        towerId = "—"
    ),
    val speedTest: SpeedCircleState = SpeedCircleState.Idle,
    val channels: ChannelSectionData = ChannelSectionData(
        currentWifi = emptyList(),
        interference = emptyList(),
        otherNetworks = emptyList()
    ),
    val activeTransportLabel: String = "Offline"
)

@Immutable
data class WifiCardData(
    val carrier: String,
    val title: String,
    val band: String,
    val quality: SignalQuality,
    val dbm: Int,
    val pingMs: Int?,
    val connectedTo: String,
    val linkSpeedMbps: Int?
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
    val pingMs: Int?,
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

    data class Uploading(
        val uploadMbps: Float,
        val pingMs: Int
    ) : SpeedCircleState

    data class Result(
        val downloadMbps: Float,
        val uploadMbps: Float,
        val pingMs: Int
    ) : SpeedCircleState
}