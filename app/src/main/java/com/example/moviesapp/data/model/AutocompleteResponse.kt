package com.example.moviesapp.data.model

data class AutocompleteResponse(
    val results: List<AutocompleteResult>
)

data class AutocompleteResult(
    val name: String,
    val relevance: Double,
    val type: String,
    val id: Int,
    val year: Int?,
    val result_type: String,
    val tmdb_id: Int?,
    val tmdb_type: String?,
    val image_url: String?
)
