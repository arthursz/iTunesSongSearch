package com.arthurzettler.musiclibrary.data.remote.datasource

import com.arthurzettler.musiclibrary.data.remote.dto.ITunesResponse

interface RemoteDataSource {
    suspend fun searchSongs(query: String, offset: Int, limit: Int): ITunesResponse
    suspend fun getAlbumSongs(collectionId: Long): ITunesResponse
}
