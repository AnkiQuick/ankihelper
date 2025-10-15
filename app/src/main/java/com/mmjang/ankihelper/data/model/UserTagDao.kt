package com.mmjang.ankihelper.data.model

import androidx.room.*

/**
 * DAO for UserTagEntity
 * Provides all CRUD operations for user tags using suspend functions
 */
@Dao
interface UserTagDao {

    /**
     * Get all user tags sorted alphabetically
     * @return List of all tags
     */
    @Query("SELECT * FROM usertag ORDER BY tag ASC")
    suspend fun getAllTags(): List<UserTagEntity>

    /**
     * Get a tag by name
     * @param tag The tag name
     * @return The tag or null if not found
     */
    @Query("SELECT * FROM usertag WHERE tag = :tag")
    suspend fun getTagByName(tag: String): UserTagEntity?

    /**
     * Search tags by partial match
     * @param query The search query
     * @return List of tags matching the query
     */
    @Query("SELECT * FROM usertag WHERE tag LIKE '%' || :query || '%' ORDER BY tag ASC")
    suspend fun searchTags(query: String): List<UserTagEntity>

    /**
     * Insert a new tag
     * @param tag The tag to insert
     * @return The row ID of the inserted tag
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: UserTagEntity): Long

    /**
     * Insert multiple tags
     * @param tags The tags to insert
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<UserTagEntity>)

    /**
     * Update an existing tag
     * @param tag The tag to update
     * @return Number of rows updated
     */
    @Update
    suspend fun updateTag(tag: UserTagEntity): Int

    /**
     * Delete a tag
     * @param tag The tag to delete
     * @return Number of rows deleted
     */
    @Delete
    suspend fun deleteTag(tag: UserTagEntity): Int

    /**
     * Delete a tag by name
     * @param tagName The name of the tag to delete
     * @return Number of rows deleted
     */
    @Query("DELETE FROM usertag WHERE tag = :tagName")
    suspend fun deleteTagByName(tagName: String): Int

    /**
     * Delete all tags
     * @return Number of rows deleted
     */
    @Query("DELETE FROM usertag")
    suspend fun deleteAllTags(): Int

    /**
     * Check if a tag exists
     * @param tag The tag name
     * @return True if tag exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM usertag WHERE tag = :tag)")
    suspend fun tagExists(tag: String): Boolean

    /**
     * Get count of all tags
     * @return Number of tags
     */
    @Query("SELECT COUNT(*) FROM usertag")
    suspend fun getTagCount(): Int
}
