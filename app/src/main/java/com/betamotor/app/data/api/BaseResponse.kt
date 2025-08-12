package com.betamotor.app.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse(
	@SerialName("status")
	val status: String? = null
)
