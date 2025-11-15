package com.example.moviesapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Title(
    val id: Int,
    val title: String,
    val type: String,
    val year: Int?,
    val source_release_date: String?,
    val imdb_id: String?,
    val tmdb_id: Int?,
    val poster: String?,
    val user_rating: Double?,
    val genres: List<Int>? // NEW: genres as a List of IDs

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

data class TitleListResponse(
    val titles: List<Title>
)


@Parcelize
data class ListTitlesResponse(
    val titles: List<Title>,
    val page: Int,
    val total_pages: Int,
    val total_results: Int
) : Parcelable

@Parcelize
data class Genre(
    val id: Int,
    val name: String,
    val tmdb_id: Int?
) : Parcelable

data class Release(
    val id: Int,
    val title: String,
    val poster_url: String?,
    val year: Int?,
    val source_release_date: String?,
    val type: String,      // <-- use this to distinguish
    val imdb_id: String?,   // <-- Add this!
    val tmdb_id: Int?,      // <-- Add this!
    val genres: List<Int>?, // optional, can be used similarly
)
data class ReleasesResponse(
    val releases: List<Release>
)

