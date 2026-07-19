package com.algorithm.wificellgsignalstrength.ui.model

data class SignalUiState(
    val wifiCard: WifiCardData = WifiCardData(),
    val sim1: CellSignalData = CellSignalData(),
    val sim2: CellSignalData = CellSignalData(),
    val speedTest: SpeedCircleState = SpeedCircleState.Idle,
    val channels: ChannelSectionData = ChannelSectionData(),
    val activeTransportLabel: String = "Offline"
)
