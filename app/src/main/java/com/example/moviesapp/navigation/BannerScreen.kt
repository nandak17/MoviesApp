package com.example.moviesapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.navigation.NavController

@Composable
fun BannerScreen(navController: NavController, modifier: Modifier = Modifier) {

    Column {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "This is the banner Screen", modifier = Modifier.align(
                    Alignment.Center
                )
            )
            Button(
                onClick = { navController.navigate("Home Screen") }, modifier = Modifier.align(
                    Alignment.Center
                )
            ) {
                Text(text = "Get In")
            }
        }
    }
}