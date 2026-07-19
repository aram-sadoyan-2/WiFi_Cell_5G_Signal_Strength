package com.algorithm.wificellgsignalstrength.ui.model

data class ChannelSectionData(
    val currentWifi: List<ChannelRowData> = emptyList(),
    val interference: List<ChannelRowData> = emptyList(),
    val otherNetworks: List<ChannelRowData> = emptyList()
)
