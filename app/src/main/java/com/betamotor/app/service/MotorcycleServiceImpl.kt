package com.betamotor.app.service

import com.betamotor.app.data.api.ErrorResponse
import com.betamotor.app.data.api.ErrorResponse2
import com.betamotor.app.data.api.HttpRoutes
import com.betamotor.app.data.api.motorcycle.CreateMotorcycleRequest
import com.betamotor.app.data.api.motorcycle.CreateMotorcycleRespData
import com.betamotor.app.data.api.motorcycle.GenericResponse
import com.betamotor.app.data.api.motorcycle.GenericResponseErrorMessage
import com.betamotor.app.data.api.motorcycle.MotorcycleItem
import com.betamotor.app.data.api.motorcycle.MotorcycleListResponse
import com.betamotor.app.data.api.motorcycle.MotorcycleTypesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
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
}