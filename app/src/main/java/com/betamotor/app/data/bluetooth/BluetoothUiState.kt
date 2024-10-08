package com.betamotor.app.data.bluetooth


data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isScanning: Boolean = false,
    val connectedDevice: BluetoothDevice? = null,
    val isConnectionAuthorized: Boolean = false
)
