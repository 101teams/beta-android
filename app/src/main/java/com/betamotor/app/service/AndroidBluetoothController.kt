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
import com.betamotor.app.utils.LocalLogging
import com.betamotor.app.utils.MQTTHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.reflect.Method
import java.nio.ByteBuffer
import java.util.Timer
import java.util.UUID
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask
import kotlin.experimental.and
import kotlin.experimental.xor

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context
) : BluetoothController {
    private val deviceNameFilter = "Beta"
    private val SCSUuid = UUID.fromString("b6ff6ee9-90bf-4f16-8f83-922db0431472")
    private val SCScharUuidWx = UUID.fromString("4c069b22-5a1b-4d8f-a3f1-84fe8b9d901c") // write
    private val SCScharUuidRx = UUID.fromString("58ae4252-fc41-4ea7-baf3-8982586bb53e") // read

    private val DESUuid = UUID.fromString("5638d86e-6590-44cc-a144-c56acf0eb819")
    private val DEScharUuidWx = UUID.fromString("dc5f1ba3-44b0-420a-b0e8-69f686468c67") // write
    private val DEScharUuidRx = UUID.fromString("a8e086f5-e398-4efa-ac30-25fc8be70e1e") // read

    private var SCSService: BluetoothGattService? = null
    private var SCSRX: BluetoothGattCharacteristic? = null
    private var SCSWX: BluetoothGattCharacteristic? = null

    private var DESService: BluetoothGattService? = null
    private var DESRX: BluetoothGattCharacteristic? = null
    private var DESWX: BluetoothGattCharacteristic? = null

    private var password: String? = null
    private var timer: Timer? = null
    private var timeoutCallback: (Boolean, String) -> Unit = { _: Boolean, _: String -> }

    private var onDataReceivedCallback: (Byte, ByteArray) -> Unit = { _: Byte, _: ByteArray -> }

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

    @OptIn(ExperimentalUnsignedTypes::class)
    private val gattCallback = object : BluetoothGattCallback() {

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (characteristic?.value != null) {
                onDataReadReceived(characteristic.value!!, characteristic.uuid)
            }
        }
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            onDataReadReceived(value, characteristic.uuid)
        }
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            LocalLogging().writeLog(context, "ConnectionStateChange ${status} | ${newState}")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        val bondState = gatt?.device?.bondState
                        LocalLogging().writeLog(context, "Bond State $bondState")

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
                                    LocalLogging().writeLog(context, "discover service failed to start")
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
            when (service.uuid) {
                SCSUuid -> {
                    LocalLogging().writeLog(context, "Set SCS Service => ${service.uuid}")

                    val SCSRX = service.getCharacteristic(SCScharUuidRx) ?: return
                    val SCSWX = service.getCharacteristic(SCScharUuidWx) ?: return

                    SCSRX.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    this@AndroidBluetoothController.SCSService = service
                    this@AndroidBluetoothController.SCSRX = SCSRX
                    this@AndroidBluetoothController.SCSWX = SCSWX

                    sendCommandByteSCS(prepareDataPacket(1L, 0xA0.toByte(), byteArrayOf(0x00, 0x00, 0x00, 0x00), byteArrayOf(0x00, 0x00, 0x00, 0x00)))

                    Handler(Looper.getMainLooper()).postDelayed({
                        gatt?.readCharacteristic(SCSRX)
                    },500)
                }
                DESUuid -> {
                    LocalLogging().writeLog(context, "Set DES Service : ${service.uuid}")

                    val DESRX = service.getCharacteristic(DEScharUuidRx) ?: return
                    val DESWX = service.getCharacteristic(DEScharUuidWx) ?: return

                    DESRX.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    this@AndroidBluetoothController.DESService = service
                    this@AndroidBluetoothController.DESRX = DESRX
                    this@AndroidBluetoothController.DESWX = DESWX
                }
                else -> {
                    return
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val gattServices: List<BluetoothGattService> =
                    gatt?.services?.toList() ?: emptyList()
                LocalLogging().writeLog(context, "Services count => ${gattServices.size}")
                for (gattService in gattServices) {
                    setServiceAndChars(gattService)
                }
            } else {
                LocalLogging().writeLog(context, "error. onServicesDiscovered received:  $status")
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

        // use reusable function call for handling change event because of different supported version above
        fun onDataReadReceived(data: ByteArray, fromUUID: UUID) {
            LocalLogging().writeLog(context, "Read Data From => $fromUUID")
            LocalLogging().writeLog(context, "Read Data Received => ${data.joinToString(" ") { String.format("%02X", it.toInt()) }}")
            MQTTHelper(context).publishMessage("BetaDebug", "Read Data Received => ${data.joinToString(" ") { String.format("%02X", it.toInt()) }}")

            if (fromUUID == SCScharUuidRx) {
                if (data.size >= 12) {
                    if (data[4] == 0x01.toByte()) {
                        logByteArray("onDataReadReceivedByte SEED", data)

                        val udata = UByteArray(14) { 0u } // Create an unsigned byte array of size 14 initialized with zeros

                        // Dummy packet from device to central
                        udata[0] = data[0].toUByte()
                        udata[1] = data[1].toUByte()
                        udata[2] = data[2].toUByte()
                        udata[3] = data[3].toUByte()
                        udata[4] = data[4].toUByte()

                        // Packet sessionRXudata00
                        // Seed value from 32 - 63 bit
                        udata[5] = data[5].toUByte()
                        udata[6] = data[6].toUByte()
                        udata[7] = data[7].toUByte()
                        udata[8] = data[8].toUByte()

                        // Packet sessionRXudata00
                        // Seed value from 0 - 31 bit
                        udata[9] = data[9].toUByte()
                        udata[10] = data[10].toUByte()
                        udata[11] = data[11].toUByte()
                        udata[12] = data[12].toUByte()

                        udata[13] = data[13].toUByte()

                        val seed = UIntArray(2)
                        seed[0] = (udata[9].toUInt() shl 24) or
                                (udata[10].toUInt() shl 16) or
                                (udata[11].toUInt() shl 8) or
                                udata[12].toUInt()

                        seed[1] = (udata[5].toUInt() shl 24) or
                                (udata[6].toUInt() shl 16) or
                                (udata[7].toUInt() shl 8) or
                                udata[8].toUInt()

                        // Kunci xTEA 128-bit
                        val key = uintArrayOf(
                            0x74966834u, 0x88aa497fu, 0xb02f4931u, 0x9a353d19u
                        )

                        LocalLogging().writeLog(context, "Convert to unsigned int data from device => ${udata.joinToString(" ") { String.format("%02X", it.toInt()) }}")
                        LocalLogging().writeLog(context, "Seed value (from converted packet) => ${String.format("%08X", seed[0].toInt())} ${String.format("%08X", seed[1].toInt())}")

                        encipher(32u, seed, key)

                        LocalLogging().writeLog(context, "Encipher result => ${String.format("%08X", seed[0].toInt())} ${String.format("%08X", seed[1].toInt())}")

                        val packetWrite = UByteArray(14) { 0u }
                        packetWrite[0] = 0x01u
                        packetWrite[1] = 0x02u
                        packetWrite[2] = 0x03u
                        packetWrite[3] = 0x04u
                        packetWrite[4] = 0xA1u // Authorization Command Request

                        // SessionTxData00 - Key from 32 - 63 bit
                        packetWrite[5] = (seed[1] shr 24).toUByte()
                        packetWrite[6] = (seed[1] shr 16).toUByte()
                        packetWrite[7] = (seed[1] shr 8).toUByte()
                        packetWrite[8] = (seed[1] and 0xFFu).toUByte()

                        // SessionTxData01 - Key from 0 - 31 bit
                        packetWrite[9] = (seed[0] shr 24).toUByte()
                        packetWrite[10] = (seed[0] shr 16).toUByte()
                        packetWrite[11] = (seed[0] shr 8).toUByte()
                        packetWrite[12] = (seed[0] and 0xFFu).toUByte()

                        packetWrite[13] = 0xFAu // Dummy CRC

                        Handler(Looper.getMainLooper()).postDelayed({
                            sendCommandByteSCS(prepareDataPacket(1L,
                                packetWrite[4].toByte(),
                                byteArrayOf(
                                    packetWrite[5].toByte(),
                                    packetWrite[6].toByte(),
                                    packetWrite[7].toByte(),
                                    packetWrite[8].toByte()
                                ),
                                byteArrayOf(packetWrite[9].toByte(),
                                    packetWrite[10].toByte(),
                                    packetWrite[11].toByte(),
                                    packetWrite[12].toByte()
                                )
                            ))

                            // if first received data is available then connection is authorized.
                            if (!isConnectionAuthorized.value) {
                                _isConnectionAuthorized.value = true
                                cancelTimer()
                            }
                        }, 100)

                        timeoutCallback(true, "success")
                    }
                }
            } else if (fromUUID == DEScharUuidRx) {
                if (data.size >= 6) {
                    LocalLogging().writeLog(context, "Read Data DES Success, send callback to ui")
                    onDataReceivedCallback(data[5], data)
                } else {
                    LocalLogging().writeLog(context, "Read Data DES failed, data length < 6")
                    Toast.makeText(
                        context,
                        "Read Data DES failed, data length < 6", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        fun encipher(numRounds: UInt, v: UIntArray, key: UIntArray) {
            var v0 = v[0]
            var v1 = v[1]
            var sum = 0u
            val delta = 0x9E3779B9u

            for (i in 0 until numRounds.toInt()) {
                v0 += (((v1 shl 4) xor (v1 shr 5)) + v1) xor (sum + key[(sum and 3u).toInt()])
                sum += delta
                v1 += (((v0 shl 4) xor (v0 shr 5)) + v0) xor (sum + key[(sum shr 11).toInt() and 3])
            }

            // Update the vector
            v[0] = v0
            v[1] = v1
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

    override fun onReadDataDES(onDataReceived: (Byte, ByteArray) -> Unit) {
        this.onDataReceivedCallback = onDataReceived
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

    fun prepareDataPacket(centralID: Long, byte4: Byte, byte5to8: ByteArray, byte9to12: ByteArray): ByteArray {
        val data = ByteArray(14)

        // Byte 0-3: Central ID
        val centralIdBytes = ByteBuffer.allocate(4).putInt(centralID.toInt()).array()
        System.arraycopy(centralIdBytes, 0, data, 0, 4)

        // Byte 4: 0xA0
        data[4] = byte4

        // Byte 5-8: Custom byte array
        System.arraycopy(byte5to8, 0, data, 5, 4)

        // Byte 9-12: Custom byte array
        System.arraycopy(byte9to12, 0, data, 9, 4)

        // Byte 13: CRC8
        data[13] = calculateCRC(data.copyOfRange(0, 13))

        return data
    }

    fun calculateCRC(data: ByteArray): Byte {
        val polynomial: Byte = 0x2F
        var crc: Byte = 0xFF.toByte()
        val xorValue: Byte = 0xFF.toByte()

        for (byte in data) {
            crc = crc.xor(byte)

            for (j in 0 until 8) {
                crc = if (crc.toInt() and 0x80 != 0) {
                    (crc.toInt() shl 1).xor(polynomial.toInt()).toByte()
                } else {
                    (crc.toInt() shl 1).toByte()
                }
                crc = crc.and(0xFF.toByte()) // Keep crc in byte range
            }
        }

        return crc.xor(xorValue)
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
        try {
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

                }

                callback(isSuccess, message)
            }

            startTimerForTimeout { isSuccess, error ->
                Log.d("helow", "timeout")
                connectDeviceCallback(isSuccess, error)
            }
        } catch (e: IllegalArgumentException) {
            callback(false, "Invalid Bluetooth address: ${device.macAddress}")
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
        val SCSWX = SCSWX ?: return

        Log.d("helow", "sending command: $command to ${SCSWX.uuid}")

        // Send the password after connection and service discovery
        val commandByte = command.toByteArray(Charsets.US_ASCII)
        // Set write type
        val writeType = when {
            SCSWX.containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE) -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            SCSWX.containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else -> error("Characteristic ${SCSWX.uuid} cannot be written to")
        }

        currentlyRunningCommand = command
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt?.writeCharacteristic(SCSWX, commandByte, writeType)
        } else {
            SCSWX.writeType = writeType
            SCSWX.setValue(commandByte)
            gatt?.writeCharacteristic(this.SCSWX)
        }
    }

    override fun sendCommandByteSCS(command: ByteArray) {
        val SCSWX = SCSWX ?: return
        sendCommandByte(command, SCSWX)
    }

    override fun sendCommandByteDES(command: ByteArray) {
        val DESWX = DESWX ?: return

        val data = ByteArray(command.size + 1)
        for ((index, i) in command.withIndex()) {
            data[index] = i
        }

        data[command.size] = calculateCRC(data.copyOfRange(0, command.size - 1))

        sendCommandByte(data, DESWX)

        Handler(Looper.getMainLooper()).postDelayed({
            gatt?.readCharacteristic(DESRX)
        },500)
    }

    private fun sendCommandByte(command: ByteArray, characteristic: BluetoothGattCharacteristic) {
        LocalLogging().writeLog(context, "Sending Command Byte to ${characteristic.uuid} => ${command.joinToString(" ") { String.format("%02X", it.toInt()) }}")

        // Set write type
        val writeType = when {
            characteristic.containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE) -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt?.writeCharacteristic(characteristic, command, writeType)
        } else {
            characteristic.writeType = writeType
            characteristic.value = command
            gatt?.writeCharacteristic(characteristic)
        }
    }

    fun logByteArray(tag: String, byteArray: ByteArray) {
        Log.d(tag, byteArray.size.toString())
        val hexString = byteArray.joinToString(" ") { "%02X".format(it) }
        Log.d(tag, hexString)
    }

    override fun disconnectDevice() {
        if (isConnectionAuthorized.value) {
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
        val result = clearServicesCache()
        if (!result) {
            Log.e("helow", "Failed to clear service cache")
        }

        password = null
        SCSService = null
        SCSWX = null
        SCSRX = null
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