package com.algorithm.wificellgsignalstrength.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.DhcpInfo
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.SignalStrength
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.text.format.Formatter
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.glance.appwidget.updateAll
import com.algorithm.wificellgsignalstrength.R
import com.algorithm.wificellgsignalstrength.data.mock.MockSignalData
import com.algorithm.wificellgsignalstrength.data.model.CellInfoPopupData
import com.algorithm.wificellgsignalstrength.data.model.WifiInfoPopupData
import com.algorithm.wificellgsignalstrength.data.prefs.EncryptedSpeedTestStore
import com.algorithm.wificellgsignalstrength.data.prefs.SavedSpeedTestResult
import com.algorithm.wificellgsignalstrength.data.speedtest.RealSpeedTestResult
import com.algorithm.wificellgsignalstrength.ui.model.CellSignalData
import com.algorithm.wificellgsignalstrength.ui.model.ChannelRowData
import com.algorithm.wificellgsignalstrength.ui.model.ChannelSectionData
import com.algorithm.wificellgsignalstrength.ui.model.SignalQuality
import com.algorithm.wificellgsignalstrength.ui.model.SignalUiState
import com.algorithm.wificellgsignalstrength.ui.model.SpeedCircleState
import com.algorithm.wificellgsignalstrength.ui.model.WifiCardData
import com.algorithm.wificellgsignalstrength.widget.SpeedTestWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wifiManager: WifiManager,
    private val telephonyManager: TelephonyManager,
    private val connectivityManager: ConnectivityManager,
    private val encryptedSpeedTestStore: EncryptedSpeedTestStore
) {

    private var latestSignalStrength: SignalStrength? = null

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val useMockData: Boolean
        get() = FORCE_MOCK_DATA || isRunningOnEmulator()

    fun requestRefresh() {
        refreshTrigger.tryEmit(Unit)
    }

    fun signalUpdates(): Flow<SignalUiState> = callbackFlow {
        trySend(currentSnapshot())

        val wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                trySend(currentSnapshot())
            }
        }
        ContextCompat.registerReceiver(
            context,
            wifiReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        var telephonyCallback: TelephonyCallback? = null
        registerTelephony(
            onCallbackCreated = { telephonyCallback = it },
            onUpdate = { trySend(currentSnapshot()) }
        )

        val pollJob = launch {
            while (isActive) {
                if (hasWifiScanPermission()) {
                    runCatching { wifiManager.startScan() }
                }
                trySend(currentSnapshot())
                delay(REFRESH_INTERVAL_MS)
            }
        }

        val triggerJob = launch {
            refreshTrigger.collect { trySend(currentSnapshot()) }
        }

        awaitClose {
            pollJob.cancel()
            triggerJob.cancel()
            runCatching { context.unregisterReceiver(wifiReceiver) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyCallback?.let {
                    runCatching { telephonyManager.unregisterTelephonyCallback(it) }
                }
            }
        }
    }

    fun currentSnapshot(): SignalUiState {
        if (useMockData) {
            return MockSignalData.fullState
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return SignalUiState()
        }
        return buildSignalUiState()
    }

    suspend fun saveSpeedTestResult(result: RealSpeedTestResult) {
        encryptedSpeedTestStore.save(
            SavedSpeedTestResult(
                downloadMbps = result.downloadMbps,
                uploadMbps = result.uploadMbps,
                pingMs = result.pingMs,
                testedAtEpochMs = System.currentTimeMillis()
            )
        )
        persistWidgetSpeedResult(result.uploadMbps, result.pingMs)
    }

    suspend fun loadLastSpeedTestResult(): SavedSpeedTestResult? =
        encryptedSpeedTestStore.load()

    private fun persistWidgetSpeedResult(uploadMbps: Float, pingMs: Int) {
        val prefs = context.getSharedPreferences("speed_widget_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("speed_value", String.format("%.1f", uploadMbps))
            putString("speed_unit", "Mbps")
            putString("ping_value", "$pingMs ms")
        }
    }

    suspend fun updateSpeedWidget() {
        runCatching { SpeedTestWidget().updateAll(context) }
    }

    @SuppressLint("MissingPermission")
    private fun registerTelephony(
        onCallbackCreated: (TelephonyCallback) -> Unit,
        onUpdate: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            latestSignalStrength = null
            return
        }

        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            latestSignalStrength = null
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val callback = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    latestSignalStrength = signalStrength
                    onUpdate()
                }
            }
            onCallbackCreated(callback)

            runCatching {
                telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
            }

            latestSignalStrength = runCatching { telephonyManager.signalStrength }.getOrNull()
        } else {
            latestSignalStrength = runCatching { telephonyManager.signalStrength }.getOrNull()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
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
            ?: context.getString(R.string.not_connected)

        val wifiRssi = wifiInfo?.rssi ?: -127
        val wifiFrequency = wifiInfo?.frequency ?: 0
        val wifiBand = when {
            wifiFrequency in 2400..2500 -> context.getString(R.string.band_2_4_ghz)
            wifiFrequency in 4900..5900 -> context.getString(R.string.band_5_ghz)
            wifiFrequency in 5925..7125 -> context.getString(R.string.band_6_ghz)
            else -> context.getString(R.string.unknown)
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
            carrier = if (isWifi) context.getString(R.string.connected) else context.getString(R.string.wifi_label),
            title = context.getString(R.string.wifi_signal),
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

        val sim1Label = context.getString(R.string.sim_1)
        val sim2Label = context.getString(R.string.sim_2)

        val activeSubs = getActiveSubscriptions()
        val sim1Info = activeSubs.firstOrNull { it.simSlotIndex == 0 }
        val sim2Info = activeSubs.firstOrNull { it.simSlotIndex == 1 }

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
            .map { it.toChannelRow(context) }

        val interferenceRows = sortedScans
            .filterNot { sameSsid(it.SSID, ssid) }
            .filter { isOverlappingChannel(it.frequency, wifiFrequency) }
            .map { it.toChannelRow(context) }

        val otherRows = sortedScans
            .filterNot { sameSsid(it.SSID, ssid) }
            .filterNot { isOverlappingChannel(it.frequency, wifiFrequency) }
            .map { it.toChannelRow(context) }

        return SignalUiState(
            wifiCard = wifiCard,
            sim1 = sim1,
            sim2 = sim2,
            speedTest = SpeedCircleState.Idle,
            channels = ChannelSectionData(
                currentWifi = if (currentWifiRows.isNotEmpty()) {
                    currentWifiRows
                } else {
                    listOf(
                        ChannelRowData(
                            context.getString(R.string.current),
                            ssid,
                            wifiQuality
                        )
                    )
                },
                interference = interferenceRows,
                otherNetworks = otherRows
            ),
            activeTransportLabel = when {
                isWifi -> context.getString(R.string.wifi_label)
                isCell -> context.getString(R.string.cellular)
                else -> context.getString(R.string.offline)
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun getActiveSubscriptions(): List<SubscriptionInfo> {
        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) return emptyList()

        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        return runCatching {
            subscriptionManager.activeSubscriptionInfoList.orEmpty()
        }.getOrDefault(emptyList())
    }

    @RequiresApi(Build.VERSION_CODES.P)
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
                .getOrDefault(context.getString(R.string.unknown))
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
            title = context.getString(R.string.cell_signal),
            simLabel = simLabel,
            networkType = networkType,
            quality = quality,
            asu = asu,
            dbm = dbm,
            pingMs = null,
            towerId = context.getString(R.string.dash)
        )

        return base.copy(infoPopup = buildCellInfoPopup(base))
    }

    private fun buildNoSimData(simLabel: String): CellSignalData {
        val base = CellSignalData(
            carrier = context.getString(R.string.no_sim),
            title = context.getString(R.string.no_sim_title),
            simLabel = simLabel,
            networkType = context.getString(R.string.dash),
            quality = SignalQuality.POOR,
            asu = 0,
            dbm = 0,
            pingMs = null,
            towerId = context.getString(R.string.dash)
        )

        return base.copy(infoPopup = buildCellInfoPopup(base))
    }

    @SuppressLint("MissingPermission")
    private fun buildWifiInfoPopup(
        wifiInfo: WifiInfo?,
        ssid: String,
        wifiFrequency: Int,
        linkSpeed: Int?
    ): WifiInfoPopupData {
        val dhcpInfo: DhcpInfo? = runCatching { wifiManager.dhcpInfo }.getOrNull()

        val ipAddress = dhcpInfo?.ipAddress?.toIpString() ?: context.getString(R.string.dash)
        val gateway = dhcpInfo?.gateway?.toIpString() ?: context.getString(R.string.dash)
        val dns1 = dhcpInfo?.dns1?.toIpString() ?: context.getString(R.string.dash)
        val dns2 = dhcpInfo?.dns2?.toIpString() ?: context.getString(R.string.dash)
        val dhcpServer = dhcpInfo?.serverAddress?.toIpString() ?: context.getString(R.string.dash)

        val routerMac = runCatching {
            wifiManager.scanResults
                .firstOrNull { it.SSID == ssid || "\"${it.SSID}\"" == wifiInfo?.ssid }
                ?.BSSID
        }.getOrNull() ?: wifiInfo?.bssid ?: context.getString(R.string.dash)

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

    private fun buildCellInfoPopup(data: CellSignalData): CellInfoPopupData {
        return CellInfoPopupData(
            carrier = data.carrier,
            simLabel = data.simLabel,
            networkType = data.networkType,
            dbm = data.dbm,
            asu = data.asu,
            qualityLabel = when (data.quality) {
                SignalQuality.POOR -> context.getString(R.string.quality_poor)
                SignalQuality.GOOD -> context.getString(R.string.quality_good)
                SignalQuality.EXCELLENT -> context.getString(R.string.quality_excellent)
                SignalQuality.OK_ORANGE -> context.getString(R.string.quality_good)
            },
            operatorName = safeString(runCatching { telephonyManager.simOperatorName }.getOrNull()),
            countryIso = safeString(runCatching { telephonyManager.simCountryIso }.getOrNull()).uppercase(),
            roaming = runCatching { telephonyManager.isNetworkRoaming }.getOrDefault(false)
        )
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
            TelephonyManager.NETWORK_TYPE_NR -> context.getString(R.string.network_5g)
            TelephonyManager.NETWORK_TYPE_LTE -> context.getString(R.string.network_4g_lte)
            TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_UMTS -> context.getString(R.string.network_3g)
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_GSM -> context.getString(R.string.network_2g)
            else -> context.getString(R.string.unknown)
        }
    }

    private fun hasWifiScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
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

    private fun safeString(value: String?): String {
        return value?.takeIf { it.isNotBlank() } ?: context.getString(R.string.dash)
    }

    private fun isRunningOnEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT ?: ""
        val model = Build.MODEL ?: ""
        val product = Build.PRODUCT ?: ""
        val hardware = Build.HARDWARE ?: ""
        val brand = Build.BRAND ?: ""
        val device = Build.DEVICE ?: ""

        return fingerprint.startsWith("generic")
            || fingerprint.startsWith("unknown")
            || fingerprint.contains("emulator", ignoreCase = true)
            || model.contains("google_sdk")
            || model.contains("Emulator")
            || model.contains("Android SDK built for")
            || product.contains("sdk")
            || product.contains("emulator")
            || hardware.contains("goldfish")
            || hardware.contains("ranchu")
            || (brand.startsWith("generic") && device.startsWith("generic"))
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 5000L
        const val FORCE_MOCK_DATA = false
    }
}
