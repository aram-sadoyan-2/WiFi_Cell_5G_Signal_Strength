package com.algorithm.wificellgsignalstrength.ui.model

sealed interface SpeedCircleState {
    data object Idle : SpeedCircleState

    data class Downloading(
        val downloadMbps: Float,
        val pingMs: Int
    ) : SpeedCircleState

    data class Uploading(
        val uploadMbps: Float,
        val pingMs: Int
    ) : SpeedCircleState

    data class FinalResult(
        val downloadMbps: Float,
        val uploadMbps: Float,
        val pingMs: Int
    ) : SpeedCircleState
}
