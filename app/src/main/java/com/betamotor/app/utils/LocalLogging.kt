package com.betamotor.app.utils

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale

class LocalLogging(val context: Context) {
    private val fileName = "app_log.txt"
    fun writeLog(log: String) {
        Log.d("helow", "localLogging write: $log")
        try {
            val file = File(context.filesDir, fileName)

            // Create the file if it doesn't exist
            if (!file.exists()) {
                file.createNewFile()
            }

            val timestampMillis = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val timestamp = sdf.format(timestampMillis)

            FileOutputStream(file, true).use { fos ->
                val logJson = """{"message": "$log", "timestamp": "$timestamp"}"""
                fos.write(logJson.toByteArray())
                fos.write("\n".toByteArray())
            }
        } catch (e: Exception) {
            MQTTHelper(context).publishMessage("BetaDebug", e.message.toString())
            e.printStackTrace()
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
            MQTTHelper(context).publishMessage("BetaDebug", e.message.toString())
            e.printStackTrace()
            logs
        }
    }
    fun clearLog() {
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