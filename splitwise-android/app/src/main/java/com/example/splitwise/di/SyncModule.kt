package com.example.splitwise.di

import com.example.splitwise.data.sync.SyncManager
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Abstract module for binding SyncManager.
 * Implementations provided in flavor-specific source sets.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    // Abstract bind method to be implemented by flavor-specific modules
    // abstract fun bindSyncManager(impl: SyncManagerImpl): SyncManager
}
