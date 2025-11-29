package com.example.splitwise.data.sync

import com.example.splitwise.data.user.UserIdManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Hybrid Logical Clocks (HLC) for P2P conflict resolution.
 * Ensures causal ordering even with clock skew.
 *
 * Based on the paper "Logical Physical Clocks and Consistent Snapshots in Globally Distributed Databases"
 */
@Singleton
class HybridClockManager @Inject constructor(
    private val userIdManager: UserIdManager
) {
    private val nodeId: String by lazy { userIdManager.getUserId() }
    private var lastTimestamp: HybridTimestamp = HybridTimestamp(0, 0, nodeId)

    /**
     * Generate a new timestamp for a local event.
     */
    @Synchronized
    fun generateTimestamp(): HybridTimestamp {
        val wallClock = System.currentTimeMillis()
        val last = lastTimestamp

        val newWallClock: Long
        val newCounter: Int

        if (wallClock > last.wallClock) {
            newWallClock = wallClock
            newCounter = 0
        } else {
            newWallClock = last.wallClock
            newCounter = last.logicalCounter + 1
        }

        val newTimestamp = HybridTimestamp(newWallClock, newCounter, nodeId)
        lastTimestamp = newTimestamp
        return newTimestamp
    }

    /**
     * Update local clock based on a received remote event's timestamp.
     */
    @Synchronized
    fun receiveTimestamp(remote: HybridTimestamp) {
        val wallClock = System.currentTimeMillis()
        val last = lastTimestamp

        val newWallClock = maxOf(wallClock, remote.wallClock, last.wallClock)
        val newCounter: Int

        when {
            // Clocks are in sync, increment max counter
            newWallClock == last.wallClock && newWallClock == remote.wallClock -> {
                newCounter = kotlin.math.max(last.logicalCounter, remote.logicalCounter) + 1
            }
            // Our clock was behind, take remote counter and increment
            newWallClock == remote.wallClock -> {
                newCounter = remote.logicalCounter + 1
            }
            // Remote was behind, increment our counter
            newWallClock == last.wallClock -> {
                newCounter = last.logicalCounter + 1
            }
            // Both were behind wall clock, reset counter
            else -> {
                newCounter = 0
            }
        }

        lastTimestamp = HybridTimestamp(newWallClock, newCounter, nodeId)
    }
}

/**
 * Represents a Hybrid Logical Clock timestamp.
 * @param wallClock Physical time (System.currentTimeMillis()).
 * @param logicalCounter Monotonic counter for events within the same millisecond.
 * @param nodeId Unique identifier for the node/device to act as a tie-breaker.
 */
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
