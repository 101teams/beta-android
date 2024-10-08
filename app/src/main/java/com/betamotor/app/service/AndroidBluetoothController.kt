package com.betamotor.app.service

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.betamotor.app.R
import com.betamotor.app.data.bluetooth.BluetoothDeviceDomain
import com.betamotor.app.data.bluetooth.FoundDeviceReceiver
import com.betamotor.app.data.bluetooth.toBluetoothDeviceDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.reflect.Method
import java.util.Timer
import java.util.UUID
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context
) : BluetoothController {
    private val deviceNameFilter = ""
    private val serviceUuid = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    private val charUuidRx = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E") // write
    private val charUuidTx = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E") // notify

    private var service: BluetoothGattService? = null
    private var tx: BluetoothGattCharacteristic? = null
    private var rx: BluetoothGattCharacteristic? = null

    private var password: String? = null
    private var timer: Timer? = null
    private var timeoutCallback: (Boolean, String) -> Unit = { _: Boolean, _: String -> }
    private var dataReceivedCallback: (String) -> Unit = {}
    private var currentlyRunningCommand: String? = null
    private val maxRetry = 3
    private var retryCount = 0

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private var gatt: BluetoothGatt? = null

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothDeviceDomain?>(null)
    override val connectedDevice: StateFlow<BluetoothDeviceDomain?>
        get() = _connectedDevice.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean>
        get() = _isScanning.asStateFlow()

    private val _isConnectionAuthorized = MutableStateFlow(false)
    override val isConnectionAuthorized: StateFlow<Boolean>
        get() = _isConnectionAuthorized.asStateFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        if (device.name == null) {
            return@FoundDeviceReceiver
        }

        if (!device.name.contains(deviceNameFilter, ignoreCase = true)) {
            return@FoundDeviceReceiver
        }

        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    fun updateBluetoothData(
        newCount: Int? = null,
        newBattery: Int? = null,
        newWifi: String? = null,
        newSn: String? = null,
        newId: String? = null,
        newPwd: String? = null,
    ) {

    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        val bondState = gatt?.device?.bondState

                        if (bondState == BluetoothDevice.BOND_NONE || bondState == BluetoothDevice.BOND_BONDED) {
                            var delayWhenBonded: Long = 0
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                                delayWhenBonded = 1000
                            }

                            val delay =
                                if (bondState == BluetoothDevice.BOND_BONDED) delayWhenBonded else 0

                            val discoverServiceTask = Timer()
                            discoverServiceTask.schedule(timerTask {
                                _connectedDevice.value = gatt.device?.toBluetoothDeviceDomain()
                                val result = gatt.discoverServices()
                                if (!result) {
                                    Log.d("helow", "discover service failed to start")
                                }
                            }, delay)
                        }
                    }

                    // use IS DISCONNECTING if need to close screen faster
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        gatt?.close()
                        resetLocalState()
                    }
                }
            } else {
                gatt?.close()
                resetLocalState()
            }
        }

        fun setServiceAndChars(service: BluetoothGattService) {
            if (service.uuid != serviceUuid) {
                return
            }

            val tx = service.getCharacteristic(charUuidTx) ?: return
            val rx = service.getCharacteristic(charUuidRx) ?: return

            tx.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            gatt?.setCharacteristicNotification(tx, true)
            tx.descriptors.forEach {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt?.writeDescriptor(it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    val descriptor = it
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt?.writeDescriptor(it)
                }
            }

            this@AndroidBluetoothController.service = service
            this@AndroidBluetoothController.tx = tx
            this@AndroidBluetoothController.rx = rx
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val gattServices: List<BluetoothGattService> =
                    gatt?.services?.toList() ?: emptyList()
                Log.e("helow", "Services count: " + gattServices.size)
                for (gattService in gattServices) {
                    setServiceAndChars(gattService)
                }
            } else {
                Log.w("helow", "error. onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                return
            }

            // retry sending command a few times
            if (retryCount < maxRetry) {
                currentlyRunningCommand?.let { sendCommand(it) }
                retryCount += 1

                return
            }

            // handle errors
            cancelTimer()
            currentlyRunningCommand?.let {
                timeoutCallback(false, "Failed to send data to device")
                currentlyRunningCommand = null
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            val strValue = value.toString(Charsets.US_ASCII)
            onCharacteristicChanged(strValue)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            val strValue = characteristic?.value?.toString(Charsets.US_ASCII) ?: ""
            onCharacteristicChanged(strValue)
        }

        // use reusable function call for handling change event because of different supported version above
        fun onCharacteristicChanged(message: String) {
            dataReceivedCallback(message) // send data to mqtt
            val attribute = message.substringBefore("=")
            val value = message.substringAfter("=").replace("\n", "")

            // if first received data is available then connection is authorized.
            if (!isConnectionAuthorized.value && attribute == "CNT") {
                _isConnectionAuthorized.value = true
                cancelTimer()
            }

            if (attribute == "WiFi") {
                cancelTimer()
            }

            when (attribute) {
                "SN" -> {
                    updateBluetoothData(newSn = value)
//                    updateBluetoothData(newSn = "A0A3B32D4AC0,androclient,1234".toMqttCred())
                }

                "ID" -> {
                    updateBluetoothData(newId = value)
                }

                "PWD" -> {
                    updateBluetoothData(newPwd = value)
                }

                "CNT" -> {
                    updateBluetoothData(newCount = value.toIntOrNull())
                }

                "BAT" -> {
                    updateBluetoothData(newBattery = value.toIntOrNull())
                }

                "WiFi" -> {
                    updateBluetoothData(newWifi = value)
                    when (value) {
                        "?" -> { // wifi is not configured yet
                            Log.d("helow", "wifi not configured")
                        }

                        "OFF" -> { // wifi in device is disabled
                            Log.d("helow", "wifi disabled")
                            timeoutCallback(true, DeviceMessage.WIFI_CONFIGURED_OFF.toString())
                        }

                        else -> { // wifi is enabled and configured
                            Log.d("helow", "wifi is configured")
                            timeoutCallback(true, DeviceMessage.WIFI_CONFIGURED_ON.toString())
                        }
                    }
                }

                "Session expired" -> {
                    disconnectDevice()
                }
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS && password != null) {
                Timer().schedule(2000) {
                    password?.let {
                        sendCommand(it)
                    }
                }
            }
        }
    }

    init {
        updatePairedDevices()
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun startDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                Toast.makeText(
                    context,
                    "Cannot start bluetooth because BLUETOOTH_SCAN is not granted", Toast.LENGTH_LONG
                ).show()

                return
            }

            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                Toast.makeText(
                    context,
                    "Cannot start bluetooth because BLUETOOTH_CONNECT is not granted", Toast.LENGTH_LONG
                ).show()

                return
            }
        } else {
            if (!hasPermission(Manifest.permission.BLUETOOTH)) {
                Toast.makeText(
                    context,
                    "Cannot start bluetooth because BLUETOOTH is not granted", Toast.LENGTH_LONG
                ).show()

                return
            }
        }

        if (_isScanning.value) {
            return
        }

        Log.d("helow", "startDiscovery")
        _isScanning.value = true

        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        updatePairedDevices()

        bluetoothAdapter?.startDiscovery()

        Handler(Looper.getMainLooper()).postDelayed({
            if (_isScanning.value && _scannedDevices.value.isEmpty()) {
                stopDiscovery()
            }
        },3000)
    }

    override fun stopDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                return
            }
        } else if (!hasPermission(Manifest.permission.BLUETOOTH)) {
            return
        }

        if (!_isScanning.value) {
            return
        }

        Log.d("helow", "stopDiscovery")
        _isScanning.value = false

        bluetoothAdapter?.cancelDiscovery()
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
    }

    private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }

        bluetoothAdapter
            ?.bondedDevices
            ?.filter { it.name.contains(deviceNameFilter, ignoreCase = true) }
            ?.map { it.toBluetoothDeviceDomain() }
            ?.also { _pairedDevices.update { it } }
    }

    private fun unpairDevice(device: BluetoothDeviceDomain) {
        val btDevice = bluetoothAdapter?.getRemoteDevice(device.macAddress) ?: return
        try {
            val method: Method = btDevice.javaClass.getMethod("removeBond")
            val result = method.invoke(btDevice) as Boolean
            Log.d("helow", if (result) "successfully remove bond" else "failed to remove bond")
        } catch (e: Exception) {
            Log.e("helow", "ERROR: could not remove bond")
            e.printStackTrace()
        }
    }

    override fun connectDevice(
        device: BluetoothDeviceDomain,
        password: String?,
        callback: (Boolean, String) -> Unit,
        onDataReceived: (String) -> Unit
    ) {
        this@AndroidBluetoothController.dataReceivedCallback = onDataReceived
        // remove bond to make sure notification is sent from ble
        unpairDevice(device)

        stopDiscovery()

        val btDevice = bluetoothAdapter?.getRemoteDevice(device.macAddress)
        this@AndroidBluetoothController.password = password

        // uses transport_le because sometime gatt error 133 when not using it
        gatt = btDevice?.connectGatt(context, true, gattCallback, BluetoothDevice.TRANSPORT_LE)
        clearServicesCache()

        fun connectDeviceCallback(isSuccess: Boolean, message: String) {
            if (!message.isDeviceMessage()) {
                disconnectDevice()
            }

            callback(isSuccess, message)
        }

        startTimerForTimeout { isSuccess, error ->
            Log.d("helow", "timeout")
            connectDeviceCallback(isSuccess, error)
        }
    }

    private fun startTimerForTimeout(callback: (Boolean, String) -> Unit) {
        timeoutCallback = callback
        val duration: Long = 10_000 // 10s in millisecond
        timer = Timer()
        timer?.schedule(duration) {
            timeoutCallback(false, context.getString(R.string.connection_timeout))
        }
    }

    // Cancel the timer
    fun cancelTimer() {
        timer?.cancel()
    }

    override fun toggleWifi(
        flag: Boolean,
        ssid: String,
        pass: String,
        callback: (Boolean, String) -> Unit
    ) {
        if (flag) { // turn on wifi
            val command = "WiFi=$ssid,$pass".replace("\"", "")
            sendCommand(command)
        } else { // turn off wifi
            sendCommand("WiFi=OFF")
        }

        startTimerForTimeout { isSuccess, err ->
            callback(isSuccess, err)
        }
    }

    override fun sendCommand(command: String) {
        val rx = rx ?: return

        Log.d("helow", "sending command: $command to ${rx.uuid}")

        // Send the password after connection and service discovery
        val commandByte = command.toByteArray(Charsets.US_ASCII)
        // Set write type
        val writeType = when {
            rx.containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE) -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            rx.containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else -> error("Characteristic ${rx.uuid} cannot be written to")
        }

        currentlyRunningCommand = command
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt?.writeCharacteristic(rx, commandByte, writeType)
        } else {
            rx.writeType = writeType
            rx.setValue(commandByte)
            gatt?.writeCharacteristic(this.rx)
        }
    }

    override fun disconnectDevice() {
        if (isConnectionAuthorized.value) {
            sendCommand("END")
            Timer().schedule(1000) {
                gatt?.disconnect()
            }
        } else {
            gatt?.disconnect()
        }

        _isConnectionAuthorized.value = false
    }

    private fun clearServicesCache(): Boolean {
        var isRefreshed = false

        try {
            val localMethod = gatt?.javaClass?.getMethod("refresh")
            if (localMethod != null) {
                isRefreshed = (localMethod.invoke(gatt) as Boolean)
                Log.d("helow", "Gatt cache refresh successful: $isRefreshed")
            }
        } catch (localException: Exception) {
            Log.e("helow", "An exception occured while refreshing device: $localException")
        }

        return isRefreshed
    }

    fun resetLocalState() {
        Log.d("helow", "reseting local state")

        val result = clearServicesCache()
        if (!result) {
            Log.e("helow", "Failed to clear service cache")
        }

        password = null
        service = null
        rx = null
        rx = null
        timer = null
        currentlyRunningCommand = null
        retryCount = 0
        _connectedDevice.value = null
        _isConnectionAuthorized.value = false
        this@AndroidBluetoothController.gatt = null
    }

    override fun resetDevices() {
        _scannedDevices.value = emptyList()
        _pairedDevices.value = emptyList()
    }
}

fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
    return properties and property != 0
}