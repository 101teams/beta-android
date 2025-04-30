package com.betamotor.app.presentation.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betamotor.app.data.constants
import com.betamotor.app.service.BluetoothController
import com.betamotor.app.utils.LocalLogging
import com.betamotor.app.utils.MQTTHelper
import com.betamotor.app.utils.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailDeviceViewModel @Inject constructor(
    private val bluetoothController: BluetoothController,
    private val context: Context,
    private val prefManager: PrefManager
) : ViewModel(), DefaultLifecycleObserver {
    private val csvDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val logger = LocalLogging(context)
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _csvData = MutableStateFlow(mutableListOf<String>())
    val csvData: StateFlow<List<String>> = _csvData.asStateFlow()

    val rpm = mutableStateOf("-")
    val gasPosition = mutableStateOf("-")
    val actuatedSpark = mutableStateOf("-")
    val engineCoolant = mutableStateOf("-")
    val airTemp = mutableStateOf("-")
    val atmospherePressure = mutableStateOf("-")
    val operatingHours = mutableStateOf("-")
    val batteryVoltage = mutableStateOf("-")

    private val engineData = listOf(
        Pair(constants.RLI_ENGINE_SPEED, constants.RLI_ENGINE_SPEED_REFRESH_RATE),
        Pair(constants.RLI_GAS_POSITION, constants.RLI_GAS_POSITION_REFRESH_RATE),
        Pair(constants.RLI_ACTUATED_SPARK, constants.RLI_ACTUATED_SPARK_REFRESH_RATE),
        Pair(constants.RLI_COOLANT_TEMP, constants.RLI_COOLANT_TEMP_REFRESH_RATE),
        Pair(constants.RLI_AIR_TEMP, constants.RLI_AIR_TEMP_REFRESH_RATE),
        Pair(constants.RLI_ATMOSPHERE_PRESSURE, constants.RLI_ATMOSPHERE_PRESSURE_REFRESH_RATE),
        Pair(constants.RLI_OPERATING_HOURS, constants.RLI_OPERATING_HOURS_REFRESH_RATE),
        Pair(constants.RLI_BATTERY_VOLTAGE, constants.RLI_BATTERY_VOLTAGE_REFRESH_RATE)
    )

    fun toggleStreaming() {
        _isStreaming.value = !_isStreaming.value
        if (_isStreaming.value) {
            startStreaming()
        } else {
            stopStreaming()
        }
    }

    fun toggleRecording() {
        _isRecording.value = !_isRecording.value
    }

    fun clearCsvData() {
        _csvData.value = mutableListOf()
    }

    private fun startStreaming() {
        setOnReadDataDESCalback { rliID, fullData ->
            if (fullData[1].toInt() != constants.SUB_COMMAND_ID and 0xFF) return@setOnReadDataDESCalback
            onEngineDataReceived(rliID, fullData)
        }
        subscribeToMultipleRli(engineData)
    }

    private fun stopStreaming() {
        unsubscribeFromMultipleRli(engineData.map { it.first })
    }

    fun saveCsvData(type: String, value: String) {
        if (_isRecording.value) {
            val time = csvDateFormat.format(Date())
            _csvData.value = _csvData.value.toMutableList().apply {
                add("$time,$type,$value")
            }
        }
    }

    fun onEngineDataReceived(rliID: Byte, fullData: ByteArray) {
        logger.writeLog("engine data received from live subscription")
        when (rliID) {
            constants.RLI_ENGINE_SPEED.toByte() -> extractResData(fullData).toString().also { rpm.value = it; saveCsvData("RPM", it) }
            constants.RLI_GAS_POSITION.toByte() -> (extractResData(fullData) / 16).toString().also { gasPosition.value = it; saveCsvData("THROTTLE", it) }
            constants.RLI_ACTUATED_SPARK.toByte() -> (extractSignedResData(fullData) / 16).toString().also { actuatedSpark.value = it; saveCsvData("SPARK ADV", it) }
            constants.RLI_COOLANT_TEMP.toByte() -> (extractSignedResData(fullData) / 16).toString().also { engineCoolant.value = it; saveCsvData("ENGINE TEMP", it) }
            constants.RLI_AIR_TEMP.toByte() -> (extractSignedResData(fullData) / 16).toString().also { airTemp.value = it; saveCsvData("AIR TEMP", it) }
            constants.RLI_ATMOSPHERE_PRESSURE.toByte() -> extractResData(fullData).toString().also { atmospherePressure.value = it; saveCsvData("ATM PRESSURE", it) }
            constants.RLI_OPERATING_HOURS.toByte() -> (extractResData(fullData) / 8).toString().also { operatingHours.value = it; saveCsvData("OP. TIME", it) }
            constants.RLI_BATTERY_VOLTAGE.toByte() -> (extractResData(fullData) / 16).toString().also { batteryVoltage.value = it; saveCsvData("BATTERY VOLTAGE", it) }
            else -> return
        }

        val jsonPayload = """
            {
                "macAddress": "${prefManager.getMacAddress()}",
                "rpm": ${stripToNull(rpm.value)},
                "throttle": ${stripToNull(gasPosition.value)},
                "sparkAdv": ${stripToNull(actuatedSpark.value)},
                "engineTemp": ${stripToNull(engineCoolant.value)},
                "airTemp": ${stripToNull(airTemp.value)},
                "atmPressure": ${stripToNull(atmospherePressure.value)},
                "opTime": ${stripToNull(operatingHours.value)},
                "batteryVoltage": ${stripToNull(batteryVoltage.value)}
            }
        """.trimIndent()

        MQTTHelper(context).publishMessage("Beta/${prefManager.getSelectedMotorcycleId()}/enginedata", jsonPayload)
    }

    private fun stripToNull(value: String): String = if (value == "-") "null" else value

    private fun extractResData(fullData: ByteArray): Int = when {
        fullData.size > 7 -> ((fullData[6].toUByte().toInt() shl 8) or fullData[7].toUByte().toInt()) and 0xFFFF
        fullData.size > 6 -> fullData[6].toUByte().toInt()
        else -> 0
    }

    private fun extractSignedResData(fullData: ByteArray): Int = when {
        fullData.size > 7 -> convertSignedTwosComplement(((fullData[6].toInt() shl 8) or fullData[7].toInt()) and 0xFFFF, 16)
        fullData.size > 6 -> convertSignedTwosComplement(fullData[6].toInt(), 8)
        else -> 0
    }

    private fun convertSignedTwosComplement(value: Int, bitSize: Int): Int {
        val maxValue = 1 shl (bitSize - 1)
        return if (value >= maxValue) value - (1 shl bitSize) else value
    }

    fun sendCommandByteDES(command: ByteArray) {
        viewModelScope.launch {
            bluetoothController.sendCommandByteDES(command)
        }
    }

    private fun subscribeToRli(rliId: Int, updatePeriodMs: Long) {
        val data = ByteArray(7)
        data[0] = ((constants.SUB_COMMAND_ID shr 8) and 0xFF).toByte()
        data[1] = (constants.SUB_COMMAND_ID and 0xFF).toByte()
        data[2] = 0x04
        data[3] = ((rliId shr 8) and 0xFF).toByte()
        data[4] = (rliId and 0xFF).toByte()
        data[5] = ((updatePeriodMs shr 8) and 0xFF).toByte()
        data[6] = (updatePeriodMs and 0xFF).toByte()
        sendCommandByteDES(data)
    }

    private fun unsubscribeFromRli(rliId: Int) {
        val data = ByteArray(5)
        data[0] = ((constants.UNSUB_COMMAND_ID shr 8) and 0xFF).toByte()
        data[1] = (constants.UNSUB_COMMAND_ID and 0xFF).toByte()
        data[2] = 0x02
        data[3] = ((rliId shr 8) and 0xFF).toByte()
        data[4] = (rliId and 0xFF).toByte()
        sendCommandByteDES(data)
    }

    fun subscribeToMultipleRli(rliList: List<Pair<Int, Long>>) {
        rliList.forEach { (rliId, updatePeriodMs) ->
            Handler(Looper.getMainLooper()).postDelayed({
                subscribeToRli(rliId, updatePeriodMs)
            }, 50)
        }
    }

    fun unsubscribeFromMultipleRli(rliList: List<Int>) {
        rliList.forEach { unsubscribeFromRli(it) }
    }

    fun setOnReadDataDESCalback(onDataReceived: (Byte, ByteArray) -> Unit) {
        viewModelScope.launch {
            bluetoothController.onReadDataDES(onDataReceived)
        }
    }
}
