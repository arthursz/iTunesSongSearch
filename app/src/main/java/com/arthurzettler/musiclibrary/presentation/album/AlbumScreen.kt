package com.arthurzettler.musiclibrary.presentation.album

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.arthurzettler.musiclibrary.R
import com.arthurzettler.musiclibrary.domain.model.Song
import com.arthurzettler.musiclibrary.presentation.components.MusicTopAppBar
import com.arthurzettler.musiclibrary.presentation.components.SongListItem
import com.arthurzettler.musiclibrary.presentation.components.SongListItemConfig
import com.arthurzettler.musiclibrary.ui.preview.previewPlaylist
import com.arthurzettler.musiclibrary.ui.preview.previewSong
import com.arthurzettler.musiclibrary.ui.theme.MusicLibraryTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PullRefreshIndicatorDurationMs = 1500L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    viewModel: AlbumViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlbumScreenScaffold(
        uiState = uiState,
        isRefreshing = isRefreshing,
        pullRefreshState = pullRefreshState,
        onNavigateBack = onNavigateBack,
        onRefresh = {
            isRefreshing = true
            viewModel.refresh()
            scope.launch {
                delay(PullRefreshIndicatorDurationMs)
                isRefreshing = false
            }
        },
        onSongSelected = { songs, index ->
            viewModel.onSongSelected(songs, index)
            onNavigateToPlayer()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlbumScreenScaffold(
    uiState: AlbumUiState,
    isRefreshing: Boolean,
    pullRefreshState: androidx.compose.material3.pulltorefresh.PullToRefreshState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onSongSelected: (List<Song>, Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val screenDescription = stringResource(R.string.album_screen_description)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = screenDescription },
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            MusicTopAppBar(
                title = uiState.albumName,
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        when (val state = uiState.screenState) {
            is AlbumScreenState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    state = pullRefreshState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    AlbumScreenBody(
                        uiState = uiState,
                        screenState = state,
                        isPullRefreshing = isRefreshing,
                        onSongSelected = onSongSelected
                    )
                }
            }
        }
    }
}

@Composable
internal fun AlbumScreenBody(
    uiState: AlbumUiState,
    screenState: AlbumScreenState,
    isPullRefreshing: Boolean,
    onSongSelected: (List<Song>, Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    when (screenState) {
        is AlbumScreenState.Loading -> Unit

        is AlbumScreenState.Error -> {
            if (!isPullRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.error_pull_to_refresh),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
        }

        is AlbumScreenState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    AsyncImage(
                        model = uiState.artworkUrl,
                        contentDescription = stringResource(
                            R.string.album_cover_description,
                            uiState.albumName
                        ),
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.albumName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (screenState.syncError != null) {
                    item(key = "album_sync_error") {
                        Text(
                            text = stringResource(R.string.album_sync_failed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else if (screenState.isStale) {
                    item(key = "album_cached_hint") {
                        Text(
                            text = stringResource(R.string.album_cached_loading),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                itemsIndexed(
                    items = screenState.songs,
                    key = { _, song -> song.trackId }
                ) { index, song ->
                    SongListItem(
                        song = song,
                        config = SongListItemConfig(hasMoreOption = false),
                        onClick = { onSongSelected(screenState.songs, index) },
                        onMoreClick = { }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

private class AlbumScreenStatePreviewProvider : PreviewParameterProvider<AlbumScreenState> {
    override val values: Sequence<AlbumScreenState> = sequenceOf(
        AlbumScreenState.Loading,
        AlbumScreenState.Error(message = "Network error"),
        AlbumScreenState.Success(songs = previewPlaylist, isStale = false),
        AlbumScreenState.Success(songs = previewPlaylist, isStale = true),
        AlbumScreenState.Success(
            songs = previewPlaylist,
            syncError = "Could not refresh album"
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Album — states")
@Composable
private fun AlbumScreenStatePreview(
    @PreviewParameter(AlbumScreenStatePreviewProvider::class) screenState: AlbumScreenState
) {
    MusicLibraryTheme {
        AlbumScreenScaffold(
            uiState = AlbumUiState(
                albumName = previewSong.collectionName,
                artistName = previewSong.artistName,
                artworkUrl = previewSong.artworkUrl,
                screenState = screenState
            ),
            isRefreshing = false,
            pullRefreshState = rememberPullToRefreshState(),
            onNavigateBack = {},
            onRefresh = {},
            onSongSelected = { _, _ -> }
        )
    }
}
