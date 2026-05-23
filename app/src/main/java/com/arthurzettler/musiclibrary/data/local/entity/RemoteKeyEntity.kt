package com.arthurzettler.musiclibrary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val query: String,
    val nextOffset: Int,
    val cachedAt: Long = System.currentTimeMillis()
)
