package com.betamotor.app.utils

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale

class LocalLogging {
    private val fileName = "app_log.txt"

    fun writeLog(context: Context, log: String) {
        val file = File(context.filesDir, fileName)

        try {
            val timestampMillis = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val timestamp = sdf.format(timestampMillis)

            FileOutputStream(file, true).use { fos ->
                val logJson = """{"message": "$log", "timestamp": "$timestamp"}"""
                fos.write(logJson.toByteArray())
                fos.write("\n".toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun readLog(context: Context): List<Pair<String?, String?>> {
        val file = File(context.filesDir, fileName)

        val logs = mutableListOf<Pair<String?, String?>>()

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
        } catch (e: IOException) {
            e.printStackTrace()
            logs
        }
    }
    fun clearLog(context: Context) {
        val file = File(context.filesDir, fileName)

        try {
            FileOutputStream(file).use { fos ->
                fos.write("".toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}