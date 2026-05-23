package com.arthurzettler.musiclibrary.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arthurzettler.musiclibrary.domain.playback.AudioPlayer
import com.arthurzettler.musiclibrary.domain.repository.PlaybackRepository
import com.arthurzettler.musiclibrary.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ProgressPollIntervalMs = 200L

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playbackRepository: PlaybackRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val playbackProgress: Flow<PlaybackProgress> = audioPlayer.isPlaying.flatMapLatest { playing ->
        flow {
            if (!playing) {
                emit(currentPlaybackProgress())
                return@flow
            }
            while (true) {
                emit(currentPlaybackProgress())
                delay(ProgressPollIntervalMs)
            }
        }
    }

    init {
        viewModelScope.launch {
            audioPlayer.connect()
            loadPlaylist()
            observePlayerState()
            observeSongEnded()
        }
    }

    private fun currentPlaybackProgress() = PlaybackProgress(
        currentPosition = audioPlayer.currentPosition,
        duration = audioPlayer.duration
    )

    private fun loadPlaylist() {
        viewModelScope.launch {
            val playlist = playbackRepository.getPlaylist()
            val currentIndex = playbackRepository.getCurrentIndex()

            if (playlist.isNotEmpty() && currentIndex != null) {
                val song = playlist.getOrNull(currentIndex)
                if (song != null) {
                    _uiState.update {
                        it.copy(
                            screenState = PlayerScreenState.Ready(
                                currentSong = song,
                                playlist = playlist,
                                currentIndex = currentIndex
                            )
                        )
                    }
                    prepareCurrent()
                }
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            audioPlayer.isPlaying.collect { playing ->
                _uiState.update { state ->
                    if (state.isPlaying && !playing) {
                        state.copy(
                            isPlaying = playing,
                            currentPosition = audioPlayer.currentPosition
                        )
                    } else {
                        state.copy(isPlaying = playing)
                    }
                }
            }
        }
    }

    private fun observeSongEnded() {
        viewModelScope.launch {
            audioPlayer.onSongEnded.collect {
                if (_uiState.value.isRepeatEnabled) {
                    restartCurrentSong()
                } else {
                    skipNext()
                }
            }
        }
    }

    fun playPause() {
        if (_uiState.value.isPlaying) {
            audioPlayer.pause()
        } else {
            audioPlayer.resume()
        }
    }

    fun pauseForNavigation() {
        if (audioPlayer.isPlaying.value) {
            val position = audioPlayer.currentPosition
            audioPlayer.pause()
            _uiState.update {
                it.copy(
                    pausedByNavigation = true,
                    pausedPosition = position,
                    currentPosition = position
                )
            }
        }
    }

    fun resumeFromNavigation() {
        val state = _uiState.value
        if (!state.pausedByNavigation) return

        val screenState = state.screenState
        if (screenState is PlayerScreenState.Ready && screenState.currentSong.previewUrl.isNotBlank()) {
            audioPlayer.play(screenState.currentSong.previewUrl)
            audioPlayer.seekTo(state.pausedPosition)
        }
        _uiState.update { it.copy(pausedByNavigation = false) }
    }

    fun stopPlayback() {
        audioPlayer.stop()
    }

    fun seekTo(position: Long) {
        val duration = audioPlayer.duration.takeIf { it > 0L } ?: _uiState.value.duration
        val clampedPosition = if (duration > 0L) {
            position.coerceIn(0L, duration)
        } else {
            position.coerceAtLeast(0L)
        }
        audioPlayer.seekTo(clampedPosition)
        _uiState.update { it.copy(currentPosition = clampedPosition) }
    }

    fun toggleRepeat() {
        _uiState.update { it.copy(isRepeatEnabled = !it.isRepeatEnabled) }
    }

    fun skipNext() {
        val state = _uiState.value.screenState
        if (state !is PlayerScreenState.Ready) return

        val nextIndex = state.currentIndex + 1
        if (nextIndex < state.playlist.size) {
            val nextSong = state.playlist[nextIndex]
            _uiState.update {
                it.copy(
                    screenState = PlayerScreenState.Ready(
                        currentSong = nextSong,
                        playlist = state.playlist,
                        currentIndex = nextIndex
                    )
                )
            }
            viewModelScope.launch {
                playbackRepository.setCurrentIndex(nextIndex)
            }
            prepareCurrent()
        }
    }

    fun skipPrevious() {
        val state = _uiState.value.screenState
        if (state !is PlayerScreenState.Ready) return

        val prevIndex = state.currentIndex - 1
        if (prevIndex >= 0) {
            val prevSong = state.playlist[prevIndex]
            _uiState.update {
                it.copy(
                    screenState = PlayerScreenState.Ready(
                        currentSong = prevSong,
                        playlist = state.playlist,
                        currentIndex = prevIndex
                    )
                )
            }
            viewModelScope.launch {
                playbackRepository.setCurrentIndex(prevIndex)
            }
            prepareCurrent()
        }
    }

    private fun prepareCurrent() {
        val state = _uiState.value.screenState
        if (state !is PlayerScreenState.Ready) return

        val song = state.currentSong
        viewModelScope.launch {
            songRepository.markAsPlayed(song)
        }
        if (song.previewUrl.isNotBlank()) {
            audioPlayer.play(song.previewUrl)
        }
        _uiState.update { it.copy(duration = 0L, currentPosition = 0L) }
    }

    private fun restartCurrentSong() {
        val state = _uiState.value.screenState
        if (state !is PlayerScreenState.Ready) return

        val song = state.currentSong
        if (song.previewUrl.isNotBlank()) {
            audioPlayer.seekTo(0L)
            audioPlayer.resume()
        }
        _uiState.update { it.copy(currentPosition = 0L) }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
