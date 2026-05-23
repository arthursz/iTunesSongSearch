package com.arthurzettler.musiclibrary.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.arthurzettler.musiclibrary.domain.model.NetworkMonitor
import com.arthurzettler.musiclibrary.presentation.components.ConnectivityBanner
import com.arthurzettler.musiclibrary.presentation.navigation.NavGraph
import com.arthurzettler.musiclibrary.presentation.splash.SplashScreen
import com.arthurzettler.musiclibrary.ui.theme.MusicLibraryTheme

@Composable
fun MusicLibraryRoot(
    networkMonitor: NetworkMonitor,
    modifier: Modifier = Modifier
) {
    var showSplash by rememberSaveable { mutableStateOf(true) }

    MusicLibraryTheme {
        val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()
        val navController = rememberNavController()
        val backgroundColor = MaterialTheme.colorScheme.background

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (!isOnline) {
                    ConnectivityBanner(
                        isOffline = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    )
                }

                NavGraph(
                    navController = navController,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .imePadding()
                        .then(
                            if (isOnline) {
                                Modifier.statusBarsPadding()
                            } else {
                                Modifier
                            }
                        )
                )
            }

            AnimatedVisibility(
                visible = showSplash,
                exit = fadeOut(animationSpec = tween(durationMillis = 280)),
                modifier = Modifier.fillMaxSize()
            ) {
                SplashScreen(
                    onFinished = { showSplash = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
