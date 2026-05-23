package com.arthurzettler.musiclibrary.presentation.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arthurzettler.musiclibrary.domain.model.Song
import com.arthurzettler.musiclibrary.domain.repository.PlaybackRepository
import com.arthurzettler.musiclibrary.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongsUiState())
    val uiState: StateFlow<SongsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagingFlow: Flow<PagingData<Song>> = _searchQuery
        .debounce(DEBOUNCE_MILLIS)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(PagingData.empty())
            else songRepository.searchSongsPaged(query)
        }
        .cachedIn(viewModelScope)

    init {
        loadRecentlyPlayed()
    }

    private fun loadRecentlyPlayed() {
        viewModelScope.launch {
            songRepository.getRecentlyPlayed().collect { songs ->
                _uiState.update { it.copy(recentlyPlayed = songs) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQuery.value = query
        if (query.isBlank()) {
            _uiState.update { it.copy(screenState = SongsScreenState.Idle) }
        } else {
            _uiState.update { it.copy(screenState = SongsScreenState.Searching) }
        }
    }

    fun onSongSelected(songs: List<Song>, index: Int) {
        viewModelScope.launch {
            playbackRepository.setPlaylist(songs, index)
        }
    }

    companion object {
        private const val DEBOUNCE_MILLIS = 500L
    }
}
