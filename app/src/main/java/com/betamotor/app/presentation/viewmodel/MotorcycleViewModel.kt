package com.betamotor.app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betamotor.app.data.api.motorcycle.CreateMotorcycleRequest
import com.betamotor.app.data.api.motorcycle.MotorcycleItem
import com.betamotor.app.data.api.motorcycle.MotorcycleTypeItem
import com.betamotor.app.data.bluetooth.BluetoothDeviceDomain
import com.betamotor.app.service.BluetoothController
import com.betamotor.app.service.MotorcycleService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotorcycleViewModel @Inject constructor(
    private val apiService: MotorcycleService
): ViewModel() {
    private val _motorcycleTypes = MutableStateFlow<List<MotorcycleTypeItem?>>(emptyList())
    val motorcycleTypes: StateFlow<List<MotorcycleTypeItem?>> = _motorcycleTypes

    private val _motorcycles = MutableStateFlow<List<MotorcycleItem>>(emptyList())
    val motorcycles: StateFlow<List<MotorcycleItem>> = _motorcycles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    suspend fun getMotorcycleTypes(): Pair<List<MotorcycleTypeItem?>, String> {
        _isLoading.value = true
        val result = apiService.getMotorcycleTypes()
        Log.d("helow", result.first?.data.toString())
        var types: List<MotorcycleTypeItem?> = arrayListOf()

        if (result.first != null) {
            types = result.first?.data?.motorcycleTypes ?: emptyList()
        }

        _motorcycleTypes.value = types
        _isLoading.value = false
        return Pair(_motorcycleTypes.value, result.second)
    }

    fun getMotorcycles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getMotorcycles()
                _motorcycles.value = response.first ?: emptyList()

//                // DEBUG ONLY
//                val device = MotorcycleItem(
//                    name = "My Motorcycle",
//                    macAddress = "00:11:22:33:44:55",
//                    deviceId = "1234567890",
//                    motorcycleTypeId = 1
//                )
//
//                val device2 = MotorcycleItem(
//                    name = "My Motorcycle 2",
//                    macAddress = "00:11:22:33:44:56",
//                    deviceId = "1234567891",
//                    motorcycleTypeId = 2
//                )
//
//                _savedDevices.value = listOf(device, device2) + _savedDevices.value
//                // END DEBUG ONLY

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load saved devices: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun saveMotorcycle(device: CreateMotorcycleRequest): Boolean {
        _isLoading.value = true
        try {
            val resp = apiService.saveMotorcycle(device)

            if (resp.first != true) {
                throw Exception(resp.second)
            }

            _error.value = null

            return true
        } catch (e: Exception) {
            _error.value = "Failed to save device: ${e.message}"
            return false
        } finally {
            _isLoading.value = false
        }
    }

    fun removeMotorcycle(device: MotorcycleItem) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                apiService.removeMotorcycle(device.macAddress)
                getMotorcycles() // Refresh the list
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to remove device: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
