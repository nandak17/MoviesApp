package com.example.moviesapp.data.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.paging.*
import androidx.paging.rxjava3.flowable
import com.example.moviesapp.data.local.dao.TitleDao
import com.example.moviesapp.data.local.dao.TitleDetailsDao
import com.example.moviesapp.data.mapper.toEntity
import com.example.moviesapp.data.mapper.toModel
import com.example.moviesapp.data.model.StreamingSource
import com.example.moviesapp.data.model.StreamingSourcesResponse
import com.example.moviesapp.data.model.Title
import com.example.moviesapp.data.model.TitleDetails
import com.example.moviesapp.data.paging.MoviesRemoteMediator
import com.example.moviesapp.data.paging.TVShowsRemoteMediator
import com.example.moviesapp.data.remote.WatchmodeApiService
import com.example.moviesapp.presentation.viewmodel.SortOption
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TitleRepository @Inject constructor(
    private val apiService: WatchmodeApiService,
    private val titleDao: TitleDao,
    private val titleDetailsDao: TitleDetailsDao,
    private val apiKey: String
) {

    companion object {
        private const val TAG = "TitleRepository"
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getMoviesPaged(sortOption: SortOption = SortOption.RATING_DESC): Flowable<PagingData<Title>> {
        Log.d(TAG, "Getting movies with sort: ${sortOption.displayName}")

        return Pager(
            config = PagingConfig(
                pageSize = 50,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            remoteMediator = MoviesRemoteMediator(apiService, titleDao, apiKey),
            pagingSourceFactory = {
                when (sortOption) {
                    SortOption.RATING_DESC -> titleDao.getMoviesByRatingDesc()
                    SortOption.RATING_ASC -> titleDao.getMoviesByRatingAsc()
                    SortOption.YEAR_DESC -> titleDao.getMoviesByYearDesc()
                    SortOption.YEAR_ASC -> titleDao.getMoviesByYearAsc()
                    SortOption.TITLE_ASC -> titleDao.getMoviesByTitleAsc()
                    SortOption.TITLE_DESC -> titleDao.getMoviesByTitleDesc()
                }
            }
        ).flowable
            .map { pagingData ->
                pagingData.map { it.toModel() }
            }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getTVShowsPaged(sortOption: SortOption = SortOption.RATING_DESC): Flowable<PagingData<Title>> {
        Log.d(TAG, "Getting TV shows with sort: ${sortOption.displayName}")

        return Pager(
            config = PagingConfig(
                pageSize = 50,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            remoteMediator = TVShowsRemoteMediator(apiService, titleDao, apiKey),
            pagingSourceFactory = {
                when (sortOption) {
                    SortOption.RATING_DESC -> titleDao.getTVShowsByRatingDesc()
                    SortOption.RATING_ASC -> titleDao.getTVShowsByRatingAsc()
                    SortOption.YEAR_DESC -> titleDao.getTVShowsByYearDesc()
                    SortOption.YEAR_ASC -> titleDao.getTVShowsByYearAsc()
                    SortOption.TITLE_ASC -> titleDao.getTVShowsByTitleAsc()
                    SortOption.TITLE_DESC -> titleDao.getTVShowsByTitleDesc()
                }
            }
        ).flowable
            .map { pagingData ->
                pagingData.map { it.toModel() }
            }
    }

    fun getTitleSources(titleId: Int): Single<List<StreamingSource>> {
        return apiService.getTitleSources(titleId, apiKey, "US,IN")
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
