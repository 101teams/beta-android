package com.betamotor.app.data.api.motorcycle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookmarkMotorcycleResponse(

	@SerialName("data")
	val data: BookmarkData? = null,

	@SerialName("status")
	val status: String? = null
)

@Serializable
data class BookmarkData(

	@SerialName("bookmarks")
	val bookmarks: List<BookmarksItem?>? = null
)

@Serializable
data class MotorcycleType(

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

	@SerialName("imageType")
	val imageType: String? = null,

	@SerialName("updatedAt")
	val updatedAt: String? = null
)

@Serializable
data class BookmarksItem(

	@SerialName("createdAt")
	val createdAt: String? = null,

	@SerialName("motorcycleName")
	val motorcycleName: String? = null,

	@SerialName("motorcycleTypeId")
	val motorcycleTypeId: Int? = null,

	@SerialName("motorcycleType")
	val motorcycleType: MotorcycleType? = null,

	@SerialName("vin")
	val vin: String? = null,

	@SerialName("id")
	val id: Int? = null,

	@SerialName("userId")
	val userId: Int? = null,

	@SerialName("updatedAt")
	val updatedAt: String? = null
)
