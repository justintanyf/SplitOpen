package com.example.splitwise.data.sync

import kotlinx.serialization.Serializable
import java.util.UUID

// Moved from p2p source set to solve dependency issue.
@Serializable
data class HybridTimestamp(
    val wallClock: Long,
    val logicalCounter: Int,
    val nodeId: String
) : Comparable<HybridTimestamp> {
    override fun compareTo(other: HybridTimestamp): Int {
        return when {
            wallClock != other.wallClock -> wallClock.compareTo(other.wallClock)
            logicalCounter != other.logicalCounter -> logicalCounter.compareTo(other.logicalCounter)
            else -> nodeId.compareTo(other.nodeId)
        }
    }
}

@Serializable
enum class EventType {
    GROUP_CREATE,
    GROUP_EDIT,
    EXPENSE_ADD,
    EXPENSE_EDIT,
    EXPENSE_DELETE
}

@Serializable
data class SyncEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: EventType,
    val userId: String,
    val groupId: String,
    val data: Map<String, String>,
    val timestamp: Long = 0, // Used by Firebase as server timestamp, and as wall clock for P2P
    val hybridTimestamp: HybridTimestamp? = null // Used by P2P for full conflict resolution
)
