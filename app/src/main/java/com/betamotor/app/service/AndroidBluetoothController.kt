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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.betamotor.app.BuildConfig
import com.betamotor.app.R
import com.betamotor.app.data.bluetooth.BluetoothDeviceDomain
import com.betamotor.app.data.bluetooth.FoundDeviceReceiver
import com.betamotor.app.data.bluetooth.toBluetoothDeviceDomain
import com.betamotor.app.data.constants
import com.betamotor.app.utils.LocalLogging
import com.betamotor.app.utils.MQTTHelper
import com.betamotor.app.utils.PrefManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.reflect.Method
import java.nio.ByteBuffer
import java.util.Timer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask
import kotlin.experimental.and
import kotlin.experimental.xor

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context,
    private val mqttHelper: MQTTHelper
) : BluetoothController {
    private val logger = LocalLogging(context)
    private val prefManager = PrefManager(context)
    private val deviceNameFilter = if (BuildConfig.DEBUG) "" else "Beta"
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

    private val dataReceivedCallbacks = ConcurrentHashMap<String, (Byte, ByteArray) -> Unit>()

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

    private val bondStateReceiver = BondStateReceiver { _, bondState, _ ->
        Log.d("helow", "bond state changed: $bondState")
        when(bondState) {
            BluetoothDevice.BOND_BONDED -> {
                Log.d("helow", "device bonded")
            }

            BluetoothDevice.BOND_BONDING -> {
                Log.d("helow", "device bonding")
            }

            BluetoothDevice.BOND_NONE -> {
                Log.d("helow", "device not bonded")
            }
        }
    }

    private val pairingRequestReceiver = PairingRequestReceiver { device, type, pin ->
        Log.d("helow", "pairing request received: type: $type, pin: $pin")

        if (_connectedDevice.value == null) {
            return@PairingRequestReceiver
        }

        if (device.address != _connectedDevice.value?.macAddress) {
            return@PairingRequestReceiver
        }
    }

    fun onSecurityAccessValidation(data: ByteArray) {
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

        logger.writeLog("Convert to unsigned int data from device => ${udata.joinToString(" ") { String.format("%02X", it.toInt()) }}")
        logger.writeLog("Seed value (from converted packet) => ${String.format("%08X", seed[0].toInt())} ${String.format("%08X", seed[1].toInt())}")

        encipher(32u, seed, key)

        logger.writeLog("Encipher result => ${String.format("%08X", seed[0].toInt())} ${String.format("%08X", seed[1].toInt())}")

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

    fun onActiveAccessGranted(data: ByteArray) {
        _isConnectionAuthorized.value = true
        cancelTimer()
    }

    fun getServiceName(uuid: UUID): String {
        return when (uuid) {
            SCSUuid -> return "SCS"
            SCScharUuidRx -> return "SCS RX"
            SCScharUuidWx -> return "SCS WX"
            DESUuid -> return "DES"
            DEScharUuidRx -> return "DES RX"
            DEScharUuidWx -> return "DES WX"
            else -> "Unknown Service => $uuid"
        }
    }

    private fun sendSessionBeginRequest() {
        logger.writeLog("Sending SessionBeginRequest (0xA0) to SCS")

        if (SCSWX == null) {
            logger.writeLog("SCSWX is null, cannot send SessionBeginRequest")
            return
        }

        if (SCSRX == null) {
            logger.writeLog("SCSRX is null, cannot send SessionBeginRequest")
            return
        }

        sendCommandByteSCS(
            prepareDataPacket(
                1L,
                0xA0.toByte(),
                byteArrayOf(0x00, 0x00, 0x00, 0x00),
                byteArrayOf(0x00, 0x00, 0x00, 0x00)
            )
        )

        Handler(Looper.getMainLooper()).postDelayed({
            gatt?.readCharacteristic(SCSRX)
        }, 500)
    }

    // use reusable function call for handling change event because of different supported version above
    fun onDataReadReceived(data: ByteArray, fromUUID: UUID) {
        val log = "Source: ${getServiceName(fromUUID)}. Data received => ${data.joinToString(" ") { String.format("%02X", it.toInt()) }}"
        logger.writeLog(log)
        mqttHelper.publishMessage("BetaDebug", log)

        if (fromUUID == SCScharUuidRx) {
            if (data.size < 12) {
                logger.writeLog("Read Data SCS failed, data length < 12")
                return
            }

            val sessionStatus = SessionStatus.fromCode(data[4])
            when (sessionStatus) {
                SessionStatus.HANDSHAKING -> {
                    logger.writeLog("Session Status HANDSHAKING — restarting handshake")
                    sendSessionBeginRequest()
                }

                SessionStatus.NO_SESSION_ACTIVE -> {
                    logger.writeLog("Session Status NO_SESSION_ACTIVE — retrying handshake")
                    sendSessionBeginRequest()
                }

                SessionStatus.SECURITY_ACCESS_VALIDATION -> {
                    logger.writeLog("Session Status SECURITY_ACCESS_VALIDATION — processing seed")
                    onSecurityAccessValidation(data)
                }

                SessionStatus.ACTIVE_ACCESS_GRANTED -> {
                    logger.writeLog("Session Status ACTIVE_ACCESS_GRANTED — session is authorized")
                    onActiveAccessGranted(data)
                }

                SessionStatus.INACTIVE_ACCESS_DENIED -> {
                    logger.writeLog("Session Status INACTIVE_ACCESS_DENIED — session rejected, try reconnecting.")
                    cancelTimer()
                    disconnectDevice()
                    timeoutCallback(false, "Session rejected, please try reconnecting")
                }

                SessionStatus.INACTIVE_ACCESS_DENIED_DEVICE_LOCKED -> {
                    logger.writeLog("Session Status INACTIVE_ACCESS_DENIED_DEVICE_LOCKED — wait 10s then reconnect")
                    cancelTimer()
                    disconnectDevice()
                    timeoutCallback(false, "Device is locked, please wait 10 seconds and try again")
                }

                null -> logger.writeLog("Unknown session status: ${data[4]}")
            }
        } else if (fromUUID == DEScharUuidRx) {
            if (data.size >= 6) {
                logger.writeLog("Read Data DES Success, send callback to ui")
                dataReceivedCallbacks.values.forEach { it(data[5], data) }
            } else {
                logger.writeLog("Read Data DES failed, data length < 6")
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
            logger.writeLog("ConnectionStateChange ${status} | ${newState}")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        val bondState = gatt?.device?.bondState
                        logger.writeLog("Bond State $bondState")

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
                                    logger.writeLog("discover service failed to start")
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

                return
            }

            // handle connection failed, most of the time 133 unknown error
            disconnectDevice()
            gatt?.close()
            resetLocalState()

            when (status) {
                BluetoothGatt.GATT_CONNECTION_CONGESTED -> {
                    logger.writeLog("BLE_CONNECT_ERR: connection congested")
                }

                BluetoothGatt.GATT_FAILURE -> {
                    logger.writeLog("BLE_CONNECT_ERR: connection failure")
                }

                BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> {
                    logger.writeLog("BLE_CONNECT_ERR: insufficient authentication")
                }

                BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> {
                    logger.writeLog("BLE_CONNECT_ERR: insufficient encryption")
                }

                BluetoothGatt.GATT_INVALID_OFFSET -> {
                    logger.writeLog("BLE_CONNECT_ERR: invalid offset")
                }

                BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                    logger.writeLog("BLE_CONNECT_ERR: read not permitted")
                }

                BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> {
                    logger.writeLog("BLE_CONNECT_ERR: request not supported")
                }

                BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                    logger.writeLog("BLE_CONNECT_ERR: write not permitted")
                }

                else -> {
                    logger.writeLog("BLE_CONNECT_ERR: unknown status: $status")
                }
            }
        }

        fun setServiceAndChars(service: BluetoothGattService) {
            when (service.uuid) {
                SCSUuid -> {
                    logger.writeLog("Set SCS Service => ${service.uuid}")

                    val SCSRX = service.getCharacteristic(SCScharUuidRx) ?: return
                    val SCSWX = service.getCharacteristic(SCScharUuidWx) ?: return

                    SCSRX.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    this@AndroidBluetoothController.SCSService = service
                    this@AndroidBluetoothController.SCSRX = SCSRX
                    this@AndroidBluetoothController.SCSWX = SCSWX

                    logger.writeLog("checking session status")
                    gatt?.readCharacteristic(SCSRX)
                }
                DESUuid -> {
                    logger.writeLog("Set DES Service : ${service.uuid}")

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
                logger.writeLog("Services count => ${gattServices.size}")

                if (BuildConfig.DEBUG) {
                    val byteArr = ByteArray(0)
                    onActiveAccessGranted(byteArr)
                }

                for (gattService in gattServices) {
                    setServiceAndChars(gattService)
                }
            } else {
                logger.writeLog("error. onServicesDiscovered received:  $status")
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

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            logger.writeLog("onDescriptorWrite status: $status")
        }
    }

    override fun hasCallback(key: String): Boolean {
        return dataReceivedCallbacks.containsKey(key)
    }

    override fun addOnDataReceivedCallback(key: String, callback: (Byte, ByteArray) -> Unit) {
        dataReceivedCallbacks[key] = callback
    }

    override fun removeOnDataReceivedCallback(key: String) {
        dataReceivedCallbacks.remove(key)
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

        context.registerReceiver(
            bondStateReceiver,
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        )

        context.registerReceiver(
            pairingRequestReceiver,
            IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST)
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

    private fun unpairDevice(macAddress: String) {
        val btDevice = bluetoothAdapter?.getRemoteDevice(macAddress) ?: return
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
        logger.writeLog("connecting to device ${device.macAddress}")
        prefManager.clearMacAddress()

        try {
            // remove bond to make sure notification is sent from ble
            unpairDevice(device.macAddress)

            stopDiscovery()

            val btDevice = bluetoothAdapter?.getRemoteDevice(device.macAddress)

            if (btDevice == null) {
                logger.writeLog("Device not found")
                callback(false, "Device not found")
                return
            } else {
                logger.writeLog("Device found")
            }

            this@AndroidBluetoothController.password = password

            // uses transport_le because sometime gatt error 133 when not using it
            gatt = if (BuildConfig.DEBUG) {
                btDevice.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_AUTO)
            } else {
                btDevice.connectGatt(context, true, gattCallback, BluetoothDevice.TRANSPORT_LE)
            }
            clearServicesCache()

            fun connectDeviceCallback(isSuccess: Boolean, message: String) {
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
        val duration: Long = 20_000 // 10s in millisecond
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

    private fun addCRCtoCommand(command: ByteArray): ByteArray {
        val data = ByteArray(command.size + 1)
        for ((index, i) in command.withIndex()) {
            data[index] = i
        }

        data[command.size] = calculateCRC(data.copyOfRange(0, command.size - 1))
        return data
    }

    override fun sendCommandByteDES(command: ByteArray) {
        if (BuildConfig.DEBUG) { // simulate engine data on debug mode
            if (command[1].toInt() == 0x0101 and 0xFF) { // engine data
                val random = java.util.Random()
                val rpmValue = random.nextInt(65536)
                val gasValue = random.nextInt(65536)

                val rpm = byteArrayOf(0x01, 0x01, 0x00, 0x04, 0x00, 0x01, (rpmValue shr 8).toByte(), (rpmValue and 0xFF).toByte(), 0x55)
                val gas = byteArrayOf(0x01, 0x01, 0x00, 0x04, 0x00, 0x02, (gasValue shr 8).toByte(), (gasValue and 0xFF).toByte(), 0x47)

                if (
                    command[3] == ((constants.RLI_GAS_POSITION shr 8) and 0xFF).toByte() &&
                    command[4] == (constants.RLI_GAS_POSITION and 0xFF).toByte()
                    ) {
                    onDataReadReceived(gas, DEScharUuidRx)
                }

                if (
                    command[3] == ((constants.RLI_ENGINE_SPEED shr 8) and 0xFF).toByte() &&
                    command[4] == (constants.RLI_ENGINE_SPEED and 0xFF).toByte()
                    ) {
                    onDataReadReceived(rpm, DEScharUuidRx)
                }
            }
        }

        val DESWX = DESWX ?: return

        val data = addCRCtoCommand(command)
        sendCommandByte(data, DESWX)

        Handler(Looper.getMainLooper()).postDelayed({
            gatt?.readCharacteristic(DESRX)
        }, 50)
    }

    private fun sendCommandByte(command: ByteArray, characteristic: BluetoothGattCharacteristic) {
        logger.writeLog("Sending Command Byte to ${characteristic.uuid} => ${command.joinToString(" ") { String.format("%02X", it.toInt()) }}")

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

class BondStateReceiver (
    private val onStateChanged: (BluetoothDevice, Int, Int) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }

                val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                val previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)

                device?.let {
                    onStateChanged(it, bondState, previousBondState)
                }
            }
        }
    }
}

class PairingRequestReceiver (
    private val onPairingRequest: (BluetoothDevice, Int, ByteArray?) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothDevice.ACTION_PAIRING_REQUEST -> {
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }

                val type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, -1)
                val pin = intent.getByteArrayExtra(BluetoothDevice.EXTRA_PAIRING_KEY)

                device?.let {
                    onPairingRequest(it, type, pin)
                }
            }
        }
    }
}

enum class SessionStatus(val code: Byte) {
    HANDSHAKING(0xFE.toByte()),
    NO_SESSION_ACTIVE(0x00.toByte()),
    SECURITY_ACCESS_VALIDATION(0x01.toByte()),
    ACTIVE_ACCESS_GRANTED(0x02.toByte()),
    INACTIVE_ACCESS_DENIED(0x03.toByte()),
    INACTIVE_ACCESS_DENIED_DEVICE_LOCKED(0x04.toByte());

    companion object {
        fun fromCode(code: Byte): SessionStatus? = entries.find { it.code == code }
    }

    fun toByte(): Byte = code
}