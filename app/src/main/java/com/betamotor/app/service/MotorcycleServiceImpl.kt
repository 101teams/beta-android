package com.betamotor.app.service

import android.util.Log
import com.betamotor.app.data.api.BaseResponse
import com.betamotor.app.data.api.ErrorResponse
import com.betamotor.app.data.api.ErrorResponse2
import com.betamotor.app.data.api.HttpRoutes
import com.betamotor.app.data.api.motorcycle.BookmarkMotorcycleCreateRequest
import com.betamotor.app.data.api.motorcycle.BookmarkMotorcycleResponse
import com.betamotor.app.data.api.motorcycle.BookmarksItem
import com.betamotor.app.data.api.motorcycle.CreateMotorcycleRequest
import com.betamotor.app.data.api.motorcycle.CreateMotorcycleRespData
import com.betamotor.app.data.api.motorcycle.GenericResponse
import com.betamotor.app.data.api.motorcycle.GenericResponseErrorMessage
import com.betamotor.app.data.api.motorcycle.HistoryMotorcycleTrackingDataItem
import com.betamotor.app.data.api.motorcycle.HistoryMotorcycleTrackingResponse
import com.betamotor.app.data.api.motorcycle.MotorcycleAccessoriesData
import com.betamotor.app.data.api.motorcycle.MotorcycleAccessoriesResponse
import com.betamotor.app.data.api.motorcycle.MotorcycleItem
import com.betamotor.app.data.api.motorcycle.MotorcycleListResponse
import com.betamotor.app.data.api.motorcycle.MotorcycleTypesResponse
import com.betamotor.app.data.api.motorcycle.StarMotorcycleTrackingResponse
import com.betamotor.app.data.api.motorcycle.StartMotorcycleTrackingRequest
import com.betamotor.app.data.api.motorcycle.StopMotorcycleTrackingRequest
import com.betamotor.app.data.api.motorcycle.StopMotorcycleTrackingResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class MotorcycleServiceImpl(
    private val client: HttpClient
): MotorcycleService {
    override suspend fun getMotorcycleTypes(): Pair<MotorcycleTypesResponse?, String> {
        return try {
            val response = client.get {
                url(HttpRoutes.MOTORCYCLE_TYPES)
                contentType(ContentType.Application.Json)
            }.body<MotorcycleTypesResponse>()

            Pair(response, "")
        } catch (e: RedirectResponseException) {
            Pair(null, "Error 3xx: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            val responseAsErrorResponse = e.response.body<ErrorResponse>()
            if (responseAsErrorResponse.errors.isNullOrEmpty()) {
                val response = e.response.body<ErrorResponse2>()
                return Pair(null, response.message.toString())
            } else {
                return Pair(null, responseAsErrorResponse.errors[0]?.message!!)
            }
        } catch (e: ServerResponseException) {
            try{
                val response = e.response.body<ErrorResponse>()
                Pair(null, response.errors?.get(0)?.message!!)
            } catch (er: Exception) {
                Pair(null, "Error 5xx: ${e.response.status.description}")
            }
        } catch (e: Exception) {
            Pair(null, "Error: ${e.message}")
        }
    }

    override suspend fun getMotorcycles(): Pair<List<MotorcycleItem>?, String> {
        return try {
            val response = client.get {
                url(HttpRoutes.MOTORCYCLE)
                contentType(ContentType.Application.Json)
            }.body<MotorcycleListResponse>()

            Pair(response.data.motorcycles, "")
        } catch (e: RedirectResponseException) {
            Pair(null, "Error 3xx: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            val responseAsErrorResponse = e.response.body<ErrorResponse>()
            if (responseAsErrorResponse.errors.isNullOrEmpty()) {
                val response = e.response.body<ErrorResponse2>()
                return Pair(null, response.message.toString())
            } else {
                return Pair(null, responseAsErrorResponse.errors[0]?.message!!)
            }
        } catch (e: ServerResponseException) {
            try{
                val response = e.response.body<ErrorResponse>()
                Pair(null, response.errors?.get(0)?.message!!)
            } catch (er: Exception) {
                Pair(null, "Error 5xx: ${e.response.status.description}")
            }
        } catch (e: ResponseException) {
            if (e.response.status.value == 401) {
                Pair(null, "${e.response.status.value}")
            } else {
                try{
                    val response = e.response.body<ErrorResponse>()
                    Pair(null, response.errors?.get(0)?.message!!)
                } catch (er: Exception) {
                    Pair(null, "Error 5xx: ${e.response.status.description}")
                }
            }
        } catch (e: Exception) {
            Pair(null, "Error: ${e.message}")
        }
    }

    override suspend fun saveMotorcycle(device: CreateMotorcycleRequest): Pair<Boolean?, String> {
        return try {
            val response = client.post {
                url(HttpRoutes.MOTORCYCLE)
                contentType(ContentType.Application.Json)
                setBody(device)
            }.body<GenericResponse<CreateMotorcycleRespData>>()
            Pair(response.status == "success", "")
        } catch (e: RedirectResponseException) {
            Pair(null, "Error 3xx: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            if (e.response.body<GenericResponse<GenericResponseErrorMessage>>().data?.message != null) {
                throw Exception(e.response.body<GenericResponse<GenericResponseErrorMessage>>().data?.message)
            }

            val responseAsErrorResponse = e.response.body<ErrorResponse>()
            if (responseAsErrorResponse.errors.isNullOrEmpty()) {
                val response = e.response.body<ErrorResponse2>()
                throw Exception(response.message.toString())
            } else {
                throw Exception(responseAsErrorResponse.errors[0]?.message!!)
            }
        } catch (e: ServerResponseException) {
            try{
                val response = e.response.body<ErrorResponse>()
                Pair(null, response.errors?.get(0)?.message!!)
            } catch (er: Exception) {
                Pair(null, "Error 5xx: ${e.response.status.description}")
            }
        } catch (e: Exception) {
            Pair(null, "Error: ${e.message}")
        }
    }

    override suspend fun removeMotorcycle(macAddress: String): Pair<Boolean?, String> {
        // Implement the logic to remove a device from the backend
        return Pair(true, "") // Placeholder
    }

    override suspend fun updateLastConnected(macAddress: String): Pair<Boolean?, String> {
        // Implement the logic to update the last connected timestamp on the backend
        return Pair(true, "") // Placeholder
    }

    override suspend fun startTracking(data: StartMotorcycleTrackingRequest): Pair<StarMotorcycleTrackingResponse?, String> {
        return try {
            val response = client.post {
                url(HttpRoutes.START_TRACKING_MOTORCYCLE)
                contentType(ContentType.Application.Json)
                setBody(data)
            }.body<GenericResponse<StarMotorcycleTrackingResponse>>()
            Pair(response.data, "")
        } catch (e: RedirectResponseException) {
            Pair(null, "Error 3xx: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            if (e.response.body<GenericResponse<GenericResponseErrorMessage>>().data?.message != null) {
                throw Exception(e.response.body<GenericResponse<GenericResponseErrorMessage>>().data?.message)
            }

            val responseAsErrorResponse = e.response.body<ErrorResponse>()
            if (responseAsErrorResponse.errors.isNullOrEmpty()) {
                val response = e.response.body<ErrorResponse2>()
                throw Exception(response.message.toString())
            } else {
                throw Exception(responseAsErrorResponse.errors[0]?.message!!)
            }
        } catch (e: ServerResponseException) {
            try{
                val response = e.response.body<ErrorResponse>()
                Pair(null, response.errors?.get(0)?.message!!)
            } catch (er: Exception) {
                Pair(null, "Error 5xx: ${e.response.status.description}")
            }
        } catch (e: Exception) {
            Pair(null, "Error: ${e.message}")
        }
    }

    override suspend fun stopTracking(data: StopMotorcycleTrackingRequest): Pair<Boolean?, String> {
        return try {
            val response = client.post {
                url(HttpRoutes.STOP_TRACKING_MOTORCYCLE)
                contentType(ContentType.Application.Json)
                setBody(data)
            }.body<GenericResponse<StopMotorcycleTrackingResponse>>()
            Pair(response.status == "success", "")
        } catch (e: RedirectResponseException) {
            Pair(null, "Error 3xx: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            if (e.response.body<GenericResponse<GenericResponseErrorMessage>>().data?.message != null) {
                throw Exception(e.response.body<GenericResponse<GenericResponseErrorMessage>>().data?.message)
            }

            val responseAsErrorResponse = e.response.body<ErrorResponse>()
            if (responseAsErrorResponse.errors.isNullOrEmpty()) {
                val response = e.response.body<ErrorResponse2>()
                throw Exception(response.message.toString())
            } else {
                throw Exception(responseAsErrorResponse.errors[0]?.message!!)
            }
        } catch (e: ServerResponseException) {
            try{
                val response = e.response.body<ErrorResponse>()
                Pair(null, response.errors?.get(0)?.message!!)
            } catch (er: Exception) {
                Pair(null, "Error 5xx: ${e.response.status.description}")
            }
        } catch (e: Exception) {
            Pair(null, "Error: ${e.message}")
        }
    }

    override suspend fun getTrackingHistory(transactionID: String): Pair<List<HistoryMotorcycleTrackingDataItem>?, String> {
        return try {
            val response = client.get {
                url(HttpRoutes.HISTORY_TRACKING_MOTORCYCLE + "/${transactionID}")
                contentType(ContentType.Application.Json)
            }.body<HistoryMotorcycleTrackingResponse>()

            Pair(response.data, "")
        } catch (e: RedirectResponseException) {
            Pair(null, "Error 3xx: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            val responseAsErrorResponse = e.response.body<ErrorResponse>()
            if (responseAsErrorResponse.errors.isNullOrEmpty()) {
                val response = e.response.body<ErrorResponse2>()
                return Pair(null, response.message.toString())
            } else {
                return Pair(null, responseAsErrorResponse.errors[0]?.message!!)
            }
        } catch (e: ServerResponseException) {
            try{
                val response = e.response.body<ErrorResponse>()
                Pair(null, response.errors?.get(0)?.message!!)
            } catch (er: Exception) {
                Pair(null, "Error 5xx: ${e.response.status.description}")
            }
        } catch (e: Exception) {
            Pair(null, "Error: ${e.message}")
        }
    }

    override suspend fun getMotorcyclesAccessories(vin: String): Pair<MotorcycleAccessoriesData?, String> {
        return try {
            val response = client.get {
                url("${HttpRoutes.MOTORCYCLE_ACCESSORIES}?vin=$vin")
                contentType(ContentType.Application.Json)
            }.body<MotorcycleAccessoriesResponse>()

            Pair(response.data, "")
        } catch (e: RedirectResponseException) {
            Pair(null, "Error 3xx: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            val responseAsErrorResponse = e.response.body<ErrorResponse>()
            if (responseAsErrorResponse.errors.isNullOrEmpty()) {
                val response = e.response.body<ErrorResponse2>()
                return Pair(null, response.message.toString())
            } else {
                return Pair(null, responseAsErrorResponse.errors[0]?.message!!)
            }
        } catch (e: ServerResponseException) {
            try{
                val response = e.response.body<ErrorResponse>()
                Pair(null, response.errors?.get(0)?.message!!)
            } catch (er: Exception) {
                Pair(null, "Error 5xx: ${e.response.status.description}")
            }
        } catch (e: Exception) {
            Pair(null, "Error: ${e.message}")
        }
    }

    override suspend fun getBookmarksMotorcycles(): Pair<List<BookmarksItem?>?, String> {
        return try {
            val response = client.get {
                url(HttpRoutes.MOTORCYCLE_BOOKMARKS)
                contentType(ContentType.Application.Json)
            }.body<BookmarkMotorcycleResponse>()

            Pair(response.data?.bookmarks!!, "")
        } catch (e: RedirectResponseException) {
            Pair(null, "Error 3xx: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            val responseAsErrorResponse = e.response.body<ErrorResponse>()
            if (responseAsErrorResponse.errors.isNullOrEmpty()) {
                val response = e.response.body<ErrorResponse2>()
                return Pair(null, response.message.toString())
            } else {
                return Pair(null, responseAsErrorResponse.errors[0]?.message!!)
            }
        } catch (e: ServerResponseException) {
            try{
                val response = e.response.body<ErrorResponse>()
                Pair(null, response.errors?.get(0)?.message!!)
            } catch (er: Exception) {
                Pair(null, "Error 5xx: ${e.response.status.description}")
            }
        } catch (e: Exception) {
            Pair(null, "Error: ${e.message}")
        }
    }

    override suspend fun saveBookmarksMotorcycle(data: BookmarkMotorcycleCreateRequest): Pair<Boolean?, String> {
        return try {
            val response = client.post {
                url(HttpRoutes.MOTORCYCLE_BOOKMARKS)
                contentType(ContentType.Application.Json)
                setBody(data)
            }.body<GenericResponse<BaseResponse>>()
            Pair(response.status == "success", "")
        } catch (e: RedirectResponseException) {
            Pair(null, "Error 3xx: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            if (e.response.body<GenericResponse<GenericResponseErrorMessage>>().data?.message != null) {
                throw Exception(e.response.body<GenericResponse<GenericResponseErrorMessage>>().data?.message)
            }

            val responseAsErrorResponse = e.response.body<ErrorResponse>()
            if (responseAsErrorResponse.errors.isNullOrEmpty()) {
                val response = e.response.body<ErrorResponse2>()
                throw Exception(response.message.toString())
            } else {
                throw Exception(responseAsErrorResponse.errors[0]?.message!!)
            }
        } catch (e: ServerResponseException) {
            try{
                val response = e.response.body<ErrorResponse>()
                Pair(null, response.errors?.get(0)?.message!!)
            } catch (er: Exception) {
                Pair(null, "Error 5xx: ${e.response.status.description}")
            }
        } catch (e: Exception) {
            Pair(null, "Error: ${e.message}")
        }
    }

    override suspend fun deleteBookmarksMotorcycle(id: Int): Pair<Boolean?, String> {
        return try {
            val response = client.delete {
                url("${HttpRoutes.MOTORCYCLE_BOOKMARKS}/$id")
            }.body<GenericResponse<BaseResponse>>()
            Pair(response.status == "success", "")
        } catch (e: RedirectResponseException) {
            Pair(null, "Error 3xx: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            if (e.response.body<GenericResponse<GenericResponseErrorMessage>>().data?.message != null) {
                throw Exception(e.response.body<GenericResponse<GenericResponseErrorMessage>>().data?.message)
            }

            val responseAsErrorResponse = e.response.body<ErrorResponse>()
            if (responseAsErrorResponse.errors.isNullOrEmpty()) {
                val response = e.response.body<ErrorResponse2>()
                throw Exception(response.message.toString())
            } else {
                throw Exception(responseAsErrorResponse.errors[0]?.message!!)
            }
        } catch (e: ServerResponseException) {
            try{
                val response = e.response.body<ErrorResponse>()
                Pair(null, response.errors?.get(0)?.message!!)
            } catch (er: Exception) {
                Pair(null, "Error 5xx: ${e.response.status.description}")
            }
        } catch (e: Exception) {
            Pair(null, "Error: ${e.message}")
        }
    }
}