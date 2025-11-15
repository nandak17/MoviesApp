package com.example.moviesapp.data.repository

import MoviesRemoteMediator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.paging.*
import androidx.paging.rxjava3.flowable
import com.example.moviesapp.BuildConfig
import com.example.moviesapp.data.local.dao.TitleDao
import com.example.moviesapp.data.local.dao.TitleDetailsDao
import com.example.moviesapp.data.mapper.toEntity
import com.example.moviesapp.data.mapper.toModel
import com.example.moviesapp.data.model.Genre
import com.example.moviesapp.data.model.PersonCredit
import com.example.moviesapp.data.model.StreamingServiceInfo
import com.example.moviesapp.data.model.StreamingSource
import com.example.moviesapp.data.model.Title
import com.example.moviesapp.data.model.TitleDetails
import com.example.moviesapp.data.paging.TVShowsRemoteMediator
import com.example.moviesapp.data.remote.WatchmodeApiService
import com.example.moviesapp.presentation.viewmodel.SortOption
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


@Singleton
class TitleRepository @Inject constructor(
    private val apiService: WatchmodeApiService,
    private val titleDao: TitleDao,
    private val titleDetailsDao: TitleDetailsDao,
    private val apiKey: String,
    ) {

    companion object {
        private const val TAG = "TitleRepository"
    }

    private var cachedSources: Map<Int, StreamingServiceInfo>? = null


    fun getSources(): Single<Map<Int, StreamingServiceInfo>> {
        return apiService.getSources()
            .map { sources ->
                sources.associateBy { it.id }.also { cachedSources = it }
            }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getMoviesPaged(sortOption: SortOption = SortOption.YEAR_ASC, genreIds: List<Int> = emptyList()): Flowable<PagingData<Title>> {
        return Pager(
            config = PagingConfig(pageSize = 50, prefetchDistance = 10, enablePlaceholders = false),
            remoteMediator = MoviesRemoteMediator(apiService, titleDao, apiKey, sortOption, genreIds),
            pagingSourceFactory = {
                when (sortOption) {
                    SortOption.YEAR_DESC -> titleDao.getMoviesByYearDesc()
                    SortOption.YEAR_ASC -> titleDao.getMoviesByYearAsc()
                    SortOption.TITLE_ASC -> titleDao.getMoviesByTitleAsc()
                    SortOption.TITLE_DESC -> titleDao.getMoviesByTitleDesc()
                }
            }
        ).flowable.map { pagingData -> pagingData.map { it.toModel() } }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getRecentReleases(): List<Title> {
        val today = LocalDate.now()
        val priorDate = today.minusDays(30)
        val formatter = DateTimeFormatter.BASIC_ISO_DATE
        val startDate = priorDate.format(formatter)
        val endDate = today.format(formatter)

        val releaseResponse = apiService.getReleases(
            apiKey = apiKey,
            startDate = startDate,
            endDate = endDate
        )
        val releases = releaseResponse.releases

        return releases.map { release ->
            Title(
                id = release.id,
                title = release.title,
                type = release.type,
                year = release.year,
                source_release_date = release.source_release_date,
                poster = release.poster_url,
                imdb_id = release.imdb_id,
                tmdb_id = release.tmdb_id,
                user_rating = null,
                genres = null
            )
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getTVShowsPaged(
        sortOption: SortOption = SortOption.YEAR_ASC,
        genreIds: List<Int> = emptyList()
    ): Flowable<PagingData<Title>> {
        return Pager(
            config = PagingConfig(pageSize = 50, prefetchDistance = 10, enablePlaceholders = false),
            remoteMediator = TVShowsRemoteMediator(apiService, titleDao, apiKey, sortOption, genreIds),
            pagingSourceFactory = {
                when (sortOption) {
                    SortOption.YEAR_DESC -> titleDao.getTVShowsByYearDesc()
                    SortOption.YEAR_ASC -> titleDao.getTVShowsByYearAsc()
                    SortOption.TITLE_ASC -> titleDao.getTVShowsByTitleAsc()
                    SortOption.TITLE_DESC -> titleDao.getTVShowsByTitleDesc()
                }
            }
        ).flowable.map { pagingData -> pagingData.map { it.toModel() } }
    }

    fun getTitleSources(titleId: Int): Single<List<StreamingSource>> {
        return apiService.getTitleSources(titleId, apiKey, "US,IN")
            .subscribeOn(Schedulers.io())
    }

    fun getCastAndCrew(titleId: Int): Single<List<PersonCredit>> {
        return apiService.getCastAndCrew(titleId)
    }

    fun getGenres(): Single<List<Genre>> {
        return apiService.getGenres(
            apiKey = BuildConfig.WATCHMODE_API_KEY
        )
            .subscribeOn(Schedulers.io())
    }

    @SuppressLint("CheckResult")
    fun getTitleDetails(titleId: Int): Single<TitleDetails> {
        Log.d(TAG, "Getting details for title: $titleId")

        return titleDetailsDao.getTitleDetails(titleId)
            .subscribeOn(Schedulers.io())
            .map { entity ->
                Log.d(TAG, "✅ Details found in cache for: $titleId")
                entity.toModel()
            }
            .onErrorResumeNext { error ->
                Log.d(TAG, "Cache miss, fetching from API for: $titleId")
                apiService.getTitleDetails(titleId, apiKey)
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess { details ->
                        Log.d(TAG, "Caching details for: $titleId")
                        titleDetailsDao.insert(details.toEntity())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                { Log.d(TAG, "Cached details for: $titleId") },
                                { e -> Log.e(TAG, "Failed to cache details", e) }
                            )
                    }
            }
    }
}
