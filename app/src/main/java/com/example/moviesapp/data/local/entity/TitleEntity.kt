package com.example.moviesapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "titles")
data class TitleEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val type: String,
    val year: Int?,
    val imdb_id: String?,
    val tmdb_id: Int?,
    val poster: String?,
    val user_rating: Double?,
    val source_release_date: String?,
    val genres: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "title_details")
data class TitleDetailsEntity(
    @PrimaryKey val id: Int,
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
    val genres: String?,
    val genre_names: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)
