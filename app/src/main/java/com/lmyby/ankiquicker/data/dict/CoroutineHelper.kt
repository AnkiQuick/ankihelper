package com.lmyby.ankiquicker.data.dict

import android.database.Cursor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Helper class for Java-Kotlin coroutine interop.
 * Provides methods to execute suspend functions from Java code with callbacks.
 */
object CoroutineHelper {

    /**
     * Generic async executor for suspend functions
     * @param scope CoroutineScope to launch the coroutine in
     * @param block Suspend function to execute on IO dispatcher
     * @param onSuccess Callback with result on Main dispatcher
     * @param onError Callback with exception on Main dispatcher
     */
    @JvmStatic
    fun <T> executeAsync(
        scope: CoroutineScope,
        block: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Exception) -> Unit
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                val result = withContext(Dispatchers.IO) {
                    block()
                }
                onSuccess(result)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    /**
     * Blocking version for backward compatibility (use sparingly)
     * Runs the suspend function on IO dispatcher and blocks until complete
     * @param block Suspend function to execute
     * @return Result of the suspend function
     */
    @JvmStatic
    fun <T> executeBlocking(block: suspend () -> T): T {
        return kotlinx.coroutines.runBlocking(Dispatchers.IO) {
            block()
        }
    }

    /**
     * Execute a Cursor-returning DAO query asynchronously
     */
    @JvmStatic
    fun executeCursorAsync(
        scope: CoroutineScope,
        block: suspend () -> Cursor,
        onSuccess: (Cursor) -> Unit,
        onError: (Exception) -> Unit
    ) {
        executeAsync(scope, block, onSuccess, onError)
    }

    /**
     * Execute a Cursor-returning DAO query in blocking mode
     */
    @JvmStatic
    fun executeCursorBlocking(block: suspend () -> Cursor): Cursor {
        return executeBlocking(block)
    }
}

/**
 * Callback interface for async operations
 */
interface AsyncCallback<T> {
    fun onSuccess(result: T)
    fun onError(error: Exception)
}
