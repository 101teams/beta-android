package com.betamotor.app.data.api.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(

	@SerialName("data")
	val data: User? = null,

	@SerialName("message")
	val message: String? = null,

	@SerialName("status")
	val status: String? = null
)

@Serializable
data class User(

	@SerialName("abilities")
	val abilities: List<String?>? = null,

	@SerialName("lastUsedAt")
	val lastUsedAt: String? = null,

	@SerialName("name")
	val name: String? = null,

	@SerialName("type")
	val type: String? = null,

	@SerialName("expiresAt")
	val expiresAt: String? = null,

	@SerialName("token")
	val token: String? = null
)
