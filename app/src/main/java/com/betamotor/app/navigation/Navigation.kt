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
import com.betamotor.app.presentation.screen.DetailDeviceScreen
import com.betamotor.app.presentation.screen.ForgotPasswordScreen
import com.betamotor.app.presentation.screen.LoginScreen
import com.betamotor.app.presentation.screen.MainScreen
import com.betamotor.app.presentation.screen.MotorcycleTypesScreen
import com.betamotor.app.presentation.screen.MyMotorcycleScreen
import com.betamotor.app.presentation.screen.RegisterScreen
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
            route = Screen.Login.route,
            exitTransition = exitTransition,
            popExitTransition = popExitTransition,
            enterTransition = enterTransition,
            popEnterTransition = popEnterTransition
        ) {
            LoginScreen(navController)
        }

        composable(
            route = Screen.Register.route,
            exitTransition = exitTransition,
            popExitTransition = popExitTransition,
            enterTransition = enterTransition,
            popEnterTransition = popEnterTransition
        ) {
            RegisterScreen(navController)
        }

        composable(
            route = Screen.ForgotPassword.route,
            exitTransition = exitTransition,
            popExitTransition = popExitTransition,
            enterTransition = enterTransition,
            popEnterTransition = popEnterTransition
        ) {
            ForgotPasswordScreen(navController)
        }

        composable(
            route = Screen.MotorcycleTypes.route,
            exitTransition = exitTransition,
            popExitTransition = popExitTransition,
            enterTransition = enterTransition,
            popEnterTransition = popEnterTransition
        ) {
            MotorcycleTypesScreen(navController)
        }

        composable(
            route = Screen.MyMotorcycle.route,
            exitTransition = exitTransition,
            popExitTransition = popExitTransition,
            enterTransition = enterTransition,
            popEnterTransition = popEnterTransition
        ) {
            MyMotorcycleScreen(navController)
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

        composable(
            route = Screen.DetailDevice.route,
            exitTransition = exitTransition,
            popExitTransition = popExitTransition,
            enterTransition = enterTransition,
            popEnterTransition = popEnterTransition
        ) {
            DetailDeviceScreen(navController)
        }
    }
}
