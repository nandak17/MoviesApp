package com.example.moviesapp.viewModel

import com.example.moviesapp.models.Details
import com.example.moviesapp.models.MoviesList
import com.example.moviesapp.utils.RetrofitInstance
import retrofit2.Response

class Repository {

    suspend fun getMovieList(page : Int):Response<MoviesList>{
        return RetrofitInstance.api.getMovies(page)
    }
    suspend fun getDetailsById(id : Int):Response<Details>{
        return RetrofitInstance.api.getDetailsById(id)
    }
}