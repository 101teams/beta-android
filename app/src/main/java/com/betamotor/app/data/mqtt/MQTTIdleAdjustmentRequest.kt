package com.betamotor.app.data.mqtt

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MQTTIdleAdjustmentRequest(

	@SerialName("idleTarget")
	val idleTarget: Int? = null,

	@SerialName("adjustment")
	val adjustment: Int? = null
)
