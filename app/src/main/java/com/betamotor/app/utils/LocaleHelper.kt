package com.betamotor.app.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Helper class to manage locale changes in the app
 */
object LocaleHelper {
    
    /**
     * Update the app's locale configuration
     */
    fun setLocale(context: Context, languageCode: String): Context {
        return if (languageCode == "system") {
            // Reset to system default
            updateResources(context, getSystemLocale())
        } else {
            // Apply selected language
            updateResources(context, Locale(languageCode))
        }
    }
    
    /**
     * Get the currently selected locale
     */
    fun getLocale(languageCode: String): Locale {
        return if (languageCode == "system") {
            getSystemLocale()
        } else {
            Locale(languageCode)
        }
    }
    
    /**
     * Get the system's default locale
     */
    private fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Locale.getDefault(Locale.Category.DISPLAY)
        } else {
            @Suppress("DEPRECATION")
            Locale.getDefault()
        }
    }
    
    /**
     * Update the app's resources configuration with the specified locale
     */
    @Suppress("DEPRECATION")
    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        
        val resources = context.resources
        val config = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        } else {
            config.locale = locale
            resources.updateConfiguration(config, resources.displayMetrics)
            return context
        }
    }
}
