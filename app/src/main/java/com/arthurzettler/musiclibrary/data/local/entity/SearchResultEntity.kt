package com.arthurzettler.musiclibrary.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "search_results",
    primaryKeys = ["query", "trackId"]
)
data class SearchResultEntity(
    val query: String,
    val trackId: Long,
    val position: Int,
    val cachedAt: Long = System.currentTimeMillis()
)
