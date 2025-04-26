package com.betamotor.app.utils

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.*

class MQTTHelper(context: Context) {

    private val serverUri = "tcp://85.215.117.250:1883"
    private val clientId = "AndroidClient_" + System.currentTimeMillis()
    private val username = "beta-app"
    private val passwordd = "beta-app"

    private val mqttClient = MqttClient(serverUri, clientId, null)

    init {
        connect()
    }

    private fun connect() {
        val options = MqttConnectOptions().apply {
            isCleanSession = true
            userName = username
            password = passwordd.toCharArray()
        }

        mqttClient.connect(options)
    }

    fun publishMessage(topic: String, message: String) {
        try {
            Log.d("MQTT", "$topic => $message")
            val mqttMessage = MqttMessage()
            mqttMessage.payload = message.toByteArray()
            mqttClient.publish(topic, mqttMessage)
        } catch (e: MqttException) {
            Log.e("MQTT", "Error publishing message: ${e.message}")
        }
    }
}
