//package com.example.connectwatch
//
//import android.Manifest
//import android.bluetooth.*
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.widget.Button
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import java.util.UUID
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var statusTextView: TextView
//    private lateinit var checkButton: Button
//    private lateinit var dataTextView: TextView
//
//    private lateinit var heartRateTextView: TextView
//    private lateinit var spo2TextView: TextView
//    private lateinit var stepsTextView: TextView
//    private lateinit var caloriesTextView: TextView
//    private lateinit var sleepTextView: TextView
//    private lateinit var temperatureTextView: TextView
//
//    private lateinit var bluetoothAdapter: BluetoothAdapter
//    private var bluetoothGatt: BluetoothGatt? = null
//
//    companion object {
//        private const val REQUEST_BLUETOOTH_PERMISSIONS = 1
//        private const val WATCH_NAME = "FireBoltt 039"  // Replace with your device name
//
//        // Example Heart Rate Service and Characteristic UUIDs
//        private val SERVICE_UUID: UUID =
//            UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
//        private val CHARACTERISTIC_UUID: UUID =
//            UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
//        private val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID =
//            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
//    }
//
//    private val gattCallback = object : BluetoothGattCallback() {
//        override fun onConnectionStateChange(
//            gatt: BluetoothGatt,
//            status: Int,
//            newState: Int
//        ) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    runOnUiThread {
//                        statusTextView.text = "Connected. Discovering services..."
//                        dataTextView.text = ""
//                    }
//                    gatt.discoverServices()
//                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                    runOnUiThread {
//                        statusTextView.text = "Disconnected from device."
//                        dataTextView.text = ""
//                    }
//                    bluetoothGatt?.close()
//                    bluetoothGatt = null
//                }
//            } else {
//                runOnUiThread {
//                    statusTextView.text = "Connection error: $status"
//                    dataTextView.text = ""
//                }
//                bluetoothGatt?.close()
//                bluetoothGatt = null
//            }
//        }
//
//        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                val service = gatt.getService(SERVICE_UUID)
//                if (service != null) {
//                    val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
//                    if (characteristic != null) {
//                        val notificationSet =
//                            gatt.setCharacteristicNotification(characteristic, true)
//                        runOnUiThread {
//                            statusTextView.text =
//                                "Notifications ${if (notificationSet) "enabled" else "not enabled"}"
//                        }
//                        val descriptor =
//                            characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
//                        descriptor?.let {
//                            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                            gatt.writeDescriptor(it)
//                        }
//                        gatt.readCharacteristic(characteristic)
//                    } else {
//                        runOnUiThread { statusTextView.text = "Characteristic not found." }
//                    }
//                } else {
//                    runOnUiThread { statusTextView.text = "Service not found." }
//                }
//            } else {
//                runOnUiThread { statusTextView.text = "Service discovery failed: $status" }
//            }
//        }
//
//        override fun onCharacteristicRead(
//            gatt: BluetoothGatt,
//            characteristic: BluetoothGattCharacteristic,
//            status: Int
//        ) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                handleSensorData(characteristic.value)
//            }
//        }
//
//        override fun onCharacteristicChanged(
//            gatt: BluetoothGatt,
//            characteristic: BluetoothGattCharacteristic
//        ) {
//            handleSensorData(characteristic.value)
//        }
//    }
//
//    private fun handleSensorData(data: ByteArray?) {
//        if (data == null || data.isEmpty()) return
//
//        try {
//            val flags = data[0].toInt()
//            val isHeartRateUInt16 = flags and 0x01 != 0
//
//            val heartRate = if (isHeartRateUInt16) {
//                // 16-bit format
//                ((data[1].toInt() and 0xFF) or ((data[2].toInt() and 0xFF) shl 8))
//            } else {
//                // 8-bit format
//                data[1].toInt() and 0xFF
//            }
//
//            runOnUiThread {
//                heartRateTextView.text = "Heart Rate: $heartRate bpm"
//                // Leave others empty / placeholder (since this char only contains HRM)
//                spo2TextView.text = "SpO₂: Not Available"
//                stepsTextView.text = "Steps: Not Available"
//                caloriesTextView.text = "Calories: Not Available"
//                sleepTextView.text = "Sleep: Not Available"
//                temperatureTextView.text = "Temperature: Not Available"
//                dataTextView.text = "Current Reading: ${data.getOrNull(1)?.toUByte() ?: "N/A"}"
//
////                dataTextView.text = "Raw: ${data.joinToString(" ") { it.toUByte().toString() }}"
//            }
//        } catch (e: Exception) {
//            runOnUiThread {
//                statusTextView.text = "Error parsing data"
//                dataTextView.text = "Current Reading: ${data.getOrNull(1)?.toUByte() ?: "N/A"}"
//
////                dataTextView.text = "Raw: ${data.joinToString(" ") { it.toUByte().toString() }}"
//            }
//        }
//    }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        statusTextView = findViewById(R.id.tvStatus)
//        checkButton = findViewById(R.id.btnCheckStatus)
//        dataTextView = findViewById(R.id.tvData)
//
//        heartRateTextView = findViewById(R.id.tvHeartRate)
//        spo2TextView = findViewById(R.id.tvSpo2)
//        stepsTextView = findViewById(R.id.tvSteps)
//        caloriesTextView = findViewById(R.id.tvCalories)
//        sleepTextView = findViewById(R.id.tvSleep)
//        temperatureTextView = findViewById(R.id.tvTemperature)
//
//        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
//        bluetoothAdapter = bluetoothManager.adapter
//
//        checkButton.setOnClickListener {
//            if (bluetoothAdapter.isEnabled) {
//                connectToWatch()
//            } else {
//                statusTextView.text = "Please enable Bluetooth"
//            }
//        }
//
//        requestBluetoothPermissions()
//    }
//
//    private fun connectToWatch() {
//        bluetoothGatt?.close()
//        bluetoothGatt = null
//
//        val pairedDevice = bluetoothAdapter.bondedDevices.find {
//            it.name.equals(WATCH_NAME, ignoreCase = true)
//        }
//
//        if (pairedDevice == null) {
//            runOnUiThread {
//                statusTextView.text = "Device \"$WATCH_NAME\" not paired or found"
//                dataTextView.text = ""
//            }
//            return
//        }
//
//        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            pairedDevice.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
//        } else {
//            pairedDevice.connectGatt(this, false, gattCallback)
//        }
//        statusTextView.text = "Connecting to ${pairedDevice.name}..."
//    }
//
//    private fun requestBluetoothPermissions() {
//        val permissionsToRequest = mutableListOf<String>()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
//            }
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.BLUETOOTH_SCAN
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
//            }
//        } else {
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
//            }
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.BLUETOOTH
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissionsToRequest.add(Manifest.permission.BLUETOOTH)
//            }
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.BLUETOOTH_ADMIN
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
//            }
//        }
//        if (permissionsToRequest.isNotEmpty()) {
//            ActivityCompat.requestPermissions(
//                this,
//                permissionsToRequest.toTypedArray(),
//                REQUEST_BLUETOOTH_PERMISSIONS
//            )
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
//            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
//                statusTextView.text = "Permissions granted"
//            } else {
//                statusTextView.text = "Permissions denied"
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        bluetoothGatt?.close()
//        bluetoothGatt = null
//    }
//}
package com.example.connectwatch

import android.Manifest
import android.bluetooth.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.UUID
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var checkButton: Button
    private lateinit var dataTextView: TextView

    private lateinit var heartRateTextView: TextView
    private lateinit var spo2TextView: TextView
    private lateinit var bloodPressureTextView: TextView
    private lateinit var stepsTextView: TextView
    private lateinit var caloriesTextView: TextView
    private lateinit var sleepTextView: TextView
    private lateinit var temperatureTextView: TextView

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null

    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 1
        private const val WATCH_NAME = "FireBoltt 039"  // Replace with your device name

        // --- Heart Rate (0x180D / 0x2A37) ---
        private val HR_SERVICE_UUID: UUID =
            UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        private val HR_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

        // --- SpO2 (Pulse Oximeter Service 0x1822 / Measurement 0x2A5F) ---
        private val SPO2_SERVICE_UUID: UUID =
            UUID.fromString("00001822-0000-1000-8000-00805f9b34fb")
        private val SPO2_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00002a5f-0000-1000-8000-00805f9b34fb")

        // --- Blood Pressure (0x1810 / 0x2A35) ---
        private val BP_SERVICE_UUID: UUID =
            UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")
        private val BP_MEASUREMENT_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00002a35-0000-1000-8000-00805f9b34fb")

        // CCCD
        private val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    // ---- Bluetooth callbacks ----
    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    runOnUiThread {
                        statusTextView.text = "Connected. Discovering services..."
                        dataTextView.text = ""
                    }
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    runOnUiThread {
                        statusTextView.text = "Disconnected from device."
                        dataTextView.text = ""
                        // Reset readings (optional)
                        heartRateTextView.text = "Heart Rate: Not Available"
                        spo2TextView.text = "SpO₂: Not Available"
                        bloodPressureTextView.text = "Blood Pressure: Not Available"
                    }
                    bluetoothGatt?.close()
                    bluetoothGatt = null
                }
            } else {
                runOnUiThread {
                    statusTextView.text = "Connection error: $status"
                    dataTextView.text = ""
                }
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Enable Heart Rate notifications
                gatt.getService(HR_SERVICE_UUID)
                    ?.getCharacteristic(HR_CHARACTERISTIC_UUID)
                    ?.let { enableNotifications(gatt, it) }

                // Enable SpO2 notifications (if available)
                gatt.getService(SPO2_SERVICE_UUID)
                    ?.getCharacteristic(SPO2_CHARACTERISTIC_UUID)
                    ?.let { enableNotifications(gatt, it) }

                // Enable Blood Pressure notifications (if available)
                gatt.getService(BP_SERVICE_UUID)
                    ?.getCharacteristic(BP_MEASUREMENT_CHARACTERISTIC_UUID)
                    ?.let { enableNotifications(gatt, it) }

                runOnUiThread { statusTextView.text = "Services ready" }
            } else {
                runOnUiThread { statusTextView.text = "Service discovery failed: $status" }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleSensorCharacteristic(characteristic)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            handleSensorCharacteristic(characteristic)
        }
    }

    // ---- Helpers ----
    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
        descriptor?.let {
            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(it)
        }
    }

    // Parse IEEE-11073 16-bit SFLOAT (used by BP and some SpO2 formats)
    private fun parseSfloat(lsb: Int, msb: Int): Float {
        val raw = (msb shl 8) or (lsb and 0xFF)
        var mantissa = raw and 0x0FFF
        var exponent = (raw ushr 12) and 0x000F
        if (mantissa >= 0x0800) mantissa = mantissa - 0x1000 // sign extend mantissa
        if (exponent >= 0x0008) exponent = exponent - 16     // sign extend exponent
        return (mantissa * 10.0.pow(exponent)).toFloat()
    }

    private fun handleSensorCharacteristic(characteristic: BluetoothGattCharacteristic) {
        val data = characteristic.value ?: return

        try {
            when (characteristic.uuid) {

                // -------- Heart Rate Measurement 0x2A37 --------
                HR_CHARACTERISTIC_UUID -> {
                    val flags = data[0].toInt()
                    val isHeartRateUInt16 = flags and 0x01 != 0

                    val heartRate = if (isHeartRateUInt16 && data.size >= 3) {
                        (data[1].toInt() and 0xFF) or ((data[2].toInt() and 0xFF) shl 8)
                    } else {
                        data.getOrNull(1)?.toInt()?.and(0xFF) ?: 0
                    }

                    runOnUiThread {
                        heartRateTextView.text = "Heart Rate: $heartRate bpm"
                        // As you requested earlier: show only the second byte as Current Reading
                        dataTextView.text =
                            "Current Reading: ${data.getOrNull(1)?.toUByte() ?: "N/A"}"

                        // Keep placeholders for others
                        stepsTextView.text = "Steps: Not Available"
                        caloriesTextView.text = "Calories: Not Available"
                        sleepTextView.text = "Sleep: Not Available"
                        temperatureTextView.text = "Temperature: Not Available"
                    }
                }

                // -------- SpO2 (Pulse Oximeter Measurement 0x2A5F) --------
                SPO2_CHARACTERISTIC_UUID -> {
                    // The full spec uses flags + SFLOATs.
                    // Many wearables place SpO2 as first SFLOAT after flags.
                    // We'll try to decode SFLOAT at bytes [1..2]. If it fails, fallback to byte[1].
                    val spo2Val: Float = if (data.size >= 3) {
                        parseSfloat(data[1].toInt() and 0xFF, data[2].toInt() and 0xFF)
                    } else {
                        (data.getOrNull(1)?.toUByte()?.toInt() ?: 0).toFloat()
                    }

                    val spo2Rounded = if (spo2Val.isFinite()) spo2Val else 0f

                    runOnUiThread {
                        spo2TextView.text = "SpO₂: ${spo2Rounded.toInt()} %"
                        dataTextView.text = "Current Reading: ${spo2Rounded.toInt()}"
                    }
                }

                // -------- Blood Pressure Measurement 0x2A35 --------
                BP_MEASUREMENT_CHARACTERISTIC_UUID -> {
                    // Flags tell units & optional fields.
                    // Byte 0 flags:
                    // bit 0: 0=mmHg, 1=kPa
                    // bit 1: Time Stamp present
                    // bit 2: Pulse Rate present
                    // bit 3: User ID present
                    // bit 4: Measurement Status present
                    val flags = data[0].toInt()
                    val unitIsKpa = (flags and 0x01) != 0

                    // Systolic/Diastolic/MAP are SFLOATs at bytes:
                    // Systolic: 1..2, Diastolic: 3..4, MAP: 5..6 (if present)
                    var offset = 1
                    val systolic = if (data.size >= offset + 2)
                        parseSfloat(data[offset].toInt() and 0xFF, data[offset + 1].toInt() and 0xFF)
                    else 0f
                    offset += 2

                    val diastolic = if (data.size >= offset + 2)
                        parseSfloat(data[offset].toInt() and 0xFF, data[offset + 1].toInt() and 0xFF)
                    else 0f
                    offset += 2

                    val map = if (data.size >= offset + 2)
                        parseSfloat(data[offset].toInt() and 0xFF, data[offset + 1].toInt() and 0xFF)
                    else 0f
                    offset += 2

                    // Optional fields follow; we ignore for now.

                    val unit = if (unitIsKpa) "kPa" else "mmHg"
                    val sys = if (systolic.isFinite()) systolic else 0f
                    val dia = if (diastolic.isFinite()) diastolic else 0f
                    val mapVal = if (map.isFinite()) map else 0f

                    runOnUiThread {
                        // Round to nearest int for nice display
                        bloodPressureTextView.text =
                            "Blood Pressure: ${sys.toInt()}/${dia.toInt()} $unit (MAP ${mapVal.toInt()} $unit)"
                        // For "Current Reading", show the diastolic byte position (index 1) if present-like behavior,
                        // but better to show systolic as primary:
                        dataTextView.text = "Current Reading: ${sys.toInt()}"
                    }
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                statusTextView.text = "Error parsing data"
            }
        }
    }

    // ---- Activity lifecycle ----
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.tvStatus)
        checkButton = findViewById(R.id.btnCheckStatus)
        dataTextView = findViewById(R.id.tvData)

        heartRateTextView = findViewById(R.id.tvHeartRate)
        spo2TextView = findViewById(R.id.tvSpo2)
        bloodPressureTextView = findViewById(R.id.tvBloodPressure)
        stepsTextView = findViewById(R.id.tvSteps)
        caloriesTextView = findViewById(R.id.tvCalories)
        sleepTextView = findViewById(R.id.tvSleep)
        temperatureTextView = findViewById(R.id.tvTemperature)

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        checkButton.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                connectToWatch()
            } else {
                statusTextView.text = "Please enable Bluetooth"
            }
        }

        requestBluetoothPermissions()
    }

    private fun connectToWatch() {
        bluetoothGatt?.close()
        bluetoothGatt = null

        val pairedDevice = bluetoothAdapter.bondedDevices.find {
            it.name.equals(WATCH_NAME, ignoreCase = true)
        }

        if (pairedDevice == null) {
            runOnUiThread {
                statusTextView.text = "Device \"$WATCH_NAME\" not paired or found"
                dataTextView.text = ""
            }
            return
        }

        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pairedDevice.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            pairedDevice.connectGatt(this, false, gattCallback)
        }
        statusTextView.text = "Connecting to ${pairedDevice.name}..."
    }

    private fun requestBluetoothPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_BLUETOOTH_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                statusTextView.text = "Permissions granted"
            } else {
                statusTextView.text = "Permissions denied"
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
