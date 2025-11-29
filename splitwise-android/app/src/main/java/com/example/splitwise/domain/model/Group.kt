package com.example.splitwise.domain.model

/**
 * Domain model for a group.
 * Clean representation without Room annotations.
 */
data class Group(
    val id: String,
    val name: String,
    val createdBy: String,
    val createdAt: Long,
    val members: List<GroupMember> = emptyList()
)

data class GroupMember(
    val userId: String,
    val displayName: String,
    val joinedAt: Long
)
