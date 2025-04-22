package com.betamotor.app.data.mqtt

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MQTTEngineDiagnoseRequest(

	@SerialName("codes")
	val codes: List<CodesItem?>? = null,

	@SerialName("checkEngineLight")
	val checkEngineLight: Boolean? = null
)

@Serializable
data class CodesItem(

	@SerialName("code")
	val code: String? = null,

	@SerialName("binaryValue")
	val binaryValue: String? = null
)
