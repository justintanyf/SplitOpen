package com.example.splitwise.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.splitwise.data.local.dao.*
import com.example.splitwise.data.local.entity.*

@Database(
    entities = [
        GroupEntity::class,
        GroupMemberEntity::class,
        ExpenseEntity::class,
        ExpenseSplitEntity::class,
        DebtEntity::class,
        ProcessedEventEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun expenseSplitDao(): ExpenseSplitDao
    abstract fun debtDao(): DebtDao
    abstract fun processedEventDao(): ProcessedEventDao
}
