package com.arthurzettler.musiclibrary.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ITunesResponse(
    @SerialName("resultCount") val resultCount: Int = 0,
    @SerialName("results") val results: List<SongDto> = emptyList()
)

@Serializable
data class SongDto(
    @SerialName("trackId") val trackId: Long? = null,
    @SerialName("trackName") val trackName: String? = null,
    @SerialName("artistName") val artistName: String? = null,
    @SerialName("collectionName") val collectionName: String? = null,
    @SerialName("artworkUrl100") val artworkUrl100: String? = null,
    @SerialName("previewUrl") val previewUrl: String? = null,
    @SerialName("trackTimeMillis") val trackTimeMillis: Long? = null,
    @SerialName("collectionId") val collectionId: Long? = null,
    @SerialName("trackNumber") val trackNumber: Int? = null,
    @SerialName("releaseDate") val releaseDate: String? = null,
    @SerialName("wrapperType") val wrapperType: String? = null
)
