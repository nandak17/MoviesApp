package com.example.moviesapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


@Composable
fun Navigation(){
    val navController = rememberNavController()

    NavHost(navController = navController,startDestination = "Banner Screen"){
        composable("Banner Screen"){
            BannerScreen(navController)
        }
        composable("Home Screen"){
            HomeScreen(navController)
        }
        composable("Details Screen/{id}",
            arguments = listOf(
                navArgument("id"){
                    type = NavType.IntType
                }
            )
        ){
            it.arguments?.getInt("id")?.let { id ->
                DetailsScreen(id,navController)
            }

        }
    }
}