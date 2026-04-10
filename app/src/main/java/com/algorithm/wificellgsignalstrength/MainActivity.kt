package com.algorithm.wificellgsignalstrength

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.DhcpInfo
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.telephony.SignalStrength
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.text.format.Formatter
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import com.algorithm.wificellgsignalstrength.ui.CellSignalData
import com.algorithm.wificellgsignalstrength.ui.ChannelRowData
import com.algorithm.wificellgsignalstrength.ui.ChannelSectionData
import com.algorithm.wificellgsignalstrength.ui.SignalQuality
import com.algorithm.wificellgsignalstrength.ui.SignalUiState
import com.algorithm.wificellgsignalstrength.ui.SpeedCircleState
import com.algorithm.wificellgsignalstrength.ui.WifiCardData
import com.algorithm.wificellgsignalstrength.ui.WifiCellSignalScreen
import com.algorithm.wificellgsignalstrength.widget.SpeedTestWidget
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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

    private val realSpeedTester = RealSpeedTester()

    private var uiState by androidx.compose.runtime.mutableStateOf(SignalUiState())
    private var speedTestState by androidx.compose.runtime.mutableStateOf<SpeedCircleState>(SpeedCircleState.Idle)

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
                onGoClick = { runRealSpeedTest() },
                onResetSpeedTest = { resetSpeedTest() },
                onSettingsClick = {
                    startActivity(Intent(this, SettingsActivity::class.java))
                },
                openSettingsFromWidget = false
            )
        }

        if (shouldRunSpeedTest) {
            runRealSpeedTest()
            intent?.removeExtra(EXTRA_RUN_SPEED_TEST)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val shouldRunSpeedTest = intent.getBooleanExtra(EXTRA_RUN_SPEED_TEST, false)
        if (shouldRunSpeedTest) {
            runRealSpeedTest()
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

    private fun resetSpeedTest() {
        speedTestJob?.cancel()
        speedTestJob = null
        speedTestState = SpeedCircleState.Idle
        uiState = buildSignalUiState()
    }

    private fun runRealSpeedTest() {
        speedTestJob?.cancel()
        speedTestJob = lifecycleScope.launch {
            try {
                val result = realSpeedTester.run(
                    onDownloadProgress = { mbps, ping ->
                        speedTestState = SpeedCircleState.Downloading(
                            downloadMbps = mbps,
                            pingMs = ping
                        )
                        uiState = buildSignalUiState()
                    },
                    onUploadProgress = { mbps, ping ->
                        speedTestState = SpeedCircleState.Uploading(
                            uploadMbps = mbps,
                            pingMs = ping
                        )
                        uiState = buildSignalUiState()
                    }
                )

                speedTestState = SpeedCircleState.FinalResult(
                    downloadMbps = result.downloadMbps,
                    uploadMbps = result.uploadMbps,
                    pingMs = result.pingMs
                )
                uiState = buildSignalUiState()

                val prefs = getSharedPreferences("speed_widget_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("speed_value", String.format("%.1f", result.uploadMbps))
                    .putString("speed_unit", "Mbps")
                    .putString("ping_value", "${result.pingMs} ms")
                    .apply()

                SpeedTestWidget().updateAll(this@MainActivity)
            } catch (_: Exception) {
                speedTestState = SpeedCircleState.Idle
                uiState = buildSignalUiState()
            }
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
            ?.takeUnless { it.isBlank() || it == "<unknown ssid>" }
            ?: getString(R.string.not_connected)

        val wifiRssi = wifiInfo?.rssi ?: -127
        val wifiFrequency = wifiInfo?.frequency ?: 0
        val wifiBand = when {
            wifiFrequency in 2400..2500 -> getString(R.string.band_2_4_ghz)
            wifiFrequency in 4900..5900 -> getString(R.string.band_5_ghz)
            wifiFrequency in 5925..7125 -> getString(R.string.band_6_ghz)
            else -> getString(R.string.unknown)
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

        val wifiCardBase = WifiCardData(
            carrier = if (isWifi) getString(R.string.connected) else getString(R.string.wifi_label),
            title = getString(R.string.wifi_signal),
            band = wifiBand,
            quality = wifiQuality,
            dbm = wifiRssi,
            pingMs = null,
            connectedTo = ssid,
            linkSpeedMbps = linkSpeed
        )

        val wifiCard = wifiCardBase.copy(
            infoPopup = buildWifiInfoPopup(
                wifiInfo = wifiInfo,
                ssid = ssid,
                wifiFrequency = wifiFrequency,
                linkSpeed = linkSpeed
            )
        )

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
            ?: getString(R.string.cellular)

        val networkType = getSafeNetworkTypeLabel()

        val activeSubs = getActiveSubscriptions()

        val sim1Info = activeSubs.firstOrNull { it.simSlotIndex == 0 }
        val sim2Info = activeSubs.firstOrNull { it.simSlotIndex == 1 }

        val sim1Label = getString(R.string.sim_1)
        val sim2Label = getString(R.string.sim_2)

        val sim1 = sim1Info?.let {
            buildCellSignalDataForSubscription(it, sim1Label)
        } ?: buildNoSimData(sim1Label)

        val sim2 = sim2Info?.let {
            buildCellSignalDataForSubscription(it, sim2Label)
        } ?: buildNoSimData(sim2Label)

        val sortedScans = scanResults
            .sortedByDescending { it.level }
            .take(20)

        val currentWifiRows = sortedScans
            .filter { sameSsid(it.SSID, ssid) }
            .map { it.toChannelRow(this) }

        val interferenceRows = sortedScans
            .filterNot { sameSsid(it.SSID, ssid) }
            .filter { isOverlappingChannel(it.frequency, wifiFrequency) }
            .map { it.toChannelRow(this) }

        val otherRows = sortedScans
            .filterNot { sameSsid(it.SSID, ssid) }
            .filterNot { isOverlappingChannel(it.frequency, wifiFrequency) }
            .map { it.toChannelRow(this) }

        return SignalUiState(
            wifiCard = wifiCard,
            sim1 = sim1,
            sim2 = sim2,
            speedTest = speedTestState,
            channels = ChannelSectionData(
                currentWifi = if (currentWifiRows.isNotEmpty()) {
                    currentWifiRows
                } else {
                    listOf(
                        ChannelRowData(
                            getString(R.string.current),
                            ssid,
                            wifiQuality
                        )
                    )
                },
                interference = interferenceRows,
                otherNetworks = otherRows
            ),
            activeTransportLabel = when {
                isWifi -> getString(R.string.wifi_label)
                isCell -> getString(R.string.cellular)
                else -> getString(R.string.offline)
            }
        )
    }


    @SuppressLint("MissingPermission")
    private fun getActiveSubscriptions(): List<SubscriptionInfo> {
        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) return emptyList()

        val subscriptionManager =
            getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        return runCatching {
            subscriptionManager.activeSubscriptionInfoList.orEmpty()
        }.getOrDefault(emptyList())
    }

    @SuppressLint("MissingPermission")
    private fun buildCellSignalDataForSubscription(
        subInfo: SubscriptionInfo,
        simLabel: String
    ): CellSignalData {
        val telephonyForSub = telephonyManager.createForSubscriptionId(subInfo.subscriptionId)

        val carrierName = subInfo.carrierName?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: runCatching { telephonyForSub.simOperatorName }.getOrNull()
                ?.takeIf { it.isNotBlank() }
            ?: simLabel

        val networkType = runCatching {
            networkTypeLabel(telephonyForSub.dataNetworkType)
        }.getOrElse {
            runCatching { networkTypeLabel(telephonyForSub.networkType) }
                .getOrDefault(getString(R.string.unknown))
        }

        val signalStrength = runCatching { telephonyForSub.signalStrength }.getOrNull()

        val cellSignal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            signalStrength?.cellSignalStrengths?.firstOrNull()
        } else {
            null
        }

        val dbm = cellSignal?.dbm ?: 0
        val asu = cellSignal?.asuLevel ?: 0
        val quality = when (cellSignal?.level ?: 0) {
            0 -> SignalQuality.POOR
            1, 2 -> SignalQuality.GOOD
            else -> SignalQuality.EXCELLENT
        }

        val base = CellSignalData(
            carrier = carrierName,
            title = getString(R.string.cell_signal),
            simLabel = simLabel,
            networkType = networkType,
            quality = quality,
            asu = asu,
            dbm = dbm,
            pingMs = null,
            towerId = getString(R.string.dash)
        )

        return base.copy(
            infoPopup = buildCellInfoPopup(base)
        )
    }

    @SuppressLint("MissingPermission")
    private fun getSecondSimInfoOrNull(): SubscriptionInfo? {
        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) return null

        val subscriptionManager =
            getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        val activeSubs = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                subscriptionManager.activeSubscriptionInfoList
            } else {
                @Suppress("DEPRECATION")
                subscriptionManager.activeSubscriptionInfoList
            }
        }.getOrNull().orEmpty()

        // slotIndex 0 = SIM 1, slotIndex 1 = SIM 2
        return activeSubs.firstOrNull { it.simSlotIndex == 1 }
    }

    private fun buildNoSimData(simLabel: String): CellSignalData {
        val base = CellSignalData(
            carrier = getString(R.string.no_sim),
            title = getString(R.string.no_sim_title),
            simLabel = simLabel,
            networkType = getString(R.string.dash),
            quality = SignalQuality.POOR,
            asu = 0,
            dbm = 0,
            pingMs = null,
            towerId = getString(R.string.dash)
        )

        return base.copy(
            infoPopup = buildCellInfoPopup(base)
        )
    }
    @SuppressLint("MissingPermission")
    private fun buildWifiInfoPopup(
        wifiInfo: android.net.wifi.WifiInfo?,
        ssid: String,
        wifiFrequency: Int,
        linkSpeed: Int?
    ): WifiInfoPopupData {
        val dhcpInfo: DhcpInfo? = runCatching { wifiManager.dhcpInfo }.getOrNull()

        val ipAddress = dhcpInfo?.ipAddress?.toIpString() ?: getString(R.string.dash)
        val gateway = dhcpInfo?.gateway?.toIpString() ?: getString(R.string.dash)
        val dns1 = dhcpInfo?.dns1?.toIpString() ?: getString(R.string.dash)
        val dns2 = dhcpInfo?.dns2?.toIpString() ?: getString(R.string.dash)
        val dhcpServer = dhcpInfo?.serverAddress?.toIpString() ?: getString(R.string.dash)

        val routerMac = runCatching {
            wifiManager.scanResults
                .firstOrNull { it.SSID == ssid || "\"${it.SSID}\"" == wifiInfo?.ssid }
                ?.BSSID
        }.getOrNull() ?: wifiInfo?.bssid ?: getString(R.string.dash)

        return WifiInfoPopupData(
            wifiName = ssid,
            accessPoint = ssid,
            frequencyMHz = wifiFrequency,
            channel = frequencyToChannel(wifiFrequency),
            linkSpeedMbps = linkSpeed,
            is5GHzSupported = runCatching { wifiManager.is5GHzBandSupported }.getOrDefault(false),
            ipAddress = ipAddress,
            gateway = gateway,
            routerMac = routerMac,
            dns1 = dns1,
            dns2 = dns2,
            dhcpServer = dhcpServer
        )
    }

    private fun buildCellInfoPopup(
        data: CellSignalData
    ): CellInfoPopupData {
        return CellInfoPopupData(
            carrier = data.carrier,
            simLabel = data.simLabel,
            networkType = data.networkType,
            dbm = data.dbm,
            asu = data.asu,
            qualityLabel = when (data.quality) {
                SignalQuality.POOR -> getString(R.string.quality_poor)
                SignalQuality.GOOD -> getString(R.string.quality_good)
                SignalQuality.EXCELLENT -> getString(R.string.quality_excellent)
                SignalQuality.OK_ORANGE -> getString(R.string.quality_good)
            },
            operatorName = safeString(runCatching { telephonyManager.simOperatorName }.getOrNull(), this),
            countryIso = safeString(runCatching { telephonyManager.simCountryIso }.getOrNull(), this).uppercase(),
            roaming = runCatching { telephonyManager.isNetworkRoaming }.getOrDefault(false)
        )
    }

    @SuppressLint("MissingPermission")
    private fun getSafeNetworkTypeLabel(): String {
        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            return getString(R.string.unknown)
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

        val currentChannel = frequencyToChannel(currentFreq)
        val otherChannel = frequencyToChannel(otherFreq)

        val both24 = currentFreq in 2400..2500 && otherFreq in 2400..2500
        val both5 = currentFreq in 4900..5900 && otherFreq in 4900..5900

        return when {
            both24 -> kotlin.math.abs(currentChannel - otherChannel) <= 2
            both5 -> currentChannel == otherChannel
            else -> false
        }
    }

    private fun networkTypeLabel(type: Int): String {
        return when (type) {
            TelephonyManager.NETWORK_TYPE_NR -> getString(R.string.network_5g)
            TelephonyManager.NETWORK_TYPE_LTE -> getString(R.string.network_4g_lte)
            TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_UMTS -> getString(R.string.network_3g)
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_GSM -> getString(R.string.network_2g)
            else -> getString(R.string.unknown)
        }
    }
}

private fun ScanResult.toChannelRow(context: Context): ChannelRowData {
    val quality = when {
        level <= -85 -> SignalQuality.POOR
        level <= -70 -> SignalQuality.OK_ORANGE
        level <= -60 -> SignalQuality.GOOD
        else -> SignalQuality.EXCELLENT
    }

    return ChannelRowData(
        channel = context.getString(R.string.channel_format, frequencyToChannel(frequency)),
        name = SSID.ifBlank { context.getString(R.string.hidden_network) },
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

@Suppress("DEPRECATION")
private fun Int.toIpString(): String = Formatter.formatIpAddress(this)

private fun safeString(value: String?, context: Context): String {
    return value?.takeIf { it.isNotBlank() } ?: context.getString(R.string.dash)
}