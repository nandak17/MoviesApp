package com.example.moviesapp.data.mapper

import com.example.moviesapp.data.local.entity.TitleDetailsEntity
import com.example.moviesapp.data.local.entity.TitleEntity
import com.example.moviesapp.data.model.Title
import com.example.moviesapp.data.model.TitleDetails
import com.google.gson.Gson

fun Title.toEntity(): TitleEntity {
    return TitleEntity(
        id = id,
        title = title,
        type = type,
        year = year,
        source_release_date = source_release_date,
        imdb_id = imdb_id,
        tmdb_id = tmdb_id,
        poster = poster,
        user_rating = user_rating,
        genres = genres?.joinToString(",")
    )
}

fun TitleEntity.toModel(): Title {
    return Title(
        id = id,
        title = title,
        type = type,
        year = year,
        source_release_date = source_release_date,
        imdb_id = imdb_id,
        tmdb_id = tmdb_id,
        poster = poster,
        user_rating = user_rating,
        genres = listOf(28)
    )
}

fun TitleDetails.toEntity(): TitleDetailsEntity {
    val gson = Gson()
    return TitleDetailsEntity(
        id = id,
        title = title,
        original_title = original_title,
        plot_overview = plot_overview,
        type = type,
        runtime_minutes = runtime_minutes,
        year = year,
        release_date = release_date,
        user_rating = user_rating,
        critic_score = critic_score,
        poster = poster,
        backdrop = backdrop,
        original_language = original_language,
        genres = gson.toJson(genres),
        genre_names = gson.toJson(genre_names)
    )
}

fun TitleDetailsEntity.toModel(): TitleDetails {
    val gson = Gson()
    return TitleDetails(
        id = id,
        title = title,
        original_title = original_title,
        plot_overview = plot_overview,
        type = type,
        runtime_minutes = runtime_minutes,
        year = year,
        release_date = release_date,
        user_rating = user_rating,
        critic_score = critic_score,
        poster = poster,
        backdrop = backdrop,
        original_language = original_language,
        genres = try {
            gson.fromJson(genres, List::class.java)?.mapNotNull { (it as? Double)?.toInt() }
        } catch (e: Exception) {
            null
        },
        genre_names = try {
            gson.fromJson(genre_names, List::class.java) as? List<String>
        } catch (e: Exception) {
            null
        },
        tmdb_id = null,
        imdb_id = null,
        sources = null
    )
}
