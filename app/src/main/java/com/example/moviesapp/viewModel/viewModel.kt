package com.example.moviesapp.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviesapp.models.Data
import kotlinx.coroutines.launch
import com.example.moviesapp.models.Details


class MovieViewModel : ViewModel() {
    private val Repository = Repository()
    var state by mutableStateOf(
        ScreenState()
    )
    var id by mutableStateOf(
        0
    )

    init {
        viewModelScope.launch {
            val response = Repository.getMovieList(state.page)
            state = state.copy(
                movies = response.body()!!.data
            )
        }
    }

    fun getDetailsById() {
        viewModelScope.launch {
            try {
                val response = Repository.getDetailsById(id)
                if(response.isSuccessful){
                    state = state.copy(
                        detailsData = response.body()!!
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class ScreenState(
    val movies: List<Data> = emptyList(),
    val page: Int = 1,
    val detailsData: Details = Details()
)