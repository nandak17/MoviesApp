
package com.example.moviesapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.rxjava3.cachedIn
import com.example.moviesapp.data.model.PersonCredit
import com.example.moviesapp.data.model.Title
import com.example.moviesapp.data.network.ConnectivityObserver
import com.example.moviesapp.data.remote.WatchmodeApiService
import com.example.moviesapp.data.repository.TitleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ContentType {
    MOVIES, TV_SHOWS
}

enum class SortOption(val displayName: String) {
    RATING_DESC("Rating: High to Low"),
    RATING_ASC("Rating: Low to High"),
    YEAR_DESC("Year: Newest First"),
    YEAR_ASC("Year: Oldest First"),
    TITLE_ASC("Title: A to Z"),
    TITLE_DESC("Title: Z to A")
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TitleRepository,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _selectedTab = MutableStateFlow(ContentType.MOVIES)
    val selectedTab: StateFlow<ContentType> = _selectedTab.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.RATING_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _moviesPagingData = MutableStateFlow<PagingData<Title>>(PagingData.empty())
    val moviesPagingData: StateFlow<PagingData<Title>> = _moviesPagingData.asStateFlow()

    private val _tvShowsPagingData = MutableStateFlow<PagingData<Title>>(PagingData.empty())
    val tvShowsPagingData: StateFlow<PagingData<Title>> = _tvShowsPagingData.asStateFlow()

    private val _networkStatus = MutableStateFlow(ConnectivityObserver.Status.Available)
    val networkStatus: StateFlow<ConnectivityObserver.Status> = _networkStatus.asStateFlow()

    private val disposables = CompositeDisposable()
    private var wasDisconnected = false

    init {
        Log.d(TAG, "HomeViewModel initialized")
        observeNetworkStatus()
        observeSortChanges()
        loadMovies()
        loadTVShows()
    }

    private fun observeSortChanges() {
        viewModelScope.launch {
            _sortOption
                .drop(1)
                .distinctUntilChanged()
                .collect { newSort ->
                    Log.d(TAG, "Sort changed to: ${newSort.displayName}")
                    reloadData()
                }
        }
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                Log.d(TAG, "Network status changed: $status")
                _networkStatus.value = status

                if (status == ConnectivityObserver.Status.Lost ||
                    status == ConnectivityObserver.Status.Unavailable) {
                    wasDisconnected = true
                    Log.d(TAG, "Network lost - marking for reload")
                }

                if (status == ConnectivityObserver.Status.Available && wasDisconnected) {
                    Log.d(TAG, "Network restored - auto-reloading data")
                    wasDisconnected = false
                    delay(500)
                    reloadData()
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadMovies() {
        disposables.add(
            repository.getMoviesPaged(_sortOption.value)
                .cachedIn(viewModelScope)
                .subscribe(
                    { pagingData ->
                        Log.d(TAG, "✅ Movies loaded successfully with sort: ${_sortOption.value.displayName}")
                        _moviesPagingData.value = pagingData
                    },
                    { error ->
                        Log.e(TAG, "❌ Error loading movies: ${error.message}", error)
                    }
                )
        )
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadTVShows() {
        disposables.add(
            repository.getTVShowsPaged(_sortOption.value)
                .cachedIn(viewModelScope)
                .subscribe(
                    { pagingData ->
                        Log.d(TAG, "✅ TV shows loaded successfully with sort: ${_sortOption.value.displayName}")
                        _tvShowsPagingData.value = pagingData
                    },
                    { error ->
                        Log.e(TAG, "❌ Error loading TV shows: ${error.message}", error)
                    }
                )
        )
    }

    fun reloadData() {
        Log.d(TAG, "Reloading all data")
        disposables.clear()
        loadMovies()
        loadTVShows()
    }

    fun setSelectedTab(tab: ContentType) {
        _selectedTab.value = tab
    }

    fun setSortOption(option: SortOption) {
        Log.d(TAG, "Setting sort option: ${option.displayName}")
        _sortOption.value = option
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
