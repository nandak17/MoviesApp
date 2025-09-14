package com.example.moviesapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.moviesapp.viewModel.MovieViewModel
import kotlin.math.roundToInt


@Composable
fun DetailsScreen(id: Int, navController: NavController) {

    val movieViewModel = viewModel<MovieViewModel>()
    movieViewModel.id = id
    movieViewModel.getDetailsById()
    val state = movieViewModel.state
    val movie = state.detailsData
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xff121212))
        ) {
            AsyncImage(
                model = movie.poster,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    //.size(562.dp, 830.dp)
                    .offset((0).dp, (-380).dp)
                    .alpha(0.8f)
                    .drawWithContent {
                        val colors = listOf(
                            Color(0xff121212),
                            Color.Transparent
                        )
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(colors),
                            blendMode = BlendMode.DstIn
                        )
                    }

            )
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xff121212)
                            )
                        )
                    )
            )
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 31.dp, start = 23.dp, end = 23.dp)
            ) {
                Box {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = movie.title,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Clip
                )
                Text(
                    text = movie.genres?.joinToString(separator = "  .  ") ?: " ",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 15.dp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(modifier = Modifier.padding(top = 13.dp)) {
                    val rating = movie?.imdb_rating?.toDoubleOrNull()?.roundToInt() ?: 0
                    repeat(rating) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xffE0E401),
                            modifier = Modifier
                                .size(22.dp)
                                .padding(end = 2.dp)
                        )
                    }
                    Text(
                        text = " (${movie.imdb_rating})",
                        modifier = Modifier.padding(start = 5.dp),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(370.dp)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = movie.plot,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 36.dp, end = 36.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(370.dp)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.height(17.dp))
                Text(
                    text = "Cast", fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = movie.actors.split(",").joinToString(separator = "  .  "),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 36.dp, end = 36.dp)

                )
                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(370.dp)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "Images", fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(22.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                ) {
                        
                }
            }
        }
    }
}