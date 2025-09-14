package com.example.moviesapp.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.moviesapp.R
import com.example.moviesapp.viewModel.MovieViewModel


@Composable
fun HomeScreen(navController: NavController) {
    val movieViewModel = viewModel<MovieViewModel>()
    val state = movieViewModel.state
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff161616))

    ) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(52.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(52.dp))
            Text(
                text = "Search for Your", modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = " Favorite Movies.", modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(33.dp))
            Column(
                modifier = Modifier
                    .background(shape = RoundedCornerShape(9.dp), color = Color(0xff0B0D0F))
                    .alpha(0.75f)
                    .size(width = 368.dp, height = 35.dp)
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(start = 12.dp, top = 7.dp, bottom = 3.dp)
                            .size(30.dp, 30.dp)
                            .alpha(0.59f)
                    )
                    Text(
                        text = "Search",
                        color = Color.White,
                        modifier = Modifier.padding(end = 230.dp),
                        fontSize = 14.sp
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.mic),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(30.dp, 30.dp)
                            .padding(end = 5.dp)
                            .alpha(0.59f)

                    )
                }
            }
            Spacer(modifier = Modifier.height(19.dp))
            Box() {
                Image(
                    painter = painterResource(id = R.drawable.banner), contentDescription = null,
                    modifier = Modifier.size(368.dp, 128.dp)
                )
            }
            Spacer(modifier = Modifier.height(19.dp))
            Text(
                text = "Favorites", color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 25.dp)
                    .align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .height(203.dp)
                    .fillMaxWidth()
                    .padding(start = 10.dp)
            ) {
                val customIndices = listOf(7, 3, 8, 9, 5)
                LazyHorizontalGrid(rows = GridCells.Fixed(1),
                    content = {
                        items(count = customIndices.size) { index ->
                            val customIndex = customIndices[index] // Map to your custom index
                            if (customIndex < state.movies.size) {
                                AsyncImage(
                                    model = state.movies[customIndex].poster, contentDescription = null,
                                    modifier = Modifier
                                        .padding(7.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .size(138.dp, 204.dp)
                                        .clickable {
                                            navController.navigate("Details Screen/${state.movies[customIndex].id}")
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(19.dp))
            Text(
                text = "Latest Movies", color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 25.dp)
                    .align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .height(203.dp)
                    .fillMaxWidth()
                    .padding(start = 10.dp)
            ) {
                LazyHorizontalGrid(rows = GridCells.Fixed(1),
                    content = {
                        items(count = state.movies.size) {
                            AsyncImage(
                                model = state.movies[it].poster, contentDescription = null,
                                modifier = Modifier
                                    .padding(7.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .size(138.dp, 204.dp)
                                    .clickable {
                                        navController.navigate("Details Screen/${state.movies[it].id}")
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                )
            }
        }
    }
}
