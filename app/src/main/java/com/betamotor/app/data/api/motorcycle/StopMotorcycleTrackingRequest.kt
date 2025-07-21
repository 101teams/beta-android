package com.betamotor.app.data.api.motorcycle

import kotlinx.serialization.Serializable

@Serializable
data class StopMotorcycleTrackingRequest(
    val device_id: String,
    val transaction_key: String,
)