package com.example.splitwise.data.local.dao

import androidx.room.*
import com.example.splitwise.data.local.entity.GroupMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupMemberDao {
    @Query("SELECT * FROM group_members WHERE groupId = :groupId ORDER BY joinedAt")
    fun getMembersForGroup(groupId: String): Flow<List<GroupMemberEntity>>

    @Query("SELECT groupId FROM group_members WHERE userId = :userId")
    fun getGroupIdsForUser(userId: String): Flow<List<String>>

    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun getMember(groupId: String, userId: String): GroupMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: GroupMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<GroupMemberEntity>)

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun deleteMember(groupId: String, userId: String)
}
