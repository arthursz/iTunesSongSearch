package com.arthurzettler.musiclibrary.data.repository

import com.arthurzettler.musiclibrary.data.local.dao.NowPlayingDao
import com.arthurzettler.musiclibrary.data.local.entity.NowPlayingEntity
import com.arthurzettler.musiclibrary.data.mapper.toDomain
import com.arthurzettler.musiclibrary.di.IoDispatcher
import com.arthurzettler.musiclibrary.domain.model.Song
import com.arthurzettler.musiclibrary.domain.repository.PlaybackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaybackRepositoryImpl @Inject constructor(
    private val nowPlayingDao: NowPlayingDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PlaybackRepository {

    override suspend fun setPlaylist(songs: List<Song>, startIndex: Int) {
        withContext(ioDispatcher) {
            val entities = songs.mapIndexed { index, song ->
                NowPlayingEntity(
                    position = index,
                    trackId = song.trackId,
                    trackName = song.trackName,
                    artistName = song.artistName,
                    collectionName = song.collectionName,
                    artworkUrl = song.artworkUrl,
                    previewUrl = song.previewUrl,
                    trackTimeMillis = song.trackTimeMillis,
                    collectionId = song.collectionId,
                    trackNumber = song.trackNumber,
                    isCurrentTrack = index == startIndex
                )
            }
            nowPlayingDao.setPlaylist(entities, startIndex)
        }
    }

    override suspend fun getPlaylist(): List<Song> {
        return withContext(ioDispatcher) {
            nowPlayingDao.getPlaylist().map { it.toDomain() }
        }
    }

    override suspend fun getCurrentIndex(): Int? {
        return withContext(ioDispatcher) {
            nowPlayingDao.getCurrentTrack()?.position
        }
    }

    override suspend fun setCurrentIndex(index: Int) {
        withContext(ioDispatcher) {
            nowPlayingDao.setCurrentIndex(index)
        }
    }


}
