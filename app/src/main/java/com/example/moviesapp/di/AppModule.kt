package com.example.moviesapp.di

import android.content.Context
import androidx.room.Room
import com.example.moviesapp.BuildConfig
import com.example.moviesapp.data.local.AppDatabase
import com.example.moviesapp.data.local.dao.TitleDao
import com.example.moviesapp.data.local.dao.TitleDetailsDao
import com.example.moviesapp.data.network.ConnectivityObserver
import com.example.moviesapp.data.network.NetworkConnectivityObserver
import com.example.moviesapp.data.remote.WatchmodeApiService
import com.example.moviesapp.data.repository.TitleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    companion object {
        @Provides
        @Singleton
        fun provideApiKey(): String {
            return BuildConfig.WATCHMODE_API_KEY
        }

        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "movies_database"
            ).fallbackToDestructiveMigration().build()
        }

        @Provides
        @Singleton
        fun provideTitleDao(database: AppDatabase): TitleDao {
            return database.titleDao()
        }

        @Provides
        @Singleton
        fun provideTitleDetailsDao(database: AppDatabase): TitleDetailsDao {
            return database.titleDetailsDao()
        }

        @Provides
        @Singleton
        fun provideTitleRepository(
            apiService: WatchmodeApiService,
            titleDao: TitleDao,
            titleDetailsDao: TitleDetailsDao,
            apiKey: String
        ): TitleRepository {
            return TitleRepository(apiService, titleDao, titleDetailsDao, apiKey)
        }

        @Provides
        @Singleton
        fun provideConnectivityObserver(
            @ApplicationContext context: Context
        ): ConnectivityObserver {
            return NetworkConnectivityObserver(context)
        }
    }
}
