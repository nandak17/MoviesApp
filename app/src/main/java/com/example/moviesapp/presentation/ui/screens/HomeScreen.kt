package com.example.moviesapp.presentation.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
    onTitleClick: (Int) -> Unit
) {
    val selectedTab by homeViewModel.selectedTab.collectAsState()
    val sortOption by homeViewModel.sortOption.collectAsState()
    val networkStatus by homeViewModel.networkStatus.collectAsState()
    val moviesPagingData = homeViewModel.moviesPagingData.collectAsLazyPagingItems()
    val tvShowsPagingData = homeViewModel.tvShowsPagingData.collectAsLazyPagingItems()
    val autocompleteResults by searchViewModel.autocompleteResults.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showConnectedBanner by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // debounce search input and trigger autocomplete
    var debouncedQuery by remember { mutableStateOf("") }
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedQuery = searchQuery.trim()
        searchViewModel.searchAutocomplete(debouncedQuery)
    }

    LaunchedEffect(networkStatus) {
        if (networkStatus == ConnectivityObserver.Status.Available) {
            moviesPagingData.retry()
            tvShowsPagingData.retry()
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
        homeViewModel.setSelectedTab(if (pagerState.currentPage == 0) ContentType.MOVIES else ContentType.TV_SHOWS)
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
                    AnimatedVisibility(visible = !isSearchExpanded, enter = fadeIn(), exit = fadeOut()) {
                        Text("Movies & TV Shows")
                    }
                },
                actions = {
                    AnimatedVisibility(visible = isSearchExpanded, enter = expandHorizontally() + fadeIn(), exit = shrinkHorizontally() + fadeOut()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.7f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(if (pagerState.currentPage == 0) "Search movies..." else "Search TV shows...")
                                },
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
                            if (debouncedQuery.isNotBlank()) {
                                Text(
                                    "${autocompleteResults.size}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                    if (!isSearchExpanded) {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "Sort",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (sortOption == option) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                            } else {
                                                Spacer(modifier = Modifier.width(28.dp))
                                            }
                                            Text(option.displayName)
                                        }
                                    },
                                    onClick = {
                                        homeViewModel.setSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
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
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xff161616), titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xff161616)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                        )
                        Spacer(Modifier.width(16.dp))
                        HomeTabButton(
                            text = "TV Shows",
                            isSelected = pagerState.currentPage == 1,
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
                        )
                    }
                }
                HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                    when {
                        isSearchExpanded && debouncedQuery.isNotBlank() -> {
                            AutocompleteSearchResults(results = autocompleteResults, onTitleClick = onTitleClick)
                        }
                        isSearchExpanded && debouncedQuery.isBlank() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                        else -> {
                            val items = if (page == 0) moviesPagingData else tvShowsPagingData
                            PagingContent(items = items, onTitleClick)
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).absolutePadding(left = 0.dp, right = 0.dp), contentAlignment = Alignment.TopCenter) {
                NetworkBanner(networkStatus = networkStatus, showConnectedBanner = showConnectedBanner)
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

@Composable
fun AutocompleteSearchResults(results: List<AutocompleteResult>, onTitleClick: (Int) -> Unit) {
    if (results.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
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
                        Box(Modifier.fillMaxSize().background(Color(0xff2A2A2A)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
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
private fun PagingContent(items: LazyPagingItems<Title>, onTitleClick: (Int) -> Unit) {
    when (items.loadState.refresh) {
        is LoadState.Loading -> {
            ShimmerHomeScreen()
        }
        is LoadState.Error -> {
            val error = (items.loadState.refresh as LoadState.Error).error
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Failed to load content", color = Color.White, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error.message ?: "Unknown error", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { items.retry() }) {
                        Text("Retry")
                    }
                }
            }
        }
        is LoadState.NotLoading -> {
            if (items.itemCount == 0) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No titles available", color = Color.White, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { items.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    items(count = items.itemCount) { index ->
                        items[index]?.let { title ->
                            TitleListItem(title = title, onClick = { onTitleClick(title.id) })
                        }
                    }
                    when (items.loadState.append) {
                        is LoadState.Loading -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }
                        }
                        is LoadState.Error -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Button(onClick = { items.retry() }) {
                                        Text("Load More")
                                    }
                                }
                            }
                        }
                        else -> { }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun TitleListItem(title: Title, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xff1E1E1E)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            if (!title.poster.isNullOrEmpty()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(title.poster).crossfade(true).build(),
                    contentDescription = title.title,
                    modifier = Modifier.width(80.dp).height(120.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xff2A2A2A)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        }
                    },
                    error = {
                        PosterPlaceholder()
                    }
                )
            } else {
                PosterPlaceholder()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                title.year?.let {
                    Text(
                        text = it.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                title.user_rating?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rating: ${String.format("%.1f", it)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun PosterPlaceholder() {
    Box(
        modifier = Modifier.width(80.dp).height(120.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xff2A2A2A)),
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
private fun NetworkBanner(networkStatus: ConnectivityObserver.Status, showConnectedBanner: Boolean) {
    Column {
        AnimatedVisibility(
            visible = networkStatus == ConnectivityObserver.Status.Lost || networkStatus == ConnectivityObserver.Status.Unavailable,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color = Color(0xFFD32F2F),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "No network", tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("No internet connection", color = Color.White, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium)
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
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Connected", tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back online", color = Color.White, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
