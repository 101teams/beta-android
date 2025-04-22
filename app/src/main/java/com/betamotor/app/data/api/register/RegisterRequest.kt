package com.betamotor.app.data.api.register

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
)