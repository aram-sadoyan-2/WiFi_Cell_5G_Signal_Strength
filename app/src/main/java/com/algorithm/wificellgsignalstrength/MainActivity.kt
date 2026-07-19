package com.algorithm.wificellgsignalstrength

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.algorithm.wificellgsignalstrength.ui.screens.main.WifiCellSignalScreen
import com.algorithm.wificellgsignalstrength.ui.viewmodel.SignalViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_RUN_SPEED_TEST = "extra_run_speed_test"
    }

    private val viewModel: SignalViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNeededPermissions()

        val shouldRunSpeedTest = intent?.getBooleanExtra(EXTRA_RUN_SPEED_TEST, false) == true

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            WifiCellSignalScreen(
                state = state,
                onRefresh = { viewModel.refresh() },
                onGoClick = { viewModel.runSpeedTest() },
                onResetSpeedTest = { viewModel.resetSpeedTest() },
                onSettingsClick = {
                    startActivity(Intent(this, SettingsActivity::class.java))
                },
                openSettingsFromWidget = false
            )
        }

        if (shouldRunSpeedTest) {
            viewModel.runSpeedTest()
            intent?.removeExtra(EXTRA_RUN_SPEED_TEST)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent.getBooleanExtra(EXTRA_RUN_SPEED_TEST, false)) {
            viewModel.runSpeedTest()
            intent.removeExtra(EXTRA_RUN_SPEED_TEST)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.refresh()
    }

    private fun requestNeededPermissions() {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_WIFI_STATE)
            add(Manifest.permission.ACCESS_NETWORK_STATE)
            add(Manifest.permission.READ_PHONE_STATE)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }.distinct()

        val missing = permissions.filterNot { hasPermission(it) }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}
