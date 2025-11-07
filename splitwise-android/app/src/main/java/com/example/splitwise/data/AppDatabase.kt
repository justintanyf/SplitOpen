package com.example.splitwise.data

import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Database(entities = [Group::class, User::class, Expense::class, Debt::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun debtDao(): DebtDao
}

interface GroupDao {
    @Query("SELECT * FROM groups")
    fun getAllGroups(): Flow<List<Group>>

    @Insert
    suspend fun insertGroup(group: Group)
}

interface UserDao {
    @Query("SELECT * FROM users WHERE groupId = :groupId")
    fun getUsersForGroup(groupId: Int): Flow<List<User>>

    @Insert
    suspend fun insertUser(user: User)
}

interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE groupId = :groupId")
    fun getExpensesForGroup(groupId: Int): Flow<List<Expense>>

    @Insert
    suspend fun insertExpense(expense: Expense)
}

interface DebtDao {
    @Query("SELECT * FROM debts WHERE groupId = :groupId")
    fun getDebtsForGroup(groupId: Int): Flow<List<Debt>>

    @Insert
    suspend fun insertDebt(debt: Debt)
}