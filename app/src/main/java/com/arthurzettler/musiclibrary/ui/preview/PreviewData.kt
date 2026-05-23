package com.arthurzettler.musiclibrary.ui.preview

import com.arthurzettler.musiclibrary.domain.model.Song

val previewSong = Song(
    trackId = 1L,
    trackName = "Get Lucky",
    artistName = "Daft Punk",
    collectionName = "Random Access Memories",
    artworkUrl = "https://example.com/art.jpg",
    previewUrl = "https://example.com/preview.m4a",
    trackTimeMillis = 248_000L,
    collectionId = 100L,
    trackNumber = 8
)

val previewSongLongTitle = previewSong.copy(
    trackId = 2L,
    trackName = "Harder, Better, Faster, Stronger (Live Extended Remix Edition)",
    artistName = "Daft Punk featuring Pharrell Williams and Nile Rodgers",
    collectionName = "Alive 2007 (Deluxe Remastered Anniversary Edition)"
)

val previewSongMissingArt = previewSong.copy(
    trackId = 3L,
    trackName = "Aerodynamic",
    trackNumber = 2,
    artworkUrl = ""
)

val previewPlaylist = listOf(
    previewSong,
    previewSong.copy(
        trackId = 4L,
        trackName = "Instant Crush",
        trackNumber = 9
    ),
    previewSong.copy(
        trackId = 5L,
        trackName = "Lose Yourself to Dance",
        trackNumber = 10
    )
)
