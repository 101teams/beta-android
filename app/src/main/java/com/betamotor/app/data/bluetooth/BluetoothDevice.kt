package com.betamotor.app.data.bluetooth

import android.net.Uri
import com.betamotor.app.navigation.JsonNavType
import com.google.gson.Gson

typealias BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val identity: String?,
    val macAddress: String,
    var name: String = "-",
) {
    override fun toString(): String {
        return Uri.encode(Gson().toJson(this))
    }
}

class BluetoothDeviceArgType : JsonNavType<BluetoothDevice>() {
    override fun fromJsonParse(value: String): BluetoothDevice {
        return Gson().fromJson(value, BluetoothDevice::class.java)
    }

    override fun BluetoothDevice.getJsonParse(): String {
        return Gson().toJson(this)
    }
}
