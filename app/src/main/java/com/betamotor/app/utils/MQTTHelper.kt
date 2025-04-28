package com.betamotor.app.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MQTTHelper @Inject constructor(@ApplicationContext private val context: Context) {

    private val serverUri = "tcp://85.215.117.250:1883"
    private val clientId = "AndroidClient_" + System.currentTimeMillis()
    private val username = "beta-app"
    private val passwordd = "beta-app"

    private val mqttClient by lazy {
        MqttClient(serverUri, clientId, null)
    }

    private val isConnected = AtomicBoolean(false)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        connect()
    }

    private fun connect() {
        coroutineScope.launch {
            try {
                val options = MqttConnectOptions().apply {
                    isCleanSession = true
                    userName = username
                    password = passwordd.toCharArray()
                }

                mqttClient.connect(options)
                isConnected.set(true)
                Log.d("MQTT", "Successfully connected to MQTT broker")
            } catch (e: Exception) {
                Log.e("MQTT", "Error connecting to MQTT broker: ${e.message}")
                isConnected.set(false)
            }
        }
    }

    fun publishMessage(topic: String, message: String) {
        coroutineScope.launch {
            try {
                Log.d("MQTT", "$topic => $message")

                // Try to reconnect if not connected
                if (!isConnected.get()) {
                    try {
                        connect()
                    } catch (e: Exception) {
                        Log.e("MQTT", "Reconnection failed: ${e.message}")
                        return@launch
                    }
                }

                val mqttMessage = MqttMessage()
                mqttMessage.payload = message.toByteArray()
                mqttClient.publish(topic, mqttMessage)
            } catch (e: MqttException) {
                Log.e("MQTT", "Error publishing message: ${e.message}")
                isConnected.set(false)
            }
        }
    }

    // For graceful shutdown when the app closes
    fun disconnect() {
        try {
            if (mqttClient.isConnected) {
                mqttClient.disconnect()
                isConnected.set(false)
            }
        } catch (e: MqttException) {
            Log.e("MQTT", "Error disconnecting: ${e.message}")
        }
    }
}

@HiltViewModel
class MqttViewModel @Inject constructor(
    private val mqtt: MQTTHelper
) : ViewModel() {
    fun publishMessage(topic: String, message: String) {
        mqtt.publishMessage(topic, message)
    }
}