package com.arthurzettler.musiclibrary.domain.playback

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    val isPlaying: StateFlow<Boolean>
    val onSongEnded: SharedFlow<Unit>
    val currentPosition: Long
    val duration: Long

    suspend fun connect()
    suspend fun disconnect()

    fun play(url: String)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(position: Long)
}
