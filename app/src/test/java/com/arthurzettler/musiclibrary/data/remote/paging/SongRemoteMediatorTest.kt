package com.arthurzettler.musiclibrary.data.remote.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.RemoteMediator.InitializeAction
import androidx.paging.RemoteMediator.MediatorResult
import com.arthurzettler.musiclibrary.data.local.DatabaseTransactionRunner
import com.arthurzettler.musiclibrary.data.local.dao.SearchCacheDao
import com.arthurzettler.musiclibrary.data.local.dao.SongDao
import com.arthurzettler.musiclibrary.data.local.entity.RemoteKeyEntity
import com.arthurzettler.musiclibrary.data.local.entity.SongEntity
import com.arthurzettler.musiclibrary.data.remote.datasource.RemoteDataSource
import com.arthurzettler.musiclibrary.data.remote.dto.ITunesResponse
import com.arthurzettler.musiclibrary.data.remote.dto.SongDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPagingApi::class)
class SongRemoteMediatorTest {

    private val remoteDataSource: RemoteDataSource = mockk()
    private val transactionRunner: DatabaseTransactionRunner = mockk()
    private val songDao: SongDao = mockk(relaxed = true)
    private val searchCacheDao: SearchCacheDao = mockk(relaxed = true)
    private lateinit var mediator: SongRemoteMediator

    private val query = "daft punk"

    private val trackDto = SongDto(
        trackId = 1L,
        trackName = "Get Lucky",
        artistName = "Daft Punk",
        collectionName = "RAM",
        artworkUrl100 = "https://example.com/100x100.jpg",
        previewUrl = "https://example.com/preview.m4a",
        trackTimeMillis = 248000L,
        collectionId = 100L,
        trackNumber = 8,
        releaseDate = "2013-05-17",
        wrapperType = "track"
    )

    private val pagingState = PagingState<Int, SongEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 20),
        leadingPlaceholderCount = 0
    )

    @Before
    fun setup() {
        mediator = SongRemoteMediator(
            query = query,
            remoteDataSource = remoteDataSource,
            transactionRunner = transactionRunner,
            songDao = songDao,
            searchCacheDao = searchCacheDao
        )
        coEvery { transactionRunner.runInTransaction(any<suspend () -> Boolean>()) } coAnswers {
            firstArg<suspend () -> Boolean>().invoke()
        }
    }

    @Test
    fun `initialize launches refresh when no remote key`() = runTest {
        coEvery { searchCacheDao.getRemoteKey(query) } returns null

        val action = mediator.initialize()

        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, action)
    }

    @Test
    fun `initialize skips refresh when cache is fresh`() = runTest {
        coEvery { searchCacheDao.getRemoteKey(query) } returns RemoteKeyEntity(
            query = query,
            nextOffset = 20,
            cachedAt = System.currentTimeMillis()
        )

        val action = mediator.initialize()

        assertEquals(InitializeAction.SKIP_INITIAL_REFRESH, action)
    }

    @Test
    fun `initialize launches refresh when cache is expired`() = runTest {
        coEvery { searchCacheDao.getRemoteKey(query) } returns RemoteKeyEntity(
            query = query,
            nextOffset = 20,
            cachedAt = System.currentTimeMillis() - SongRemoteMediator.CACHE_TTL_MILLIS - 1
        )

        val action = mediator.initialize()

        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, action)
    }

    @Test
    fun `load REFRESH clears cache and stores results`() = runTest {
        coEvery { remoteDataSource.searchSongs(query, 0, 20) } returns ITunesResponse(
            resultCount = 1,
            results = listOf(trackDto)
        )
        coEvery { searchCacheDao.getTrackIdsForQuery(query) } returns emptyList()

        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is MediatorResult.Success)
        coVerify { searchCacheDao.clearSearchResults(query) }
        coVerify { searchCacheDao.clearRemoteKey(query) }
        coVerify { searchCacheDao.evictExpiredCache(SongRemoteMediator.CACHE_TTL_MILLIS) }
        coVerify { songDao.upsertSongs(any()) }
        coVerify { searchCacheDao.insertSearchResults(any()) }
    }

    @Test
    fun `load APPEND stops when API returns duplicates`() = runTest {
        coEvery { searchCacheDao.getRemoteKey(query) } returns RemoteKeyEntity(
            query = query,
            nextOffset = 20,
            cachedAt = System.currentTimeMillis()
        )
        coEvery { remoteDataSource.searchSongs(query, 20, 20) } returns ITunesResponse(
            resultCount = 1,
            results = listOf(trackDto)
        )
        coEvery { searchCacheDao.getTrackIdsForQuery(query) } returns listOf(1L)

        val result = mediator.load(LoadType.APPEND, pagingState)

        assertTrue(result is MediatorResult.Success)
        val success = result as MediatorResult.Success
        assertTrue(success.endOfPaginationReached)
        coVerify(exactly = 0) { songDao.upsertSongs(any()) }
    }

    @Test
    fun `load PREPEND returns end of pagination`() = runTest {
        val result = mediator.load(LoadType.PREPEND, pagingState)

        assertTrue(result is MediatorResult.Success)
        val success = result as MediatorResult.Success
        assertTrue(success.endOfPaginationReached)
        coVerify(exactly = 0) { remoteDataSource.searchSongs(any(), any(), any()) }
    }

    @Test
    fun `load returns error when network fails`() = runTest {
        coEvery { remoteDataSource.searchSongs(query, 0, 20) } throws Exception("Network error")

        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is MediatorResult.Error)
    }
}
