package com.example.moviesapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StreamingSourcesResponse(val sources: List<StreamingSource>) : Parcelable

@Parcelize
data class StreamingSource(
    val source_id: Int,
    val name: String,
    val type: String,
    val region: String,
    val ios_url: String?,
    val android_url: String?,
    val web_url: String?,
    val format: String?,
    val price: Double?,
    val seasons: Int?,
    val episodes: Int?,
) : Parcelable

data class StreamingServiceInfo(
    val id: Int,
    val name: String,
    val type: String,
    val logo_100px: String?,
    val ios_appstore_url: String?,
    val android_playstore_url: String?,
    val web_url : String?,
    val regions: List<String>?
)

