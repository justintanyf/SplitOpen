package com.example.splitwise.data.local.dao

import androidx.room.*
import com.example.splitwise.data.local.entity.DebtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts WHERE groupId = :groupId AND isSettled = 0")
    fun getDebtsForGroup(groupId: String): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE groupId = :groupId")
    fun getAllDebtsForGroup(groupId: String): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE fromUserId = :userId OR toUserId = :userId")
    fun getDebtsForUser(userId: String): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debt: DebtEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(debts: List<DebtEntity>)

    @Query("DELETE FROM debts WHERE groupId = :groupId")
    suspend fun deleteDebtsForGroup(groupId: String)

    @Query("UPDATE debts SET isSettled = 1 WHERE id = :debtId")
    suspend fun markAsSettled(debtId: String)
}
