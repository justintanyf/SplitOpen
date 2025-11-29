package com.example.splitwise.data.local.dao

import androidx.room.*
import com.example.splitwise.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Transaction
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getExpensesForGroup(groupId: String): Flow<List<com.example.splitwise.data.local.entity.ExpenseWithSplits>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: String): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getAllExpensesForGroup(groupId: String): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("UPDATE expenses SET isDeleted = 1, lastModifiedAt = :timestamp WHERE id = :expenseId")
    suspend fun markAsDeleted(expenseId: String, timestamp: Long)
}
