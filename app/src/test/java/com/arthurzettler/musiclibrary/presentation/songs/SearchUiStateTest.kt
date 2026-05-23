package com.arthurzettler.musiclibrary.presentation.songs

import androidx.paging.LoadState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SearchUiStateTest {

    @Test
    fun `from derives initial load when refreshing with no items`() {
        val state = SearchUiState.from(
            refreshLoadState = LoadState.Loading,
            appendLoadState = LoadState.NotLoading(endOfPaginationReached = false),
            itemCount = 0,
            searchQuery = "daft punk",
            isNearListEnd = false
        )

        assertTrue(state.isInitialLoad)
        assertFalse(state.showEmptyResults)
    }

    @Test
    fun `from derives empty results when query has no matches`() {
        val state = SearchUiState.from(
            refreshLoadState = LoadState.NotLoading(endOfPaginationReached = true),
            appendLoadState = LoadState.NotLoading(endOfPaginationReached = true),
            itemCount = 0,
            searchQuery = "zzzznotfound",
            isNearListEnd = false
        )

        assertTrue(state.showEmptyResults)
        assertFalse(state.showBlockingError)
    }

    @Test
    fun `from derives blocking error when refresh fails with no items`() {
        val state = SearchUiState.from(
            refreshLoadState = LoadState.Error(IOException("network")),
            appendLoadState = LoadState.NotLoading(endOfPaginationReached = false),
            itemCount = 0,
            searchQuery = "daft punk",
            isNearListEnd = false
        )

        assertTrue(state.showBlockingError)
        assertFalse(state.showInlineError)
    }

    @Test
    fun `from derives inline append loading near list end`() {
        val state = SearchUiState.from(
            refreshLoadState = LoadState.NotLoading(endOfPaginationReached = false),
            appendLoadState = LoadState.Loading,
            itemCount = 20,
            searchQuery = "daft punk",
            isNearListEnd = true
        )

        assertTrue(state.showAppendLoading)
        assertFalse(state.showBlockingError)
    }
}
