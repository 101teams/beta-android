package com.betamotor.app.presentation.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betamotor.app.data.constants
import com.betamotor.app.service.BluetoothController
import com.betamotor.app.utils.LocalLogging
import com.betamotor.app.utils.MQTTHelper
import com.betamotor.app.utils.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DeviceDetailViewModel @Inject constructor(
    private val bluetoothController: BluetoothController,
    private val mqttHelper: MQTTHelper,
    @ApplicationContext context: Context
) : ViewModel() {
    
    private val logger = LocalLogging(context)
    private val prefManager = PrefManager(context)
    
    // Engine Data values
    private val _rpm = MutableStateFlow("-")
    val rpm: StateFlow<String> = _rpm.asStateFlow()
    
    private val _gasPosition = MutableStateFlow("-")
    val gasPosition: StateFlow<String> = _gasPosition.asStateFlow()
    
    private val _actuatedSpark = MutableStateFlow("-")
    val actuatedSpark: StateFlow<String> = _actuatedSpark.asStateFlow()
    
    private val _engineCoolant = MutableStateFlow("-")
    val engineCoolant: StateFlow<String> = _engineCoolant.asStateFlow()
    
    private val _airTemp = MutableStateFlow("-")
    val airTemp: StateFlow<String> = _airTemp.asStateFlow()
    
    private val _atmospherePressure = MutableStateFlow("-")
    val atmospherePressure: StateFlow<String> = _atmospherePressure.asStateFlow()
    
    private val _operatingHours = MutableStateFlow("-")
    val operatingHours: StateFlow<String> = _operatingHours.asStateFlow()
    
    private val _batteryVoltage = MutableStateFlow("-")
    val batteryVoltage: StateFlow<String> = _batteryVoltage.asStateFlow()

    // Engine Info values
    private val _vin = MutableStateFlow("-")
    val vin: StateFlow<String> = _vin.asStateFlow()
    
    private val _ecuDRW = MutableStateFlow("-")
    val ecuDRW: StateFlow<String> = _ecuDRW.asStateFlow()
    
    private val _ecuHW = MutableStateFlow("-")
    val ecuHW: StateFlow<String> = _ecuHW.asStateFlow()
    
    private val _ecuSW = MutableStateFlow("-")
    val ecuSW: StateFlow<String> = _ecuSW.asStateFlow()
    
    private val _calibration = MutableStateFlow("-")
    val calibration: StateFlow<String> = _calibration.asStateFlow()
    
    private val _homologation = MutableStateFlow("-")
    val homologation: StateFlow<String> = _homologation.asStateFlow()
    
    // Status
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    // Data recording
    private val _csvData = MutableStateFlow<MutableList<String>>(mutableListOf())
    val csvData: StateFlow<MutableList<String>> = _csvData.asStateFlow()
    
    // Map to track pending commands and their timeouts
    private val pendingCommands = mutableMapOf<Int, Handler>()
    private val ENGINE_DATA_COMMAND_TIMEOUT = 500L // 500ms timeout
    private val ENGINE_INFO_COMMAND_TIMEOUT = 1000L // 1s timeout

    fun overwriteValueWhenEmpty(oldValue: String, newValue: String): String {
        if (newValue == "-") {
            return oldValue
        }

        return newValue
    }
    
    // ===== Engine Data Functions =====
    
    fun toggleStreaming() {
        _isStreaming.value = !_isStreaming.value
        
        if (_isStreaming.value) {
            logger.writeLog("Starting data streaming")
            setupEngineDataCallback()
            startEngineDataCycle()
        } else {
            logger.writeLog("Stopping data streaming")
            cancelAllPendingCommands()
        }
    }
    
    fun toggleRecording() {
        if (!_isStreaming.value) {
            return
        }
        
        _isRecording.value = !_isRecording.value
        
        if (!_isRecording.value) {
            // Stopped recording, ready to export
            logger.writeLog("Stopped recording data")
        } else {
            logger.writeLog("Started recording data")
            _csvData.value = mutableListOf()
        }
    }
    
    fun clearRecordingData() {
        _csvData.value = mutableListOf()
    }
    
    private fun startEngineDataCycle() {
        if (!_isStreaming.value) return
        
        // Cancel any pending commands/timeouts first
        cancelAllPendingCommands()
        
        // Start with ENGINE_SPEED (first in sequence)
        val data = ByteArray(5)
        data[0] = ((0x0101 shr 8) and 0xFF).toByte()
        data[1] = (0x0101 and 0xFF).toByte()
        data[2] = 0x02
        data[3] = ((constants.RLI_ENGINE_SPEED shr 8) and 0xFF).toByte()
        data[4] = (constants.RLI_ENGINE_SPEED and 0xFF).toByte()
        
        // Set timeout for first command
        val timeoutHandler = Handler(Looper.getMainLooper())
        pendingCommands[constants.RLI_ENGINE_SPEED] = timeoutHandler
        
        timeoutHandler.postDelayed({
            // If we reach this point, command timed out
            logger.writeLog("Command timeout for initial RLI_ENGINE_SPEED")
            pendingCommands.remove(constants.RLI_ENGINE_SPEED)

//            // Mark value as unavailable
//            _rpm.value = "-"
            
            // Continue with next command despite timeout
            processNextEngineDataCommand(constants.RLI_ENGINE_SPEED)
        }, ENGINE_DATA_COMMAND_TIMEOUT)
        
        // Send the first command
        bluetoothController.sendCommandByteDES(data)
    }
    
    private fun processNextEngineDataCommand(currentRliId: Int) {
        val nextRliId = when(currentRliId) {
            constants.RLI_ENGINE_SPEED -> constants.RLI_GAS_POSITION
            constants.RLI_GAS_POSITION -> constants.RLI_ACTUATED_SPARK
            constants.RLI_ACTUATED_SPARK -> constants.RLI_COOLANT_TEMP
            constants.RLI_COOLANT_TEMP -> constants.RLI_AIR_TEMP
            constants.RLI_AIR_TEMP -> constants.RLI_ATMOSPHERE_PRESSURE
            constants.RLI_ATMOSPHERE_PRESSURE -> constants.RLI_OPERATING_HOURS
            constants.RLI_OPERATING_HOURS -> constants.RLI_BATTERY_VOLTAGE
            constants.RLI_BATTERY_VOLTAGE -> -1 // End of sequence, restart
            else -> constants.RLI_ENGINE_SPEED // If unknown, restart from beginning
        }
        
        if (nextRliId == -1) {
            // Complete cycle finished, publish data and restart
            publishEngineData()
            startEngineDataCycle() // Restart the cycle
            return
        }
        
        // Setup command data
        val data = ByteArray(5)
        data[0] = ((0x0101 shr 8) and 0xFF).toByte()
        data[1] = (0x0101 and 0xFF).toByte()
        data[2] = 0x02
        data[3] = ((nextRliId shr 8) and 0xFF).toByte()
        data[4] = (nextRliId and 0xFF).toByte()
        
        if (_isStreaming.value) {
            // Set timeout for this command
            val timeoutHandler = Handler(Looper.getMainLooper())
            pendingCommands[nextRliId] = timeoutHandler
            
            timeoutHandler.postDelayed({
                // If we reach this point, command timed out
                logger.writeLog("Command timeout for RLI ID: $nextRliId")
                pendingCommands.remove(nextRliId)
                
//                // Mark value as unavailable
//                setEngineDataValueAsUnavailable(nextRliId)
                
                // Continue with next command despite timeout
                processNextEngineDataCommand(nextRliId)
            }, ENGINE_DATA_COMMAND_TIMEOUT)
            
            // Send the command
            Handler(Looper.getMainLooper()).postDelayed({
                if (_isStreaming.value) {
                    bluetoothController.sendCommandByteDES(data)
                }
            }, 50)
        }
    }
    
    private fun setupEngineDataCallback() {
        bluetoothController.onReadDataDES { rliID, fullData ->
            onEngineDataReceived(rliID, fullData)
        }
    }
    
    private fun onEngineDataReceived(rliID: Byte, fullData: ByteArray) {
        logger.writeLog("DES data received: rliID = $rliID; ${fullData.joinToString(", ")}")
        
        // Convert rliID byte to Int for comparison with constants
        val rliIDInt = rliID.toInt() and 0xFF
        
        // Cancel timeout for this command if it exists
        pendingCommands[rliIDInt]?.removeCallbacksAndMessages(null)
        pendingCommands.remove(rliIDInt)
        
        viewModelScope.launch {
            try {
                if (fullData[1].toInt() == 0x0101 and 0xFF) {
                    when (rliID) {
                        constants.RLI_ENGINE_SPEED.toByte() -> {
                            val value = extractResData(fullData).toString()
                            logger.writeLog("RLI_ENGINE_SPEED data: $value")
                            _rpm.value = value
                            saveCsvData("RPM", value)
                            processNextEngineDataCommand(constants.RLI_ENGINE_SPEED)
                        }
                        constants.RLI_GAS_POSITION.toByte() -> {
                            val value = (extractResData(fullData) / 16).toString()
                            logger.writeLog("RLI_GAS_POSITION data: $value")
                            _gasPosition.value = value
                            saveCsvData("THROTTLE", value)
                            processNextEngineDataCommand(constants.RLI_GAS_POSITION)
                        }
                        constants.RLI_ACTUATED_SPARK.toByte() -> {
                            val value = (extractSignedResData(fullData) / 16).toString()
                            logger.writeLog("RLI_ACTUATED_SPARK data: $value")
                            _actuatedSpark.value = value
                            saveCsvData("SPARK ADV", value)
                            processNextEngineDataCommand(constants.RLI_ACTUATED_SPARK)
                        }
                        constants.RLI_COOLANT_TEMP.toByte() -> {
                            val value = (extractSignedResData(fullData) / 16).toString()
                            logger.writeLog("RLI_COOLANT_TEMP data: $value")
                            _engineCoolant.value = value
                            saveCsvData("ENGINE TEMP", value)
                            processNextEngineDataCommand(constants.RLI_COOLANT_TEMP)
                        }
                        constants.RLI_AIR_TEMP.toByte() -> {
                            val value = (extractSignedResData(fullData) / 16).toString()
                            logger.writeLog("RLI_AIR_TEMP data: $value")
                            _airTemp.value = value
                            saveCsvData("AIR TEMP", value)
                            processNextEngineDataCommand(constants.RLI_AIR_TEMP)
                        }
                        constants.RLI_ATMOSPHERE_PRESSURE.toByte() -> {
                            val value = extractResData(fullData).toString()
                            logger.writeLog("RLI_ATMOSPHERE_PRESSURE data: $value")
                            _atmospherePressure.value = value
                            saveCsvData("ATM PRESSURE", value)
                            processNextEngineDataCommand(constants.RLI_ATMOSPHERE_PRESSURE)
                        }
                        constants.RLI_OPERATING_HOURS.toByte() -> {
                            val value = (extractResData(fullData) / 8).toString()
                            logger.writeLog("RLI_OPERATING_HOURS data: $value")
                            _operatingHours.value = value
                            saveCsvData("OP. TIME", value)
                            processNextEngineDataCommand(constants.RLI_OPERATING_HOURS)
                        }
                        constants.RLI_BATTERY_VOLTAGE.toByte() -> {
                            val value = (extractResData(fullData) / 16).toString()
                            logger.writeLog("RLI_BATTERY_VOLTAGE data: $value")
                            _batteryVoltage.value = value
                            saveCsvData("BATTERY VOLTAGE", value)
                            processNextEngineDataCommand(constants.RLI_BATTERY_VOLTAGE)
                        }
                        else -> {
                            if (fullData[1].toInt() == 0x0301 and 0xFF) {
                                handleEngineInfoData(rliID, fullData)
                            } else {
                                logger.writeLog("Unknown data received. RliId: $rliID")
                                // Continue with next command even for unknown data
                                processNextEngineDataCommand(rliIDInt)
                            }
                        }
                    }
                } else if (fullData[1].toInt() == 0x0301 and 0xFF) {
                    handleEngineInfoData(rliID, fullData)
                } else {
                    // Continue with next command even if response format is unexpected
                    processNextEngineDataCommand(rliIDInt)
                }
            } catch (e: Exception) {
                logger.writeLog("Error in engine data: ${e.message}")
                // Continue with next command despite error
                processNextEngineDataCommand(rliIDInt)
            }
        }
    }

    // ===== Engine Info Functions =====
    
    fun fetchEngineInfo() {
        logger.writeLog("Starting engine info data fetch sequence")
        
        // Cancel any pending commands
        cancelAllPendingCommands()
        
        // Setup callback
        setupEngineInfoCallback()
        
        // Start with ECU_VIN (first in sequence)
        val data = ByteArray(5)
        data[0] = ((0x0301 shr 8) and 0xFF).toByte()
        data[1] = (0x0301 and 0xFF).toByte()
        data[2] = 0x02
        data[3] = ((constants.ECU_VIN shr 8) and 0xFF).toByte()
        data[4] = (constants.ECU_VIN and 0xFF).toByte()
        
        // Set timeout for first command
        val timeoutHandler = Handler(Looper.getMainLooper())
        pendingCommands[constants.ECU_VIN] = timeoutHandler
        
        timeoutHandler.postDelayed({
            // If we reach this point, command timed out
            logger.writeLog("Command timeout for initial ECU_VIN")
            pendingCommands.remove(constants.ECU_VIN)
            
//            // Mark value as unavailable
//            _vin.value = "-"
            
            // Continue with next command despite timeout
            processNextEngineInfoCommand(constants.ECU_VIN)
        }, ENGINE_INFO_COMMAND_TIMEOUT)
        
        // Send the first command
        bluetoothController.sendCommandByteDES(data)
    }
    
    private fun processNextEngineInfoCommand(currentRliId: Int) {
        val nextRliId = when(currentRliId) {
            constants.ECU_VIN -> constants.ECU_DRAWING_NUMBER
            constants.ECU_DRAWING_NUMBER -> constants.ECU_HW_NUMBER
            constants.ECU_HW_NUMBER -> constants.ECU_SW_NUMBER
            constants.ECU_SW_NUMBER -> constants.ECU_SW_VERSION
            constants.ECU_SW_VERSION -> constants.ECU_HOMOLOGATION
            constants.ECU_HOMOLOGATION -> -1 // End of sequence
            else -> constants.ECU_VIN // If unknown, restart from beginning
        }
        
        if (nextRliId == -1) {
            // Complete cycle finished, publish data
            publishEngineInfoData()
            return
        }
        
        // Setup command data
        val data = ByteArray(5)
        data[0] = ((0x0301 shr 8) and 0xFF).toByte()
        data[1] = (0x0301 and 0xFF).toByte()
        data[2] = 0x02
        data[3] = ((nextRliId shr 8) and 0xFF).toByte()
        data[4] = (nextRliId and 0xFF).toByte()
        
        // Set timeout for this command
        val timeoutHandler = Handler(Looper.getMainLooper())
        pendingCommands[nextRliId] = timeoutHandler
        
        timeoutHandler.postDelayed({
            // If we reach this point, command timed out
            logger.writeLog("Engine Info command timeout for RLI ID: $nextRliId")
            pendingCommands.remove(nextRliId)
            
//            // Mark value as unavailable
//            setEngineInfoValueAsUnavailable(nextRliId)
            
            // Continue with next command despite timeout
            processNextEngineInfoCommand(nextRliId)
        }, ENGINE_INFO_COMMAND_TIMEOUT)
        
        // Send the command
        Handler(Looper.getMainLooper()).postDelayed({
            bluetoothController.sendCommandByteDES(data)
        }, 50)
    }
    
    private fun setupEngineInfoCallback() {
        bluetoothController.onReadDataDES { rliID, fullData ->
            onEngineDataReceived(rliID, fullData) // Reuse the same callback
        }
    }
    
    private fun handleEngineInfoData(rliID: Byte, fullData: ByteArray) {
        // Convert rliID byte to Int for comparison with constants
        val rliIDInt = rliID.toInt() and 0xFF
        
        try {
            when (rliID) {
                constants.ECU_VIN.toByte() -> {
                    val newVal = convertVINData(fullData)
                    _vin.value = newVal
                    prefManager.setMotorcycleVIN(_vin.value)
                    logger.writeLog("ECU_VIN: $newVal => ${_vin.value}")
                    processNextEngineInfoCommand(constants.ECU_VIN)
                }
                constants.ECU_DRAWING_NUMBER.toByte() -> {
                    val newVal = convertVINData(fullData)
                    _ecuDRW.value = newVal
                    logger.writeLog("ECU_DRAWING_NUMBER: $newVal => ${_ecuDRW.value}")
                    processNextEngineInfoCommand(constants.ECU_DRAWING_NUMBER)
                }
                constants.ECU_HW_NUMBER.toByte() -> {
                    val newVal = convertVINData(fullData)
                    _ecuHW.value = newVal
                    logger.writeLog("ECU_HW_NUMBER: $newVal => ${_ecuHW.value}")
                    processNextEngineInfoCommand(constants.ECU_HW_NUMBER)
                }
                constants.ECU_SW_NUMBER.toByte() -> {
                    val newVal = convertVINData(fullData)
                    _ecuSW.value = newVal
                    logger.writeLog("ECU_SW_NUMBER: $newVal => ${_ecuSW.value}")
                    processNextEngineInfoCommand(constants.ECU_SW_NUMBER)
                }
                constants.ECU_SW_VERSION.toByte() -> {
                    val newVal = convertVINData(fullData)
                    _calibration.value = newVal
                    logger.writeLog("ECU_SW_VERSION: $newVal => ${_calibration.value}")
                    processNextEngineInfoCommand(constants.ECU_SW_VERSION)
                }
                constants.ECU_HOMOLOGATION.toByte() -> {
                    val newVal = convertVINData(fullData)
                    _homologation.value = newVal
                    logger.writeLog("ECU_HOMOLOGATION $newVal =>: ${_homologation.value}")
                    processNextEngineInfoCommand(constants.ECU_HOMOLOGATION)
                }
                else -> {
                    logger.writeLog("Unknown engine info data received. RliId: $rliID")
                    // Continue with next command even for unknown data
                    processNextEngineInfoCommand(rliIDInt)
                }
            }
        } catch (e: Exception) {
            logger.writeLog("Error in engine info: ${e.message}")
            // Continue with next command despite error
            processNextEngineInfoCommand(rliIDInt)
        }
    }

    // ===== Utility Functions =====
    
    private fun cancelAllPendingCommands() {
        pendingCommands.forEach { (_, handler) ->
            handler.removeCallbacksAndMessages(null)
        }
        pendingCommands.clear()
    }
    
    private fun setEngineDataValueAsUnavailable(rliId: Int) {
        when(rliId) {
            constants.RLI_ENGINE_SPEED -> _rpm.value = "-"
            constants.RLI_GAS_POSITION -> _gasPosition.value = "-"
            constants.RLI_ACTUATED_SPARK -> _actuatedSpark.value = "-"
            constants.RLI_COOLANT_TEMP -> _engineCoolant.value = "-"
            constants.RLI_AIR_TEMP -> _airTemp.value = "-"
            constants.RLI_ATMOSPHERE_PRESSURE -> _atmospherePressure.value = "-"
            constants.RLI_OPERATING_HOURS -> _operatingHours.value = "-"
            constants.RLI_BATTERY_VOLTAGE -> _batteryVoltage.value = "-"
        }
    }
    
    private fun setEngineInfoValueAsUnavailable(rliId: Int) {
        when(rliId) {
            constants.ECU_VIN -> _vin.value = "-"
            constants.ECU_DRAWING_NUMBER -> _ecuDRW.value = "-"
            constants.ECU_HW_NUMBER -> _ecuHW.value = "-"
            constants.ECU_SW_NUMBER -> _ecuSW.value = "-"
            constants.ECU_SW_VERSION -> _calibration.value = "-"
            constants.ECU_HOMOLOGATION -> _homologation.value = "-"
        }
    }
    
    private fun publishEngineData() {
        val jsonPayload = """
        {
          "vin": "${prefManager.getMotorcycleVIN()}",
          "rpm": ${if (_rpm.value == "-") "null" else _rpm.value},
          "throttle": ${if (_gasPosition.value == "-") "null" else _gasPosition.value},
          "sparkAdv": ${if (_actuatedSpark.value == "-") "null" else _actuatedSpark.value},
          "engineTemp": ${if (_engineCoolant.value == "-") "null" else _engineCoolant.value},
          "airTemp": ${if (_airTemp.value == "-") "null" else _airTemp.value},
          "atmPressure": ${if (_atmospherePressure.value == "-") "null" else _atmospherePressure.value},
          "opTime": ${if (_operatingHours.value == "-") "null" else _operatingHours.value},
          "batteryVoltage": ${if (_batteryVoltage.value == "-") "null" else _batteryVoltage.value}
        }
        """.trimIndent()
        
        mqttHelper.publishMessage("Beta/${prefManager.getSelectedMotorcycleId()}/enginedata", jsonPayload)
    }
    
    private fun publishEngineInfoData() {
        val jsonPayload = """
        {
            "vin": "${if (_vin.value == "-") "" else _vin.value}",
            "ecuDrw": "${if (_ecuDRW.value == "-") "" else _ecuDRW.value}",
            "ecuHw": "${if (_ecuHW.value == "-") "" else _ecuHW.value}",
            "ecuSw": "${if (_ecuSW.value == "-") "" else _ecuSW.value}",
            "calibration": "${if (_calibration.value == "-") "" else _calibration.value}",
            "homolCode": "${if (_homologation.value == "-") "" else _homologation.value}"
        }
        """.trimIndent()
        
        mqttHelper.publishMessage("Beta/${prefManager.getSelectedMotorcycleId()}/engineinfo", jsonPayload)
    }
    
    private fun saveCsvData(type: String, value: String) {
        Log.d(type, value)
        if (_isRecording.value) {
            Log.d("write", value)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val time = dateFormat.format(Date())
            
            val tmpCSVData = _csvData.value.toMutableList()
            tmpCSVData.add("$time,$type,$value")
            
            _csvData.value = tmpCSVData
        }
    }
    
    private fun extractResData(fullData: ByteArray): Int {
        return when {
            fullData.size > 7 -> ((fullData[6].toUByte().toInt() shl 8) or fullData[7].toUByte().toInt()) and 0xFFFF
            fullData.size > 6 -> fullData[6].toUByte().toInt()
            else -> 0
        }
    }
    
    private fun extractSignedResData(fullData: ByteArray): Int {
        return when {
            fullData.size > 7 -> convertSignedTwosComplement(((fullData[6].toInt() shl 8) or fullData[7].toInt()) and 0xFFFF, 16)
            fullData.size > 6 -> convertSignedTwosComplement(fullData[6].toInt(), 8)
            else -> 0
        }
    }
    
    private fun convertSignedTwosComplement(value: Int, bitSize: Int): Int {
        val maxValue = (1 shl (bitSize - 1))
        return if (value >= maxValue) {
            value - (1 shl bitSize)
        } else {
            value
        }
    }
    
    private fun convertVINData(fullData: ByteArray): String {
        var value = ""
        
        val length = fullData[3].toInt() - 2
        
        for(i in 6..6+(length-1)) {
            value += fullData[i].toInt().toChar()
        }
        
        return value
    }
    
    override fun onCleared() {
        super.onCleared()
        cancelAllPendingCommands()
    }
}