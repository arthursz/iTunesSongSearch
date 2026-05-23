package com.arthurzettler.musiclibrary.presentation.player

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arthurzettler.musiclibrary.domain.model.Song
import com.arthurzettler.musiclibrary.domain.playback.AudioPlayer
import com.arthurzettler.musiclibrary.domain.repository.PlaybackRepository
import com.arthurzettler.musiclibrary.domain.repository.SongRepository
import com.arthurzettler.musiclibrary.ui.theme.MusicLibraryTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class PlayerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val songRepository: SongRepository = mockk(relaxed = true)
    private val playbackRepository: PlaybackRepository = mockk(relaxed = true)
    private val audioPlayer: AudioPlayer = mockk(relaxed = true)

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

    private fun createViewModel(
        songs: List<Song> = listOf(fakeSong),
        startIndex: Int = 0,
        isPlaying: Boolean = false
    ): PlayerViewModel {
        coEvery { playbackRepository.getPlaylist() } returns songs
        coEvery { playbackRepository.getCurrentIndex() } returns startIndex
        coEvery { audioPlayer.connect() } returns Unit
        every { audioPlayer.isPlaying } returns MutableStateFlow(isPlaying)
        every { audioPlayer.onSongEnded } returns MutableSharedFlow(extraBufferCapacity = 1)
        every { audioPlayer.currentPosition } returns 0L
        every { audioPlayer.duration } returns 248000L
        return PlayerViewModel(
            songRepository,
            playbackRepository,
            audioPlayer
        )
    }

    private fun setPlayerScreen(
        viewModel: PlayerViewModel,
        onNavigateBack: () -> Unit = {},
        onLifecycleOwnerReady: (LifecycleOwner) -> Unit = {}
    ) {
        composeTestRule.setContent {
            onLifecycleOwnerReady(LocalLifecycleOwner.current)
            MusicLibraryTheme {
                PlayerScreen(
                    viewModel = viewModel,
                    onNavigateBack = onNavigateBack,
                    onViewAlbum = { _, _, _, _ -> }
                )
            }
        }
    }

    @Test
    fun player_screen_displays_now_playing() {
        setPlayerScreen(createViewModel())

        composeTestRule.onNodeWithText("Now playing").assertIsDisplayed()
    }

    @Test
    fun player_screen_displays_song_info() {
        setPlayerScreen(createViewModel())

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Get Lucky").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daft Punk").assertIsDisplayed()
    }

    @Test
    fun player_screen_displays_playback_controls() {
        setPlayerScreen(createViewModel())

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Play").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Previous song").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Next song").assertIsDisplayed()
    }

    @Test
    fun player_screen_displays_timeline() {
        every { audioPlayer.currentPosition } returns 60000L
        setPlayerScreen(createViewModel())

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Song progress").assertIsDisplayed()
    }

    @Test
    fun player_screen_navigates_back_on_click() {
        var navigatedBack = false
        setPlayerScreen(
            viewModel = createViewModel(),
            onNavigateBack = { navigatedBack = true }
        )

        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        assert(navigatedBack)
    }

    @Test
    fun player_screen_pauses_for_navigation_on_pause() {
        val viewModel = createViewModel(isPlaying = true)
        lateinit var lifecycleOwner: LifecycleOwner

        setPlayerScreen(
            viewModel = viewModel,
            onLifecycleOwnerReady = { lifecycleOwner = it }
        )

        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread {
            (lifecycleOwner.lifecycle as LifecycleRegistry)
                .handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
        composeTestRule.waitForIdle()

        verify { audioPlayer.pause() }
    }
}
