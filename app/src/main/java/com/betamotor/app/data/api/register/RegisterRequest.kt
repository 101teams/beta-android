package com.betamotor.app.data.api.register

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val passwordConfirmation: String,
)