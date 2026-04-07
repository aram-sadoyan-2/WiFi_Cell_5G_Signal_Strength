package com.algorithm.wificell5gsignalstrength

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.algorithm.wificell5gsignalstrength.ui.CellSignalData
import com.algorithm.wificell5gsignalstrength.ui.ChannelRowData
import com.algorithm.wificell5gsignalstrength.ui.ChannelSectionData
import com.algorithm.wificell5gsignalstrength.ui.SignalQuality
import com.algorithm.wificell5gsignalstrength.ui.SignalUiState
import com.algorithm.wificell5gsignalstrength.ui.SpeedCircleState
import com.algorithm.wificell5gsignalstrength.ui.WifiCardData
import com.algorithm.wificell5gsignalstrength.ui.WifiCellSignalScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.glance.appwidget.updateAll
import com.algorithm.wificell5gsignalstrength.widget.SpeedTestWidget
import com.algorithm.wificell5gsignalstrength.widget.SpeedTestWidgetReceiver

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_RUN_SPEED_TEST = "extra_run_speed_test"
    }

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

        val shouldRunSpeedTest = intent?.getBooleanExtra(EXTRA_RUN_SPEED_TEST, false) == true

        setContent {
            WifiCellSignalScreen(
                state = uiState,
                onRefresh = { refreshAll() },
                onGoClick = { runFakeSpeedTest() },
                onResetSpeedTest = { resetSpeedTest() },
                onSettingsClick = {
                    startActivity(Intent(this, SettingsActivity::class.java))
                },
                openSettingsFromWidget = false
            )
        }

        if (shouldRunSpeedTest) {
            runFakeSpeedTest()
            intent?.removeExtra(EXTRA_RUN_SPEED_TEST)
        }
    }

    private fun resetSpeedTest() {
        speedTestJob?.cancel()
        speedTestJob = null
        speedTestState = SpeedCircleState.Idle
        uiState = buildSignalUiState()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val shouldRunSpeedTest = intent.getBooleanExtra(EXTRA_RUN_SPEED_TEST, false)
        if (shouldRunSpeedTest) {
            runFakeSpeedTest()
            intent.removeExtra(EXTRA_RUN_SPEED_TEST)
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
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
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

            val finalUpload = 14.8f
            val finalPing = 16

            speedTestState = SpeedCircleState.UploadResult(
                uploadMbps = finalUpload,
                pingMs = finalPing
            )
            uiState = buildSignalUiState()

            val prefs = getSharedPreferences("speed_widget_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("speed_value", String.format("%.1f", finalUpload))
                .putString("speed_unit", "Mbps")
                .putString("ping_value", "$finalPing ms")
                .apply()

            SpeedTestWidget().updateAll(this@MainActivity)
        }
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

    @SuppressLint("MissingPermission")
    private fun registerTelephony() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            latestSignalStrength = null
            return
        }

        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            latestSignalStrength = null
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
                telephonyManager.registerTelephonyCallback(
                    mainExecutor,
                    callback
                )
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

    @SuppressLint("MissingPermission")
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

        val cellSignal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            latestSignalStrength?.cellSignalStrengths?.firstOrNull()
        } else {
            null
        }

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

        val wifiCard = WifiCardData(
            carrier = if (isWifi) "Connected" else "Wi-Fi",
            title = "WiFi Signal",
            band = wifiBand,
            quality = wifiQuality,
            dbm = wifiRssi,
            pingMs = null,
            connectedTo = ssid,
            linkSpeedMbps = linkSpeed
        )

        val sim1 = CellSignalData(
            carrier = carrierName,
            title = "Cell Signal",
            simLabel = "SIM 1",
            networkType = networkType,
            quality = cellQuality,
            asu = cellAsu,
            dbm = cellDbm,
            pingMs = null,
            towerId = "—"
        )

        val sim2 = CellSignalData(
            carrier = "SIM 2",
            title = "Cell Signal",
            simLabel = "SIM 2",
            networkType = "—",
            quality = SignalQuality.POOR,
            asu = 0,
            dbm = 0,
            pingMs = null,
            towerId = "—"
        )

        return SignalUiState(
            wifiCard = wifiCard,
            sim1 = sim1,
            sim2 = sim2,
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

    @SuppressLint("MissingPermission")
    private fun getSafeNetworkTypeLabel(): String {
        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            return "Unknown"
        }

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

