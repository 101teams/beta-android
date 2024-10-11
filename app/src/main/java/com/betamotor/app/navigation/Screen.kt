package com.betamotor.app.navigation

sealed class Screen(val route: String) {
    object Splash: Screen("splash")
    object Main: Screen("main")
    object ScanDevice: Screen("scan-device")
    object DetailDevice: Screen("detail-device")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach {
                append("/$it")
            }
        }
    }
}
