package com.betamotor.app.service

import com.betamotor.app.data.api.google.GoogleAltitudeResponse
import com.betamotor.app.data.api.motorcycle.CreateMotorcycleRequest
import com.betamotor.app.data.api.motorcycle.MotorcycleItem
import com.betamotor.app.data.api.motorcycle.MotorcycleTypesResponse

interface GoogleService {
    suspend fun getAltitude(latitude: Double, longitude: Double): Pair<GoogleAltitudeResponse?, String>
}
