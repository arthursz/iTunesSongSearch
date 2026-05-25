package com.arthurzettler.musiclibrary.presentation.album

import com.arthurzettler.musiclibrary.domain.model.Outcome
import com.arthurzettler.musiclibrary.domain.model.Song
import com.arthurzettler.musiclibrary.domain.repository.PlaybackRepository
import com.arthurzettler.musiclibrary.domain.repository.SongRepository
import com.arthurzettler.musiclibrary.presentation.navigation.albumRouteSavedStateHandle
import com.arthurzettler.musiclibrary.presentation.navigation.unmockAlbumRouteSavedStateHandle
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val songRepository: SongRepository = mockk()
    private val playbackRepository: PlaybackRepository = mockk(relaxed = true)
    private lateinit var viewModel: AlbumViewModel

    private val fakeSong = Song(
        trackId = 1L,
        trackName = "Around The World",
        artistName = "Daft Punk",
        collectionName = "Homework",
        artworkUrl = "https://example.com/art.jpg",
        previewUrl = "https://example.com/preview.m4a",
        trackTimeMillis = 420000L,
        collectionId = 200L,
        trackNumber = 1
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockAlbumRouteSavedStateHandle()
    }

    private fun createViewModel(collectionId: Long = 200L): AlbumViewModel {
        return AlbumViewModel(
            songRepository,
            playbackRepository,
            albumRouteSavedStateHandle(collectionId = collectionId)
        )
    }

    @Test
    fun `initial state is Loading`() = runTest {
        every { songRepository.getAlbumSongs(200L) } returns flowOf(Outcome.Success(emptyList()))

        viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.screenState is AlbumScreenState.Loading)
    }

    @Test
    fun `loads album songs successfully transitions to Success`() = runTest {
        val songs = listOf(fakeSong, fakeSong.copy(trackId = 2L, trackName = "Da Funk"))
        every { songRepository.getAlbumSongs(200L) } returns flowOf(Outcome.Success(songs))

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value.screenState as AlbumScreenState.Success
        assertEquals(2, state.songs.size)
        assertEquals(false, state.isStale)
        assertNull(state.syncError)
    }

    @Test
    fun `handles error transitions to Error state`() = runTest {
        every { songRepository.getAlbumSongs(200L) } returns flowOf(
            Outcome.Error("Network error")
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value.screenState
        assertTrue(state is AlbumScreenState.Error)
        assertEquals("Network error", (state as AlbumScreenState.Error).message)
    }

    @Test
    fun `stale then error keeps songs and exposes sync error`() = runTest {
        val songs = listOf(fakeSong)
        every { songRepository.getAlbumSongs(200L) } returns flowOf(
            Outcome.Stale(songs),
            Outcome.Error("Network error")
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value.screenState as AlbumScreenState.Success
        assertEquals(1, state.songs.size)
        assertTrue(state.isStale)
        assertEquals("Network error", state.syncError)
    }

    @Test
    fun `sets album metadata from route`() = runTest {
        every { songRepository.getAlbumSongs(200L) } returns flowOf(Outcome.Success(emptyList()))

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Homework", state.albumName)
        assertEquals("Daft Punk", state.artistName)
        assertEquals("https://example.com/art.jpg", state.artworkUrl)
    }

    @Test
    fun `refresh fetches songs again`() = runTest {
        every { songRepository.getAlbumSongs(200L) } returns flowOf(
            Outcome.Error("Network error")
        )

        viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.screenState is AlbumScreenState.Error)

        val songs = listOf(fakeSong)
        every { songRepository.getAlbumSongs(200L) } returns flowOf(Outcome.Success(songs))

        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value.screenState as AlbumScreenState.Success
        assertEquals(1, state.songs.size)
    }

    @Test
    fun `refresh keeps isRefreshing true until network completes after stale`() = runTest {
        val songs = listOf(fakeSong)
        every { songRepository.getAlbumSongs(200L) } returns flowOf(Outcome.Success(songs))

        viewModel = createViewModel()
        advanceUntilIdle()

        val refreshFlow = MutableSharedFlow<Outcome<List<Song>>>(extraBufferCapacity = 2)
        every { songRepository.getAlbumSongs(200L) } returns refreshFlow

        viewModel.refresh()
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.uiState.value.isRefreshing)

        refreshFlow.emit(Outcome.Stale(songs))
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isRefreshing)
        val staleState = viewModel.uiState.value.screenState as AlbumScreenState.Success
        assertTrue(staleState.isStale)

        refreshFlow.emit(Outcome.Success(songs))
        advanceUntilIdle()

        val finalState = viewModel.uiState.value.screenState as AlbumScreenState.Success
        assertEquals(false, finalState.isStale)
        assertTrue(!viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `refresh from error uses pull indicator instead of full screen loading`() = runTest {
        every { songRepository.getAlbumSongs(200L) } returns flowOf(
            Outcome.Error("Network error")
        )

        viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.screenState is AlbumScreenState.Error)

        val refreshFlow = MutableSharedFlow<Outcome<List<Song>>>(extraBufferCapacity = 1)
        every { songRepository.getAlbumSongs(200L) } returns refreshFlow

        viewModel.refresh()
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isRefreshing)
        assertTrue(viewModel.uiState.value.screenState is AlbumScreenState.Error)
    }

    @Test
    fun `refresh clears isRefreshing after stale then error`() = runTest {
        val songs = listOf(fakeSong)
        every { songRepository.getAlbumSongs(200L) } returns flowOf(Outcome.Success(songs))

        viewModel = createViewModel()
        advanceUntilIdle()

        every { songRepository.getAlbumSongs(200L) } returns flowOf(
            Outcome.Stale(songs),
            Outcome.Error("Network error")
        )

        viewModel.refresh()
        advanceUntilIdle()

        assertTrue(!viewModel.uiState.value.isRefreshing)
        val state = viewModel.uiState.value.screenState as AlbumScreenState.Success
        assertEquals("Network error", state.syncError)
    }

    @Test
    fun `onSongSelected calls playback repository`() = runTest {
        val songs = listOf(fakeSong, fakeSong.copy(trackId = 2L))
        every { songRepository.getAlbumSongs(200L) } returns flowOf(Outcome.Success(songs))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSongSelected(songs, 1)
        advanceUntilIdle()

        coVerify { playbackRepository.setPlaylist(songs, 1) }
    }
}
