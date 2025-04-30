package com.betamotor.app.presentation.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betamotor.app.data.bluetooth.BluetoothDevice
import com.betamotor.app.data.bluetooth.BluetoothUiState
import com.betamotor.app.service.BluetoothController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController,
) : ViewModel(), DefaultLifecycleObserver {
    private val _state = MutableStateFlow(BluetoothUiState())

    val state = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        bluetoothController.isScanning,
        bluetoothController.isConnectionAuthorized,
        _state
    ) { scannedDevices, pairedDevices, isScanning, isConnectionAuthorized, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            isScanning = isScanning,
            isConnectionAuthorized = isConnectionAuthorized
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    private inline fun <T1, T2, T3, T4, T5, R> combine(
        flow: Flow<T1>,
        flow2: Flow<T2>,
        flow3: Flow<T3>,
        flow4: Flow<T4>,
        flow5: Flow<T5>,
        crossinline transform: suspend (T1, T2, T3, T4, T5) -> R
    ): Flow<R> {
        return kotlinx.coroutines.flow.combine(
            flow,
            flow2,
            flow3,
            flow4,
            flow5,
        ) { args: Array<*> ->
            @Suppress("UNCHECKED_CAST")
            transform(
                args[0] as T1,
                args[1] as T2,
                args[2] as T3,
                args[3] as T4,
                args[4] as T5,
            )
        }
    }

    fun startScan() {
        bluetoothController.startDiscovery()
    }

    fun stopScan() {
        bluetoothController.stopDiscovery()
    }

    fun connectDevice(
        device: BluetoothDevice,
        password: String,
        callback: (Boolean, String) -> Unit,
        onDataReceived: (String) -> Unit
    ) {
        viewModelScope.launch {
            bluetoothController.connectDevice(device, password, callback, onDataReceived = {
                onDataReceived(it)
            })
        }
    }

    fun configureWifi(ssid: String, wifiPass: String, callback: (Boolean, String) -> Unit) {
        bluetoothController.toggleWifi(true, ssid, wifiPass, callback)
    }

    fun turnOffWifi(callback: (Boolean, String) -> Unit) {
        bluetoothController.toggleWifi(false, callback = callback)
    }

    fun disconnectDevice() {
        bluetoothController.disconnectDevice()
    }

    fun resetDevices() {
        bluetoothController.resetDevices()
    }
}