package com.betamotor.app.service

import com.betamotor.app.data.api.ErrorResponse
import com.betamotor.app.data.api.ErrorResponse2
import com.betamotor.app.data.api.HttpRoutes
import com.betamotor.app.data.api.google.GoogleAltitudeResponse
import com.betamotor.app.utils.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class GoogleServiceImpl(
    private val client: HttpClient
): GoogleService {
    override suspend fun getAltitude(
        latitude: Double,
        longitude: Double
    ): Pair<GoogleAltitudeResponse?, String> {
        return try {
            val response = client.get {
                url("${HttpRoutes.GOOGLE_ALTITUDE}?locations=$latitude,$longitude&key=${Constants.GOOGLE_API_KEY}")
                contentType(ContentType.Application.Json)
            }.body<GoogleAltitudeResponse>()

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
}