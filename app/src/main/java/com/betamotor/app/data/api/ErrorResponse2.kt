package com.betamotor.app.data.api

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse2(

	@SerialName("data")
	val data: List<DataItem?>? = null,

	@SerialName("message")
	val message: String? = null,

	@SerialName("status")
	val status: String? = null
)

@Serializable
data class DataItem(

	@SerialName("field")
	val field: String? = null,

	@SerialName("rule")
	val rule: String? = null,

	@SerialName("message")
	val message: String? = null
)
