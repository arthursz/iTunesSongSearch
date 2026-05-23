package com.arthurzettler.musiclibrary.data.playback

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.arthurzettler.musiclibrary.domain.playback.AudioPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExoPlayerAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioPlayer {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val connectMutex = Mutex()

    private var player: ExoPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _onSongEnded = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val onSongEnded: SharedFlow<Unit> = _onSongEnded.asSharedFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                _isPlaying.value = false
                _onSongEnded.tryEmit(Unit)
            }
        }
    }

    override val currentPosition: Long
        get() = player?.currentPosition ?: 0L

    override val duration: Long
        get() = player?.duration?.coerceAtLeast(0L) ?: 0L

    override suspend fun connect() = connectMutex.withLock {
        if (player != null) return@withLock
        withContext(Dispatchers.Main.immediate) {
            player = ExoPlayer.Builder(context).build().also { exoPlayer ->
                exoPlayer.addListener(playerListener)
                _isPlaying.value = exoPlayer.isPlaying
            }
        }
    }

    override suspend fun disconnect() = connectMutex.withLock {
        withContext(Dispatchers.Main.immediate) {
            player?.let { exoPlayer ->
                exoPlayer.removeListener(playerListener)
                exoPlayer.stop()
                exoPlayer.release()
            }
            player = null
            _isPlaying.value = false
        }
    }

    override fun play(url: String) {
        runOnMainThread {
            player?.run {
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                play()
            }
        }
    }

    override fun pause() {
        runOnMainThread { player?.pause() }
    }

    override fun resume() {
        runOnMainThread { player?.play() }
    }

    override fun stop() {
        runOnMainThread {
            player?.stop()
            _isPlaying.value = false
        }
    }

    override fun seekTo(position: Long) {
        runOnMainThread { player?.seekTo(position) }
    }

    private fun runOnMainThread(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }
}
