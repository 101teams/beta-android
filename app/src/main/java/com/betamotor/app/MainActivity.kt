package com.betamotor.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.betamotor.app.navigation.Navigation
import com.betamotor.app.service.LogNotificationService
import com.betamotor.app.theme.AppTheme
import com.betamotor.app.utils.LocalLogging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var logger: LocalLogging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.writeLog("Application started")

        val serviceIntent = Intent(this, LogNotificationService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        setContent {
            AppTheme {
                Navigation()
            }
        }
    }
}

fun Context.findActivity() : Activity? = when(this){
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}