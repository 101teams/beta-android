package com.betamotor.app.service

import com.betamotor.app.data.api.auth.User
import com.betamotor.app.data.api.auth.AuthRequest
import com.betamotor.app.data.api.forgot_password.ForgotPasswordRequest
import com.betamotor.app.data.api.motorcycle.MotorcycleListResponse
import com.betamotor.app.data.api.register.RegisterRequest
import com.betamotor.app.data.api.register.RegisterData
import com.idrolife.app.data.api.forgot_password.ForgotPasswordResponse

interface MotorcycleService {
    suspend fun listMotorcycle(): Pair<MotorcycleListResponse?, String>
}
