package com.example.moviesapp.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.moviesapp.data.local.entity.TitleDetailsEntity
import com.example.moviesapp.data.local.entity.TitleEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface TitleDao {
    //movies
    @Query("SELECT * FROM titles WHERE type = 'movie' ORDER BY user_rating DESC")
    fun getMoviesByRatingDesc(): PagingSource<Int, TitleEntity>

    @Query("SELECT * FROM titles WHERE type = 'movie' ORDER BY user_rating ASC")
    fun getMoviesByRatingAsc(): PagingSource<Int, TitleEntity>

    @Query("SELECT * FROM titles WHERE type = 'movie' ORDER BY year DESC")
    fun getMoviesByYearDesc(): PagingSource<Int, TitleEntity>

    @Query("SELECT * FROM titles WHERE type = 'movie' ORDER BY year ASC")
    fun getMoviesByYearAsc(): PagingSource<Int, TitleEntity>

    @Query("SELECT * FROM titles WHERE type = 'movie' ORDER BY title ASC")
    fun getMoviesByTitleAsc(): PagingSource<Int, TitleEntity>

    @Query("SELECT * FROM titles WHERE type = 'movie' ORDER BY title DESC")
    fun getMoviesByTitleDesc(): PagingSource<Int, TitleEntity>

    // TV Shows
    @Query("SELECT * FROM titles WHERE type = 'tv_series' ORDER BY user_rating DESC")
    fun getTVShowsByRatingDesc(): PagingSource<Int, TitleEntity>

    @Query("SELECT * FROM titles WHERE type = 'tv_series' ORDER BY user_rating ASC")
    fun getTVShowsByRatingAsc(): PagingSource<Int, TitleEntity>

    @Query("SELECT * FROM titles WHERE type = 'tv_series' ORDER BY year DESC")
    fun getTVShowsByYearDesc(): PagingSource<Int, TitleEntity>

    @Query("SELECT * FROM titles WHERE type = 'tv_series' ORDER BY year ASC")
    fun getTVShowsByYearAsc(): PagingSource<Int, TitleEntity>

    @Query("SELECT * FROM titles WHERE type = 'tv_series' ORDER BY title ASC")
    fun getTVShowsByTitleAsc(): PagingSource<Int, TitleEntity>

    @Query("SELECT * FROM titles WHERE type = 'tv_series' ORDER BY title DESC")
    fun getTVShowsByTitleDesc(): PagingSource<Int, TitleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(titles: List<TitleEntity>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(title: TitleEntity): Completable

    @Query("DELETE FROM titles WHERE type = :type")
    fun deleteByType(type: String): Completable

    @Query("DELETE FROM titles")
    fun deleteAll(): Completable
}


@Dao
interface TitleDetailsDao {
    @Query("SELECT * FROM title_details WHERE id = :titleId")
    fun getTitleDetails(titleId: Int): Single<TitleDetailsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(details: TitleDetailsEntity): Completable

    @Query("DELETE FROM title_details WHERE id = :titleId")
    fun delete(titleId: Int): Completable
}
