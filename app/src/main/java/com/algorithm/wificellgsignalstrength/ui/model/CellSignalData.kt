package com.algorithm.wificellgsignalstrength.ui.model

import com.algorithm.wificellgsignalstrength.data.model.CellInfoPopupData

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
