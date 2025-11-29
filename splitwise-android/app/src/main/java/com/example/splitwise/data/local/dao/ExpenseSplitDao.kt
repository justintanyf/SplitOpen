package com.example.splitwise.data.local.dao

import androidx.room.*
import com.example.splitwise.data.local.entity.ExpenseSplitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseSplitDao {
    @Query("SELECT * FROM expense_splits WHERE expenseId = :expenseId")
    fun getSplitsForExpense(expenseId: String): Flow<List<ExpenseSplitEntity>>

    @Query("SELECT * FROM expense_splits WHERE userId = :userId")
    fun getSplitsForUser(userId: String): Flow<List<ExpenseSplitEntity>>

    @Query("SELECT * FROM expense_splits WHERE expenseId = :expenseId AND userId = :userId")
    suspend fun getSplit(expenseId: String, userId: String): ExpenseSplitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(split: ExpenseSplitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(splits: List<ExpenseSplitEntity>)

    @Query("DELETE FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun deleteSplitsForExpense(expenseId: String)

    @Query("DELETE FROM expense_splits WHERE id = :splitId")
    suspend fun deleteById(splitId: String)
}
