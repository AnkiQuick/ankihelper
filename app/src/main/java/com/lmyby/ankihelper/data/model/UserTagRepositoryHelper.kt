package com.lmyby.ankihelper.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

/**
 * Helper class to provide simpler access to UserTagRepository
 *
 * This class wraps the suspend functions in the repository for use in Java code
 * and synchronous contexts that don't have access to coroutine scope.
 */
class UserTagRepositoryHelper(private val repository: UserTagRepository) {

    /**
     * Callback interface for tag operations
     */
    interface TagsCallback {
        fun onSuccess(tags: List<UserTagEntity>)
        fun onError(error: Throwable)
    }

    /**
     * Callback interface for tag strings
     */
    interface TagStringsCallback {
        fun onSuccess(tagStrings: List<String>)
        fun onError(error: Throwable)
    }

    /**
     * Get all tags with callback
     * Non-blocking operation for use with lifecycle scope
     */
    fun getAllTags(callback: TagsCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tags = repository.getAllTags()
                callback.onSuccess(tags)
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    /**
     * Get all tags synchronously (blocking)
     * Use this when you need synchronous behavior (e.g., in onCreate)
     */
    fun getAllTagsBlocking(): List<UserTagEntity> {
        val latch = CountDownLatch(1)
        val result = AtomicReference<List<UserTagEntity>>()
        val error = AtomicReference<Throwable>()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                result.set(repository.getAllTags())
            } catch (e: Exception) {
                error.set(e)
            } finally {
                latch.countDown()
            }
        }

        latch.await()
        error.get()?.let { throw it }
        return result.get() ?: emptyList()
    }

    /**
     * Get all tag strings with callback
     * Non-blocking operation
     */
    fun getAllTagStrings(callback: TagStringsCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tagStrings = repository.getAllTagStrings()
                callback.onSuccess(tagStrings)
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    /**
     * Get all tag strings synchronously (blocking)
     * Use this when you need synchronous behavior
     */
    fun getAllTagStringsBlocking(): List<String> {
        val latch = CountDownLatch(1)
        val result = AtomicReference<List<String>>()
        val error = AtomicReference<Throwable>()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                result.set(repository.getAllTagStrings())
            } catch (e: Exception) {
                error.set(e)
            } finally {
                latch.countDown()
            }
        }

        latch.await()
        error.get()?.let { throw it }
        return result.get() ?: emptyList()
    }

    /**
     * Insert a tag in the background
     * Fire-and-forget operation
     */
    fun insertTag(tag: UserTagEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.insertTag(tag)
            } catch (e: Exception) {
                android.util.Log.e("UserTagRepoHelper", "Error inserting tag", e)
            }
        }
    }

    /**
     * Insert a tag by string value in the background
     * Fire-and-forget operation
     */
    fun insertTagString(tagValue: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.insertTagString(tagValue)
            } catch (e: Exception) {
                android.util.Log.e("UserTagRepoHelper", "Error inserting tag string", e)
            }
        }
    }

    /**
     * Insert multiple tags in the background
     * Fire-and-forget operation
     */
    fun insertTags(tags: List<UserTagEntity>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.insertTags(tags)
            } catch (e: Exception) {
                android.util.Log.e("UserTagRepoHelper", "Error inserting tags", e)
            }
        }
    }

    /**
     * Check if a tag exists synchronously (blocking)
     */
    fun tagExistsBlocking(tag: String): Boolean {
        val latch = CountDownLatch(1)
        val result = AtomicReference<Boolean>()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                result.set(repository.tagExists(tag))
            } catch (e: Exception) {
                result.set(false)
            } finally {
                latch.countDown()
            }
        }

        latch.await()
        return result.get() ?: false
    }

    /**
     * Delete a tag in the background
     * Fire-and-forget operation
     */
    fun deleteTag(tag: UserTagEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.deleteTag(tag)
            } catch (e: Exception) {
                android.util.Log.e("UserTagRepoHelper", "Error deleting tag", e)
            }
        }
    }

    /**
     * Delete a tag by value in the background
     * Fire-and-forget operation
     */
    fun deleteTagByValue(tagValue: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.deleteTagByValue(tagValue)
            } catch (e: Exception) {
                android.util.Log.e("UserTagRepoHelper", "Error deleting tag by value", e)
            }
        }
    }
}
