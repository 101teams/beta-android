package com.betamotor.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.betamotor.app.navigation.Navigation
import com.betamotor.app.service.LogNotificationService
import com.betamotor.app.theme.AppTheme
import com.betamotor.app.utils.LocalLogging
import com.betamotor.app.utils.LocaleHelper
import com.betamotor.app.utils.PrefManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceIntent = Intent(this, LogNotificationService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        setContent {
            AppTheme {
                Navigation()
            }
        }
    }
    
    override fun attachBaseContext(newBase: Context) {
        val prefManager = PrefManager(newBase)
        val languageCode = prefManager.getCurrentLanguage()
        
        // Apply the saved locale to this activity's context
        super.attachBaseContext(LocaleHelper.setLocale(newBase, languageCode))
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-apply the language configuration when system configuration changes
        val prefManager = PrefManager(this)
        val languageCode = prefManager.getCurrentLanguage()
        LocaleHelper.setLocale(this, languageCode)
    }
}

fun Context.findActivity() : Activity? = when(this){
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}