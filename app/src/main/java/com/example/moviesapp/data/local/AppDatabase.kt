package com.example.moviesapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.moviesapp.data.local.dao.TitleDao
import com.example.moviesapp.data.local.dao.TitleDetailsDao
import com.example.moviesapp.data.local.entity.TitleDetailsEntity
import com.example.moviesapp.data.local.entity.TitleEntity

@Database(
    entities = [TitleEntity::class, TitleDetailsEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun titleDao(): TitleDao
    abstract fun titleDetailsDao(): TitleDetailsDao
}
