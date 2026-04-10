package com.algorithm.wificellgsignalstrength.ui

fun speedToProgress(mbps: Float): Float {
    val maxGaugeSpeed = 300f
    return (mbps / maxGaugeSpeed).coerceIn(0f, 1f)
}