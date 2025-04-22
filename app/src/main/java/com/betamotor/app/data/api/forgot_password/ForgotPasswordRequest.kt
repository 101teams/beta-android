package com.betamotor.app.data.api.forgot_password

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequest(
    val email: String,
)