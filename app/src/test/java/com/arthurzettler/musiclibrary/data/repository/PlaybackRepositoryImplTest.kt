package com.arthurzettler.musiclibrary.data.repository

import com.arthurzettler.musiclibrary.data.local.dao.NowPlayingDao
import com.arthurzettler.musiclibrary.data.local.entity.NowPlayingEntity
import com.arthurzettler.musiclibrary.domain.model.Song
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val nowPlayingDao: NowPlayingDao = mockk(relaxed = true)
    private val repository = PlaybackRepositoryImpl(nowPlayingDao, testDispatcher)

    private val fakeSong = Song(
        trackId = 1L,
        trackName = "Get Lucky",
        artistName = "Daft Punk",
        collectionName = "Random Access Memories",
        artworkUrl = "https://example.com/art.jpg",
        previewUrl = "https://example.com/preview.m4a",
        trackTimeMillis = 248000L,
        collectionId = 100L,
        trackNumber = 8
    )

    @Test
    fun `setPlaylist converts songs to entities and saves`() = runTest {
        val songs = listOf(fakeSong, fakeSong.copy(trackId = 2L, trackName = "Aerodynamic"))
        val entitiesSlot = slot<List<NowPlayingEntity>>()
        val indexSlot = slot<Int>()

        coEvery { nowPlayingDao.setPlaylist(capture(entitiesSlot), capture(indexSlot)) } returns Unit

        repository.setPlaylist(songs, 1)

        coVerify { nowPlayingDao.setPlaylist(any(), 1) }
        assertEquals(2, entitiesSlot.captured.size)
        assertEquals(0, entitiesSlot.captured[0].position)
        assertEquals(1, entitiesSlot.captured[1].position)
        assertEquals(false, entitiesSlot.captured[0].isCurrentTrack)
        assertEquals(true, entitiesSlot.captured[1].isCurrentTrack)
        assertEquals(1, indexSlot.captured)
    }

    @Test
    fun `getPlaylist returns domain songs from dao`() = runTest {
        val entity = NowPlayingEntity(
            position = 0,
            trackId = 1L,
            trackName = "Get Lucky",
            artistName = "Daft Punk",
            collectionName = "Random Access Memories",
            artworkUrl = "https://example.com/art.jpg",
            previewUrl = "https://example.com/preview.m4a",
            trackTimeMillis = 248000L,
            collectionId = 100L,
            trackNumber = 8,
            isCurrentTrack = true
        )
        coEvery { nowPlayingDao.getPlaylist() } returns listOf(entity)

        val result = repository.getPlaylist()

        assertEquals(1, result.size)
        assertEquals(fakeSong, result[0])
    }

    @Test
    fun `getCurrentIndex returns position of current track`() = runTest {
        val entity = NowPlayingEntity(
            position = 3,
            trackId = 1L,
            trackName = "Get Lucky",
            artistName = "Daft Punk",
            collectionName = "Random Access Memories",
            artworkUrl = "https://example.com/art.jpg",
            previewUrl = "https://example.com/preview.m4a",
            trackTimeMillis = 248000L,
            collectionId = 100L,
            trackNumber = 8,
            isCurrentTrack = true
        )
        coEvery { nowPlayingDao.getCurrentTrack() } returns entity

        val index = repository.getCurrentIndex()

        assertEquals(3, index)
    }

    @Test
    fun `getCurrentIndex returns null when no current track`() = runTest {
        coEvery { nowPlayingDao.getCurrentTrack() } returns null

        val index = repository.getCurrentIndex()

        assertEquals(null, index)
    }

    @Test
    fun `setCurrentIndex delegates to transactional dao method`() = runTest {
        coEvery { nowPlayingDao.setCurrentIndex(5) } returns Unit

        repository.setCurrentIndex(5)

        coVerify { nowPlayingDao.setCurrentIndex(5) }
    }
}
