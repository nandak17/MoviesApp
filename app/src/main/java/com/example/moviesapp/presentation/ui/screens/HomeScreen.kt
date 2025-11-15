package com.example.moviesapp.presentation.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.moviesapp.R
import com.example.moviesapp.data.model.AutocompleteResult
import com.example.moviesapp.data.model.Title
import com.example.moviesapp.data.network.ConnectivityObserver
import com.example.moviesapp.presentation.ui.components.ShimmerHomeScreen
import com.example.moviesapp.presentation.viewmodel.ContentType
import com.example.moviesapp.presentation.viewmodel.HomeViewModel
import com.example.moviesapp.presentation.viewmodel.SearchViewModel
import com.example.moviesapp.presentation.viewmodel.SortOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
    onTitleClick: (Int) -> Unit
) {
    val selectedTab by homeViewModel.selectedTab.collectAsState()
    val genres by homeViewModel.genres.collectAsState()
    val selectedGenres by homeViewModel.selectedGenres.collectAsState()
    val networkStatus by homeViewModel.networkStatus.collectAsState()
    val autocompleteResults by searchViewModel.autocompleteResults.collectAsState()
    val releases by homeViewModel.releases.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showConnectedBanner by remember { mutableStateOf(false) }
    var showGenrePanel by remember { mutableStateOf(false) }

    var debouncedQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            delay(100)
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedQuery = searchQuery.trim()
        searchViewModel.searchAutocomplete(debouncedQuery)
    }

    LaunchedEffect(networkStatus) {
        if (networkStatus == ConnectivityObserver.Status.Available) {
            showConnectedBanner = true
            delay(3000)
            showConnectedBanner = false
        }
    }

    val pagerState = rememberPagerState(
        initialPage = if (selectedTab == ContentType.MOVIES) 0 else 1,
        pageCount = { 2 }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        homeViewModel.setSelectedTab(if (pagerState.currentPage == 0)
            ContentType.MOVIES else ContentType.TV_SHOWS)
    }
    LaunchedEffect(selectedTab) {
        val targetPage = if (selectedTab == ContentType.MOVIES) 0 else 1
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            placeholder = { Text("Search movies or TV shows...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    } else {
                        Text("Movies & TV Shows")
                    }
                },
                actions = {
                    if (!isSearchExpanded) {
                        IconButton(onClick = { showGenrePanel = !showGenrePanel }) {
                            Icon(
                                painter = painterResource(R.drawable.filter),
                                contentDescription = "Genre Filter",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(onClick = {
                        if (isSearchExpanded) {
                            searchQuery = ""
                            debouncedQuery = ""
                            isSearchExpanded = false
                        } else {
                            isSearchExpanded = true
                        }
                    }) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchExpanded) "Close search" else "Search",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xff161616),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xff161616)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!isSearchExpanded) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xff161616)),
                    horizontalArrangement = Arrangement.Center
                ) {
                    HomeTabButton(
                        text = "Movies",
                        isSelected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            homeViewModel.setSelectedTab(ContentType.MOVIES)
                        }
                    )
                    Spacer(Modifier.width(16.dp))
                    HomeTabButton(
                        text = "TV Shows",
                        isSelected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            homeViewModel.setSelectedTab(ContentType.TV_SHOWS)
                        }
                    )
                }
                AnimatedVisibility(
                    visible = showGenrePanel,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xff161616))
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        genres.forEach { genre ->
                            FilterChip(
                                selected = selectedGenres.contains(genre.id),
                                onClick = {  },
                                label = { Text(genre.name, color = Color.White, fontSize = 13.sp) },
                                modifier = Modifier.padding(4.dp)
                                    .border(width = 1.dp,
                                        brush = Brush.horizontalGradient(colors = listOf(
                                            Color(0xFF6366F1),
                                            Color(0xFFA855F7),
                                            Color(0xFFEC4899)
                                        )), shape = FilterChipDefaults.shape),
                                border = null
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                val filteredReleases = when (page) {
                    0 -> releases.filter { it.type.equals("movie", ignoreCase = true) }
                    else -> releases.filter {
                        it.type.equals("tv_series", ignoreCase = true) || it.type.equals("tv",
                            ignoreCase = true)
                    }
                }
                val sortedReleases = filteredReleases.sortedByDescending {
                    it.source_release_date ?: it.year?.toString() ?: ""
                }

                if (isSearchExpanded) {
                    if (debouncedQuery.isNotBlank()) {
                        AutocompleteSearchResults(results = autocompleteResults, onTitleClick = onTitleClick)
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Type to search...",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                } else {
                    if (sortedReleases.isEmpty()) {
                        ShimmerHomeScreen()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(sortedReleases) { title ->
                                ReleasePosterCard(title = title, onClick = { onTitleClick(title.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
            fontSize = 18.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReleasePosterCard(title: Title, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xff1E1E1E)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!title.poster.isNullOrEmpty()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(title.poster)
                        .crossfade(true).build(),
                    contentDescription = title.title,
                    modifier = Modifier.width(80.dp).height(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    loading = { PosterPlaceholder() },
                    error = { PosterPlaceholder() }
                )
            } else {
                PosterPlaceholder()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Top) {
                Text(
                    text = title.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val formattedDate = title.source_release_date?.let {
                    try {
                        val parsedDate = LocalDate.parse(it)
                        parsedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    } catch (e: DateTimeParseException) {
                        it
                    }
                } ?: title.year?.toString()
                formattedDate?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                            .copy(fontWeight = FontWeight.Normal, letterSpacing = 0.3.sp),
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}



@Composable
fun PosterPlaceholder() {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xff2A2A2A)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "No image",
            tint = Color.Gray,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun AutocompleteSearchResults(results: List<AutocompleteResult>, onTitleClick: (Int) -> Unit) {
    if (results.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Search, contentDescription = null,
                    modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(Modifier.height(16.dp))
                Text("No results found", color = Color.White, fontSize = 18.sp)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
            items(results.size) { idx ->
                val item = results[idx]
                AutocompleteResultListItem(result = item, onClick = { onTitleClick(item.id) })
            }
        }
    }
}

@Composable
fun AutocompleteResultListItem(result: AutocompleteResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xff1E1E1E)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!result.image_url.isNullOrEmpty()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(result.image_url)
                        .crossfade(true)
                        .build(),
                    contentDescription = result.name,
                    modifier = Modifier.size(width = 80.dp, height = 120.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color(0xff2A2A2A)), contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        PosterPlaceholder()
                    }
                )
            } else {
                PosterPlaceholder()
            }
            Spacer(Modifier.width(12.dp))
            Text(result.name, color = Color.White, fontSize = 20.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun NetworkBanner(networkStatus: ConnectivityObserver.Status, showConnectedBanner: Boolean) {
    Column {
        AnimatedVisibility(
            visible = networkStatus == ConnectivityObserver.Status.Lost ||
                    networkStatus == ConnectivityObserver.Status.Unavailable,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = Color(0xFFD32F2F),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "No network",
                        tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("No internet connection", color = Color.White, fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        AnimatedVisibility(
            visible = showConnectedBanner,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color = Color(0xFF388E3C),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Connected",
                        tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back online", color = Color.White, fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}