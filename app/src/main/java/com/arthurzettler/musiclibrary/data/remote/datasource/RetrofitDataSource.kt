package com.arthurzettler.musiclibrary.data.remote.datasource

import com.arthurzettler.musiclibrary.data.remote.api.ITunesApi
import com.arthurzettler.musiclibrary.data.remote.dto.ITunesResponse
import javax.inject.Inject

class RetrofitDataSource @Inject constructor(
    private val api: ITunesApi
) : RemoteDataSource {

    override suspend fun searchSongs(query: String, offset: Int, limit: Int): ITunesResponse {
        return api.searchSongs(term = query, offset = offset, limit = limit)
    }

    override suspend fun getAlbumSongs(collectionId: Long): ITunesResponse {
        return api.getAlbumSongs(collectionId = collectionId)
    }
}
