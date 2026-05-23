package com.arthurzettler.musiclibrary.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SongsRoute

@Serializable
data class PlayerRoute(val origin: String = "songs")

@Serializable
data class AlbumRoute(
    val collectionId: Long,
    val albumName: String,
    val artistName: String,
    val artworkUrl: String
)
