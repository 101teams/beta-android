package com.betamotor.app.data.api.google

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleAltitudeResponse(

	@SerialName("results")
	val results: List<ResultsItem?>? = null,

	@SerialName("status")
	val status: String? = null
)

@Serializable
data class Location(

	@SerialName("lng")
	val lng: Double? = null,

	@SerialName("lat")
	val lat: Double? = null
)
@Serializable

data class ResultsItem(

	@SerialName("elevation")
	val elevation: Double? = null,

	@SerialName("location")
	val location: Location? = null,

	@SerialName("resolution")
	val resolution: Double? = null
)
