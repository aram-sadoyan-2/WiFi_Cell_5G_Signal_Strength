package com.algorithm.wificell5gsignalstrength

sealed interface NetworkInfoPopupData {
    val title: String
}

data class WifiInfoPopupData(
    override val title: String = "Network Information",
    val wifiName: String,
    val accessPoint: String,
    val frequencyMHz: Int,
    val channel: Int,
    val linkSpeedMbps: Int?,
    val is5GHzSupported: Boolean,
    val ipAddress: String,
    val gateway: String,
    val routerMac: String,
    val dns1: String,
    val dns2: String,
    val dhcpServer: String
) : NetworkInfoPopupData

data class CellInfoPopupData(
    override val title: String = "Network Information",
    val carrier: String,
    val simLabel: String,
    val networkType: String,
    val dbm: Int,
    val asu: Int,
    val qualityLabel: String,
    val operatorName: String,
    val countryIso: String,
    val roaming: Boolean
) : NetworkInfoPopupData