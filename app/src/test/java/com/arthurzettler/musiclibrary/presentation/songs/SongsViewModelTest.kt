package com.arthurzettler.musiclibrary.presentation.songs

import androidx.paging.PagingData
import com.arthurzettler.musiclibrary.domain.model.Song
import com.arthurzettler.musiclibrary.domain.repository.PlaybackRepository
import com.arthurzettler.musiclibrary.domain.repository.SongRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val songRepository: SongRepository = mockk()
    private val playbackRepository: PlaybackRepository = mockk(relaxed = true)
    private lateinit var viewModel: SongsViewModel

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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { songRepository.getRecentlyPlayed() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SongsViewModel {
        return SongsViewModel(songRepository, playbackRepository)
    }

    @Test
    fun `initial state is Idle`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.screenState is SongsScreenState.Idle)
    }

    @Test
    fun `search query change transitions to Searching state`() = runTest {
        every { songRepository.searchSongsPaged("daft") } returns flowOf(PagingData.from(listOf(fakeSong)))

        viewModel = createViewModel()
        viewModel.onSearchQueryChanged("daft")
        advanceUntilIdle()

        val state = viewModel.uiState.value.screenState
        assertTrue(state is SongsScreenState.Searching)
    }

    @Test
    fun `clearing search query returns to Idle`() = runTest {
        every { songRepository.searchSongsPaged("daft") } returns flowOf(PagingData.from(listOf(fakeSong)))

        viewModel = createViewModel()
        viewModel.onSearchQueryChanged("daft")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.screenState is SongsScreenState.Searching)

        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.screenState is SongsScreenState.Idle)
    }

    @Test
    fun `recently played songs are loaded on init`() = runTest {
        val recentSongs = listOf(fakeSong)
        every { songRepository.getRecentlyPlayed() } returns flowOf(recentSongs)

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.recentlyPlayed.size)
    }

    @Test
    fun `onSongSelected calls playback repository`() = runTest {
        val songs = listOf(fakeSong, fakeSong.copy(trackId = 2L))

        viewModel = createViewModel()
        viewModel.onSongSelected(songs, 1)
        advanceUntilIdle()

        coVerify { playbackRepository.setPlaylist(songs, 1) }
    }

    @Test
    fun `search query is stored in uiState`() = runTest {
        every { songRepository.searchSongsPaged("test") } returns flowOf(PagingData.empty())

        viewModel = createViewModel()
        viewModel.onSearchQueryChanged("test")
        advanceUntilIdle()

        assertEquals("test", viewModel.uiState.value.searchQuery)
    }
}
