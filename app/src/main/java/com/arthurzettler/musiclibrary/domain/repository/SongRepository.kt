package com.arthurzettler.musiclibrary.domain.repository

import androidx.paging.PagingData
import com.arthurzettler.musiclibrary.domain.model.Outcome
import com.arthurzettler.musiclibrary.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun searchSongsPaged(query: String): Flow<PagingData<Song>>
    fun getRecentlyPlayed(): Flow<List<Song>>
    fun getAlbumSongs(collectionId: Long): Flow<Outcome<List<Song>>>
    suspend fun markAsPlayed(song: Song)
}
