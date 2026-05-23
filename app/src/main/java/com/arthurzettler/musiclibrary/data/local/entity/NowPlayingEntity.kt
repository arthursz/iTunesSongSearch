package com.arthurzettler.musiclibrary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "now_playing")
data class NowPlayingEntity(
    @PrimaryKey val position: Int,
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val collectionName: String,
    val artworkUrl: String,
    val previewUrl: String,
    val trackTimeMillis: Long,
    val collectionId: Long,
    val trackNumber: Int,
    val isCurrentTrack: Boolean = false
)
