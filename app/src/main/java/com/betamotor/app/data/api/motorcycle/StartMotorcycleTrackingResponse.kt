package com.betamotor.app.data.api.motorcycle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StarMotorcycleTrackingResponse(

	@SerialName("data")
	val data: StarMotorcycleTrackingData? = null,

	@SerialName("status")
	val status: String? = null
)

@Serializable
data class StarMotorcycleTrackingData(

	@SerialName("transaction_key")
	val transactionKey: String? = null
)
