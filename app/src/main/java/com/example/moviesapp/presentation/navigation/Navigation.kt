package com.example.moviesapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moviesapp.presentation.ui.screens.DetailsScreen
import com.example.moviesapp.presentation.ui.screens.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Details : Screen("details/{titleId}") {
        fun createRoute(titleId: Int) = "details/$titleId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onTitleClick = { titleId ->
                    navController.navigate(Screen.Details.createRoute(titleId))
                }
            )
        }

        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("titleId") { type = NavType.IntType }
            )
        ) {
            DetailsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
