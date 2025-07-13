package com.betamotor.app.data.api

import com.betamotor.app.utils.Constants

object HttpRoutes {
    const val v1 = "/api"
    const val AUTH = "${Constants.API_URL}${v1}/login"
    const val MOTORCYCLE = "${Constants.API_URL}${v1}/motorcycles"
    const val MOTORCYCLE_TYPES = "${Constants.API_URL}${v1}/motorcycle-types"
    const val REGISTER = "${Constants.API_URL}${v1}/register"
    const val FORGOT_PASSWORD = "${Constants.API_URL}${v1}/forgot-password"
    const val LOGOUT = "${Constants.API_URL}${v1}/logout"

    const val GOOGLE_ALTITUDE = "https://maps.googleapis.com/maps/api/elevation/json"
}