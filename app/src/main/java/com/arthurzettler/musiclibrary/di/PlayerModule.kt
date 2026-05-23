package com.arthurzettler.musiclibrary.di

import com.arthurzettler.musiclibrary.data.playback.ExoPlayerAudioPlayer
import com.arthurzettler.musiclibrary.domain.playback.AudioPlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindAudioPlayer(impl: ExoPlayerAudioPlayer): AudioPlayer
}
