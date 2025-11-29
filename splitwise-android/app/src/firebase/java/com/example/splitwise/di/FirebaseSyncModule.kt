package com.example.splitwise.di

import com.example.splitwise.data.sync.FirebaseSyncManager
import com.example.splitwise.data.sync.SyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseSyncModule {
    @Binds
    abstract fun bindSyncManager(impl: FirebaseSyncManager): SyncManager
}
