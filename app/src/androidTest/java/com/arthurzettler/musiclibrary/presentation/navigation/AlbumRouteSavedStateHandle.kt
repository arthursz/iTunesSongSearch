package com.arthurzettler.musiclibrary.presentation.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic

fun albumRouteSavedStateHandle(
    collectionId: Long = 200L,
    albumName: String = "Homework",
    artistName: String = "Daft Punk",
    artworkUrl: String = "https://example.com/art.jpg"
): SavedStateHandle {
    val route = AlbumRoute(
        collectionId = collectionId,
        albumName = albumName,
        artistName = artistName,
        artworkUrl = artworkUrl
    )
    mockkStatic("androidx.navigation.SavedStateHandleKt")
    val savedStateHandle = SavedStateHandle()
    every { savedStateHandle.toRoute<AlbumRoute>() } returns route
    return savedStateHandle
}

fun unmockAlbumRouteSavedStateHandle() {
    unmockkStatic("androidx.navigation.SavedStateHandleKt")
}
