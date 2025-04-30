package com.betamotor.app.service

import com.betamotor.app.data.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

enum class DeviceMessage {
    WIFI_CONFIGURED_ON,
    WIFI_CONFIGURED_OFF
}

fun String.isDeviceMessage(): Boolean {
    return this == DeviceMessage.WIFI_CONFIGURED_ON.toString() ||
            this == DeviceMessage.WIFI_CONFIGURED_OFF.toString()
}

interface BluetoothController {
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val isScanning: StateFlow<Boolean>
    val connectedDevice: StateFlow<BluetoothDevice?>
    val isConnectionAuthorized: StateFlow<Boolean>

    fun startDiscovery()
    fun stopDiscovery()
    fun connectDevice(
        device: BluetoothDevice,
        password: String? = null,
        callback: (Boolean, String) -> Unit,
        onDataReceived: (String) -> Unit
    )

    fun disconnectDevice()
    fun sendCommand(command: String)
    fun sendCommandByteSCS(command: ByteArray)
    fun sendCommandByteDES(command: ByteArray)
    fun hasCallback(key: String): Boolean
    fun addOnDataReceivedCallback(key: String, callback: (Byte, ByteArray) -> Unit)
    fun removeOnDataReceivedCallback(key: String)
    fun toggleWifi(
        flag: Boolean,
        ssid: String = "",
        pass: String = "",
        callback: (Boolean, String) -> Unit
    )

    fun release()
    fun resetDevices()
}
