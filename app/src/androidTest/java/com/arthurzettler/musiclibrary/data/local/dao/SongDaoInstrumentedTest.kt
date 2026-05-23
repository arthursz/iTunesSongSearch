package com.arthurzettler.musiclibrary.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arthurzettler.musiclibrary.data.local.database.AppDatabase
import com.arthurzettler.musiclibrary.data.local.entity.SongEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SongDaoInstrumentedTest {

    private lateinit var database: AppDatabase
    private lateinit var songDao: SongDao

    private val playedSong = SongEntity(
        trackId = 1L,
        trackName = "Get Lucky",
        artistName = "Daft Punk",
        collectionName = "RAM",
        artworkUrl = "https://example.com/art.jpg",
        previewUrl = "https://example.com/preview.m4a",
        trackTimeMillis = 248000L,
        collectionId = 100L,
        trackNumber = 8,
        lastPlayedAt = 9_999L
    )

    private val apiSong = playedSong.copy(
        trackName = "Get Lucky (Updated)",
        lastPlayedAt = 0L,
        cachedAt = 2_000L
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        songDao = database.songDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun upsertSongs_preserves_lastPlayedAt_when_song_already_exists() = runBlocking {
        songDao.insertSong(playedSong)

        songDao.upsertSongs(listOf(apiSong))

        val recentlyPlayed = songDao.getRecentlyPlayed().first()
        assertEquals(1, recentlyPlayed.size)
        assertEquals(9_999L, recentlyPlayed.first().lastPlayedAt)
        assertEquals("Get Lucky (Updated)", recentlyPlayed.first().trackName)
    }

    @Test
    fun getRecentlyPlayed_orders_by_lastPlayedAt_descending() = runBlocking {
        val older = playedSong.copy(trackId = 1L, lastPlayedAt = 1_000L)
        val newer = playedSong.copy(trackId = 2L, trackName = "Aerodynamic", lastPlayedAt = 5_000L)
        songDao.insertSong(older)
        songDao.insertSong(newer)

        val recentlyPlayed = songDao.getRecentlyPlayed().first()

        assertEquals(2L, recentlyPlayed.first().trackId)
        assertEquals(1L, recentlyPlayed.last().trackId)
    }
}
