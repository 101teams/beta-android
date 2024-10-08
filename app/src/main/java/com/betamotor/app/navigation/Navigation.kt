package com.betamotor.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.betamotor.app.presentation.screen.MainScreen
import com.betamotor.app.presentation.screen.ScanDeviceScreen
import com.betamotor.app.presentation.screen.SplashScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
            {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400)
                )
            }

        val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
            {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400)
                )
            }

        val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
            {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }


        val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
            {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                )
            }

        composable(
            route = Screen.Splash.route,
            exitTransition = exitTransition,
            popExitTransition = popExitTransition,
            enterTransition = enterTransition,
            popEnterTransition = popEnterTransition
        ) {
            SplashScreen(navController)
        }

        composable(
            route = Screen.Main.route,
            exitTransition = exitTransition,
            popExitTransition = popExitTransition,
            enterTransition = enterTransition,
            popEnterTransition = popEnterTransition
        ) {
            MainScreen(navController)
        }

        composable(
            route = Screen.ScanDevice.route,
            exitTransition = exitTransition,
            popExitTransition = popExitTransition,
            enterTransition = enterTransition,
            popEnterTransition = popEnterTransition
        ) {
            ScanDeviceScreen(navController)
        }
    }
}
