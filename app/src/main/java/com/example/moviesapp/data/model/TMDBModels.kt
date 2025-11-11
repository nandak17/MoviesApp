// data/model/TMDBModels.kt
package com.example.moviesapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TMDBImagesResponse(
    val id: Int,
    val backdrops: List<TMDBImage>,
    val posters: List<TMDBImage>,
    val logos: List<TMDBImage>?
) : Parcelable

@Parcelize
data class TMDBImage(
    val aspect_ratio: Double,
    val file_path: String,
    val height: Int,
    val width: Int,
    val vote_average: Double?,
    val vote_count: Int?
) : Parcelable {

    fun getImageUrl(size: String = "w500"): String {
        return "https://image.tmdb.org/t/p/$size$file_path"
    }
}
