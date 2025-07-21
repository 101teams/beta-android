package com.betamotor.app.data.api.motorcycle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HistoryMotorcycleTrackingResponse(

	@SerialName("data")
	val data: List<HistoryMotorcycleTrackingDataItem>? = null,

	@SerialName("status")
	val status: String? = null
)

@Serializable
data class HistoryMotorcycleTrackingDataItem(

	@SerialName("altitude")
	val altitude: String? = null,

	@SerialName("latitude")
	val latitude: String? = null,

	@SerialName("_time")
	val time: String? = null,

	@SerialName("rpm")
	val rpm: String? = null,

	@SerialName("speed")
	val speed: String? = null,

	@SerialName("longitude")
	val longitude: String? = null
)
