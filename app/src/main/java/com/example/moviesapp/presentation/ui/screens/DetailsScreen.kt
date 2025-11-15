package com.example.moviesapp.presentation.ui.screens

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.palette.graphics.Palette
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.moviesapp.data.model.PersonCredit
import com.example.moviesapp.data.model.StreamingServiceInfo
import com.example.moviesapp.data.model.StreamingSource
import com.example.moviesapp.data.model.TitleDetails
import com.example.moviesapp.data.network.ConnectivityObserver
import com.example.moviesapp.data.remote.openPlayStoreUrl
import com.example.moviesapp.presentation.ui.components.ShimmerDetailsScreen
import com.example.moviesapp.presentation.viewmodel.DetailsUiState
import com.example.moviesapp.presentation.viewmodel.DetailsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun DetailsScreen(viewModel: DetailsViewModel = hiltViewModel(), onBackClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState(initial = ConnectivityObserver.Status.Available)
    var previousNetworkStatus by remember { mutableStateOf<ConnectivityObserver.Status?>(null) }
    var showConnectedBanner by remember { mutableStateOf(false) }
    val cachedSources by viewModel.cachedSources.collectAsState()


    LaunchedEffect(networkStatus) {
        if (previousNetworkStatus != null &&
            (previousNetworkStatus == ConnectivityObserver.Status.Lost ||
                    previousNetworkStatus == ConnectivityObserver.Status.Unavailable) &&
            networkStatus == ConnectivityObserver.Status.Available
        ) {
            showConnectedBanner = true
            delay(3000)
            showConnectedBanner = false
        }
        previousNetworkStatus = networkStatus
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is DetailsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize()
                    .background(Color.Black)) {
                    ShimmerDetailsScreen()
                }
            }
            is DetailsUiState.Success -> {
                DetailsContent(
                    details = state.details,
                    cast = state.cast,
                    crew = state.crew,
                    cachedSources = cachedSources,
                    onBackClick = onBackClick
                )
            }
            is DetailsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Failed to load details", color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onBackClick) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }

        NetworkBanner(
            networkStatus = networkStatus,
            showConnectedBanner = showConnectedBanner,
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun DetailsContent(
    details: TitleDetails,
    cast: List<PersonCredit>,
    crew: List<PersonCredit>,
    cachedSources: Map<Int, StreamingServiceInfo>,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf(Color(0xff1E1E1E)) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(details.poster) {
        details.poster?.let { posterUrl ->
            try {
                val request = ImageRequest.Builder(context).data(posterUrl)
                    .allowHardware(false)
                    .build()

                val result = (context.imageLoader.execute(request) as? SuccessResult)?.drawable
                val bitmap = (result as? BitmapDrawable)?.bitmap

                bitmap?.let {
                    Palette.from(it).generate { palette ->
                        val swatch = palette?.darkMutedSwatch
                            ?: palette?.mutedSwatch
                            ?: palette?.dominantSwatch
                        swatch?.let { s -> dominantColor = Color(s.rgb) }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!details.poster.isNullOrEmpty()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(details.poster)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.95f),
                                Color.Black
                            ),
                            startY = 0f,
                            endY = 800f
                        )
                    )
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (!details.poster.isNullOrEmpty()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(details.poster)
                                .crossfade(true)
                                .build(),
                            contentDescription = details.title,
                            modifier = Modifier
                                .width(180.dp)
                                .height(270.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = details.title,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        details.year?.let {
                            Text(
                                it.toString(),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }

                        details.user_rating?.let { rating ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xffFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    String.format("%.1f", rating),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                            }
                        }

                        details.runtime_minutes?.let {
                            Text(
                                "${it}min",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    details.genre_names?.let { genres ->
                        if (genres.isNotEmpty()) {
                            Text(
                                genres.take(2).joinToString("  •  "),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    "Storyline",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                details.plot_overview?.let { plot ->
                    Text(
                        text = plot,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        maxLines = if (expanded) Int.MAX_VALUE else 4,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (plot.length > 200) {
                        TextButton(
                            onClick = { expanded = !expanded },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White.copy(alpha = 0.7f)
                            )
                        ) {
                            Text(if (expanded) "Read less" else "Read more")
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                details.sources?.let { sources ->
                    if (sources.isNotEmpty()) {
                        Text(
                            "Where to Watch",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        sources.groupBy { it.type }.forEach { (type, typeSources) ->
                            val label = when (type) {
                                "sub" -> "Streaming"
                                "rent" -> "Rent"
                                "buy" -> "Buy"
                                "free" -> "Free"
                                else -> type
                            }

                            Text(
                                label,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(typeSources.distinctBy { it.name }) { source ->
                                    StreamingPlatformCard(source,cachedSources)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                Text(
                    text = "Cast",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
                    items(cast.take(10)) { castMember ->
                        CastCrewItem(
                            name = castMember.full_name,
                            role = castMember.role,
                            imageUrl = castMember.headshot_url,
                            onClick = {  }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Crew",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
                    items(crew.take(10)) { crewMember ->
                        CastCrewItem(
                            name = crewMember.full_name,
                            role = crewMember.role,
                            imageUrl = crewMember.headshot_url,
                            onClick = {  }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = dominantColor.copy(alpha = 0.2f)
                            .compositeOver(Color(0xff1A1A1A))
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Release Information",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        details.release_date?.let {
                            InfoRow("Release Date", it)
                        }
                        details.year?.let {
                            InfoRow("Year", it.toString())
                        }
                        InfoRow("Type", details.type.replace("_", " ").replaceFirstChar { it.uppercase() })
                        details.original_language?.let {
                            InfoRow("Language", it.uppercase())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CastCrewItem(name: String, role: String, imageUrl: String?, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(100.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!imageUrl.isNullOrBlank() && !imageUrl.contains("empty_headshot")) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = name,
                loading = {
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) }
                },
                error = {
                    Icon(Icons.Default.Person, contentDescription = "Person", tint = Color.White, modifier = Modifier.size(48.dp))
                },
                modifier = Modifier.size(48.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Person",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White)
        Text(role, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, color = Color.LightGray)
    }
}


@SuppressLint("DefaultLocale")
@Composable
private fun StreamingPlatformCard(source: StreamingSource,cachedSources: Map<Int, StreamingServiceInfo>) {

    val context = LocalContext.current
    val sourceInfo = cachedSources[source.source_id]
    Card(
        modifier = Modifier.width(100.dp)
            .clickable {
                source.web_url?.let { url ->
                    openPlayStoreUrl(context, url)
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xff2A2A2A)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                source.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            source.format?.let {
                Text(
                    it,
                    color = Color(0xffFFD700),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            source.price?.let {
                Text(
                    "$${String.format("%.2f", it)}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
        Text(
            value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NetworkBanner(
    networkStatus: ConnectivityObserver.Status,
    showConnectedBanner: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        AnimatedVisibility(
            visible = networkStatus == ConnectivityObserver.Status.Lost
                    || networkStatus == ConnectivityObserver.Status.Unavailable,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFD32F2F),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "No network",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "No internet connection",
                        color = Color.White,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = showConnectedBanner,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF388E3C),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Connected",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Back online",
                        color = Color.White,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
