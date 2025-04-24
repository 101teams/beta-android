package com.betamotor.app.service

import com.betamotor.app.data.api.auth.User
import com.betamotor.app.data.api.auth.AuthRequest
import com.betamotor.app.data.api.forgot_password.ForgotPasswordRequest
import com.betamotor.app.data.api.register.RegisterRequest
import com.betamotor.app.data.api.register.RegisterData
import com.idrolife.app.data.api.forgot_password.ForgotPasswordResponse

interface AuthService {
    fun resetToken()
    suspend fun login(request: AuthRequest): Pair<User?, String>
    suspend fun register(request: RegisterRequest): Pair<RegisterData?, String>
    suspend fun forgotPassword(request: ForgotPasswordRequest, language: String): Pair<ForgotPasswordResponse?, String>
    suspend fun logout(): Pair<Boolean, String>
}
