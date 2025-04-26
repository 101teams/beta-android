package com.betamotor.app.presentation.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DetailDeviceViewModel @Inject constructor() : ViewModel(), DefaultLifecycleObserver {
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _rpm = MutableStateFlow("-")
    val rpm: StateFlow<String> = _rpm

    private val _gasPosition = MutableStateFlow("-")
    val gasPosition: StateFlow<String> = _gasPosition

    private val _actuatedSpark = MutableStateFlow("-")
    val actuatedSpark: StateFlow<String> = _actuatedSpark

    private val _engineCoolant = MutableStateFlow("-")
    val engineCoolant: StateFlow<String> = _engineCoolant

    private val _airTemp = MutableStateFlow("-")
    val airTemp: StateFlow<String> = _airTemp

    private val _atmospherePressure = MutableStateFlow("-")
    val atmospherePressure: StateFlow<String> = _atmospherePressure

    private val _operatingHours = MutableStateFlow("-")
    val operatingHours: StateFlow<String> = _operatingHours

    private val _batteryVoltage = MutableStateFlow("-")
    val batteryVoltage: StateFlow<String> = _batteryVoltage

    fun setIsRecording(value: Boolean) {
        _isRecording.value = value
    }

    fun updateRpm(value: String) { _rpm.value = value }
    fun updateGasPosition(value: String) { _gasPosition.value = value }
    fun updateActuatedSpark(value: String) { _actuatedSpark.value = value }
    fun updateEngineCoolant(value: String) { _engineCoolant.value = value }
    fun updateAirTemp(value: String) { _airTemp.value = value }
    fun updateAtmospherePressure(value: String) { _atmospherePressure.value = value }
    fun updateOperatingHours(value: String) { _operatingHours.value = value }
    fun updateBatteryVoltage(value: String) { _batteryVoltage.value = value }
}