package com.algorithm.wificellgsignalstrength.data.speedtest

data class RealSpeedTestResult(
    val pingMs: Int,
    val downloadMbps: Float,
    val uploadMbps: Float
)
