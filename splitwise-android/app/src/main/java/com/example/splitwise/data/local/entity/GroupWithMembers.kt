package com.example.splitwise.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class to hold a GroupEntity and its related list of GroupMemberEntity objects.
 * This is used for queries that join groups and group_members tables.
 */
data class GroupWithMembers(
    @Embedded val group: GroupEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    )
    val members: List<GroupMemberEntity>
)
