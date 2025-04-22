package com.betamotor.app.data.api.motorcycle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MotorcycleListResponse(

	@SerialName("data")
	val data: MotorcycleData? = null,

	@SerialName("status")
	val status: String? = null
)

@Serializable
data class MotorcycleData(

	@SerialName("motorcycles")
	val motorcycles: List<MotorcyclesItem?>? = null
)

@Serializable
data class MotorcyclesItem(

	@SerialName("createdAt")
	val createdAt: String? = null,

	@SerialName("code")
	val code: String? = null,

	@SerialName("imageName")
	val imageName: String? = null,

	@SerialName("imagePath")
	val imagePath: String? = null,

	@SerialName("imageUrl")
	val imageUrl: String? = null,

	@SerialName("name")
	val name: String? = null,

	@SerialName("id")
	val id: Int? = null,

	@SerialName("imageSize")
	val imageSize: Int? = null,

	@SerialName("userId")
	val userId: Int? = null,

	@SerialName("imageType")
	val imageType: String? = null,

	@SerialName("deviceId")
	val deviceId: Int? = null,

	@SerialName("updatedAt")
	val updatedAt: String? = null
)
