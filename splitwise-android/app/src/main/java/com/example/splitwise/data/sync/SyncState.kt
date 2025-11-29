package com.example.splitwise.data.sync

/**
 * Granular sync states for better UX.
 */
sealed class SyncState {
    object Disconnected : SyncState()
    object Advertising : SyncState() // P2P only: broadcasting group
    object Discovering : SyncState() // P2P only: searching for peers
    data class Authenticating(val deviceName: String) : SyncState() // P2P connection approval
    data class Syncing(val progress: Int, val total: Int) : SyncState()
    data class UpToDate(val lastSyncTime: Long) : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * UI-friendly sync status message.
 */
fun SyncState.toDisplayMessage(): String = when (this) {
    is SyncState.Disconnected -> "Not connected"
    is SyncState.Advertising -> "Waiting for nearby devices..."
    is SyncState.Discovering -> "Looking for nearby groups..."
    is SyncState.Authenticating -> "Device '$deviceName' requesting to join"
    is SyncState.Syncing -> "Syncing $progress/$total events..."
    is SyncState.UpToDate -> {
        val minutesAgo = (System.currentTimeMillis() - lastSyncTime) / 60000
        "All synced ✓ ${if (minutesAgo > 0) "$minutesAgo minutes ago" else "just now"}"
    }
    is SyncState.Error -> "Error: $message"
}
