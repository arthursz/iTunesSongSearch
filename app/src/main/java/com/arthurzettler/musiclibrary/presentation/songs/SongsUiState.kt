package com.arthurzettler.musiclibrary.presentation.songs

import com.arthurzettler.musiclibrary.domain.model.Song

data class SongsUiState(
    val searchQuery: String = "",
    val recentlyPlayed: List<Song> = emptyList(),
    val screenState: SongsScreenState = SongsScreenState.Idle
)

sealed interface SongsScreenState {
    data object Idle : SongsScreenState
    data object Searching : SongsScreenState
}
