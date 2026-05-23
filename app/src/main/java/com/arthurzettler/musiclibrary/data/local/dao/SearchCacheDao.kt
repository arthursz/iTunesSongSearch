package com.arthurzettler.musiclibrary.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.arthurzettler.musiclibrary.data.local.entity.RemoteKeyEntity
import com.arthurzettler.musiclibrary.data.local.entity.SearchResultEntity
import com.arthurzettler.musiclibrary.data.local.entity.SongEntity

@Dao
interface SearchCacheDao {

    @Query("""
        SELECT songs.* FROM songs
        INNER JOIN search_results ON songs.trackId = search_results.trackId
        WHERE search_results.`query` = :query
        ORDER BY search_results.position ASC
    """)
    fun pagingSource(query: String): PagingSource<Int, SongEntity>

    @Query("SELECT trackId FROM search_results WHERE `query` = :query")
    suspend fun getTrackIdsForQuery(query: String): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSearchResults(results: List<SearchResultEntity>)

    @Query("DELETE FROM search_results WHERE `query` = :query")
    suspend fun clearSearchResults(query: String)

    @Query("SELECT * FROM remote_keys WHERE `query` = :query")
    suspend fun getRemoteKey(query: String): RemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteKey(key: RemoteKeyEntity)

    @Query("DELETE FROM remote_keys WHERE `query` = :query")
    suspend fun clearRemoteKey(query: String)

    @Query("DELETE FROM search_results WHERE cachedAt < :timestamp")
    suspend fun deleteSearchResultsOlderThan(timestamp: Long)

    @Query("DELETE FROM remote_keys WHERE cachedAt < :timestamp")
    suspend fun deleteRemoteKeysOlderThan(timestamp: Long)

    @Transaction
    suspend fun evictExpiredCache(ttlMillis: Long) {
        val threshold = System.currentTimeMillis() - ttlMillis
        deleteSearchResultsOlderThan(threshold)
        deleteRemoteKeysOlderThan(threshold)
    }
}
