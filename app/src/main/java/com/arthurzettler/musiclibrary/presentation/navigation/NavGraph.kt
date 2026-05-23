package com.arthurzettler.musiclibrary.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.arthurzettler.musiclibrary.presentation.album.AlbumScreen
import com.arthurzettler.musiclibrary.presentation.album.AlbumViewModel
import com.arthurzettler.musiclibrary.presentation.player.PlayerScreen
import com.arthurzettler.musiclibrary.presentation.player.PlayerViewModel
import com.arthurzettler.musiclibrary.presentation.songs.SongsScreen
import com.arthurzettler.musiclibrary.presentation.songs.SongsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = SongsRoute,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally { fullWidth -> fullWidth } + fadeIn()
        },
        exitTransition = {
            slideOutHorizontally { fullWidth -> -fullWidth / 3 } + fadeOut()
        },
        popEnterTransition = {
            slideInHorizontally { fullWidth -> -fullWidth / 3 } + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally { fullWidth -> fullWidth } + fadeOut()
        }
    ) {
        composable<SongsRoute> {
            val viewModel: SongsViewModel = hiltViewModel()
            SongsScreen(
                viewModel = viewModel,
                onNavigateToPlayer = {
                    navController.navigate(PlayerRoute(origin = "songs"))
                },
                onNavigateToAlbum = { song ->
                    navController.navigate(
                        AlbumRoute(
                            collectionId = song.collectionId,
                            albumName = song.collectionName,
                            artistName = song.artistName,
                            artworkUrl = song.artworkUrl
                        )
                    )
                }
            )
        }
        composable<PlayerRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<PlayerRoute>()
            val viewModel: PlayerViewModel = hiltViewModel()
            PlayerScreen(
                viewModel = viewModel,
                showAlbumOption = route.origin != "album",
                onNavigateBack = { navController.popBackStack() },
                onViewAlbum = { collectionId, albumName, artistName, artworkUrl ->
                    navController.navigate(
                        AlbumRoute(
                            collectionId = collectionId,
                            albumName = albumName,
                            artistName = artistName,
                            artworkUrl = artworkUrl
                        )
                    )
                }
            )
        }
        composable<AlbumRoute> { backStackEntry ->
            val viewModel: AlbumViewModel = hiltViewModel(backStackEntry)
            AlbumScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = {
                    navController.navigate(PlayerRoute(origin = "album"))
                }
            )
        }
    }
}
