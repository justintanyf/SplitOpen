package com.example.splitwise.viewmodel

import androidx.lifecycle.ViewModel
import com.example.splitwise.data.sync.SyncManager
import com.example.splitwise.data.sync.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncManager: SyncManager
) : ViewModel() {
    val syncState: StateFlow<SyncState> = syncManager.syncState
}
