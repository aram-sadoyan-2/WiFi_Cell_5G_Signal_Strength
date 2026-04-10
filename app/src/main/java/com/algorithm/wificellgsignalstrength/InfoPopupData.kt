package com.algorithm.wificellgsignalstrength

data class InfoPopupData(
    val title: String,
    val headerValue: String,
    val rows: List<InfoPopupRow>,
    val iconType: InfoPopupIconType = InfoPopupIconType.WIFI
)

data class InfoPopupRow(
    val label: String,
    val value: String,
    val boldValue: Boolean = true
)

enum class InfoPopupIconType {
    WIFI,
    CELL
}