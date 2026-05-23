package com.arthurzettler.musiclibrary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val trackId: Long,
    val trackName: String,
    val artistName: String,
    val collectionName: String,
    val artworkUrl: String,
    val previewUrl: String,
    val trackTimeMillis: Long,
    val collectionId: Long,
    val trackNumber: Int,
    val lastPlayedAt: Long = 0L,
    val cachedAt: Long = System.currentTimeMillis()
)
