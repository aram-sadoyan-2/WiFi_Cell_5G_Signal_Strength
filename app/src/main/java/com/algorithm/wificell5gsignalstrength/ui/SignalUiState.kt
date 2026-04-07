package com.algorithm.wificell5gsignalstrength.ui

import androidx.compose.runtime.Immutable

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

    data class DownloadResult(
        val downloadMbps: Float,
        val pingMs: Int
    ) : SpeedCircleState

    data class UploadResult(
        val uploadMbps: Float,
        val pingMs: Int
    ) : SpeedCircleState
}