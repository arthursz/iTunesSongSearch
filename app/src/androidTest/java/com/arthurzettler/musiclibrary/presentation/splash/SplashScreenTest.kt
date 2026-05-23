package com.arthurzettler.musiclibrary.presentation.splash

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.arthurzettler.musiclibrary.ui.theme.MusicLibraryTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SplashScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun splash_screen_displays_icon() {
        composeTestRule.setContent {
            MusicLibraryTheme {
                SplashScreen(onFinished = {})
            }
        }

        composeTestRule.mainClock.advanceTimeBy(IconEnterDurationMs.toLong())
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription("Music Library logo")
            .assertIsDisplayed()
    }

    @Test
    fun splash_screen_calls_on_finished_after_duration() {
        var finished = false

        composeTestRule.setContent {
            MusicLibraryTheme {
                SplashScreen(onFinished = { finished = true })
            }
        }

        assertFalse(finished)

        composeTestRule.mainClock.advanceTimeBy(SplashDurationMs)
        composeTestRule.waitForIdle()

        assertTrue(finished)
    }
}
