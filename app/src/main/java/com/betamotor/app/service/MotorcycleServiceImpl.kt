package com.betamotor.app.service

import com.betamotor.app.data.api.ErrorResponse
import com.betamotor.app.data.api.ErrorResponse2
import com.betamotor.app.data.api.HttpRoutes
import com.betamotor.app.data.api.auth.AuthResponse
import com.betamotor.app.data.api.auth.AuthRequest
import com.betamotor.app.data.api.auth.User
import com.betamotor.app.data.api.forgot_password.ForgotPasswordRequest
import com.betamotor.app.data.api.motorcycle.MotorcycleListResponse
import com.betamotor.app.data.api.register.RegisterData
import com.betamotor.app.data.api.register.RegisterRequest
import com.betamotor.app.data.api.register.RegisterResponse
import com.idrolife.app.data.api.forgot_password.ForgotPasswordResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.plugin
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class MotorcycleServiceImpl(
    private val client: HttpClient
): MotorcycleService {
    override suspend fun listMotorcycle(): Pair<MotorcycleListResponse?, String> {
        return try {
            val response = client.get {
                url(HttpRoutes.MOTORCYCLE)
                contentType(ContentType.Application.Json)
            }.body<MotorcycleListResponse>()
            Pair(response, "")
        } catch (e: RedirectResponseException) {
            Pair(null, "Error 3xx: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            val response = e.response.body<ErrorResponse>()
            Pair(null, response.errors?.get(0)?.message!!)
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