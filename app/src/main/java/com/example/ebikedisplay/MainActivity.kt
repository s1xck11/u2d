package com.example.ebikedisplay

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private lateinit var bleManager: BleManager
    private var webServer: WebServer? = null
    private var state by mutableStateOf(DashboardState())

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    private val trips = mutableListOf<Trip>()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            bleManager.startScan()
        } else {
            state = state.copy(bleStatus = "No permission", bleConnected = false)
        }
    }

    private val adminLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, ScreenAdminReceiver::class.java)

        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Нужно для блокировки экрана при выключении скутера"
                )
            }
            adminLauncher.launch(intent)
        }

        bleManager = BleManager(applicationContext) { newState ->
            runOnUiThread {
                val wasPowered = state.powered
                state = newState
                if (!wasPowered && newState.powered) {
                    turnScreenOn()
                }
                if (wasPowered && !newState.powered) {
                    lockScreen()
                }
            }
        }

        // Запускаем веб-сервер (с принудительным перебором портов)
        startWebServer(8080)

        if (bleManager.hasPermissions()) {
            bleManager.autoConnect()
        } else {
            requestBlePermissionsAndScan()
        }

        setContent {
            MaterialTheme {
                DashboardScreen(
                    state = state,
                    onConnectClick = { requestBlePermissionsAndScan() }
                )
            }
        }
    }

    private fun startWebServer(port: Int) {
        try {
            webServer = WebServer(
                port = port,
                stateProvider = { state },
                tripsProvider = { trips }
            )
            webServer?.start()
            Log.d(TAG, "WebServer STARTED on port $port")
        } catch (e: Exception) {
            Log.e(TAG, "WebServer FAILED on port $port", e)
            if (port < 8090) {
                Log.d(TAG, "Пробуем порт ${port + 1}...")
                startWebServer(port + 1)
            }
        }
    }

    private fun requestBlePermissionsAndScan() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        permissionLauncher.launch(permissions)
    }

    private fun lockScreen() {
        try {
            if (devicePolicyManager.isAdminActive(adminComponent)) {
                devicePolicyManager.lockNow()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    private fun turnScreenOn() {
        try {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "EBikeDisplay::WakeLock"
            )
            wakeLock.acquire(3000L)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager.disconnect()
        try {
            webServer?.stop()
            Log.d(TAG, "WebServer stopped")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}