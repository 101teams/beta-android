package com.betamotor.app.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalLogging @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mqttHelper: MQTTHelper
) {
    private val fileName = "app_log.txt"
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun writeLog(log: String) {
        Log.d("helow", "localLogging write: $log")

        // Publish message in background
        mqttHelper.publishMessage("BetaDebug", log)

        // Run file writing in background thread
        coroutineScope.launch {
            try {
                val file = File(context.filesDir, fileName)

                // Create the file if it doesn't exist
                if (!file.exists()) {
                    file.createNewFile()
                }

                val timestampMillis = System.currentTimeMillis()
                val timestamp = dateFormat.format(timestampMillis)

                FileOutputStream(file, true).use { fos ->
                    val logJson = """{"message": "$log", "timestamp": "$timestamp"}"""
                    fos.write(logJson.toByteArray())
                    fos.write("\n".toByteArray())
                }
            } catch (e: Exception) {
                // Handle exception in a way that doesn't create a circular dependency
                Log.e("LocalLogging", "Error writing log: ${e.message}")
                mqttHelper.publishMessage("BetaDebug", e.message.toString())
                e.printStackTrace()
            }
        }
    }

    fun readLog(): List<Pair<String?, String?>> {
        val logs = mutableListOf<Pair<String?, String?>>()
        val file = File(context.filesDir, fileName)

        // Check if the file exists before attempting to read
        if (!file.exists()) {
            // File doesn't exist yet, return empty list
            return logs
        }

        return try {
            FileInputStream(file).use { fis ->
                val reader = BufferedReader(InputStreamReader(fis))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    val json = JSONObject(line)
                    val message = json.getString("message")
                    val timestamp = json.getString("timestamp")
                    logs.add(Pair(timestamp, message))
                }
            }

            logs
        } catch (e: Exception) {
            mqttHelper.publishMessage("BetaDebug", e.message.toString())
            e.printStackTrace()
            logs
        }
    }

    fun clearLog() {
        coroutineScope.launch {
            try {
                val file = File(context.filesDir, fileName)
                FileOutputStream(file).use { fos ->
                    fos.write("".toByteArray())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

@HiltViewModel
class LoggerViewModel @Inject constructor(
    private val localLogging: LocalLogging
) : ViewModel() {

    fun writeLog(message: String) {
        localLogging.writeLog(message)
    }

    fun readLog(): List<Pair<String?, String?>> {
        return localLogging.readLog()
    }

    fun clearLog() {
        localLogging.clearLog()
    }
}