package com.example.moviesapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviesapp.data.model.PersonCredit
import com.example.moviesapp.data.model.StreamingServiceInfo
import com.example.moviesapp.data.model.TitleDetails
import com.example.moviesapp.data.network.ConnectivityObserver
import com.example.moviesapp.data.repository.TitleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailsUiState {
    object Loading : DetailsUiState()
    data class Success(
        val details: TitleDetails,
        val cast: List<PersonCredit>,
        val crew: List<PersonCredit>
    ) : DetailsUiState()
    data class Error(val message: String) : DetailsUiState()
}

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: TitleRepository,
    private val connectivityObserver: ConnectivityObserver,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "DetailsViewModel"
    }

    private val titleId: Int = checkNotNull(savedStateHandle["titleId"])

    private val _cachedSources = MutableStateFlow<Map<Int, StreamingServiceInfo>>(emptyMap())
    val cachedSources: StateFlow<Map<Int, StreamingServiceInfo>> = _cachedSources.asStateFlow()

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val _networkStatus = MutableStateFlow(ConnectivityObserver.Status.Available)
    val networkStatus: StateFlow<ConnectivityObserver.Status> = _networkStatus.asStateFlow()

    private val disposables = CompositeDisposable()
    private var wasDisconnected = false

    init {
        Log.d(TAG, "DetailsViewModel initialized")
        observeNetworkStatus()
        fetchDetails()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                Log.d(TAG, "Network status changed: $status")
                _networkStatus.value = status

                if (status == ConnectivityObserver.Status.Lost ||
                    status == ConnectivityObserver.Status.Unavailable) {
                    wasDisconnected = true
                    Log.d(TAG, "Network lost - details will reload on reconnect")
                }

                if (status == ConnectivityObserver.Status.Available && wasDisconnected) {
                    Log.d(TAG, "Network restored - auto-reloading detail data")
                    wasDisconnected = false
                    delay(1500)
                    fetchDetails()
                }
            }
        }
    }

    private fun fetchDetails() {
        Log.d(TAG, "Fetching details for titleId: $titleId")
        disposables.clear()
        _uiState.value = DetailsUiState.Loading

        disposables.add(
            repository.getTitleDetails(titleId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { details ->
                    repository.getTitleSources(titleId)
                        .map { sources -> details.copy(sources = sources) }
                        .onErrorReturn { details }
                }
                .flatMap { detailsWithSources ->
                    repository.getCastAndCrew(titleId)
                        .map { credits ->
                            val cast = credits.filter { it.type == "Cast" }
                            val crew = credits.filter { it.type == "Crew" }
                            Triple(detailsWithSources, cast, crew)
                        }
                        .onErrorReturn { Triple(detailsWithSources, emptyList(), emptyList()) }
                }
                .subscribe(
                    { (details, cast, crew) ->
                        _uiState.value = DetailsUiState.Success(details, cast, crew)
                    },
                    { error ->
                        _uiState.value = DetailsUiState.Error(error.message ?: "Unknown error")
                    }
                )
        )
    }

    fun retry() {
        Log.d(TAG, "Retry button pressed")
        fetchDetails()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}