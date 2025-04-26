package com.betamotor.app.utils

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import android.provider.Settings

class MQTTHelper(context: Context) {
    private val serverUri = "tcp://85.215.117.250:1883"
    private val clientId = "AndroidClient_" + getUniqueDeviceId(context)
    private val username = "beta-app"
    private val passwordd = "beta-app"

    private val mqttClient = MqttClient(serverUri, clientId, null)

    fun getUniqueDeviceId(context: Context): String {
        val deviceId: String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return deviceId
    }

    init {
        connect()
    }

    private fun connect() {
        try {
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                userName = username
                password = passwordd.toCharArray()
            }

            mqttClient.connect(options)
        } catch (e: Exception) {
            Log.e("helow", "Error connecting to MQTT broker: ${e.message}")
        }
    }

    fun publishMessage(topic: String, message: String) {
        try {
            Log.d("MQTT", "$topic => $message")
            val mqttMessage = MqttMessage()
            mqttMessage.payload = message.toByteArray()
            mqttClient.publish(topic, mqttMessage)
        } catch (e: Exception) {
            Log.e("helow", "Error publishing message: ${e.message}")
        }
    }
}
