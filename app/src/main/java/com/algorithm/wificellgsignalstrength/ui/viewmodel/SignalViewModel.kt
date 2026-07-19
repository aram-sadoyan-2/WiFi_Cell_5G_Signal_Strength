package com.algorithm.wificellgsignalstrength.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithm.wificellgsignalstrength.data.repository.SignalRepository
import com.algorithm.wificellgsignalstrength.data.speedtest.RealSpeedTester
import com.algorithm.wificellgsignalstrength.ui.model.SignalUiState
import com.algorithm.wificellgsignalstrength.ui.model.SpeedCircleState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignalViewModel @Inject constructor(
    private val repository: SignalRepository,
    private val speedTester: RealSpeedTester
) : ViewModel() {

    private val speedTestState = MutableStateFlow<SpeedCircleState>(SpeedCircleState.Idle)

    val uiState: StateFlow<SignalUiState> =
        combine(repository.signalUpdates(), speedTestState) { base, speed ->
            base.copy(speedTest = speed)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = repository.currentSnapshot()
        )

    private var speedTestJob: Job? = null

    init {
        viewModelScope.launch {
            speedTestState.value = restoredSpeedState()
        }
    }

    fun refresh() {
        repository.requestRefresh()
    }

    fun runSpeedTest() {
        speedTestJob?.cancel()
        speedTestJob = viewModelScope.launch {
            try {
                val result = speedTester.run(
                    onDownloadProgress = { mbps, ping ->
                        speedTestState.value = SpeedCircleState.Downloading(
                            downloadMbps = mbps,
                            pingMs = ping
                        )
                    },
                    onUploadProgress = { mbps, ping ->
                        speedTestState.value = SpeedCircleState.Uploading(
                            uploadMbps = mbps,
                            pingMs = ping
                        )
                    }
                )

                speedTestState.value = SpeedCircleState.FinalResult(
                    downloadMbps = result.downloadMbps,
                    uploadMbps = result.uploadMbps,
                    pingMs = result.pingMs
                )

                repository.saveSpeedTestResult(result)
                repository.updateSpeedWidget()
            } catch (_: Exception) {
                speedTestState.value = restoredSpeedState()
            }
        }
    }

    fun resetSpeedTest() {
        speedTestJob?.cancel()
        speedTestJob = null
        speedTestState.value = SpeedCircleState.Idle
    }

    private suspend fun restoredSpeedState(): SpeedCircleState {
        val saved = repository.loadLastSpeedTestResult() ?: return SpeedCircleState.Idle
        return SpeedCircleState.FinalResult(
            downloadMbps = saved.downloadMbps,
            uploadMbps = saved.uploadMbps,
            pingMs = saved.pingMs
        )
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5000L
    }
}
