package com.betamotor.app.data.api

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(

	@SerialName("errors")
	val errors: List<ErrorsItem?>? = null
)

@Serializable
data class ErrorsItem(

	@SerialName("field")
	val field: String? = null,

	@SerialName("rule")
	val rule: String? = null,

	@SerialName("message")
	val message: String? = null
)
