package com.arthurzettler.musiclibrary.presentation.songs

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.arthurzettler.musiclibrary.R
import com.arthurzettler.musiclibrary.domain.model.Song
import com.arthurzettler.musiclibrary.presentation.components.SongBottomSheet
import com.arthurzettler.musiclibrary.presentation.components.SongListItem
import com.arthurzettler.musiclibrary.ui.preview.previewPlaylist
import com.arthurzettler.musiclibrary.ui.preview.previewSongLongTitle
import com.arthurzettler.musiclibrary.ui.theme.MusicLibraryTheme

private val ListBottomSpacing = 32.dp
private const val SearchExpandDurationMs = 250
private val SearchExpandAnimationSpec = tween<Float>(
    durationMillis = SearchExpandDurationMs,
    easing = FastOutSlowInEasing
)

@Composable
internal fun SongsWelcomeContent(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.welcome_message),
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
internal fun SongsRecentlyPlayedContent(
    songs: List<Song>,
    onSongClick: (Int) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        itemsIndexed(items = songs, key = { _, song -> song.trackId }) { index, song ->
            SongListItem(
                song = song,
                onClick = { onSongClick(index) },
                onMoreClick = { onSongMoreClick(song) }
            )
        }
        item(key = "recently_played_footer") {
            Spacer(modifier = Modifier.height(ListBottomSpacing))
        }
    }
}

@Composable
internal fun SongsSearchLoadingContent(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = colorScheme.primary
        )
    }
}

@Composable
internal fun SongsSearchBlockingErrorContent(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
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

@Composable
internal fun SongsSearchEmptyContent(
    query: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_search_results, query),
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
internal fun SongsSearchResultsContent(
    songs: List<Song>,
    showInlineError: Boolean,
    showAppendError: Boolean,
    showAppendLoading: Boolean,
    onSongClick: (Int) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    LazyColumn(modifier = modifier.fillMaxSize()) {
        searchErrorBanner(
            showInlineError = showInlineError,
            showAppendError = showAppendError,
            errorColor = colorScheme.error
        )
        itemsIndexed(items = songs, key = { _, song -> song.trackId }) { index, song ->
            SongListItem(
                song = song,
                onClick = { onSongClick(index) },
                onMoreClick = { onSongMoreClick(song) }
            )
        }
        searchAppendLoading(
            showAppendLoading = showAppendLoading,
            indicatorColor = colorScheme.primary
        )
        searchListFooter()
    }
}

@Composable
internal fun SongsSearchResultsContent(
    pagingItems: LazyPagingItems<Song>,
    listState: LazyListState,
    showInlineError: Boolean,
    showAppendError: Boolean,
    showAppendLoading: Boolean,
    onSongClick: (Int) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        searchErrorBanner(
            showInlineError = showInlineError,
            showAppendError = showAppendError,
            errorColor = colorScheme.error
        )
        items(
            count = pagingItems.itemCount,
            key = { index -> pagingItems.peek(index)?.trackId ?: index }
        ) { index ->
            val song = pagingItems[index] ?: return@items
            SongListItem(
                song = song,
                onClick = { onSongClick(index) },
                onMoreClick = { onSongMoreClick(song) }
            )
        }
        searchAppendLoading(
            showAppendLoading = showAppendLoading,
            indicatorColor = colorScheme.primary
        )
        searchListFooter()
    }
}

private fun LazyListScope.searchErrorBanner(
    showInlineError: Boolean,
    showAppendError: Boolean,
    errorColor: Color
) {
    if (!showInlineError) return
    item(key = "search_error_banner") {
        Text(
            text = stringResource(
                if (showAppendError) {
                    R.string.error_loading_more
                } else {
                    R.string.error_pull_to_refresh
                }
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = errorColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

private fun LazyListScope.searchAppendLoading(
    showAppendLoading: Boolean,
    indicatorColor: Color
) {
    if (!showAppendLoading) return
    item(key = "search_append_loading") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = indicatorColor
            )
        }
    }
}

private fun LazyListScope.searchListFooter() {
    item(key = "search_footer") {
        Spacer(modifier = Modifier.height(ListBottomSpacing))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SongsSearchResults(
    searchQuery: String,
    pagingItems: LazyPagingItems<Song>,
    onSongClick: (Int) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    key(searchQuery) {
        val searchListState = rememberLazyListState()
        var scrollToTopOnNextResults by remember(searchQuery) {
            mutableStateOf(true)
        }

        val refreshLoadState = pagingItems.loadState.refresh
        val isNearListEnd by remember {
            derivedStateOf {
                val layoutInfo = searchListState.layoutInfo
                val total = layoutInfo.totalItemsCount
                if (total == 0) return@derivedStateOf false
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisible >= total - 3
            }
        }
        val searchUiState = SearchUiState.from(
            refreshLoadState = refreshLoadState,
            appendLoadState = pagingItems.loadState.append,
            itemCount = pagingItems.itemCount,
            searchQuery = searchQuery,
            isNearListEnd = isNearListEnd
        )

        LaunchedEffect(searchQuery) {
            scrollToTopOnNextResults = true
            snapshotFlow {
                pagingItems.loadState.refresh to pagingItems.itemCount
            }
                .filter { (refresh, count) ->
                    refresh is LoadState.NotLoading && count > 0
                }
                .first()
            searchListState.scrollToItem(0)
            scrollToTopOnNextResults = false
        }

        LaunchedEffect(refreshLoadState) {
            when (refreshLoadState) {
                is LoadState.Loading -> scrollToTopOnNextResults = true
                is LoadState.NotLoading -> {
                    if (scrollToTopOnNextResults && pagingItems.itemCount > 0) {
                        searchListState.scrollToItem(0)
                        scrollToTopOnNextResults = false
                    }
                }
                else -> Unit
            }
        }

        PullToRefreshBox(
            isRefreshing = searchUiState.isPullRefreshing,
            onRefresh = { pagingItems.refresh() },
            modifier = modifier
        ) {
            when {
                searchUiState.isInitialLoad -> SongsSearchLoadingContent()
                searchUiState.showBlockingError -> SongsSearchBlockingErrorContent()
                searchUiState.showEmptyResults -> SongsSearchEmptyContent(query = searchQuery)
                else -> SongsSearchResultsContent(
                    pagingItems = pagingItems,
                    listState = searchListState,
                    showInlineError = searchUiState.showInlineError,
                    showAppendError = searchUiState.hasAppendError,
                    showAppendLoading = searchUiState.showAppendLoading,
                    onSongClick = onSongClick,
                    onSongMoreClick = onSongMoreClick
                )
            }
        }
    }
}

@Composable
private fun SongsScreenHeader(
    showSearchField: Boolean,
    onSearchClick: () -> Unit,
    searchContentDescription: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val searchIconAlpha by animateFloatAsState(
        targetValue = if (showSearchField) 0f else 1f,
        animationSpec = SearchExpandAnimationSpec,
        label = "searchIconAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.songs_title),
            style = MaterialTheme.typography.headlineLarge,
            color = colorScheme.onBackground
        )

        IconButton(
            onClick = onSearchClick,
            enabled = !showSearchField,
            modifier = Modifier
                .size(48.dp)
                .alpha(searchIconAlpha)
                .semantics { contentDescription = searchContentDescription }
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun SongsSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    searchContentDescription: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .semantics { contentDescription = searchContentDescription },
        placeholder = {
            Text(
                text = stringResource(R.string.search_placeholder),
                color = colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.clear_search),
                    tint = colorScheme.onSurfaceVariant
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surfaceVariant,
            unfocusedContainerColor = colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = colorScheme.onSurface,
            unfocusedTextColor = colorScheme.onSurface
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { focusManager.clearFocus() }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    viewModel: SongsViewModel,
    onNavigateToPlayer: () -> Unit,
    onNavigateToAlbum: (Song) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.pagingFlow.collectAsLazyPagingItems()
    var selectedSongTrackId by rememberSaveable { mutableStateOf<Long?>(null) }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val isSearching = uiState.screenState is SongsScreenState.Searching
    val showSearchField = isSearchExpanded || uiState.searchQuery.isNotEmpty() || isSearching

    LaunchedEffect(showSearchField) {
        if (showSearchField) {
            delay(SearchExpandDurationMs.toLong())
            searchFocusRequester.requestFocus()
        }
    }

    val collapseSearch = {
        viewModel.onSearchQueryChanged("")
        isSearchExpanded = false
        focusManager.clearFocus()
    }

    val selectedSong = selectedSongTrackId?.let { trackId ->
        uiState.recentlyPlayed.find { it.trackId == trackId }
            ?: (0 until pagingItems.itemCount).firstNotNullOfOrNull { index ->
                pagingItems.peek(index)?.takeIf { it.trackId == trackId }
            }
    }

    val searchDescription = stringResource(R.string.search_songs)
    val colorScheme = MaterialTheme.colorScheme

    if (showSearchField) {
        BackHandler(onBack = collapseSearch)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = SearchExpandDurationMs,
                            easing = FastOutSlowInEasing
                        )
                    )
            ) {
                SongsScreenHeader(
                    showSearchField = showSearchField,
                    onSearchClick = { isSearchExpanded = true },
                    searchContentDescription = searchDescription,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                if (showSearchField) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SongsSearchField(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        onClose = collapseSearch,
                        searchContentDescription = searchDescription,
                        focusRequester = searchFocusRequester,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState.screenState) {
                is SongsScreenState.Idle -> {
                    if (uiState.recentlyPlayed.isEmpty()) {
                        SongsWelcomeContent(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    } else {
                        SongsRecentlyPlayedContent(
                            songs = uiState.recentlyPlayed,
                            onSongClick = { index ->
                                viewModel.onSongSelected(uiState.recentlyPlayed, index)
                                onNavigateToPlayer()
                            },
                            onSongMoreClick = { song -> selectedSongTrackId = song.trackId },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    }
                }

                is SongsScreenState.Searching -> {
                    SongsSearchResults(
                        searchQuery = uiState.searchQuery,
                        pagingItems = pagingItems,
                        onSongClick = { index ->
                            viewModel.onSongSelected(
                                pagingItems.itemSnapshotList.items,
                                index
                            )
                            onNavigateToPlayer()
                        },
                        onSongMoreClick = { song -> selectedSongTrackId = song.trackId },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }

    selectedSong?.let { song ->
        SongBottomSheet(
            song = song,
            onDismiss = { selectedSongTrackId = null },
            onViewAlbum = {
                selectedSongTrackId = null
                onNavigateToAlbum(song)
            }
        )
    }
}

@Preview(showBackground = true, name = "Songs — Header collapsed")
@Composable
private fun SongsScreenHeaderCollapsedPreview() {
    MusicLibraryTheme {
        SongsScreenHeader(
            showSearchField = false,
            onSearchClick = {},
            searchContentDescription = "Search songs",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Songs — Header expanded")
@Composable
private fun SongsScreenHeaderExpandedPreview() {
    MusicLibraryTheme {
        Column {
            SongsScreenHeader(
                showSearchField = true,
                onSearchClick = {},
                searchContentDescription = "Search songs",
                modifier = Modifier.padding(16.dp)
            )
            SongsSearchField(
                query = "",
                onQueryChange = {},
                onClose = {},
                searchContentDescription = "Search songs",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Songs — Search field")
@Composable
private fun SongsSearchFieldPreview() {
    MusicLibraryTheme {
        SongsSearchField(
            query = "",
            onQueryChange = {},
            onClose = {},
            searchContentDescription = "Search songs",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Songs — Idle welcome")
@Composable
private fun SongsWelcomePreview() {
    MusicLibraryTheme {
        SongsWelcomeContent(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true, name = "Songs — Recently played")
@Composable
private fun SongsRecentlyPlayedPreview() {
    MusicLibraryTheme {
        SongsRecentlyPlayedContent(
            songs = previewPlaylist,
            onSongClick = {},
            onSongMoreClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, name = "Songs — Search loading")
@Composable
private fun SongsSearchLoadingPreview() {
    MusicLibraryTheme {
        SongsSearchLoadingContent(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true, name = "Songs — Search empty")
@Composable
private fun SongsSearchEmptyPreview() {
    MusicLibraryTheme {
        SongsSearchEmptyContent(
            query = "zzzznotfound",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, name = "Songs — Search error")
@Composable
private fun SongsSearchErrorPreview() {
    MusicLibraryTheme {
        SongsSearchBlockingErrorContent(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true, name = "Songs — Search results")
@Composable
private fun SongsSearchResultsPreview() {
    MusicLibraryTheme {
        SongsSearchResultsContent(
            songs = previewPlaylist,
            showInlineError = false,
            showAppendError = false,
            showAppendLoading = false,
            onSongClick = {},
            onSongMoreClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, name = "Songs — Search append error")
@Composable
private fun SongsSearchAppendErrorPreview() {
    MusicLibraryTheme {
        SongsSearchResultsContent(
            songs = previewPlaylist,
            showInlineError = true,
            showAppendError = true,
            showAppendLoading = false,
            onSongClick = {},
            onSongMoreClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, name = "Songs — Long title item")
@Composable
private fun SongsLongTitleItemPreview() {
    MusicLibraryTheme {
        SongListItem(
            song = previewSongLongTitle,
            onClick = {},
            onMoreClick = {}
        )
    }
}
