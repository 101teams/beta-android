package com.betamotor.app.data.api

import com.betamotor.app.utils.Constants

object HttpRoutes {
    const val v1 = "/api"
    const val AUTH = "${Constants.API_URL}${v1}/login"
    const val MOTORCYCLE = "${Constants.API_URL}${v1}/motorcycles"
    const val REGISTER = "${Constants.API_URL}${v1}/register"
    const val FORGOT_PASSWORD = "${Constants.API_URL}${v1}/forgot-password"
    const val LOGOUT = "${Constants.API_URL}${v1}/logout"
}