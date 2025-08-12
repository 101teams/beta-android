package com.betamotor.app.data.api.motorcycle

import kotlinx.serialization.Serializable

@Serializable
data class BookmarkMotorcycleCreateRequest(
    val vin: String,
    val motorcycleName: String,
    val motorcycleTypeId: String,
)