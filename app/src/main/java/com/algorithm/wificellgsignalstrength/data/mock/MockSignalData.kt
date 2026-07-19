package com.algorithm.wificellgsignalstrength.data.mock

import com.algorithm.wificellgsignalstrength.data.model.CellInfoPopupData
import com.algorithm.wificellgsignalstrength.data.model.WifiInfoPopupData
import com.algorithm.wificellgsignalstrength.ui.model.CellSignalData
import com.algorithm.wificellgsignalstrength.ui.model.ChannelRowData
import com.algorithm.wificellgsignalstrength.ui.model.ChannelSectionData
import com.algorithm.wificellgsignalstrength.ui.model.SignalQuality
import com.algorithm.wificellgsignalstrength.ui.model.SignalUiState
import com.algorithm.wificellgsignalstrength.ui.model.SpeedCircleState
import com.algorithm.wificellgsignalstrength.ui.model.WifiCardData

object MockSignalData {

    val wifiPopup = WifiInfoPopupData(
        wifiName = "HomeNet_5G",
        accessPoint = "HomeNet_5G",
        frequencyMHz = 5180,
        channel = 36,
        linkSpeedMbps = 866,
        is5GHzSupported = true,
        ipAddress = "192.168.1.24",
        gateway = "192.168.1.1",
        routerMac = "A4:2B:8C:15:9F:03",
        dns1 = "192.168.1.1",
        dns2 = "8.8.8.8",
        dhcpServer = "192.168.1.1"
    )

    val wifiCard = WifiCardData(
        carrier = "Connected",
        title = "WiFi Signal",
        band = "5 GHz",
        quality = SignalQuality.EXCELLENT,
        dbm = -42,
        pingMs = null,
        connectedTo = "HomeNet_5G",
        linkSpeedMbps = 866,
        infoPopup = wifiPopup
    )

    val sim1 = CellSignalData(
        carrier = "Beeline",
        title = "Cell Signal",
        simLabel = "SIM 1",
        networkType = "5G",
        quality = SignalQuality.EXCELLENT,
        asu = 25,
        dbm = -78,
        pingMs = 32,
        towerId = "12345",
        infoPopup = CellInfoPopupData(
            carrier = "Beeline",
            simLabel = "SIM 1",
            networkType = "5G",
            dbm = -78,
            asu = 25,
            qualityLabel = "Excellent",
            operatorName = "Beeline AM",
            countryIso = "AM",
            roaming = false
        )
    )

    val sim2 = CellSignalData(
        carrier = "Ucom",
        title = "Cell Signal",
        simLabel = "SIM 2",
        networkType = "4G LTE",
        quality = SignalQuality.GOOD,
        asu = 15,
        dbm = -95,
        pingMs = 48,
        towerId = "67890",
        infoPopup = CellInfoPopupData(
            carrier = "Ucom",
            simLabel = "SIM 2",
            networkType = "4G LTE",
            dbm = -95,
            asu = 15,
            qualityLabel = "Good",
            operatorName = "Ucom LLC",
            countryIso = "AM",
            roaming = true
        )
    )

    val channels = ChannelSectionData(
        currentWifi = listOf(
            ChannelRowData("Ch 36", "HomeNet_5G", SignalQuality.EXCELLENT),
            ChannelRowData("Ch 6", "HomeNet_2.4G", SignalQuality.GOOD)
        ),
        interference = listOf(
            ChannelRowData("Ch 36", "Neighbor_5G", SignalQuality.OK_ORANGE),
            ChannelRowData("Ch 36", "TP-Link_Guest", SignalQuality.POOR)
        ),
        otherNetworks = listOf(
            ChannelRowData("Ch 1", "AndroidAP_2841", SignalQuality.GOOD),
            ChannelRowData("Ch 11", "CafeFreeWiFi", SignalQuality.OK_ORANGE),
            ChannelRowData("Ch 44", "Office_Guest", SignalQuality.POOR),
            ChannelRowData("Ch 149", "Starlink_A9F2", SignalQuality.EXCELLENT)
        )
    )

    val fullState = SignalUiState(
        wifiCard = wifiCard,
        sim1 = sim1,
        sim2 = sim2,
        speedTest = SpeedCircleState.Idle,
        channels = channels,
        activeTransportLabel = "Wi-Fi"
    )

    val speedResult = SpeedCircleState.FinalResult(
        downloadMbps = 284.6f,
        uploadMbps = 92.3f,
        pingMs = 12
    )
}
