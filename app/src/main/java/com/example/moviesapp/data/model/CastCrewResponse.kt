package com.example.moviesapp.data.model


data class CastMember(
    val id: Int,
    val name: String,
    val character_name: String?,
    val tmdb_id: Int?
)
data class CrewMember(
    val id: Int,
    val name: String,
    val job: String?,
    val tmdb_id: Int?
)
data class PersonCredit(
    val person_id: Int,
    val type: String,
    val full_name: String,
    val headshot_url: String?,
    val role: String,
    val episode_count: Int?,
    val order: Int?
)

