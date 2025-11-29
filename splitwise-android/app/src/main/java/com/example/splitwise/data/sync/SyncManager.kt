package com.example.splitwise.data.sync

import kotlinx.coroutines.flow.StateFlow

/**
 * Abstraction for sync implementations.
 * Implementations: P2PSyncManager (Nearby), FirebaseSyncManager (RTDB).
 */
interface SyncManager {
    /**
     * Initialize the sync manager.
     * For Firebase: setup auth
     * For P2P: prepare Nearby Connections
     */
    suspend fun initialize()
    
    /**
     * Get the current user's ID.
     * Must be persistent across app restarts.
     */
    suspend fun getCurrentUserId(): String
    
    /**
     * Create a new group and start advertising/publishing.
     */
    suspend fun createGroup(groupId: String, name: String): Result<Unit>
    
    /**
     * Join an existing group.
     * Firebase: use group code
     * P2P: connect to nearby device
     */
    suspend fun joinGroup(groupCode: String): Result<Unit>
    
    /**
     * Push a sync event to remote.
     * Handles retry logic and queueing.
     */
    suspend fun pushEvent(groupId: String, event: SyncEvent): Result<Unit>
    
    /**
     * Start listening for events from a group.
     * Callback is invoked for each received event.
     */
    fun startListening(groupId: String, onEvent: (SyncEvent) -> Unit)
    
    /**
     * Stop listening for a group.
     */
    fun stopListening(groupId: String)
    
    /**
     * Observable sync state for UI.
     */
    val syncState: StateFlow<SyncState>
    
    /**
     * Disconnect and cleanup.
     */
    suspend fun disconnect()
}
