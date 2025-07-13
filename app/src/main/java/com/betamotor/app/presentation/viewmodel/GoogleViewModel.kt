package com.betamotor.app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betamotor.app.data.api.google.GoogleAltitudeResponse
import com.betamotor.app.data.api.motorcycle.CreateMotorcycleRequest
import com.betamotor.app.data.api.motorcycle.MotorcycleItem
import com.betamotor.app.data.api.motorcycle.MotorcycleTypeItem
import com.betamotor.app.data.bluetooth.BluetoothDeviceDomain
import com.betamotor.app.service.BluetoothController
import com.betamotor.app.service.GoogleService
import com.betamotor.app.service.MotorcycleService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoogleViewModel @Inject constructor(
    private val googleService: GoogleService
): ViewModel() {
    private val _googleAltitude = MutableStateFlow<GoogleAltitudeResponse?>(null)
    val googleAltitude: StateFlow<GoogleAltitudeResponse?> = _googleAltitude.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    suspend fun getAltitude(latitude: Double, longitude: Double): Pair<GoogleAltitudeResponse?, String> {
        _isLoading.value = true
        val result = googleService.getAltitude(latitude, longitude)

        if (result.first != null) {
            _googleAltitude.value = result.first
        }

        _isLoading.value = false
        return Pair(_googleAltitude.value, result.second)
    }
}
