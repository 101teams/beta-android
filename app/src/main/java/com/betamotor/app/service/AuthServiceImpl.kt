package com.betamotor.app.service

import android.util.Log
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

class AuthServiceImpl(
    private val client: HttpClient
): AuthService {
    override fun resetToken() {
        client.plugin(Auth).providers.filterIsInstance<BearerAuthProvider>()
            .first().clearToken()
    }

    override suspend fun login(request: AuthRequest): Pair<User?, String> {
        return try {
            val response = client.post {
                url(HttpRoutes.AUTH)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<AuthResponse>()
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

    override suspend fun register(request: RegisterRequest): Pair<RegisterData?, String> {
        return try {
            val response = client.post {
                url(HttpRoutes.REGISTER)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<RegisterResponse>()
            Pair(response.data, "")
        } catch (e: RedirectResponseException) {
            Pair(null, "Error 3xx: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            val response = e.response.body<ErrorResponse>()
            var message = ""

            message = if (response.errors.isNullOrEmpty()) {
                val response = e.response.body<ErrorResponse2>()
                if (response.data.isNullOrEmpty()) {
                    response.message!!
                } else {
                    response.data[0]!!.message!!
                }
            } else {
                response.errors[0]?.message!!
            }
            Pair(null, message)
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

    override suspend fun forgotPassword(request: ForgotPasswordRequest, language: String): Pair<ForgotPasswordResponse?, String> {
        return try {
            val response = client.post {
                url(HttpRoutes.FORGOT_PASSWORD)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<ForgotPasswordResponse>()
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

    override suspend fun logout(): Pair<Boolean, String> {
        return try {
            client.get {
                url(HttpRoutes.LOGOUT)
            }
            Pair(true, "")
        } catch (e: RedirectResponseException) {
            Pair(false, "Error 3xx: ${e.response.status.description}")
        } catch (e: ClientRequestException) {
            val response = e.response.body<ErrorResponse>()
            Pair(false, response.errors?.get(0)?.message!!)
        } catch (e: ServerResponseException) {
            try{
                val response = e.response.body<ErrorResponse>()
                Pair(false, response.errors?.get(0)?.message!!)
            } catch (er: Exception) {
                Pair(false, "Error 5xx: ${e.response.status.description}")
            }
        } catch (e: Exception) {
            Pair(false, "Error: ${e.message}")
        }
    }
}