package com.example.moviesapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Title(
    val id: Int,
    val title: String,
    val type: String,
    val year: Int?,
    val imdb_id: String?,
    val tmdb_id: Int?,
    val poster: String?,
    val user_rating: Double?
) : Parcelable


@Parcelize
data class TitleDetails(
    val id: Int,
    val title: String,
    val original_title: String?,
    val plot_overview: String?,
    val type: String,
    val runtime_minutes: Int?,
    val year: Int?,
    val release_date: String?,
    val user_rating: Double?,
    val critic_score: Int?,
    val poster: String?,
    val backdrop: String?,
    val original_language: String?,
    val genres: List<Int>?,
    val genre_names: List<String>?,
    val tmdb_id: Int?,
    val imdb_id: String?,
    val sources: List<StreamingSource>? = null
) : Parcelable


@Parcelize
data class ListTitlesResponse(
    val titles: List<Title>,
    val page: Int,
    val total_pages: Int,
    val total_results: Int
) : Parcelable
