package com.betamotor.app.data.api.motorcycle

import kotlinx.serialization.Serializable

@Serializable
data class StartMotorcycleTrackingRequest(
    val device_id: String,
)