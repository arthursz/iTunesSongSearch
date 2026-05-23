package com.arthurzettler.musiclibrary.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertEquals
import com.arthurzettler.musiclibrary.ui.theme.MusicLibraryTheme
import org.junit.Rule
import org.junit.Test

class ConnectivityBannerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun connectivity_banner_is_visible_when_offline() {
        composeTestRule.setContent {
            MusicLibraryTheme {
                Column {
                    ConnectivityBanner(isOffline = true)
                }
            }
        }

        composeTestRule.onNodeWithText("No internet connection").assertIsDisplayed()
    }

    @Test
    fun connectivity_banner_is_hidden_when_online() {
        composeTestRule.setContent {
            MusicLibraryTheme {
                Column {
                    ConnectivityBanner(isOffline = false)
                }
            }
        }

        assertEquals(0, composeTestRule.onAllNodesWithText("No internet connection").fetchSemanticsNodes().size)
    }
}
