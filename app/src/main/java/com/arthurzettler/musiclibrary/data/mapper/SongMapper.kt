package com.arthurzettler.musiclibrary.data.mapper

import com.arthurzettler.musiclibrary.data.local.entity.NowPlayingEntity
import com.arthurzettler.musiclibrary.data.local.entity.SongEntity
import com.arthurzettler.musiclibrary.data.remote.dto.SongDto
import com.arthurzettler.musiclibrary.domain.model.Song

fun SongDto.toEntity(): SongEntity? {
    if (wrapperType != "track") return null
    return SongEntity(
        trackId = trackId ?: return null,
        trackName = trackName.orEmpty(),
        artistName = artistName.orEmpty(),
        collectionName = collectionName.orEmpty(),
        artworkUrl = artworkUrl100?.replace("100x100", "600x600").orEmpty(),
        previewUrl = previewUrl.orEmpty(),
        trackTimeMillis = trackTimeMillis ?: 0L,
        collectionId = collectionId ?: 0L,
        trackNumber = trackNumber ?: 0
    )
}

fun SongEntity.toDomain(): Song {
    return Song(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        collectionName = collectionName,
        artworkUrl = artworkUrl,
        previewUrl = previewUrl,
        trackTimeMillis = trackTimeMillis,
        collectionId = collectionId,
        trackNumber = trackNumber
    )
}

fun Song.toEntity(lastPlayedAt: Long = 0L): SongEntity {
    return SongEntity(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        collectionName = collectionName,
        artworkUrl = artworkUrl,
        previewUrl = previewUrl,
        trackTimeMillis = trackTimeMillis,
        collectionId = collectionId,
        trackNumber = trackNumber,
        lastPlayedAt = lastPlayedAt
    )
}

fun NowPlayingEntity.toDomain(): Song {
    return Song(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        collectionName = collectionName,
        artworkUrl = artworkUrl,
        previewUrl = previewUrl,
        trackTimeMillis = trackTimeMillis,
        collectionId = collectionId,
        trackNumber = trackNumber
    )
}
