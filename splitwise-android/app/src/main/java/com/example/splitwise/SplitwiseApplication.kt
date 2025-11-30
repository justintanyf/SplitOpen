package com.example.splitwise

import android.app.Application
import com.example.splitwise.data.sync.SyncEventProcessor
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SplitwiseApplication : Application() {
    
    @Inject
    lateinit var syncEventProcessor: SyncEventProcessor
    
    override fun onCreate() {
        super.onCreate()
        // Initialize sync listeners for all groups the user is a member of
        syncEventProcessor.initialize()
    }
}
