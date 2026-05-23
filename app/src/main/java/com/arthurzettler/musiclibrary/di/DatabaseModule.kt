package com.arthurzettler.musiclibrary.di

import android.content.Context
import androidx.room.Room
import com.arthurzettler.musiclibrary.data.local.DatabaseTransactionRunner
import com.arthurzettler.musiclibrary.data.local.RoomDatabaseTransactionRunner
import com.arthurzettler.musiclibrary.data.local.dao.NowPlayingDao
import com.arthurzettler.musiclibrary.data.local.dao.SearchCacheDao
import com.arthurzettler.musiclibrary.data.local.dao.SongDao
import com.arthurzettler.musiclibrary.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "music_library.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSongDao(database: AppDatabase): SongDao {
        return database.songDao()
    }

    @Provides
    @Singleton
    fun provideNowPlayingDao(database: AppDatabase): NowPlayingDao {
        return database.nowPlayingDao()
    }

    @Provides
    @Singleton
    fun provideSearchCacheDao(database: AppDatabase): SearchCacheDao {
        return database.searchCacheDao()
    }

    @Provides
    @Singleton
    fun provideDatabaseTransactionRunner(
        database: AppDatabase
    ): DatabaseTransactionRunner {
        return RoomDatabaseTransactionRunner(database)
    }
}
