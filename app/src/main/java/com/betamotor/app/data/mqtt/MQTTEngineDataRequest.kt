package com.betamotor.app.data.mqtt

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MQTTEngineDataRequest(

	@SerialName("throttle")
	val throttle: Int? = null,

	@SerialName("engineTemp")
	val engineTemp: Int? = null,

	@SerialName("atmPressure")
	val atmPressure: Int? = null,

	@SerialName("opTime")
	val opTime: Int? = null,

	@SerialName("sparkAdv")
	val sparkAdv: Int? = null,

	@SerialName("airTemp")
	val airTemp: Int? = null,

	@SerialName("rpm")
	val rpm: Int? = null
)
