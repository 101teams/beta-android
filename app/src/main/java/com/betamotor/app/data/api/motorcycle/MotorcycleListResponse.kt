package com.betamotor.app.data.api.motorcycle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MotorcycleListResponse(
	@SerialName("data")
	val data: MotorcycleData,

	@SerialName("status")
	val status: String? = null
)

@Serializable
data class MotorcycleTypesResponse(
	@SerialName("data")
	val data: MotorcycleTypesData? = null,
)

@Serializable
data class MotorcycleTypesData(
	@SerialName("motorcyclesTypes")
	val motorcycleTypes: List<MotorcycleTypeItem?>? = null

)

@Serializable
data class MotorcycleData(

	@SerialName("motorcycles")
	val motorcycles: List<MotorcycleItem>? = null
)

@Serializable
data class CreateMotorcycleRequest(
	@SerialName("name")
	val name: String,

	@SerialName("deviceId")
	val deviceId: String,

	@SerialName("devicePassword")
	val password: String,

	@SerialName("macAddress")
	val macAddress: String,

	@SerialName("motorcycleTypeId")
	val motorcycleTypeId: Int,

	@SerialName("bleGattCharRx")
	val bleGattCharRx: String,

	@SerialName("bleGattCharWx")
	val bleGattCharWx: String,
)

@Serializable
data class GenericResponse<T>(
	@SerialName("data")
	val data: T? = null,
	val status: String? = null,
)

@Serializable
data class GenericResponseErrorMessage(
	@SerialName("message")
	val message: String?

)

@Serializable
data class CreateMotorcycleRespData(
	@SerialName("motorcycle")
	val data: MotorcycleItem? = null,
)

@Serializable
data class MotorcycleItem(
	@SerialName("name")
	val name: String,

	@SerialName("macAddress")
	val macAddress: String,

	@SerialName("deviceId")
	val deviceId: String,

	@SerialName("motorcycleTypeId")
	val motorcycleTypeId: Int,

	@SerialName("motorcycleType")
	val motorcycleType: MotorcycleTypeItem? = null,
)

@Serializable
data class MotorcycleTypeItem(

	@SerialName("createdAt")
	val createdAt: String? = null,

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
