package com.example.splitwise.di

import com.example.splitwise.data.sync.P2PSyncManager
import com.example.splitwise.data.sync.SyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class P2PSyncModule {
    @Binds
    abstract fun bindSyncManager(impl: P2PSyncManager): SyncManager
}
