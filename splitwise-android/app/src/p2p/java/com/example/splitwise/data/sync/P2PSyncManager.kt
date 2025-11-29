package com.example.splitwise.data.sync

import android.content.Context
import com.example.splitwise.data.user.UserIdManager
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class P2PSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userIdManager: UserIdManager,
    private val hybridClockManager: HybridClockManager
) : SyncManager {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Disconnected)
    override val syncState: StateFlow<SyncState> = _syncState

    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val connectedEndpoints = mutableSetOf<String>()

    private val strategy = Strategy.P2P_CLUSTER
    private val serviceId = "com.example.splitwise.p2p"

    private val onEventCallbacks = mutableMapOf<String, (SyncEvent) -> Unit>()

    override suspend fun initialize() {
        // No-op for P2P, connections are on-demand
    }

    override suspend fun getCurrentUserId(): String {
        return userIdManager.getUserId()
    }

    override suspend fun createGroup(groupId: String, name: String): Result<Unit> {
        _syncState.value = SyncState.Advertising
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        return try {
            connectionsClient.startAdvertising(
                userIdManager.getDisplayName(),
                serviceId,
                connectionLifecycleCallback,
                advertisingOptions
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error("Advertising failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun joinGroup(groupCode: String): Result<Unit> {
        _syncState.value = SyncState.Discovering
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
        return try {
            connectionsClient.startDiscovery(
                serviceId,
                endpointDiscoveryCallback,
                discoveryOptions
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error("Discovery failed: ${e.message}")
            Result.failure(e)
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            _syncState.value = SyncState.Authenticating(info.endpointName)
            // In a real app, this would trigger a UI dialog. For this POC, we auto-accept.
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectedEndpoints.add(endpointId)
                _syncState.value = SyncState.UpToDate(System.currentTimeMillis())
            } else {
                _syncState.value = SyncState.Error("Connection to endpoint $endpointId failed")
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedEndpoints.remove(endpointId)
            if (connectedEndpoints.isEmpty()) {
                _syncState.value = SyncState.Disconnected
            }
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            if(info.serviceId == serviceId){
                 connectionsClient.requestConnection(
                    userIdManager.getDisplayName(),
                    endpointId,
                    connectionLifecycleCallback
                )
            }
        }

        override fun onEndpointLost(endpointId: String) {
            // Can be useful for UI to show a peer is no longer available
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val eventJson = payload.asBytes()?.toString(Charsets.UTF_8) ?: return
                try {
                    val event = Json.decodeFromString<SyncEvent>(eventJson)
                    event.hybridTimestamp?.let { hybridClockManager.receiveTimestamp(it) }
                    onEventCallbacks[event.groupId]?.invoke(event)
                } catch (e: Exception) {
                    // Log error, bad payload
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Can be used for progress bars on large transfers
        }
    }

    override suspend fun pushEvent(groupId: String, event: SyncEvent): Result<Unit> {
        return try {
            val timestamp = hybridClockManager.generateTimestamp()
            val eventWithTimestamp = event.copy(
                timestamp = timestamp.wallClock,
                hybridTimestamp = timestamp
            )
            val payload = Payload.fromBytes(Json.encodeToString(eventWithTimestamp).toByteArray())

            if (connectedEndpoints.isEmpty()) {
                 return Result.failure(Exception("No connected peers to push event to."))
            }

            connectedEndpoints.forEach { endpointId ->
                connectionsClient.sendPayload(endpointId, payload)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun startListening(groupId: String, onEvent: (SyncEvent) -> Unit) {
        onEventCallbacks[groupId] = onEvent
    }

    override fun stopListening(groupId: String) {
        onEventCallbacks.remove(groupId)
    }

    override suspend fun disconnect() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        connectedEndpoints.clear()
        _syncState.value = SyncState.Disconnected
    }
}

