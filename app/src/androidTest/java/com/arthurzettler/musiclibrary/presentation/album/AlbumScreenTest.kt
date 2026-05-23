package com.arthurzettler.musiclibrary.presentation.album

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.arthurzettler.musiclibrary.domain.model.Song
import com.arthurzettler.musiclibrary.domain.model.Outcome
import com.arthurzettler.musiclibrary.domain.repository.PlaybackRepository
import com.arthurzettler.musiclibrary.domain.repository.SongRepository
import com.arthurzettler.musiclibrary.presentation.navigation.albumRouteSavedStateHandle
import com.arthurzettler.musiclibrary.presentation.navigation.unmockAlbumRouteSavedStateHandle
import com.arthurzettler.musiclibrary.ui.theme.MusicLibraryTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Rule
import org.junit.Test

class AlbumScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val songRepository: SongRepository = mockk()
    private val playbackRepository: PlaybackRepository = mockk(relaxed = true)

    @After
    fun tearDown() {
        unmockAlbumRouteSavedStateHandle()
    }

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

    private fun createViewModel(): AlbumViewModel {
        every { songRepository.getAlbumSongs(200L) } returns flowOf(
            Outcome.Success(listOf(fakeSong))
        )
        return AlbumViewModel(
            songRepository,
            playbackRepository,
            albumRouteSavedStateHandle()
        )
    }

    @Test
    fun album_screen_displays_album_header() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MusicLibraryTheme {
                AlbumScreen(
                    viewModel = viewModel,
                    onNavigateBack = {},
                    onNavigateToPlayer = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Homework")[0].assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Daft Punk")[0].assertIsDisplayed()
    }

    @Test
    fun album_screen_displays_song_list() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MusicLibraryTheme {
                AlbumScreen(
                    viewModel = viewModel,
                    onNavigateBack = {},
                    onNavigateToPlayer = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Around The World").assertIsDisplayed()
    }

    @Test
    fun album_screen_navigates_back_on_click() {
        val viewModel = createViewModel()
        var navigatedBack = false

        composeTestRule.setContent {
            MusicLibraryTheme {
                AlbumScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigatedBack = true },
                    onNavigateToPlayer = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        assert(navigatedBack)
    }
}
