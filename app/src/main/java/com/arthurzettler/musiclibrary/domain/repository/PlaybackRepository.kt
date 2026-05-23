package com.arthurzettler.musiclibrary.domain.repository

import com.arthurzettler.musiclibrary.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface PlaybackRepository {
    suspend fun setPlaylist(songs: List<Song>, startIndex: Int)
    suspend fun getPlaylist(): List<Song>
    suspend fun getCurrentIndex(): Int?
    suspend fun setCurrentIndex(index: Int)
}
