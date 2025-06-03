package com.betamotor.app

import android.app.Application
import android.content.Context
import com.betamotor.app.utils.LocaleHelper
import com.betamotor.app.utils.MQTTHelper
import com.betamotor.app.utils.PrefManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize app locale based on saved preference
        initAppLocale()
    }
    
    private fun initAppLocale() {
        val prefManager = PrefManager(this)
        val languageCode = prefManager.getCurrentLanguage()
        
        // Apply the saved language setting
        LocaleHelper.setLocale(this, languageCode)
    }
    
    override fun attachBaseContext(base: Context) {
        // Apply the locale when the application context is being created
        val prefManager = PrefManager(base)
        val languageCode = prefManager.getCurrentLanguage()
        
        super.attachBaseContext(LocaleHelper.setLocale(base, languageCode))
    }
}