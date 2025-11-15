package com.example.moviesapp.presentation.viewmodel

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviesapp.data.model.Genre
import com.example.moviesapp.data.model.Title
import com.example.moviesapp.data.network.ConnectivityObserver
import com.example.moviesapp.data.repository.TitleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

enum class ContentType {
    MOVIES, TV_SHOWS
}

enum class SortOption(val apiValue: String) {
    YEAR_DESC("release_date_desc"),
    YEAR_ASC("release_date_asc"),
    TITLE_ASC("title_asc"),
    TITLE_DESC("title_desc")
}

@RequiresApi(Build.VERSION_CODES.O)
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


    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _selectedGenres = MutableStateFlow<Set<Int>>(emptySet())
    val selectedGenres: StateFlow<Set<Int>> = _selectedGenres.asStateFlow()

    private val _networkStatus = MutableStateFlow(ConnectivityObserver.Status.Available)
    val networkStatus: StateFlow<ConnectivityObserver.Status> = _networkStatus.asStateFlow()

    private val disposables = CompositeDisposable()
    private var wasDisconnected = false

    private val _releases = MutableStateFlow<List<Title>>(emptyList())
    val releases: StateFlow<List<Title>> = _releases.asStateFlow()


    init {
        Log.d(TAG, "HomeViewModel initialized")
        observeNetworkStatus()
        loadGenres()
        loadRecentReleases()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadRecentReleases() {
        viewModelScope.launch {
            try {
                val recent = repository.getRecentReleases()
                _releases.value = recent
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load recent releases", e)
            }
        }
    }

    @SuppressLint("CheckResult")
    fun loadGenres() {
        repository.getGenres()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ genresList -> _genres.value = genresList },
                { error ->
                    Log.e(TAG, "Failed to load genres", error) })
    }


    fun setSelectedTab(tab: ContentType) {
        _selectedTab.value = tab
        loadRecentReleases()
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

                if (status == ConnectivityObserver.Status.Available
                    && wasDisconnected) {
                    Log.d(TAG, "Network restored - auto-reloading data")
                    wasDisconnected = false
                    delay(500)
                    loadRecentReleases()
                }
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
