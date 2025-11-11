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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPagingApi::class)
class MoviesRemoteMediator(
    private val apiService: WatchmodeApiService,
    private val titleDao: TitleDao,
    private val apiKey: String
) : RemoteMediator<Int, TitleEntity>() {

    companion object {
        private const val TAG = "MoviesRemoteMediator"
    }

    private var currentPage = 1

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TitleEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> currentPage + 1
            }

            Log.d(TAG, "Loading page: $page")

            val response = withContext(Dispatchers.IO) {
                apiService.getMovies(
                    apiKey = apiKey,
                    limit = 50,
                    page = page
                ).blockingGet()
            }

            // Deleting old data on refresh
            if (loadType == LoadType.REFRESH) {
                withContext(Dispatchers.IO) {
                    titleDao.deleteByType("movie").blockingAwait()
                }
            }

            // Inserting new data
            val entities = response.titles.map { it.toEntity() }
            withContext(Dispatchers.IO) {
                titleDao.insertAll(entities).blockingAwait()
            }

            currentPage = page

            Log.d(TAG, "Cached ${entities.size} movies to database")

            MediatorResult.Success(endOfPaginationReached = response.titles.isEmpty())
        } catch (e: Exception) {
            Log.e(TAG, "Error loading movies", e)
            MediatorResult.Error(e)
        }
    }
}
