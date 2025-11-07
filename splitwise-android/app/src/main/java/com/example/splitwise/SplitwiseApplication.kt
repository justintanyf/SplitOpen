package com.example.splitwise

import android.app.Application
import androidx.room.Room
import com.example.splitwise.data.AppDatabase
import com.example.splitwise.data.SplitwiseRepository

class SplitwiseApplication : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database: AppDatabase by lazy { Room.databaseBuilder(this, AppDatabase::class.java, "splitwise.db").build() }
    val repository: SplitwiseRepository by lazy { SplitwiseRepository(database.groupDao(), database.userDao(), database.expenseDao(), database.debtDao()) }
}
