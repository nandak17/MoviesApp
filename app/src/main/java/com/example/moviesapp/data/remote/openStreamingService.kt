package com.example.moviesapp.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

fun openPlayStoreUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.setPackage("com.android.vending")
        context.startActivity(intent)
    } catch (e: Exception) {
        val webIntent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(webIntent)
    }
}
