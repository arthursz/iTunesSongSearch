package com.arthurzettler.musiclibrary.domain.model

data class NowPlaying(
    val playlist: List<Song>,
    val currentIndex: Int
)
