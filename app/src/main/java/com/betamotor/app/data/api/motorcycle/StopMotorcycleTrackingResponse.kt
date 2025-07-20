package com.betamotor.app.data.api.motorcycle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StopMotorcycleTrackingResponse(

	@SerialName("data")
	val data: StopMotorcycleData? = null,

	@SerialName("status")
	val status: String? = null
)

@Serializable
data class StopMotorcycleData(

	@SerialName("transaction_key")
	val transactionKey: String? = null
)
