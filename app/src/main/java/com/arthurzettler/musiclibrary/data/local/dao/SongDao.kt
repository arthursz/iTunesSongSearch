package com.arthurzettler.musiclibrary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.arthurzettler.musiclibrary.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Upsert
    suspend fun upsertAll(songs: List<SongEntity>)

    @Query("SELECT trackId, lastPlayedAt FROM songs WHERE trackId IN (:trackIds)")
    suspend fun getLastPlayedAtByTrackIds(trackIds: List<Long>): List<SongLastPlayed>

    @Transaction
    suspend fun upsertSongs(songs: List<SongEntity>) {
        if (songs.isEmpty()) return

        val lastPlayedByTrackId = getLastPlayedAtByTrackIds(songs.map { it.trackId })
            .associate { it.trackId to it.lastPlayedAt }

        val merged = songs.map { song ->
            val preservedLastPlayed = lastPlayedByTrackId[song.trackId]
            if (preservedLastPlayed != null) {
                song.copy(lastPlayedAt = preservedLastPlayed)
            } else {
                song
            }
        }
        upsertAll(merged)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Query("SELECT * FROM songs WHERE lastPlayedAt > 0 ORDER BY lastPlayedAt DESC LIMIT 20")
    fun getRecentlyPlayed(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE collectionId = :collectionId ORDER BY trackNumber ASC")
    suspend fun getAlbumSongs(collectionId: Long): List<SongEntity>

    @Query("UPDATE songs SET lastPlayedAt = :timestamp WHERE trackId = :trackId")
    suspend fun updateLastPlayed(trackId: Long, timestamp: Long)
}
