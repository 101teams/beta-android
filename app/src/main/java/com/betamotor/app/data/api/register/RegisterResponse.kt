package com.betamotor.app.data.api.register

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(

	@SerialName("data")
	val data: RegisterData? = null,

	@SerialName("message")
	val message: String? = null,

	@SerialName("status")
	val status: String? = null
)

@Serializable
data class RegisterData(

	@SerialName("user")
	val user: User? = null
)

@Serializable
data class User(

	@SerialName("verified")
	val verified: Boolean? = null,

	@SerialName("fullName")
	val fullName: String? = null,

	@SerialName("id")
	val id: Int? = null,

	@SerialName("email")
	val email: String? = null
)
