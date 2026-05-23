package com.arthurzettler.musiclibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arthurzettler.musiclibrary.domain.model.NetworkMonitor
import com.arthurzettler.musiclibrary.domain.playback.AudioPlayer
import com.arthurzettler.musiclibrary.presentation.MusicLibraryRoot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var audioPlayer: AudioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            setTheme(R.style.Theme_MusicLibrary)
        }
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicLibraryRoot(
                networkMonitor = networkMonitor,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            runBlocking { audioPlayer.disconnect() }
        }
        super.onDestroy()
    }
}
