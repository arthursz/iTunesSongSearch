package com.arthurzettler.musiclibrary.presentation.songs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import com.arthurzettler.musiclibrary.domain.model.Song
import com.arthurzettler.musiclibrary.domain.repository.PlaybackRepository
import com.arthurzettler.musiclibrary.domain.repository.SongRepository
import com.arthurzettler.musiclibrary.ui.theme.MusicLibraryTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class SongsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val songRepository: SongRepository = mockk()
    private val playbackRepository: PlaybackRepository = mockk(relaxed = true)

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

    private fun createViewModel(): SongsViewModel {
        return SongsViewModel(songRepository, playbackRepository)
    }

    @Test
    fun songs_screen_displays_search_field() {
        every { songRepository.getRecentlyPlayed() } returns flowOf(emptyList())
        every { songRepository.searchSongsPaged(any()) } returns flowOf(PagingData.empty())

        val viewModel = createViewModel()

        composeTestRule.setContent {
            MusicLibraryTheme {
                SongsScreen(
                    viewModel = viewModel,
                    onNavigateToPlayer = {},
                    onNavigateToAlbum = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Search songs").assertIsDisplayed()
    }

    @Test
    fun songs_screen_displays_title() {
        every { songRepository.getRecentlyPlayed() } returns flowOf(emptyList())
        every { songRepository.searchSongsPaged(any()) } returns flowOf(PagingData.empty())

        val viewModel = createViewModel()

        composeTestRule.setContent {
            MusicLibraryTheme {
                SongsScreen(
                    viewModel = viewModel,
                    onNavigateToPlayer = {},
                    onNavigateToAlbum = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Songs").assertIsDisplayed()
    }

    @Test
    fun songs_screen_displays_recently_played() {
        every { songRepository.getRecentlyPlayed() } returns flowOf(listOf(fakeSong))
        every { songRepository.searchSongsPaged(any()) } returns flowOf(PagingData.empty())

        val viewModel = createViewModel()

        composeTestRule.setContent {
            MusicLibraryTheme {
                SongsScreen(
                    viewModel = viewModel,
                    onNavigateToPlayer = {},
                    onNavigateToAlbum = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Get Lucky").assertIsDisplayed()
        composeTestRule.onNodeWithText("Recently Played").assertDoesNotExist()
    }

    @Test
    fun songs_screen_displays_welcome_message_when_empty() {
        every { songRepository.getRecentlyPlayed() } returns flowOf(emptyList())
        every { songRepository.searchSongsPaged(any()) } returns flowOf(PagingData.empty())

        val viewModel = createViewModel()

        composeTestRule.setContent {
            MusicLibraryTheme {
                SongsScreen(
                    viewModel = viewModel,
                    onNavigateToPlayer = {},
                    onNavigateToAlbum = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Welcome! Search for your favorite songs above").assertIsDisplayed()
    }
}
