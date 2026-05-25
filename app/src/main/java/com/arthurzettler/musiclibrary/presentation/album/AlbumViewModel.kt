package com.arthurzettler.musiclibrary.presentation.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.arthurzettler.musiclibrary.domain.model.Outcome
import com.arthurzettler.musiclibrary.domain.model.Song
import com.arthurzettler.musiclibrary.domain.repository.PlaybackRepository
import com.arthurzettler.musiclibrary.domain.repository.SongRepository
import com.arthurzettler.musiclibrary.presentation.navigation.AlbumRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playbackRepository: PlaybackRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    private val route = savedStateHandle.toRoute<AlbumRoute>()
    private val collectionId: Long = route.collectionId
    private val albumName: String = route.albumName
    private val artistName: String = route.artistName
    private val artworkUrl: String = route.artworkUrl

    init {
        _uiState.update {
            it.copy(
                albumName = albumName,
                artistName = artistName,
                artworkUrl = artworkUrl
            )
        }
        loadAlbumSongs()
    }

    fun loadAlbumSongs() {
        fetchAlbumSongs(showFullScreenLoading = true)
    }

    fun refresh() {
        when (_uiState.value.screenState) {
            is AlbumScreenState.Success,
            is AlbumScreenState.Error -> fetchAlbumSongs(showFullScreenLoading = false)
            else -> loadAlbumSongs()
        }
    }

    fun onSongSelected(songs: List<Song>, index: Int) {
        viewModelScope.launch {
            playbackRepository.setPlaylist(songs, index)
        }
    }

    private fun fetchAlbumSongs(showFullScreenLoading: Boolean) {
        fetchJob?.cancel()
        if (showFullScreenLoading) {
            _uiState.update { it.copy(screenState = AlbumScreenState.Loading, isRefreshing = false) }
        } else {
            _uiState.update { it.copy(isRefreshing = true) }
        }
        fetchJob = viewModelScope.launch {
            songRepository.getAlbumSongs(collectionId).collect { outcome ->
                when (outcome) {
                    is Outcome.Success -> {
                        _uiState.update {
                            it.copy(
                                screenState = AlbumScreenState.Success(
                                    songs = outcome.data,
                                    isStale = false,
                                    syncError = null
                                ),
                                isRefreshing = false
                            )
                        }
                    }

                    is Outcome.Stale -> {
                        _uiState.update {
                            it.copy(
                                screenState = AlbumScreenState.Success(
                                    songs = outcome.data,
                                    isStale = true,
                                    syncError = null
                                )
                            )
                        }
                    }

                    is Outcome.Error -> {
                        val current = _uiState.value.screenState
                        if (current is AlbumScreenState.Success) {
                            _uiState.update {
                                it.copy(
                                    screenState = current.copy(
                                        syncError = outcome.message
                                    ),
                                    isRefreshing = false
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    screenState = AlbumScreenState.Error(outcome.message),
                                    isRefreshing = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
