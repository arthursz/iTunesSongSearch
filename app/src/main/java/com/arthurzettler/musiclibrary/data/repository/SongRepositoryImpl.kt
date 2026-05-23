package com.arthurzettler.musiclibrary.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.arthurzettler.musiclibrary.data.local.DatabaseTransactionRunner
import com.arthurzettler.musiclibrary.data.local.dao.SearchCacheDao
import com.arthurzettler.musiclibrary.data.local.dao.SongDao
import com.arthurzettler.musiclibrary.data.mapper.toDomain
import com.arthurzettler.musiclibrary.data.mapper.toEntity
import com.arthurzettler.musiclibrary.data.remote.datasource.RemoteDataSource
import com.arthurzettler.musiclibrary.data.remote.paging.SongRemoteMediator
import com.arthurzettler.musiclibrary.di.IoDispatcher
import com.arthurzettler.musiclibrary.domain.model.Outcome
import com.arthurzettler.musiclibrary.domain.model.Song
import com.arthurzettler.musiclibrary.domain.repository.SongRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val transactionRunner: DatabaseTransactionRunner,
    private val songDao: SongDao,
    private val searchCacheDao: SearchCacheDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SongRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun searchSongsPaged(query: String): Flow<PagingData<Song>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            remoteMediator = SongRemoteMediator(
                query = query,
                remoteDataSource = remoteDataSource,
                transactionRunner = transactionRunner,
                songDao = songDao,
                searchCacheDao = searchCacheDao
            ),
            pagingSourceFactory = { searchCacheDao.pagingSource(query) }
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override fun getRecentlyPlayed(): Flow<List<Song>> {
        return songDao.getRecentlyPlayed().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override fun getAlbumSongs(collectionId: Long): Flow<Outcome<List<Song>>> = flow {
        val cached = songDao.getAlbumSongs(collectionId).map { it.toDomain() }
        if (cached.isNotEmpty()) {
            emit(Outcome.Stale(cached))
        }

        try {
            val response = remoteDataSource.getAlbumSongs(collectionId)
            val entities = response.results.mapNotNull { it.toEntity() }
            songDao.upsertSongs(entities)
            emit(Outcome.Success(entities.map { it.toDomain() }))
        } catch (e: Exception) {
            emit(Outcome.Error(e.message))
        }
    }.flowOn(ioDispatcher)

    override suspend fun markAsPlayed(song: Song) {
        withContext(ioDispatcher) {
            val playedAt = System.currentTimeMillis()
            songDao.insertSong(song.toEntity(lastPlayedAt = playedAt))
        }
    }

    companion object {
        private const val PAGE_SIZE = 50
        private const val PREFETCH_DISTANCE = 10
    }
}
