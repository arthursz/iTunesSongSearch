package com.arthurzettler.musiclibrary.presentation.album

import com.arthurzettler.musiclibrary.domain.model.Song

data class AlbumUiState(
    val albumName: String = "",
    val artistName: String = "",
    val artworkUrl: String = "",
    val screenState: AlbumScreenState = AlbumScreenState.Loading
)

sealed interface AlbumScreenState {
    data object Loading : AlbumScreenState
    data class Success(
        val songs: List<Song>,
        val isStale: Boolean = false,
        val syncError: String? = null
    ) : AlbumScreenState
    data class Error(val message: String?) : AlbumScreenState
}
