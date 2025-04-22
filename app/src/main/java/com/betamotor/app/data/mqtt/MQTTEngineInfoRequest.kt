package com.betamotor.app.data.mqtt

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MQTTEngineInfoRequest(

	@SerialName("ecuDrw")
	val ecuDrw: String? = null,

	@SerialName("homolCode")
	val homolCode: String? = null,

	@SerialName("ecuSw")
	val ecuSw: String? = null,

	@SerialName("ecuHw")
	val ecuHw: String? = null,

	@SerialName("vin")
	val vin: String? = null,

	@SerialName("calibration")
	val calibration: String? = null
)
