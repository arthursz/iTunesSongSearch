package com.arthurzettler.musiclibrary.presentation.songs

import androidx.paging.LoadState

internal data class SearchUiState(
    val hasRefreshError: Boolean,
    val hasAppendError: Boolean,
    val hasListError: Boolean,
    val showEmptyResults: Boolean,
    val isInitialLoad: Boolean,
    val isPullRefreshing: Boolean,
    val showBlockingError: Boolean,
    val showInlineError: Boolean,
    val showAppendLoading: Boolean
) {
    companion object {
        fun from(
            refreshLoadState: LoadState,
            appendLoadState: LoadState,
            itemCount: Int,
            searchQuery: String,
            isNearListEnd: Boolean,
            hasSettledInitialLoad: Boolean,
            isManualRefresh: Boolean
        ): SearchUiState {
            val hasRefreshError = refreshLoadState is LoadState.Error
            val hasAppendError = appendLoadState is LoadState.Error
            val hasListError = hasRefreshError || hasAppendError
            val isRefreshLoading = refreshLoadState is LoadState.Loading
            return SearchUiState(
                hasRefreshError = hasRefreshError,
                hasAppendError = hasAppendError,
                hasListError = hasListError,
                showEmptyResults = refreshLoadState is LoadState.NotLoading &&
                    appendLoadState is LoadState.NotLoading &&
                    itemCount == 0 &&
                    searchQuery.isNotBlank() &&
                    !hasListError,
                isInitialLoad = isRefreshLoading &&
                    itemCount == 0 &&
                    !hasSettledInitialLoad &&
                    !isManualRefresh,
                isPullRefreshing = isRefreshLoading && (itemCount > 0 || isManualRefresh),
                showBlockingError = itemCount == 0 && hasListError,
                showInlineError = itemCount > 0 && hasListError,
                showAppendLoading = appendLoadState is LoadState.Loading &&
                    refreshLoadState is LoadState.NotLoading &&
                    itemCount > 0 &&
                    !hasAppendError &&
                    isNearListEnd
            )
        }
    }
}
