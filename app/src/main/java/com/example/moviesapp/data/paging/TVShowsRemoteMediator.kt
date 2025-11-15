package com.example.moviesapp.data.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.moviesapp.data.local.dao.TitleDao
import com.example.moviesapp.data.local.entity.TitleEntity
import com.example.moviesapp.data.mapper.toEntity
import com.example.moviesapp.data.remote.WatchmodeApiService
import com.example.moviesapp.presentation.viewmodel.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPagingApi::class)
class TVShowsRemoteMediator(
    private val apiService: WatchmodeApiService,
    private val titleDao: TitleDao,
    private val apiKey: String,
    private val sortOption: SortOption,
    private val genreIds: List<Int> = emptyList()
) : RemoteMediator<Int, TitleEntity>() {

    companion object {
        private const val TAG = "TVShowsRemoteMediator"
    }

    private var currentPage = 1

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TitleEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    currentPage = 1
                    1
                }
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> currentPage + 1
            }

            val genresParam = if (genreIds.isNotEmpty()) genreIds.joinToString(",") else null
            Log.d(TAG, "Loading page: $page with genres: $genresParam and sort: ${sortOption.apiValue}")

            val response = withContext(Dispatchers.IO) {
                apiService.getTvShows(
                    apiKey = apiKey,
                    limit = 50,
                    page = page,
                    sortBy = sortOption.apiValue,
                    genres = genresParam
                ).blockingGet()
            }

            if (loadType == LoadType.REFRESH) {
                withContext(Dispatchers.IO) {
                    titleDao.deleteByType("tv_series").blockingAwait()
                }
            }

            val entities = response.titles.map { it.toEntity() }
            withContext(Dispatchers.IO) {
                titleDao.insertAll(entities).blockingAwait()
            }

            currentPage = page
            Log.d(TAG, "Cached ${entities.size} TV shows to database")

            MediatorResult.Success(endOfPaginationReached = response.titles.isEmpty())
        } catch (e: Exception) {
            Log.e(TAG, "Error loading TV shows", e)
            MediatorResult.Error(e)
        }
    }
}
