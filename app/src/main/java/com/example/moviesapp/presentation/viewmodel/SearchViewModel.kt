package com.example.moviesapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.rxjava3.cachedIn
import com.example.moviesapp.BuildConfig
import com.example.moviesapp.data.model.AutocompleteResult
import com.example.moviesapp.data.model.Title
import com.example.moviesapp.data.remote.WatchmodeApiService
import com.example.moviesapp.data.repository.TitleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: TitleRepository,
    private val savedStateHandle: SavedStateHandle,
    private val watchmodeApiService: WatchmodeApiService
) : ViewModel() {

    companion object {
        private const val KEY_SEARCH_QUERY = "search_query"
        private const val TAG = "SearchViewModel"
    }

    private val _searchQuery = MutableStateFlow(
        savedStateHandle.get<String>(KEY_SEARCH_QUERY) ?: ""
    )
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allMoviesPagingData = MutableStateFlow<PagingData<Title>>(PagingData.empty())
    val allMoviesPagingData: StateFlow<PagingData<Title>> = _allMoviesPagingData.asStateFlow()

    private val _filteredPagingData = MutableStateFlow<PagingData<Title>>(PagingData.empty())
    val filteredPagingData: StateFlow<PagingData<Title>> = _filteredPagingData.asStateFlow()

    private val _autocompleteResults = MutableStateFlow<List<AutocompleteResult>>(emptyList())
    val autocompleteResults: StateFlow<List<AutocompleteResult>> = _autocompleteResults.asStateFlow()

    private val disposables = CompositeDisposable()

    init {
        loadMovies()
    }

    fun searchAutocomplete(query: String) {
        if (query.isBlank()) {
            _autocompleteResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            disposables.add(
                watchmodeApiService.autocompleteSearch(
                    apiKey = BuildConfig.WATCHMODE_API_KEY,
                    query = query,
                    searchType = 1
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { response ->
                            _autocompleteResults.value = response.results
                        },
                        { error ->
                            Log.e(TAG, "Autocomplete search failed", error)
                            _autocompleteResults.value = emptyList()
                        }
                    )
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadMovies() {
        Log.d(TAG, "Loading movies for search")
        disposables.add(
            repository.getMoviesPaged()
                .cachedIn(viewModelScope)
                .subscribe(
                    { pagingData ->
                        _allMoviesPagingData.value = pagingData
                        if (_searchQuery.value.isEmpty()) {
                            _filteredPagingData.value = pagingData
                        }
                    },
                    { error ->
                        Log.e(TAG, "Error loading movies", error)
                    }
                )
        )
    }


    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
