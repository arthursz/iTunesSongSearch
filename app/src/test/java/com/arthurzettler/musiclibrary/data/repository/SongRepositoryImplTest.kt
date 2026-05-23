package com.arthurzettler.musiclibrary.data.repository

import com.arthurzettler.musiclibrary.data.local.dao.SearchCacheDao
import com.arthurzettler.musiclibrary.data.local.dao.SongDao
import com.arthurzettler.musiclibrary.data.local.DatabaseTransactionRunner
import com.arthurzettler.musiclibrary.data.local.entity.SongEntity
import com.arthurzettler.musiclibrary.data.remote.datasource.RemoteDataSource
import com.arthurzettler.musiclibrary.data.remote.dto.ITunesResponse
import com.arthurzettler.musiclibrary.data.remote.dto.SongDto
import com.arthurzettler.musiclibrary.domain.model.Outcome
import com.arthurzettler.musiclibrary.domain.model.Song
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val remoteDataSource: RemoteDataSource = mockk()
    private val transactionRunner: DatabaseTransactionRunner = mockk(relaxed = true)
    private val songDao: SongDao = mockk(relaxed = true)
    private val searchCacheDao: SearchCacheDao = mockk(relaxed = true)
    private lateinit var repository: SongRepositoryImpl

    private val fakeDto = SongDto(
        trackId = 1L,
        trackName = "Get Lucky",
        artistName = "Daft Punk",
        collectionName = "Random Access Memories",
        artworkUrl100 = "https://example.com/100x100.jpg",
        previewUrl = "https://example.com/preview.m4a",
        trackTimeMillis = 248000L,
        collectionId = 100L,
        trackNumber = 8,
        releaseDate = "2013-05-17",
        wrapperType = "track"
    )

    private val fakeEntity = SongEntity(
        trackId = 1L,
        trackName = "Get Lucky",
        artistName = "Daft Punk",
        collectionName = "Random Access Memories",
        artworkUrl = "https://example.com/600x600.jpg",
        previewUrl = "https://example.com/preview.m4a",
        trackTimeMillis = 248000L,
        collectionId = 100L,
        trackNumber = 8,
        lastPlayedAt = 1000L
    )

    @Before
    fun setup() {
        repository = SongRepositoryImpl(remoteDataSource, transactionRunner, songDao, searchCacheDao, testDispatcher)
    }

    @Test
    fun `getAlbumSongs emits Stale then Success when cache and network succeed`() = runTest {
        coEvery { songDao.getAlbumSongs(100L) } returns listOf(fakeEntity)
        coEvery { remoteDataSource.getAlbumSongs(100L) } returns ITunesResponse(
            resultCount = 1,
            results = listOf(fakeDto)
        )

        val results = repository.getAlbumSongs(100L).toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is Outcome.Stale)
        assertTrue(results[1] is Outcome.Success)
        assertEquals("Get Lucky", (results[0] as Outcome.Stale).data.first().trackName)
    }

    @Test
    fun `getAlbumSongs emits Error when no cache and network fails`() = runTest {
        coEvery { songDao.getAlbumSongs(100L) } returns emptyList()
        coEvery { remoteDataSource.getAlbumSongs(100L) } throws Exception("Network error")

        val results = repository.getAlbumSongs(100L).toList()

        assertEquals(1, results.size)
        assertTrue(results.single() is Outcome.Error)
        assertEquals("Network error", (results.single() as Outcome.Error).message)
    }

    @Test
    fun `getAlbumSongs emits Stale then Error when cache exists and network fails`() = runTest {
        coEvery { songDao.getAlbumSongs(100L) } returns listOf(fakeEntity)
        coEvery { remoteDataSource.getAlbumSongs(100L) } throws Exception("Network error")

        val results = repository.getAlbumSongs(100L).toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is Outcome.Stale)
        assertTrue(results[1] is Outcome.Error)
    }

    @Test
    fun `getAlbumSongs caches network results`() = runTest {
        coEvery { songDao.getAlbumSongs(100L) } returns emptyList()
        coEvery { remoteDataSource.getAlbumSongs(100L) } returns ITunesResponse(
            resultCount = 1,
            results = listOf(fakeDto)
        )

        repository.getAlbumSongs(100L).toList()

        coVerify { songDao.upsertSongs(any()) }
    }

    @Test
    fun `getRecentlyPlayed returns flow from dao`() = runTest {
        every { songDao.getRecentlyPlayed() } returns flowOf(listOf(fakeEntity))

        val result = repository.getRecentlyPlayed().first()

        assertEquals(1, result.size)
        assertEquals("Get Lucky", result.first().trackName)
    }

    @Test
    fun `markAsPlayed inserts song with lastPlayedAt`() = runTest {
        val song = Song(
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

        repository.markAsPlayed(song)

        coVerify(exactly = 1) { songDao.insertSong(any()) }
        coVerify(exactly = 0) { songDao.updateLastPlayed(any(), any()) }
    }

    @Test
    fun `searchSongsPaged returns a paging flow`() = runTest {
        val flow = repository.searchSongsPaged("daft")
        assertTrue(flow != null)
    }
}
