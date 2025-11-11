package com.example.moviesapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.rxjava3.cachedIn
import com.example.moviesapp.data.model.Title
import com.example.moviesapp.data.repository.TitleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: TitleRepository,
    private val savedStateHandle: SavedStateHandle
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

    private val disposables = CompositeDisposable()

    init {
        loadMovies()
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
                        // Initially show all movies
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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        savedStateHandle[KEY_SEARCH_QUERY] = query

        Log.d(TAG, "Search query changed: $query")

        if (query.isBlank()) {

            _filteredPagingData.value = _allMoviesPagingData.value
        } else {

            disposables.add(
                repository.getMoviesPaged()
                    .map { pagingData ->
                        pagingData.filter { title ->
                            title.title.contains(query, ignoreCase = true)
                        }
                    }
                    .subscribe(
                        { filteredData ->
                            _filteredPagingData.value = filteredData
                        },
                        { error ->
                            Log.e(TAG, "Error filtering", error)
                        }
                    )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
