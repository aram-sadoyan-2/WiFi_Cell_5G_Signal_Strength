package com.algorithm.wificellgsignalstrength.data.prefs

data class SavedSpeedTestResult(
    val downloadMbps: Float,
    val uploadMbps: Float,
    val pingMs: Int,
    val testedAtEpochMs: Long
)
