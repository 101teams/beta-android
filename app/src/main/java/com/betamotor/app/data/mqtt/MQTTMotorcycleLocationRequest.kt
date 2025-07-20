package com.betamotor.app.data.mqtt

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MQTTMotorcycleLocationRequest(

	@SerialName("speed")
	val speed: Int? = null,

	@SerialName("altitude")
	val altitude: Int? = null,

	@SerialName("rpm")
	val rpm: Int? = null,

	@SerialName("latitude")
	val latitude: Int? = null,

	@SerialName("longitude")
	val longitude: Int? = null,
)
