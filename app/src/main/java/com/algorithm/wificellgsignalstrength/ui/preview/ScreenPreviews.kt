package com.algorithm.wificellgsignalstrength.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.algorithm.wificellgsignalstrength.data.mock.MockSignalData
import com.algorithm.wificellgsignalstrength.ui.screens.main.WifiCellSignalScreen

@Preview(name = "Main — full mock", showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenFullPreview() {
    WifiCellSignalScreen(
        state = MockSignalData.fullState,
        onRefresh = {},
        onGoClick = {},
        onResetSpeedTest = {},
        onSettingsClick = {}
    )
}

@Preview(name = "Main — speed result", showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenSpeedResultPreview() {
    WifiCellSignalScreen(
        state = MockSignalData.fullState.copy(speedTest = MockSignalData.speedResult),
        onRefresh = {},
        onGoClick = {},
        onResetSpeedTest = {},
        onSettingsClick = {}
    )
}
