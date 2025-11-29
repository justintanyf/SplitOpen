package com.example.splitwise.data.sync

import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncManager @Inject constructor(
    private val authManager: FirebaseAuthManager,
    private val database: FirebaseDatabase
) : SyncManager {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Disconnected)
    override val syncState: StateFlow<SyncState> = _syncState

    private val eventListeners = mutableMapOf<String, ChildEventListener>()

    override suspend fun initialize() {
        _syncState.value = SyncState.Authenticating("Firebase")
        try {
            authManager.ensureAuthenticated()
            _syncState.value = SyncState.UpToDate(System.currentTimeMillis())
        } catch (e: Exception) {
            _syncState.value = SyncState.Error("Firebase auth failed: ${e.message}")
        }
    }

    override suspend fun getCurrentUserId(): String {
        return authManager.ensureAuthenticated()
    }

    override suspend fun createGroup(groupId: String, name: String): Result<Unit> {
        return try {
            val groupMeta = mapOf(
                "name" to name,
                "createdBy" to getCurrentUserId(),
                "createdAt" to ServerValue.TIMESTAMP
            )
            database.getReference("groups/$groupId/metadata").setValue(groupMeta).await()
            // Add creator to members list
            val userId = getCurrentUserId()
            database.getReference("groups/$groupId/members/$userId").setValue(true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinGroup(groupCode: String): Result<Unit> {
        return try {
            val snapshot = database.getReference("groups/$groupCode/metadata").get().await()
            if (snapshot.exists()) {
                // Add current user to members list
                val userId = getCurrentUserId()
                database.getReference("groups/$groupCode/members/$userId").setValue(true).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Group '$groupCode' not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pushEvent(groupId: String, event: SyncEvent): Result<Unit> {
        return try {
            _syncState.value = SyncState.Syncing(1, 1)
            val eventMap = event.toMap()
            database.getReference("groups/$groupId/events").child(event.id).setValue(eventMap).await()
            _syncState.value = SyncState.UpToDate(System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error("Event push failed: ${e.message}")
            Result.failure(e)
        }
    }

    private fun SyncEvent.toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "type" to type.name,
            "userId" to userId,
            "groupId" to groupId,
            "data" to data,
            "timestamp" to ServerValue.TIMESTAMP, // Let Firebase set the timestamp
            "hybridTimestamp" to null // Not used in Firebase variant
        )
    }

    override fun startListening(groupId: String, onEvent: (SyncEvent) -> Unit) {
        val eventsRef = database.getReference("groups/$groupId/events")
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.toSyncEvent()?.let(onEvent)
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Events are immutable, but could handle edits if needed
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle event removal if needed (e.g. for retracted events)
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                _syncState.value = SyncState.Error("Sync error: ${error.message}")
            }
        }
        // Only listen for events added from now on to avoid re-processing history
        eventsRef.orderByChild("timestamp").startAt(System.currentTimeMillis().toDouble())
            .addChildEventListener(listener)
        eventListeners[groupId] = listener
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun DataSnapshot.toSyncEvent(): SyncEvent? {
        return try {
            val map = value as? Map<String, Any> ?: return null
            SyncEvent(
                id = map["id"] as String,
                type = EventType.valueOf(map["type"] as String),
                userId = map["userId"] as String,
                groupId = map["groupId"] as String,
                data = map["data"] as Map<String, String>,
                timestamp = map["timestamp"] as Long
            )
        } catch (e: Exception) {
            // Log error, incompatible event structure
            null
        }
    }

    override fun stopListening(groupId: String) {
        eventListeners.remove(groupId)?.let { listener ->
            database.getReference("groups/$groupId/events").removeEventListener(listener)
        }
    }

    override suspend fun disconnect() {
        // No explicit disconnect needed for RTDB, but we can clear listeners
        eventListeners.keys.forEach(::stopListening)
        eventListeners.clear()
        // Signing out is a bigger action, might not be desired on disconnect.
        // auth.signOut() 
        _syncState.value = SyncState.Disconnected
    }
}
