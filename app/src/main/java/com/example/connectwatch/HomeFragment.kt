package com.example.connectwatch

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var tvStatus: TextView
    private lateinit var btnCheckStatus: Button
    private lateinit var tvData: TextView

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var isConnected = false

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvStatus = view.findViewById(R.id.tvStatus)
        btnCheckStatus = view.findViewById(R.id.btnCheckStatus)
        tvData = view.findViewById(R.id.tvData)

        bluetoothAdapter = (requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        btnCheckStatus.setOnClickListener {
            if (isConnected) {
                tvStatus.text = "Status: Connected"
            } else {
                tvStatus.text = "Status: Not Connected"
            }
        }

        startScan()

        return view
    }

    private fun startScan() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            tvStatus.text = "Bluetooth not available"
            return
        }

        tvStatus.text = "Scanning..."

        scanner.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)

                val device = result.device
                if (device.name?.contains("Realme", true) == true) {
                    tvStatus.text = "Device Found: ${device.name}"
                    scanner.stopScan(this)
                    connectToDevice(device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                tvStatus.text = "Scan failed: $errorCode"
            }
        })

        handler.postDelayed({
            scanner.stopScan(object : ScanCallback() {})
            if (!isConnected) {
                tvStatus.text = "Scan stopped. Device not found."
            }
        }, 10000)
    }

    private fun connectToDevice(device: BluetoothDevice) {
        tvStatus.text = "Connecting to ${device.name}..."
        bluetoothGatt = device.connectGatt(requireContext(), false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true
                handler.post {
                    tvStatus.text = "Connected to ${gatt?.device?.name}"
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                isConnected = false
                handler.post {
                    tvStatus.text = "Disconnected"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
