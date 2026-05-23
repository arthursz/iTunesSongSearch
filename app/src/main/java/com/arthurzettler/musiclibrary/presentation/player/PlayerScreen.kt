package com.arthurzettler.musiclibrary.presentation.player

import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.arthurzettler.musiclibrary.R
import com.arthurzettler.musiclibrary.domain.model.Song
import coil.compose.AsyncImage
import com.arthurzettler.musiclibrary.presentation.components.MusicTopAppBar
import com.arthurzettler.musiclibrary.presentation.components.SongBottomSheet
import com.arthurzettler.musiclibrary.ui.preview.previewPlaylist
import com.arthurzettler.musiclibrary.ui.preview.previewSong
import com.arthurzettler.musiclibrary.ui.preview.previewSongLongTitle
import com.arthurzettler.musiclibrary.ui.preview.previewSongMissingArt
import com.arthurzettler.musiclibrary.ui.theme.DarkCard
import com.arthurzettler.musiclibrary.ui.theme.MusicLibraryTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    showAlbumOption: Boolean = true,
    onNavigateBack: () -> Unit,
    onViewAlbum: (Long, String, String, String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val liveProgress by viewModel.playbackProgress
        .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
        .collectAsStateWithLifecycle(initialValue = null)
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    val currentPosition = liveProgress?.currentPosition ?: uiState.currentPosition
    val duration = liveProgress?.duration?.takeIf { it > 0L } ?: 0L

    LifecycleStartEffect(viewModel) {
        viewModel.resumeFromNavigation()
        onStopOrDispose { }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.pauseForNavigation()
    }

    when (val state = uiState.screenState) {
        is PlayerScreenState.Idle -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        is PlayerScreenState.Ready -> {
            PlayerReadyContent(
                currentSong = state.currentSong,
                isPlaying = uiState.isPlaying,
                isRepeatEnabled = uiState.isRepeatEnabled,
                canSkipPrevious = state.currentIndex > 0,
                canSkipNext = state.currentIndex < state.playlist.size - 1,
                currentPosition = currentPosition,
                duration = duration,
                showAlbumOption = showAlbumOption,
                showBottomSheet = showBottomSheet,
                onNavigateBack = onNavigateBack,
                onShowBottomSheet = { showBottomSheet = true },
                onDismissBottomSheet = { showBottomSheet = false },
                onViewAlbum = {
                    showBottomSheet = false
                    onViewAlbum(
                        state.currentSong.collectionId,
                        state.currentSong.collectionName,
                        state.currentSong.artistName,
                        state.currentSong.artworkUrl
                    )
                },
                onPlayPause = viewModel::playPause,
                onSeek = viewModel::seekTo,
                onSkipPrevious = viewModel::skipPrevious,
                onSkipNext = viewModel::skipNext,
                onToggleRepeat = viewModel::toggleRepeat
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerReadyContent(
    currentSong: Song,
    isPlaying: Boolean,
    isRepeatEnabled: Boolean,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    currentPosition: Long,
    duration: Long,
    showAlbumOption: Boolean,
    showBottomSheet: Boolean,
    onNavigateBack: () -> Unit,
    onShowBottomSheet: () -> Unit,
    onDismissBottomSheet: () -> Unit,
    onViewAlbum: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenDescription = stringResource(R.string.player_screen_description)
    val moreOptionsDescription = stringResource(R.string.more_options)
    val progressDescription = stringResource(R.string.song_progress)
    val previousDescription = stringResource(R.string.previous_song)
    val nextDescription = stringResource(R.string.next_song)
    val playDescription = stringResource(R.string.play)
    val pauseDescription = stringResource(R.string.pause)
    val repeatDescription = stringResource(R.string.repeat)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = screenDescription },
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            MusicTopAppBar(
                title = stringResource(R.string.now_playing),
                onNavigateBack = onNavigateBack,
                actions = {
                    if (showAlbumOption) {
                        IconButton(onClick = onShowBottomSheet) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = moreOptionsDescription
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLandscape) {
            PlayerLandscapeContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                currentSong = currentSong,
                currentPosition = currentPosition,
                duration = duration,
                isPlaying = isPlaying,
                isRepeatEnabled = isRepeatEnabled,
                canSkipPrevious = canSkipPrevious,
                canSkipNext = canSkipNext,
                progressDescription = progressDescription,
                playDescription = playDescription,
                pauseDescription = pauseDescription,
                previousDescription = previousDescription,
                nextDescription = nextDescription,
                repeatDescription = repeatDescription,
                onSeek = onSeek,
                onPlayPause = onPlayPause,
                onSkipPrevious = onSkipPrevious,
                onSkipNext = onSkipNext,
                onToggleRepeat = onToggleRepeat
            )
        } else {
            PlayerPortraitContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                currentSong = currentSong,
                currentPosition = currentPosition,
                duration = duration,
                isPlaying = isPlaying,
                isRepeatEnabled = isRepeatEnabled,
                canSkipPrevious = canSkipPrevious,
                canSkipNext = canSkipNext,
                progressDescription = progressDescription,
                playDescription = playDescription,
                pauseDescription = pauseDescription,
                previousDescription = previousDescription,
                nextDescription = nextDescription,
                repeatDescription = repeatDescription,
                onSeek = onSeek,
                onPlayPause = onPlayPause,
                onSkipPrevious = onSkipPrevious,
                onSkipNext = onSkipNext,
                onToggleRepeat = onToggleRepeat
            )
        }
    }

    if (showBottomSheet) {
        SongBottomSheet(
            song = currentSong,
            onDismiss = onDismissBottomSheet,
            onViewAlbum = onViewAlbum
        )
    }
}

@Composable
private fun PlayerPortraitContent(
    currentSong: Song,
    currentPosition: Long,
    duration: Long,
    isPlaying: Boolean,
    isRepeatEnabled: Boolean,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    progressDescription: String,
    playDescription: String,
    pauseDescription: String,
    previousDescription: String,
    nextDescription: String,
    repeatDescription: String,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        PlayerAlbumArtwork(
            artworkUrl = currentSong.artworkUrl,
            trackName = currentSong.trackName,
            modifier = Modifier.fillMaxWidth(0.7f)
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            PlayerSongInfo(
                trackName = currentSong.trackName,
                artistName = currentSong.artistName
            )

            Spacer(modifier = Modifier.height(20.dp))

            PlayerPlaybackControls(
                currentPosition = currentPosition,
                duration = duration,
                isPlaying = isPlaying,
                isRepeatEnabled = isRepeatEnabled,
                canSkipPrevious = canSkipPrevious,
                canSkipNext = canSkipNext,
                progressDescription = progressDescription,
                playDescription = playDescription,
                pauseDescription = pauseDescription,
                previousDescription = previousDescription,
                nextDescription = nextDescription,
                repeatDescription = repeatDescription,
                onSeek = onSeek,
                onPlayPause = onPlayPause,
                onSkipPrevious = onSkipPrevious,
                onSkipNext = onSkipNext,
                onToggleRepeat = onToggleRepeat
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PlayerLandscapeContent(
    currentSong: Song,
    currentPosition: Long,
    duration: Long,
    isPlaying: Boolean,
    isRepeatEnabled: Boolean,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    progressDescription: String,
    playDescription: String,
    pauseDescription: String,
    previousDescription: String,
    nextDescription: String,
    repeatDescription: String,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PlayerAlbumArtwork(
                artworkUrl = currentSong.artworkUrl,
                trackName = currentSong.trackName,
                modifier = Modifier
                    .fillMaxHeight(0.9f)
                    .aspectRatio(1f)
            )

            PlayerSongInfo(
                trackName = currentSong.trackName,
                artistName = currentSong.artistName,
                modifier = Modifier.weight(1f),
                titleMaxLines = 2
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PlayerPlaybackControls(
            currentPosition = currentPosition,
            duration = duration,
            isPlaying = isPlaying,
            isRepeatEnabled = isRepeatEnabled,
            canSkipPrevious = canSkipPrevious,
            canSkipNext = canSkipNext,
            progressDescription = progressDescription,
            playDescription = playDescription,
            pauseDescription = pauseDescription,
            previousDescription = previousDescription,
            nextDescription = nextDescription,
            repeatDescription = repeatDescription,
            onSeek = onSeek,
            onPlayPause = onPlayPause,
            onSkipPrevious = onSkipPrevious,
            onSkipNext = onSkipNext,
            onToggleRepeat = onToggleRepeat
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PlayerAlbumArtwork(
    artworkUrl: String,
    trackName: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = artworkUrl,
        contentDescription = stringResource(
            R.string.album_artwork_description,
            trackName
        ),
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun PlayerSongInfo(
    trackName: String,
    artistName: String,
    modifier: Modifier = Modifier,
    titleMaxLines: Int = 1
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = trackName,
            style = MaterialTheme.typography.headlineLarge,
            color = colorScheme.onBackground,
            maxLines = titleMaxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = artistName,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun PlayerPlaybackControls(
    currentPosition: Long,
    duration: Long,
    isPlaying: Boolean,
    isRepeatEnabled: Boolean,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    progressDescription: String,
    playDescription: String,
    pauseDescription: String,
    previousDescription: String,
    nextDescription: String,
    repeatDescription: String,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        PlayerSeekBar(
            currentPosition = currentPosition,
            duration = duration,
            onSeek = onSeek,
            progressDescription = progressDescription
        )

        Spacer(modifier = Modifier.height(32.dp))

        PlayerControlsRow(
            isPlaying = isPlaying,
            isRepeatEnabled = isRepeatEnabled,
            canSkipPrevious = canSkipPrevious,
            canSkipNext = canSkipNext,
            playDescription = playDescription,
            pauseDescription = pauseDescription,
            previousDescription = previousDescription,
            nextDescription = nextDescription,
            repeatDescription = repeatDescription,
            onPlayPause = onPlayPause,
            onSkipPrevious = onSkipPrevious,
            onSkipNext = onSkipNext,
            onToggleRepeat = onToggleRepeat
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerSeekBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    progressDescription: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubFraction by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentPosition, duration) {
        if (!isScrubbing && duration > 0L) {
            scrubFraction = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        }
    }

    val sliderFraction = if (isScrubbing) {
        scrubFraction
    } else if (duration > 0L) {
        (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val displayPosition = if (duration > 0L) {
        (sliderFraction * duration).toLong().coerceIn(0L, duration)
    } else {
        0L
    }

    val seekColors = SliderDefaults.colors(
        thumbColor = colorScheme.onBackground,
        activeTrackColor = colorScheme.onBackground,
        inactiveTrackColor = colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Slider(
            value = sliderFraction,
            onValueChange = { fraction ->
                if (duration <= 0L) return@Slider
                isScrubbing = true
                scrubFraction = fraction.coerceIn(0f, 1f)
                onSeek((scrubFraction * duration).toLong())
            },
            onValueChangeFinished = {
                if (duration <= 0L) {
                    isScrubbing = false
                    return@Slider
                }
                isScrubbing = false
                onSeek((scrubFraction * duration).toLong())
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .semantics { contentDescription = progressDescription },
            interactionSource = interactionSource,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    modifier = Modifier.size(14.dp),
                    colors = seekColors
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    colors = seekColors,
                    thumbTrackGapSize = 0.dp
                )
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatTime(displayPosition),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = formatRemainingTime(displayPosition, duration),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlayerControlsRow(
    isPlaying: Boolean,
    isRepeatEnabled: Boolean,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    playDescription: String,
    pauseDescription: String,
    previousDescription: String,
    nextDescription: String,
    repeatDescription: String,
    onPlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val skipEnabledTint = colorScheme.onBackground
    val skipDisabledTint = colorScheme.onBackground.copy(alpha = 0.38f)

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(DarkCard)
                    .clickable(onClick = onPlayPause)
                    .semantics {
                        contentDescription = if (isPlaying) pauseDescription else playDescription
                    },
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = isPlaying,
                    animationSpec = tween(durationMillis = 200),
                    label = "playPauseIcon"
                ) { playing ->
                    Icon(
                        imageVector = if (playing) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = null,
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            IconButton(
                onClick = onSkipPrevious,
                enabled = canSkipPrevious,
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = previousDescription }
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = null,
                    tint = if (canSkipPrevious) skipEnabledTint else skipDisabledTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = onSkipNext,
                enabled = canSkipNext,
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = nextDescription }
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = null,
                    tint = if (canSkipNext) skipEnabledTint else skipDisabledTint,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(if (isRepeatEnabled) 64.dp else 48.dp)
                .clip(CircleShape)
                .background(if (isRepeatEnabled) DarkCard else Color.Transparent)
                .clickable(onClick = onToggleRepeat)
                .semantics { contentDescription = repeatDescription },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = null,
                tint = if (isRepeatEnabled) {
                    colorScheme.onBackground
                } else {
                    colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(Locale.US, minutes, seconds)
}

private fun formatRemainingTime(current: Long, total: Long): String {
    val remaining = (total - current).coerceAtLeast(0L)
    val totalSeconds = remaining / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "-%d:%02d".format(Locale.US, minutes, seconds)
}

@Preview(
    showBackground = true,
    name = "Player — Ready (landscape)",
    widthDp = 840,
    heightDp = 360
)
@Composable
private fun PlayerReadyLandscapePreview() {
    MusicLibraryTheme {
        PlayerReadyContent(
            currentSong = previewSong,
            isPlaying = true,
            isRepeatEnabled = true,
            canSkipPrevious = true,
            canSkipNext = true,
            currentPosition = 12_000L,
            duration = 30_000L,
            showAlbumOption = true,
            showBottomSheet = false,
            onNavigateBack = {},
            onShowBottomSheet = {},
            onDismissBottomSheet = {},
            onViewAlbum = {},
            onPlayPause = {},
            onSeek = {},
            onSkipPrevious = {},
            onSkipNext = {},
            onToggleRepeat = {}
        )
    }
}

@Preview(showBackground = true, name = "Player — Idle")
@Composable
private fun PlayerIdlePreview() {
    MusicLibraryTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@Preview(showBackground = true, name = "Player — Ready (playing)")
@Composable
private fun PlayerReadyPreview() {
    MusicLibraryTheme {
        PlayerReadyContent(
            currentSong = previewSong,
            isPlaying = true,
            isRepeatEnabled = true,
            canSkipPrevious = true,
            canSkipNext = true,
            currentPosition = 86_000L,
            duration = 260_000L,
            showAlbumOption = true,
            showBottomSheet = false,
            onNavigateBack = {},
            onShowBottomSheet = {},
            onDismissBottomSheet = {},
            onViewAlbum = {},
            onPlayPause = {},
            onSeek = {},
            onSkipPrevious = {},
            onSkipNext = {},
            onToggleRepeat = {}
        )
    }
}

@Preview(showBackground = true, name = "Player — First track (skip disabled)")
@Composable
private fun PlayerFirstTrackPreview() {
    MusicLibraryTheme {
        PlayerReadyContent(
            currentSong = previewPlaylist.first(),
            isPlaying = false,
            isRepeatEnabled = false,
            canSkipPrevious = false,
            canSkipNext = previewPlaylist.size > 1,
            currentPosition = 0L,
            duration = previewPlaylist.first().trackTimeMillis,
            showAlbumOption = true,
            showBottomSheet = false,
            onNavigateBack = {},
            onShowBottomSheet = {},
            onDismissBottomSheet = {},
            onViewAlbum = {},
            onPlayPause = {},
            onSeek = {},
            onSkipPrevious = {},
            onSkipNext = {},
            onToggleRepeat = {}
        )
    }
}

@Preview(showBackground = true, name = "Player — Last track")
@Composable
private fun PlayerLastTrackPreview() {
    MusicLibraryTheme {
        PlayerReadyContent(
            currentSong = previewPlaylist.last(),
            isPlaying = false,
            isRepeatEnabled = false,
            canSkipPrevious = true,
            canSkipNext = false,
            currentPosition = 12_000L,
            duration = previewPlaylist.last().trackTimeMillis,
            showAlbumOption = true,
            showBottomSheet = false,
            onNavigateBack = {},
            onShowBottomSheet = {},
            onDismissBottomSheet = {},
            onViewAlbum = {},
            onPlayPause = {},
            onSeek = {},
            onSkipPrevious = {},
            onSkipNext = {},
            onToggleRepeat = {}
        )
    }
}

@Preview(showBackground = true, name = "Player — Long title")
@Composable
private fun PlayerLongTitlePreview() {
    MusicLibraryTheme {
        PlayerReadyContent(
            currentSong = previewSongLongTitle,
            isPlaying = false,
            isRepeatEnabled = false,
            canSkipPrevious = false,
            canSkipNext = false,
            currentPosition = 0L,
            duration = previewSongLongTitle.trackTimeMillis,
            showAlbumOption = true,
            showBottomSheet = false,
            onNavigateBack = {},
            onShowBottomSheet = {},
            onDismissBottomSheet = {},
            onViewAlbum = {},
            onPlayPause = {},
            onSeek = {},
            onSkipPrevious = {},
            onSkipNext = {},
            onToggleRepeat = {}
        )
    }
}

@Preview(showBackground = true, name = "Player — Missing artwork")
@Composable
private fun PlayerMissingArtPreview() {
    MusicLibraryTheme {
        PlayerReadyContent(
            currentSong = previewSongMissingArt,
            isPlaying = true,
            isRepeatEnabled = true,
            canSkipPrevious = false,
            canSkipNext = false,
            currentPosition = 30_000L,
            duration = previewSongMissingArt.trackTimeMillis,
            showAlbumOption = false,
            showBottomSheet = false,
            onNavigateBack = {},
            onShowBottomSheet = {},
            onDismissBottomSheet = {},
            onViewAlbum = {},
            onPlayPause = {},
            onSeek = {},
            onSkipPrevious = {},
            onSkipNext = {},
            onToggleRepeat = {}
        )
    }
}
