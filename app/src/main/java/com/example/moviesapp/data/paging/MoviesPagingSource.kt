package com.example.moviesapp.data.paging

import android.util.Log
import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import com.example.moviesapp.data.model.Title
import com.example.moviesapp.data.remote.WatchmodeApiService
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class MoviesPagingSource(private val apiService: WatchmodeApiService, private val apiKey: String
) : RxPagingSource<Int, Title>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, Title>> {
        val page = params.key ?: 1

        Log.d("MoviesPagingSource", "Loading page: $page")

        return apiService.getMovies(
            apiKey = apiKey,
            limit = 50,
            page = page
        )
            .subscribeOn(Schedulers.io())
            .map { response ->
                Log.d("MoviesPagingSource", "Page $page: ${response.titles.size} total items")

                response.titles.take(3).forEach { title ->
                    Log.d("MoviesPagingSource", "Title: ${title.title}, Poster: ${title.poster}")
                }

                LoadResult.Page(
                    data = response.titles,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (response.titles.isEmpty()) null else page + 1
                ) as LoadResult<Int, Title>
            }
            .onErrorReturn { e ->
                Log.e("MoviesPagingSource", "Error loading page $page", e)
                LoadResult.Error(e)
            }
    }

    override fun getRefreshKey(state: PagingState<Int, Title>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

class TVShowsPagingSource(
    private val apiService: WatchmodeApiService,
    private val apiKey: String
) : RxPagingSource<Int, Title>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, Title>> {
        val page = params.key ?: 1

        return apiService.getTvShows(
            apiKey = apiKey,
            limit = 50,
            page = page
        )
            .subscribeOn(Schedulers.io())
            .map { response ->
                Log.d("TVShowsPagingSource", "Page $page: ${response.titles.size} total items")

                LoadResult.Page(
                    data = response.titles,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (response.titles.isEmpty()) null else page + 1
                ) as LoadResult<Int, Title>
            }
            .onErrorReturn { e -> LoadResult.Error(e) }
    }

    override fun getRefreshKey(state: PagingState<Int, Title>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
