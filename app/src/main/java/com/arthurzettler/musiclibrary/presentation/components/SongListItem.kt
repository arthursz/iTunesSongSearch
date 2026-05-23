package com.arthurzettler.musiclibrary.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arthurzettler.musiclibrary.R
import com.arthurzettler.musiclibrary.domain.model.Song
import com.arthurzettler.musiclibrary.ui.preview.previewSong
import com.arthurzettler.musiclibrary.ui.preview.previewSongLongTitle
import com.arthurzettler.musiclibrary.ui.preview.previewSongMissingArt
import com.arthurzettler.musiclibrary.ui.theme.MusicLibraryTheme

data class SongListItemConfig(
    val hasMoreOption: Boolean = true
)

@Composable
fun SongListItem(
    song: Song,
    config: SongListItemConfig = SongListItemConfig(),
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemDescription = stringResource(R.string.song_by_artist, song.trackName, song.artistName)
    val artworkDescription = stringResource(R.string.artwork_description, song.trackName)
    val moreDescription = stringResource(R.string.more_options_description, song.trackName)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { contentDescription = itemDescription },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.artworkUrl,
            contentDescription = artworkDescription,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.trackName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artistName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (config.hasMoreOption) {
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = moreDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Song list item")
@Composable
private fun SongListItemPreview() {
    MusicLibraryTheme {
        SongListItem(
            song = previewSong,
            onClick = {},
            onMoreClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Song list item — long title")
@Composable
private fun SongListItemLongTitlePreview() {
    MusicLibraryTheme {
        SongListItem(
            song = previewSongLongTitle,
            onClick = {},
            onMoreClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Song list item — missing art")
@Composable
private fun SongListItemMissingArtPreview() {
    MusicLibraryTheme {
        SongListItem(
            song = previewSongMissingArt,
            onClick = {},
            onMoreClick = {}
        )
    }
}
