package com.arthurzettler.musiclibrary.data.remote.api

import com.arthurzettler.musiclibrary.data.remote.dto.ITunesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApi {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): ITunesResponse

    @GET("lookup")
    suspend fun getAlbumSongs(
        @Query("id") collectionId: Long,
        @Query("entity") entity: String = "song"
    ): ITunesResponse
}
