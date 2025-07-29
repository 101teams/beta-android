package com.betamotor.app.data.api.motorcycle

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MotorcycleAccessoriesResponse(
	@SerialName("data")
	val data: MotorcycleAccessoriesData? = null,

	@SerialName("message")
	val message: String? = null,

	@SerialName("status")
	val status: String? = null
)

@Serializable
data class MotorcycleAccessoriesGroupTitle(

	@SerialName("DE")
	val dE: String? = null,

	@SerialName("EN")
	val eN: String? = null,

	@SerialName("IT")
	val iT: String? = null,

	@SerialName("FR")
	val fR: String? = null,

	@SerialName("ES")
	val eS: String? = null
)

@Serializable
data class MotorcycleAccessoriesData(

	@SerialName("modelDescription")
	val modelDescription: String? = null,

	@SerialName("serialID")
	val serialID: String? = null,

	@SerialName("modelID")
	val modelID: String? = null,

	@SerialName("contents")
	val contents: List<MotorcycleAccessoriesContentsItem?>? = null,

	@SerialName("accessories")
	val accessories: List<MotorcycleAccessoriesAccessoriesItem?>? = null
)

@Serializable
data class MotorcycleAccessoriesContentsItem(

	@SerialName("file")
	val file: String? = null,

	@SerialName("name")
	val name: String? = null,

	@SerialName("description")
	val description: String? = null,

	@SerialName("modified")
	val modified: String? = null
)

@Serializable
data class MotorcycleAccessoriesTitle(

	@SerialName("DE")
	val dE: String? = null,

	@SerialName("EN")
	val eN: String? = null,

	@SerialName("IT")
	val iT: String? = null,

	@SerialName("FR")
	val fR: String? = null,

	@SerialName("ES")
	val eS: String? = null
)

@Serializable
data class MotorcycleAccessoriesDescription(

	@SerialName("DE")
	val dE: String? = null,

	@SerialName("EN")
	val eN: String? = null,

	@SerialName("IT")
	val iT: String? = null,

	@SerialName("FR")
	val fR: String? = null,

	@SerialName("ES")
	val eS: String? = null
)

@Serializable
data class MotorcycleAccessoriesImagesItem(

	@SerialName("path")
	val path: String? = null,

	@SerialName("last_update")
	val lastUpdate: String? = null,

	@SerialName("type")
	val type: String? = null
)

@Serializable
data class MotorcycleAccessoriesAccessoriesItem(

	@SerialName("group_title")
	val groupTitle: MotorcycleAccessoriesGroupTitle? = null,

	@SerialName("images")
	val images: List<MotorcycleAccessoriesImagesItem?>? = null,

	@SerialName("description")
	val description: MotorcycleAccessoriesDescription? = null,

	@SerialName("grouping_code")
	val groupingCode: String? = null,

	@SerialName("sku")
	val sku: String? = null,

	@SerialName("category")
	val category: String? = null,

	@SerialName("title")
	val title: MotorcycleAccessoriesTitle? = null,

	@SerialName("type")
	val type: String? = null,

	@SerialName("brand")
	val brand: String? = null
)
