package com.arthurzettler.musiclibrary.presentation.player

import com.arthurzettler.musiclibrary.domain.model.Song

data class PlayerUiState(
    val screenState: PlayerScreenState = PlayerScreenState.Idle,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isRepeatEnabled: Boolean = false,
    val pausedByNavigation: Boolean = false,
    val pausedPosition: Long = 0L
)

sealed interface PlayerScreenState {
    data object Idle : PlayerScreenState
    data class Ready(
        val currentSong: Song,
        val playlist: List<Song>,
        val currentIndex: Int
    ) : PlayerScreenState
}
