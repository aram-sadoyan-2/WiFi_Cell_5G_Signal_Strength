package com.algorithm.wificell5gsignalstrength.ui

import androidx.compose.runtime.Immutable
import com.algorithm.wificell5gsignalstrength.CellInfoPopupData
import com.algorithm.wificell5gsignalstrength.WifiInfoPopupData


data class SignalUiState(
    val wifiCard: WifiCardData = WifiCardData(),
    val sim1: CellSignalData = CellSignalData(),
    val sim2: CellSignalData = CellSignalData(),
    val speedTest: SpeedCircleState = SpeedCircleState.Idle,
    val channels: ChannelSectionData = ChannelSectionData(),
    val activeTransportLabel: String = "Offline"
)

data class WifiCardData(
    val carrier: String = "Wi-Fi",
    val title: String = "WiFi Signal",
    val band: String = "Unknown",
    val quality: SignalQuality = SignalQuality.POOR,
    val dbm: Int = 0,
    val pingMs: Int? = null,
    val connectedTo: String = "Not connected",
    val linkSpeedMbps: Int? = null,
    val infoPopup: WifiInfoPopupData? = null
)

data class CellSignalData(
    val carrier: String = "Cellular",
    val title: String = "Cell Signal",
    val simLabel: String = "SIM 1",
    val networkType: String = "Unknown",
    val quality: SignalQuality = SignalQuality.POOR,
    val asu: Int = 0,
    val dbm: Int = 0,
    val pingMs: Int? = null,
    val towerId: String = "—",
    val infoPopup: CellInfoPopupData? = null
)

data class ChannelSectionData(
    val currentWifi: List<ChannelRowData> = emptyList(),
    val interference: List<ChannelRowData> = emptyList(),
    val otherNetworks: List<ChannelRowData> = emptyList()
)

data class ChannelRowData(
    val channel: String,
    val name: String,
    val quality: SignalQuality
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