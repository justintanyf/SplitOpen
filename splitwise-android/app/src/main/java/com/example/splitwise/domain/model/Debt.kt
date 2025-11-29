package com.example.splitwise.domain.model

/**
 * Domain model for debt between two users.
 */
data class Debt(
    val id: String,
    val groupId: String,
    val fromUserId: String,
    val fromUserName: String,
    val toUserId: String,
    val toUserName: String,
    val amount: Double,
    val isSettled: Boolean = false
)
