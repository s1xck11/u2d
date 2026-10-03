package com.example.ebikedisplay

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.UUID

private const val TAG = "BleManager"

// UUID сервиса и характеристики (Nordic UART)
private val SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
private val TX_CHARACTERISTIC_UUID: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

// Имя нашего ESP32
private const val DEVICE_NAME = "EBike-Display"

class BleManager(
    private val context: Context,
    private val onData: (DashboardState) -> Unit
) {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null

    // Состояние дисплея — обновляется в messageHandler
    private var state = DashboardState()

    // Для расчёта мощности
    private var lastVoltage: Float = 0f
    private var lastTime: Long = 0L
    private var accumulatedWh: Float = 0f
    private var accumulatedKm: Float = 0f

    init {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        bluetoothAdapter = manager.adapter
    }

    fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun startScan() {
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) {
            Log.e(TAG, "Bluetooth выключен")
            return
        }

        val scanner = adapter.bluetoothLeScanner ?: return
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        updateStatus("Scanning...", false)

        scanner.startScan(null, settings, scanCallback)
        Log.d(TAG, "Сканирование запущено")

        // Останавливаем сканирование через 10 секунд
        Handler(Looper.getMainLooper()).postDelayed({
            scanner.stopScan(scanCallback)
            if (!state.bleConnected) {
                updateStatus("Not found", false)
            }
        }, 10000)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = try { device.name } catch (e: SecurityException) { null }

            Log.d(TAG, "Найдено: $name (${device.address})")

            if (name == DEVICE_NAME) {
                val scanner = bluetoothAdapter?.bluetoothLeScanner
                scanner?.stopScan(this)
                Log.d(TAG, "Нашли $DEVICE_NAME, подключаемся...")
                connect(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Ошибка сканирования: $errorCode")
            updateStatus("Scan error", false)
        }
    }

    private fun connect(device: BluetoothDevice) {
        try {
            updateStatus("Connecting...", false)
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "Нет разрешения на подключение", e)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Подключено, ищем сервисы...")
                    updateStatus("Connected", true)
                    try {
                        gatt.discoverServices()
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Ошибка discoverServices", e)
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Отключено")
                    updateStatus("Disconnected", false)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) return

            val service = gatt.getService(SERVICE_UUID)
            val characteristic = service?.getCharacteristic(TX_CHARACTERISTIC_UUID)

            if (characteristic == null) {
                Log.e(TAG, "Не нашли характеристику")
                return
            }

            try {
                // Включаем уведомления
                gatt.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(CCCD_UUID)
                if (descriptor != null) {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                    Log.d(TAG, "Уведомления включены")
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Ошибка включения уведомлений", e)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val data = characteristic.value?.toString(Charsets.UTF_8) ?: return
            Log.d(TAG, "Получено: $data")
            parseMessage(data)
        }
    }

    private fun parseMessage(message: String) {
        val parsed = mutableMapOf<String, String>()
        message.split(",").forEach { pair ->
            val kv = pair.split(":")
            if (kv.size == 2) parsed[kv[0].trim()] = kv[1].trim()
        }

        val speed = parsed["SPEED"]?.toFloatOrNull() ?: state.speed
        val voltage = parsed["VOLT"]?.toFloatOrNull() ?: state.voltage
        val battery = parsed["BAT"]?.toIntOrNull() ?: state.battery
        val range = parsed["RANGE"]?.toIntOrNull() ?: state.range
        val temp = parsed["TEMP"]?.toIntOrNull() ?: state.temperature
        val err = parsed["ERR"]?.toIntOrNull() ?: state.errorCode

        // Расчёт мощности по просадке напряжения
        val now = System.currentTimeMillis()
        var power = state.power
        if (lastTime > 0 && lastVoltage > 0f && speed > 1f) {
            val dt = (now - lastTime) / 1000f
            val sag = kotlin.math.abs(lastVoltage - voltage)
            val estimatedCurrent = sag * 10f
            power = voltage * estimatedCurrent
            accumulatedWh += power * dt / 3600f
            accumulatedKm += speed * dt / 3600f
        }
        lastVoltage = voltage
        lastTime = now

        val consumption = if (accumulatedKm > 0.01f) accumulatedWh / accumulatedKm else state.consumption
        val health = ((voltage / 58.4f) * 100).coerceIn(0f, 100f).toInt()

        val mode = when {
            speed < 0.5f -> "P"
            speed < 25f -> "Eco"
            else -> "Sport"
        }

        state = state.copy(
            speed = speed,
            voltage = voltage,
            battery = battery,
            range = range,
            temperature = temp,
            errorCode = err,
            mode = mode,
            power = power,
            consumption = consumption,
            batteryHealth = health
        )
        onData(state)
    }

    private fun updateStatus(text: String, connected: Boolean) {
        state = state.copy(bleStatus = text, bleConnected = connected)
        onData(state)
    }

    fun disconnect() {
        try {
            bluetoothGatt?.close()
            bluetoothGatt = null
        } catch (e: SecurityException) {
            Log.e(TAG, "Ошибка отключения", e)
        }
    }
}