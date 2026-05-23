package com.arthurzettler.musiclibrary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.arthurzettler.musiclibrary.data.local.entity.NowPlayingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NowPlayingDao {

    @Query("SELECT * FROM now_playing ORDER BY position ASC")
    suspend fun getPlaylist(): List<NowPlayingEntity>

    @Query("SELECT * FROM now_playing WHERE isCurrentTrack = 1 LIMIT 1")
    suspend fun getCurrentTrack(): NowPlayingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<NowPlayingEntity>)

    @Query("DELETE FROM now_playing")
    suspend fun clearAll()

    @Query("UPDATE now_playing SET isCurrentTrack = 0")
    suspend fun clearCurrentTrack()

    @Query("UPDATE now_playing SET isCurrentTrack = 1 WHERE position = :position")
    suspend fun setCurrentTrack(position: Int)

    @Transaction
    suspend fun setCurrentIndex(position: Int) {
        clearCurrentTrack()
        setCurrentTrack(position)
    }

    @Transaction
    suspend fun setPlaylist(tracks: List<NowPlayingEntity>, startIndex: Int) {
        clearAll()
        insertAll(tracks)
        setCurrentTrack(startIndex)
    }
}
