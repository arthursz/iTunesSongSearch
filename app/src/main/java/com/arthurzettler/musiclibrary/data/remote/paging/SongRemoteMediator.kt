package com.arthurzettler.musiclibrary.data.remote.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.arthurzettler.musiclibrary.data.local.DatabaseTransactionRunner
import com.arthurzettler.musiclibrary.data.local.dao.SearchCacheDao
import com.arthurzettler.musiclibrary.data.local.dao.SongDao
import com.arthurzettler.musiclibrary.data.local.entity.RemoteKeyEntity
import com.arthurzettler.musiclibrary.data.local.entity.SearchResultEntity
import com.arthurzettler.musiclibrary.data.local.entity.SongEntity
import com.arthurzettler.musiclibrary.data.mapper.toEntity
import com.arthurzettler.musiclibrary.data.remote.datasource.RemoteDataSource

@OptIn(ExperimentalPagingApi::class)
class SongRemoteMediator(
    private val query: String,
    private val remoteDataSource: RemoteDataSource,
    private val transactionRunner: DatabaseTransactionRunner,
    private val songDao: SongDao,
    private val searchCacheDao: SearchCacheDao
) : RemoteMediator<Int, SongEntity>() {

    override suspend fun initialize(): InitializeAction {
        val remoteKey =
            searchCacheDao.getRemoteKey(query) ?: return InitializeAction.LAUNCH_INITIAL_REFRESH

        val cacheAge = System.currentTimeMillis() - remoteKey.cachedAt
        return if (cacheAge > CACHE_TTL_MILLIS) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, SongEntity>
    ): MediatorResult {
        val offset = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKey = searchCacheDao.getRemoteKey(query)
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                remoteKey.nextOffset
            }
        }

        return try {
            val pageSize = state.config.pageSize
            val response = remoteDataSource.searchSongs(query, offset, pageSize)
            val songs = response.results.mapNotNull { it.toEntity() }
            val apiReturnedLessThanRequested = response.results.size < pageSize

            transactionRunner.runInTransaction {
                if (loadType == LoadType.REFRESH) {
                    searchCacheDao.clearSearchResults(query)
                    searchCacheDao.clearRemoteKey(query)
                    searchCacheDao.evictExpiredCache(CACHE_TTL_MILLIS)
                }

                val existingTrackIds = searchCacheDao.getTrackIdsForQuery(query).toSet()
                val newSongs = songs.filter { it.trackId !in existingTrackIds }

                if (loadType == LoadType.REFRESH) {
                    songDao.upsertSongs(songs)
                } else if (newSongs.isNotEmpty()) {
                    songDao.upsertSongs(newSongs)
                }

                val searchResults = newSongs.mapIndexed { index, song ->
                    SearchResultEntity(
                        query = query,
                        trackId = song.trackId,
                        position = offset + index,
                        cachedAt = System.currentTimeMillis()
                    )
                }
                if (searchResults.isNotEmpty()) {
                    searchCacheDao.insertSearchResults(searchResults)
                }

                searchCacheDao.insertRemoteKey(
                    RemoteKeyEntity(
                        query = query,
                        nextOffset = offset + response.results.size,
                        cachedAt = System.currentTimeMillis()
                    )
                )

                apiReturnedLessThanRequested || newSongs.isEmpty()
            }.let { endOfPagination ->
                MediatorResult.Success(endOfPaginationReached = endOfPagination)
            }
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    companion object {
        const val CACHE_TTL_MILLIS = 30 * 60 * 1000L // 30 minutes
    }
}
