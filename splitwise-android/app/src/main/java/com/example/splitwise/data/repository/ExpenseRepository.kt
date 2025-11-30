package com.example.splitwise.data.repository

import com.example.splitwise.data.local.dao.ExpenseDao
import com.example.splitwise.data.local.dao.ExpenseSplitDao
import com.example.splitwise.data.local.entity.ExpenseSplitEntity
import com.example.splitwise.data.sync.EventType
import com.example.splitwise.data.sync.SyncEvent
import com.example.splitwise.data.sync.SyncManager
import com.example.splitwise.data.user.UserIdManager
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val syncManager: SyncManager,
    private val expenseDao: ExpenseDao,
    private val expenseSplitDao: ExpenseSplitDao,
    private val userIdManager: UserIdManager
) {
    fun getExpensesForGroup(groupId: String): kotlinx.coroutines.flow.Flow<List<com.example.splitwise.domain.model.Expense>> {
        return expenseDao.getExpensesForGroup(groupId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        paidByUserId: String,
        splitWithUserIds: List<String>
    ): Result<String> {
        val expenseId = UUID.randomUUID().toString()
        val event = SyncEvent(
            id = UUID.randomUUID().toString(),
            type = EventType.EXPENSE_ADD,
            userId = userIdManager.getUserId(),
            groupId = groupId,
            data = mapOf(
                "expenseId" to expenseId,
                "description" to description,
                "amount" to amount.toString(),
                "paidBy" to paidByUserId,
                "splitWith" to splitWithUserIds.joinToString(",")
            ),
            timestamp = System.currentTimeMillis()
        )

        // Process locally for immediate UI update is now handled by SyncEventProcessor
        
        // Push event to sync
        val pushResult = syncManager.pushEvent(groupId, event)
        if (pushResult.isFailure) {
            return Result.failure(pushResult.exceptionOrNull() ?: Exception("Failed to push expense event"))
        }

        return Result.success(expenseId)
    }

}

private fun com.example.splitwise.data.local.entity.ExpenseWithSplits.toDomainModel(): com.example.splitwise.domain.model.Expense {
    return com.example.splitwise.domain.model.Expense(
        id = this.expense.id,
        groupId = this.expense.groupId,
        description = this.expense.description,
        amount = this.expense.amount,
        paidBy = this.expense.paidBy,
        createdAt = this.expense.createdAt,
        lastModifiedAt = this.expense.lastModifiedAt,
        isDeleted = this.expense.isDeleted,
        splits = this.splits.map { it.toDomainModel() }
    )
}

private fun ExpenseSplitEntity.toDomainModel(): com.example.splitwise.domain.model.ExpenseSplit {
    return com.example.splitwise.domain.model.ExpenseSplit(
        id = this.id,
        expenseId = this.expenseId,
        userId = this.userId,
        shareAmount = this.shareAmount,
        isPayer = this.isPayer
    )
}
