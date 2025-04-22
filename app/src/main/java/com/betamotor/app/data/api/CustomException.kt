package com.betamotor.app.data.api

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse

class UnprocessableEntityException(
    response: HttpResponse,
    cachedResponseText: String
) : ResponseException(response, cachedResponseText) {
    override val message: String = cachedResponseText
}

class UnauthorizedException(
    response: HttpResponse,
    cachedResponseText: String
) : ResponseException(response, cachedResponseText) {
    override val message: String = cachedResponseText
}
