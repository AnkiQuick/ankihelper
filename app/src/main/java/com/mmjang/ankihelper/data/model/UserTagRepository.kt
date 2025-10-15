package com.mmjang.ankihelper.data.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for UserTag operations
 *
 * Provides an abstraction layer for tag-related database operations,
 * including querying, inserting, and managing user-defined tags.
 */
class UserTagRepository(private val userTagDao: UserTagDao) {

    /**
     * Get all tags, sorted alphabetically
     * @return List of all UserTagEntity objects
     */
    suspend fun getAllTags(): List<UserTagEntity> = withContext(Dispatchers.IO) {
        userTagDao.getAllTags()
    }

    /**
     * Get all tag strings (just the tag values, not entities)
     * Useful for displaying tag lists or autocomplete
     * @return List of tag strings
     */
    suspend fun getAllTagStrings(): List<String> = withContext(Dispatchers.IO) {
        userTagDao.getAllTagStrings()
    }

    /**
     * Search tags by query
     * @param query The search query (partial match)
     * @return List of UserTagEntity objects with tags containing the query
     */
    suspend fun searchTags(query: String): List<UserTagEntity> = withContext(Dispatchers.IO) {
        userTagDao.searchTags(query)
    }

    /**
     * Get a specific tag by its value
     * @param tag The tag value to search for
     * @return The UserTagEntity if found, null otherwise
     */
    suspend fun getTag(tag: String): UserTagEntity? = withContext(Dispatchers.IO) {
        userTagDao.getTagByName(tag)
    }

    /**
     * Check if a tag exists
     * @param tag The tag value to check
     * @return true if the tag exists, false otherwise
     */
    suspend fun tagExists(tag: String): Boolean = withContext(Dispatchers.IO) {
        userTagDao.tagExists(tag)
    }

    /**
     * Insert a new tag
     * Uses IGNORE conflict strategy, so duplicates will be ignored
     * @param tag The UserTagEntity to insert
     * @return The row ID of the inserted tag, or -1 if already exists
     */
    suspend fun insertTag(tag: UserTagEntity): Long = withContext(Dispatchers.IO) {
        userTagDao.insertTag(tag)
    }

    /**
     * Insert a new tag by string value
     * Convenience method that creates a UserTagEntity from a string
     * @param tagValue The tag string to insert
     * @return The row ID of the inserted tag, or -1 if already exists
     */
    suspend fun insertTagString(tagValue: String): Long = withContext(Dispatchers.IO) {
        userTagDao.insertTag(UserTagEntity(tagValue))
    }

    /**
     * Insert multiple tags
     * @param tags List of UserTagEntity objects to insert
     */
    suspend fun insertTags(tags: List<UserTagEntity>) = withContext(Dispatchers.IO) {
        userTagDao.insertTags(tags)
    }

    /**
     * Delete a tag
     * @param tag The UserTagEntity to delete
     * @return The number of rows deleted
     */
    suspend fun deleteTag(tag: UserTagEntity): Int = withContext(Dispatchers.IO) {
        userTagDao.deleteTag(tag)
    }

    /**
     * Delete a tag by its value
     * @param tagValue The tag string to delete
     * @return The number of rows deleted
     */
    suspend fun deleteTagByValue(tagValue: String): Int = withContext(Dispatchers.IO) {
        userTagDao.deleteTagByName(tagValue)
    }

    /**
     * Delete all tags
     * @return The number of rows deleted
     */
    suspend fun deleteAllTags(): Int = withContext(Dispatchers.IO) {
        userTagDao.deleteAllTags()
    }

    /**
     * Get the count of all tags
     * @return The number of tags in the database
     */
    suspend fun getTagCount(): Int = withContext(Dispatchers.IO) {
        userTagDao.getTagCount()
    }
}
