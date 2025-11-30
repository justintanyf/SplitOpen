package com.example.splitwise.data.sync

import android.util.Log

import com.example.splitwise.data.local.dao.GroupDao
import com.example.splitwise.data.local.dao.GroupMemberDao
import com.example.splitwise.data.local.dao.ProcessedEventDao
import com.example.splitwise.data.local.dao.ExpenseDao
import com.example.splitwise.data.local.dao.ExpenseSplitDao
import com.example.splitwise.data.local.entity.GroupEntity
import com.example.splitwise.data.local.entity.GroupMemberEntity
import com.example.splitwise.data.local.entity.ProcessedEventEntity
import com.example.splitwise.data.local.entity.ExpenseEntity
import com.example.splitwise.data.local.entity.ExpenseSplitEntity
import com.example.splitwise.data.user.UserIdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized event processor that routes sync events to the appropriate handlers.
 * This eliminates circular dependencies between repositories.
 */
@Singleton
class SyncEventProcessor @Inject constructor(
    private val syncManager: SyncManager,
    private val groupDao: GroupDao,
    private val groupMemberDao: GroupMemberDao,
    private val expenseDao: ExpenseDao,
    private val expenseSplitDao: ExpenseSplitDao,
    private val processedEventDao: ProcessedEventDao,
    private val userIdManager: UserIdManager,
    @ApplicationScope private val scope: CoroutineScope
) {

    /**
     * Initialize sync listeners for all groups the user is a member of.
     * Should be called once during app startup.
     */
    fun initialize() {
        scope.launch {
            val userId = userIdManager.getUserId()
            groupMemberDao.getGroupIdsForUser(userId).collect { groupIds ->
                groupIds.forEach { groupId ->
                    startListeningToGroup(groupId)
                }
            }
        }
    }

    /**
     * Start listening to sync events for a specific group.
     */
    fun startListeningToGroup(groupId: String) {
        syncManager.startListening(groupId) { event ->
            scope.launch {
                processEvent(event)
            }
        }
    }

    /**
     * Process a sync event by routing to the appropriate handler.
     * Implements idempotency via ProcessedEventDao.
     */
    suspend fun processEvent(event: SyncEvent) {
        // Check idempotency
        if (processedEventDao.hasProcessed(event.id)) {
            return // Event already processed
        }

        // Route to appropriate handler
        try {
            when (event.type) {
                EventType.GROUP_CREATE -> processGroupCreateEvent(event)
                EventType.GROUP_EDIT -> processGroupEditEvent(event)
                EventType.EXPENSE_ADD -> processExpenseAddEvent(event)
                EventType.EXPENSE_EDIT -> processExpenseEditEvent(event)
                EventType.EXPENSE_DELETE -> processExpenseDeleteEvent(event)
            }

            // Mark as processed
            processedEventDao.markAsProcessed(
                ProcessedEventEntity(eventId = event.id, groupId = event.groupId)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process event. ID: ${event.id}, Type: ${event.type}, Group: ${event.groupId}", e)
            // Not marking as processed, will retry on next sync
        }
    }

    private suspend fun processGroupCreateEvent(event: SyncEvent) {
        val groupName = event.data["name"] ?: "Unnamed Group"
        
        val group = GroupEntity(
            id = event.groupId,
            name = groupName,
            createdBy = event.userId,
            createdAt = event.timestamp
        )
        groupDao.insert(group)

        val member = GroupMemberEntity(
            groupId = event.groupId,
            userId = event.userId,
            displayName = userIdManager.getDisplayName(),
            joinedAt = event.timestamp
        )
        groupMemberDao.insert(member)
    }

    private suspend fun processGroupEditEvent(event: SyncEvent) {
        // TODO: Implement group edit logic
    }

    private suspend fun processExpenseAddEvent(event: SyncEvent) {
        val expenseId = event.data["expenseId"] ?: return
        val description = event.data["description"] ?: ""
        val amount = event.data["amount"]?.toDoubleOrNull() ?: 0.0
        val paidBy = event.data["paidBy"] ?: return
        val splitWith = event.data["splitWith"]?.split(",") ?: return

        if (splitWith.isEmpty()) return

        val expense = ExpenseEntity(
            id = expenseId,
            groupId = event.groupId,
            description = description,
            amount = amount,
            paidBy = paidBy,
            createdAt = event.timestamp,
            createdBy = event.userId,
            lastModifiedAt = event.timestamp,
            lastModifiedBy = event.userId
        )
        expenseDao.insert(expense)

        // Distribute splits with remainder to avoid rounding errors
        val splits = distributeSplitAmounts(amount, splitWith).mapIndexed { index, shareAmount ->
            ExpenseSplitEntity(
                id = UUID.randomUUID().toString(),
                expenseId = expenseId,
                userId = splitWith[index],
                shareAmount = shareAmount,
                isPayer = splitWith[index] == paidBy
            )
        }
        expenseSplitDao.insertAll(splits)
    }

    private suspend fun processExpenseEditEvent(event: SyncEvent) {
        // TODO: Implement expense edit logic
    }

    private suspend fun processExpenseDeleteEvent(event: SyncEvent) {
        // TODO: Implement expense delete logic (soft delete)
    }

    /**
     * Distribute an amount evenly among participants, allocating the remainder to the first N participants.
     * This ensures: splits.sum() == amount (no rounding errors).
     *
     * Example: $10.00 / 3 = [3.34, 3.33, 3.33] (not [3.33, 3.33, 3.33] which loses a cent)
     */
    private fun distributeSplitAmounts(amount: Double, participants: List<String>): List<Double> {
        val count = participants.size
        if (count == 0) return emptyList()

        // Convert to cents to avoid floating-point issues
        val totalCents = (amount * 100).toLong()
        val baseCents = totalCents / count
        val remainderCents = (totalCents % count).toInt()

        return List(count) { index ->
            val cents = if (index < remainderCents) baseCents + 1 else baseCents
            cents / 100.0
        }
    }

    companion object {
        private const val TAG = "SyncEventProcessor"
    }
}

/**
 * Qualifier for application-scoped coroutine scope.
 */
@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
