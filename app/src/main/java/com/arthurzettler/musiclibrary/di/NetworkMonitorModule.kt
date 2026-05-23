package com.arthurzettler.musiclibrary.di

import com.arthurzettler.musiclibrary.data.network.ConnectivityNetworkMonitor
import com.arthurzettler.musiclibrary.domain.model.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkMonitorModule {

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor
}
