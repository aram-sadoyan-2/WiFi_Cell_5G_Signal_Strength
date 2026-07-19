package com.algorithm.wificellgsignalstrength.ui.model

import com.algorithm.wificellgsignalstrength.data.model.WifiInfoPopupData

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
