package com.arthurzettler.musiclibrary.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arthurzettler.musiclibrary.R
import com.arthurzettler.musiclibrary.ui.theme.MusicLibraryTheme

@Composable
fun ConnectivityBanner(
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    val bannerDescription = stringResource(R.string.no_internet_connection)

    AnimatedVisibility(
        visible = isOffline,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.error)
                .padding(vertical = 6.dp, horizontal = 16.dp)
                .semantics { contentDescription = bannerDescription },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_internet_connection),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onError,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun ConnectivityBannerPreview() {
    MusicLibraryTheme {
        ConnectivityBanner(isOffline = true)
    }
}
