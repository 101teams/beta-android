package com.idrolife.app.data.api.forgot_password

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordResponse(
	@SerialName("status")
	val status: String? = null
)
