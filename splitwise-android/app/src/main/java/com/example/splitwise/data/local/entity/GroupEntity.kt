package com.example.splitwise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    val id: String, // UUID
    val name: String,
    val createdBy: String, // userId
    val createdAt: Long
)
