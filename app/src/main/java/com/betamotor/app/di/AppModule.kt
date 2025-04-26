package com.betamotor.app.di

import android.content.Context
import com.betamotor.app.data.api.ErrorResponse
import com.betamotor.app.data.api.UnauthorizedException
import com.betamotor.app.data.api.UnprocessableEntityException
import com.betamotor.app.service.AndroidBluetoothController
import com.betamotor.app.service.AuthService
import com.betamotor.app.service.AuthServiceImpl
import com.betamotor.app.service.BluetoothController
import com.betamotor.app.service.MotorcycleService
import com.betamotor.app.service.MotorcycleServiceImpl
import com.betamotor.app.utils.LocalLogging
import com.betamotor.app.utils.MQTTHelper
import com.betamotor.app.utils.PrefManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideTokenStorage(@ApplicationContext context: Context): PrefManager {
        return PrefManager(context)
    }

    @Provides
    @Singleton
    fun provideMQTT(@ApplicationContext context: Context): MQTTHelper {
        return MQTTHelper(context)
    }

    @Provides
    @Singleton
    fun provideBluetoothController(@ApplicationContext context: Context, mqttHelper: MQTTHelper): BluetoothController {
        return AndroidBluetoothController(context, mqttHelper)
    }

    @Provides
    @Singleton
    fun provideAuthService(@ApplicationContext context: Context): AuthService {
        val prefManager = PrefManager(context)
        val client = HttpClient(Android) {
            expectSuccess = true

            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, request ->
                    val clientException = cause as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                    val exceptionResponse = clientException.response
                    if (exceptionResponse.status == HttpStatusCode.UnprocessableEntity) {
                        val firstError = (exceptionResponse.body() as? ErrorResponse)?.errors?.get(0)?.message
                        throw UnprocessableEntityException(
                            exceptionResponse,
                            firstError ?: "Something went wrong"
                        )
                    }
                }
            }

            install(Logging) {
                level = LogLevel.ALL
            }

            install(ContentNegotiation) {
                json( Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(prefManager.getToken(), "")
                    }
                }
            }
        }

        return AuthServiceImpl(client)
    }

    @Provides
    @Singleton
    fun provideMotorcycleService(@ApplicationContext context: Context): MotorcycleService {
        val prefManager = PrefManager(context)
        val client = HttpClient(Android) {
            install(Logging) {
                level = LogLevel.ALL
            }

            install(ContentNegotiation) {
                json( Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(prefManager.getToken(), "")
                    }
                }
            }

            expectSuccess = true

            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, request ->
                    val clientException = cause as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                    val exceptionResponse = clientException.response
                    if (exceptionResponse.status == HttpStatusCode.Unauthorized) {
                        val firstError = (exceptionResponse.body() as? ErrorResponse)?.errors?.get(0)?.message
                        throw UnauthorizedException(
                            exceptionResponse,
                            firstError ?: "Please login first"
                        )
                    }
                }
            }
        }

        return MotorcycleServiceImpl(client)
    }
}
