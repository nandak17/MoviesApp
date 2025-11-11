package com.example.moviesapp.data.remote

import com.example.moviesapp.data.model.*
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WatchmodeApiService {
    @GET("title/{titleId}/sources/")
    fun getTitleSources(
        @Path("titleId") titleId: Int,
        @Query("apiKey") apiKey: String,
        @Query("regions") regions: String = "US"
    ): Single<List<StreamingSource>>

    @GET("list-titles/")
    fun getMovies(
        @Query("apiKey") apiKey: String,
        @Query("types") types: String = "movie",
        @Query("limit") limit: Int = 250,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity_desc"
    ): Single<ListTitlesResponse>

    @GET("list-titles/")
    fun getTvShows(
        @Query("apiKey") apiKey: String,
        @Query("types") types: String = "tv_series",
        @Query("limit") limit: Int = 250,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity_desc"
    ): Single<ListTitlesResponse>

    @GET("title/{title_id}/details/")
    fun getTitleDetails(
        @Path("title_id") titleId: Int,
        @Query("apiKey") apiKey: String
    ): Single<TitleDetails>
}
