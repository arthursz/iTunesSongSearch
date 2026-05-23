package com.arthurzettler.musiclibrary.domain.model

data class Song(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val collectionName: String,
    val artworkUrl: String,
    val previewUrl: String,
    val trackTimeMillis: Long,
    val collectionId: Long,
    val trackNumber: Int
)
