package com.betamotor.app

import android.app.Application
import com.betamotor.app.utils.MQTTHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApp : Application() {
    @Inject
    lateinit var mqttHelper: MQTTHelper

    override fun onTerminate() {
        super.onTerminate()
        mqttHelper.disconnect()
    }
}