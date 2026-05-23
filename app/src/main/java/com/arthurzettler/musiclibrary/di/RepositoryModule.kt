package com.arthurzettler.musiclibrary.di

import com.arthurzettler.musiclibrary.data.repository.PlaybackRepositoryImpl
import com.arthurzettler.musiclibrary.data.repository.SongRepositoryImpl
import com.arthurzettler.musiclibrary.domain.repository.PlaybackRepository
import com.arthurzettler.musiclibrary.domain.repository.SongRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSongRepository(impl: SongRepositoryImpl): SongRepository

    @Binds
    @Singleton
    abstract fun bindPlaybackRepository(impl: PlaybackRepositoryImpl): PlaybackRepository
}
