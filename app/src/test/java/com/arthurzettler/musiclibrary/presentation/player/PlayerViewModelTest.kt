package com.arthurzettler.musiclibrary.presentation.player

import com.arthurzettler.musiclibrary.domain.model.Song
import com.arthurzettler.musiclibrary.domain.playback.AudioPlayer
import com.arthurzettler.musiclibrary.domain.repository.PlaybackRepository
import com.arthurzettler.musiclibrary.domain.repository.SongRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val songRepository: SongRepository = mockk(relaxed = true)
    private val playbackRepository: PlaybackRepository = mockk(relaxed = true)
    private val audioPlayer: AudioPlayer = mockk(relaxed = true)
    private val songEndedFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private lateinit var viewModel: PlayerViewModel

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
        every { audioPlayer.isPlaying } returns MutableStateFlow(false)
        every { audioPlayer.onSongEnded } returns songEndedFlow
        every { audioPlayer.currentPosition } returns 0L
        every { audioPlayer.duration } returns 0L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun runPendingCoroutines() {
        testDispatcher.scheduler.runCurrent()
    }

    private fun createViewModel(
        songs: List<Song> = emptyList(),
        startIndex: Int = 0
    ): PlayerViewModel {
        coEvery { playbackRepository.getPlaylist() } returns songs
        coEvery { playbackRepository.getCurrentIndex() } returns
            if (songs.isEmpty()) null else startIndex
        return PlayerViewModel(
            songRepository,
            playbackRepository,
            audioPlayer
        )
    }

    private fun runPlayerViewModelTest(
        songs: List<Song> = emptyList(),
        startIndex: Int = 0,
        setup: () -> Unit = {},
        block: suspend () -> Unit
    ) = runTest {
        setup()
        viewModel = createViewModel(songs, startIndex)
        try {
            runPendingCoroutines()
            block()
        } finally {
            destroyViewModel(viewModel)
        }
    }

    @Test
    fun `empty playlist results in Idle`() = runPlayerViewModelTest {
        assertTrue(viewModel.uiState.value.screenState is PlayerScreenState.Idle)
    }

    @Test
    fun `init with songs transitions to Ready and starts playing`() = runPlayerViewModelTest(
        songs = listOf(fakeSong, fakeSong.copy(trackId = 2L, trackName = "Aerodynamic"))
    ) {
        val state = viewModel.uiState.value.screenState
        assertTrue(state is PlayerScreenState.Ready)
        val ready = state as PlayerScreenState.Ready
        assertEquals(fakeSong, ready.currentSong)
        assertEquals(0, ready.currentIndex)
        assertEquals(2, ready.playlist.size)
        verify { audioPlayer.play(fakeSong.previewUrl) }
        verify(exactly = 0) { audioPlayer.pause() }
    }

    @Test
    fun `skipNext advances to next song`() = runPlayerViewModelTest(
        songs = listOf(fakeSong, fakeSong.copy(trackId = 2L, trackName = "Aerodynamic"))
    ) {
        viewModel.skipNext()
        runPendingCoroutines()

        val state = viewModel.uiState.value.screenState as PlayerScreenState.Ready
        assertEquals(1, state.currentIndex)
        assertEquals("Aerodynamic", state.currentSong.trackName)
        coVerify { playbackRepository.setCurrentIndex(1) }
    }

    @Test
    fun `skipPrevious goes to previous song`() = runPlayerViewModelTest(
        songs = listOf(fakeSong, fakeSong.copy(trackId = 2L, trackName = "Aerodynamic")),
        startIndex = 1
    ) {
        viewModel.skipPrevious()
        runPendingCoroutines()

        val state = viewModel.uiState.value.screenState as PlayerScreenState.Ready
        assertEquals(0, state.currentIndex)
        assertEquals("Get Lucky", state.currentSong.trackName)
        coVerify { playbackRepository.setCurrentIndex(0) }
    }

    @Test
    fun `skipNext does nothing at end of playlist`() = runPlayerViewModelTest(
        songs = listOf(fakeSong)
    ) {
        viewModel.skipNext()
        runPendingCoroutines()

        val state = viewModel.uiState.value.screenState as PlayerScreenState.Ready
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun `skipPrevious does nothing at start of playlist`() = runPlayerViewModelTest(
        songs = listOf(fakeSong)
    ) {
        viewModel.skipPrevious()
        runPendingCoroutines()

        val state = viewModel.uiState.value.screenState as PlayerScreenState.Ready
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun `seekTo updates position`() = runPlayerViewModelTest(songs = listOf(fakeSong)) {
        viewModel.seekTo(5000L)

        verify { audioPlayer.seekTo(5000L) }
        assertEquals(5000L, viewModel.uiState.value.currentPosition)
    }

    @Test
    fun `playPause pauses when playing`() = runPlayerViewModelTest(
        songs = listOf(fakeSong),
        setup = { every { audioPlayer.isPlaying } returns MutableStateFlow(true) }
    ) {
        viewModel.playPause()

        verify { audioPlayer.pause() }
    }

    @Test
    fun `pause syncs current position into ui state`() = runPlayerViewModelTest(
        songs = listOf(fakeSong),
        setup = {
            every { audioPlayer.isPlaying } returns MutableStateFlow(true)
            every { audioPlayer.currentPosition } returns 15_000L
        }
    ) {
        (audioPlayer.isPlaying as MutableStateFlow).value = false
        runPendingCoroutines()

        assertEquals(15_000L, viewModel.uiState.value.currentPosition)
        assertFalse(viewModel.uiState.value.isPlaying)
    }

    @Test
    fun `playPause resumes when paused`() = runPlayerViewModelTest(songs = listOf(fakeSong)) {
        viewModel.playPause()

        verify { audioPlayer.resume() }
    }

    @Test
    fun `markAsPlayed is called on init`() = runPlayerViewModelTest(songs = listOf(fakeSong)) {
        coVerify { songRepository.markAsPlayed(fakeSong) }
    }

    @Test
    fun `toggleRepeat enables repeat`() = runPlayerViewModelTest(songs = listOf(fakeSong)) {
        assertFalse(viewModel.uiState.value.isRepeatEnabled)
        viewModel.toggleRepeat()
        assertTrue(viewModel.uiState.value.isRepeatEnabled)
    }

    @Test
    fun `toggleRepeat disables repeat when already enabled`() = runPlayerViewModelTest(
        songs = listOf(fakeSong)
    ) {
        viewModel.toggleRepeat()
        assertTrue(viewModel.uiState.value.isRepeatEnabled)

        viewModel.toggleRepeat()
        assertFalse(viewModel.uiState.value.isRepeatEnabled)
    }

    @Test
    fun `song restarts when repeat is enabled and song ends`() = runPlayerViewModelTest(
        songs = listOf(fakeSong)
    ) {
        viewModel.toggleRepeat()
        songEndedFlow.emit(Unit)
        runPendingCoroutines()

        verify { audioPlayer.seekTo(0L) }
        verify { audioPlayer.resume() }
    }

    @Test
    fun `song advances to next when repeat is disabled and song ends`() = runPlayerViewModelTest(
        songs = listOf(fakeSong, fakeSong.copy(trackId = 2L, trackName = "Aerodynamic"))
    ) {
        songEndedFlow.emit(Unit)
        runPendingCoroutines()

        val state = viewModel.uiState.value.screenState as PlayerScreenState.Ready
        assertEquals(1, state.currentIndex)
        assertEquals("Aerodynamic", state.currentSong.trackName)
        coVerify { playbackRepository.setCurrentIndex(1) }
        verify(atLeast = 1) { audioPlayer.play("https://example.com/preview.m4a") }
    }

    @Test
    fun `song does not advance when repeat is disabled and song ends on last track`() = runPlayerViewModelTest(
        songs = listOf(fakeSong)
    ) {
        songEndedFlow.emit(Unit)
        runPendingCoroutines()

        val state = viewModel.uiState.value.screenState as PlayerScreenState.Ready
        assertEquals(0, state.currentIndex)
        verify(exactly = 0) { audioPlayer.seekTo(0L) }
        coVerify(exactly = 0) { playbackRepository.setCurrentIndex(any()) }
    }

    @Test
    fun `song does not advance when repeat is enabled and song ends`() = runPlayerViewModelTest(
        songs = listOf(fakeSong, fakeSong.copy(trackId = 2L, trackName = "Aerodynamic"))
    ) {
        viewModel.toggleRepeat()
        songEndedFlow.emit(Unit)
        runPendingCoroutines()

        val state = viewModel.uiState.value.screenState as PlayerScreenState.Ready
        assertEquals(0, state.currentIndex)
        verify { audioPlayer.seekTo(0L) }
        coVerify(exactly = 0) { playbackRepository.setCurrentIndex(1) }
    }

    @Test
    fun `pauseForNavigation pauses when playing`() = runPlayerViewModelTest(
        songs = listOf(fakeSong),
        setup = { every { audioPlayer.isPlaying } returns MutableStateFlow(true) }
    ) {
        viewModel.pauseForNavigation()

        verify { audioPlayer.pause() }
        assertTrue(viewModel.uiState.value.pausedByNavigation)
        assertEquals(0L, viewModel.uiState.value.pausedPosition)
    }

    @Test
    fun `resumeFromNavigation replays song from paused position`() = runPlayerViewModelTest(
        songs = listOf(fakeSong),
        setup = {
            every { audioPlayer.isPlaying } returns MutableStateFlow(true)
            every { audioPlayer.currentPosition } returns 15000L
        }
    ) {
        viewModel.pauseForNavigation()
        assertEquals(15000L, viewModel.uiState.value.pausedPosition)
        viewModel.resumeFromNavigation()

        verify { audioPlayer.play(fakeSong.previewUrl) }
        verify { audioPlayer.seekTo(15000L) }
        assertFalse(viewModel.uiState.value.pausedByNavigation)
    }

    @Test
    fun `resumeFromNavigation does nothing if not paused by navigation`() = runPlayerViewModelTest(
        songs = listOf(fakeSong)
    ) {
        viewModel.resumeFromNavigation()

        verify(exactly = 0) { audioPlayer.resume() }
    }

    @Test
    fun `stopPlayback stops the audio player`() = runPlayerViewModelTest(songs = listOf(fakeSong)) {
        viewModel.stopPlayback()

        verify { audioPlayer.stop() }
    }

    @Test
    fun `onCleared stops playback without releasing player`() = runPlayerViewModelTest(
        songs = listOf(fakeSong)
    ) {
        destroyViewModel(viewModel)

        verify { audioPlayer.stop() }
        coVerify(exactly = 0) { audioPlayer.disconnect() }
    }

    private fun destroyViewModel(viewModel: PlayerViewModel) {
        val method = ViewModel::class.java.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(viewModel)
        viewModel.viewModelScope.cancel()
        runPendingCoroutines()
    }
}
