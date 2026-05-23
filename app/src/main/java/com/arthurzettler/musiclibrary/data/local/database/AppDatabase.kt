package com.arthurzettler.musiclibrary.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arthurzettler.musiclibrary.data.local.dao.NowPlayingDao
import com.arthurzettler.musiclibrary.data.local.dao.SearchCacheDao
import com.arthurzettler.musiclibrary.data.local.dao.SongDao
import com.arthurzettler.musiclibrary.data.local.entity.NowPlayingEntity
import com.arthurzettler.musiclibrary.data.local.entity.RemoteKeyEntity
import com.arthurzettler.musiclibrary.data.local.entity.SearchResultEntity
import com.arthurzettler.musiclibrary.data.local.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        NowPlayingEntity::class,
        SearchResultEntity::class,
        RemoteKeyEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun nowPlayingDao(): NowPlayingDao
    abstract fun searchCacheDao(): SearchCacheDao
}
