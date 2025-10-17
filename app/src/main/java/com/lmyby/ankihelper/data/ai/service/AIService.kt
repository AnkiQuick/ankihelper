package com.lmyby.ankihelper.data.ai.service

import android.util.Log
import com.lmyby.ankihelper.data.ai.AIErrorType
import com.lmyby.ankihelper.data.ai.AIException
import com.lmyby.ankihelper.data.ai.EncryptionUtil
import com.lmyby.ankihelper.data.ai.LLMConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
open class AIService(timeoutSeconds: Int = DEFAULT_TIMEOUT_SECONDS) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
        .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
        .writeTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
        .build()

    /**
     * Calls the LLM API with a single prompt (backward compatibility)
     *
     * @param config LLM configuration
     * @param prompt User prompt
     * @return API response as string
     * @throws IOException if network error occurs
     * @throws AIException if timeout or API error occurs
     */
    @Throws(IOException::class, AIException::class)
    fun callLLM(config: LLMConfig, prompt: String): String {
        return callLLM(config, "You are a helpful assistant.", prompt)
    }

    /**
     * Calls the LLM API with system and user messages
     *
     * @param config LLM configuration
     * @param systemMessage System role message
     * @param userMessage User role message
     * @return API response as string
     * @throws IOException if network error occurs
     * @throws AIException if timeout or API error occurs
     */
    @Throws(IOException::class, AIException::class)
    fun callLLM(config: LLMConfig, systemMessage: String, userMessage: String): String {
        return callLLMWithRetry(config, systemMessage, userMessage, MAX_RETRIES)
    }

    /**
     * Calls the LLM API with retry mechanism
     *
     * @param config LLM configuration
     * @param systemMessage System role message
     * @param userMessage User role message
     * @param maxRetries Maximum number of retries
     * @return API response as string
     * @throws IOException if network error occurs
     * @throws AIException if timeout or API error occurs
     */
    @Throws(IOException::class, AIException::class)
    fun callLLMWithRetry(
        config: LLMConfig,
        systemMessage: String,
        userMessage: String,
        maxRetries: Int
    ): String {
        var apiToken = config.apiToken
        if (!apiToken.isNullOrEmpty()) {
            apiToken = EncryptionUtil.decrypt(apiToken)
        }

        // Create messages array
        val messages = JSONArray()

        try {
            // Add system message
            val systemMsg = JSONObject().apply {
                put("role", "system")
                put("content", systemMessage)
            }
            messages.put(systemMsg)

            // Add user message
            val userMsg = JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            }
            messages.put(userMsg)
        } catch (e: JSONException) {
            Log.e(TAG, "Error creating JSON messages", e)
            throw AIException(AIErrorType.INVALID_RESPONSE, "Error creating request messages", e)
        }

        // Create the request body
        val jsonBody = JSONObject()
        try {
            jsonBody.put("model", config.modelName)
            jsonBody.put("messages", messages)
            jsonBody.put("temperature", 0.3)
            jsonBody.put("max_tokens", 1000)

            // Add response format for structured output (now default for all providers)
            // This ensures consistent behavior across all LLM providers
            val responseFormat = JSONObject().apply {
                put("type", "json_object")
            }
            jsonBody.put("response_format", responseFormat)
            Log.d(TAG, "Added response_format parameter for structured output")
        } catch (e: JSONException) {
            Log.e(TAG, "Error creating JSON body", e)
            throw AIException(AIErrorType.INVALID_RESPONSE, "Error creating request body", e)
        }

        // Get the full API URL with chat completion endpoint
        val apiUrl = config.getChatCompletionUrl()
        if (apiUrl.isNullOrEmpty()) {
            throw AIException(AIErrorType.API_ERROR, "Invalid base URL in LLM configuration")
        }

        Log.d(TAG, "Calling LLM API at URL: $apiUrl")
        Log.d(TAG, "Request body: $jsonBody")
        Log.d(TAG, "Model name: ${config.modelName}")
        Log.d(TAG, "API token present: ${!apiToken.isNullOrEmpty()}")

        // Mask the API token for security in logs (show first 4 chars if available)
        val maskedToken = when {
            apiToken.isNullOrEmpty() -> "NOT_SET"
            apiToken.length > 4 -> "${apiToken.substring(0, 4)}..."
            else -> "***"
        }
        Log.d(TAG, "API token (masked): $maskedToken")

        // Create the request
        val request = Request.Builder()
            .url(apiUrl)
            .header("Authorization", "Bearer $apiToken")
            .header("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody(JSON))
            .build()

        // Execute the request with retries
        var lastException: IOException? = null
        for (attempt in 0..maxRetries) {
            try {
                val startTime = System.currentTimeMillis()
                client.newCall(request).execute().use { response ->
                    val endTime = System.currentTimeMillis()
                    Log.d(TAG, "API call took ${endTime - startTime}ms")

                    if (!response.isSuccessful) {
                        Log.e(TAG, "API call failed with code: ${response.code}, message: ${response.message}")
                        handleErrorResponse(response)
                    }

                    val responseBody = response.body?.string() ?: ""
                    Log.d(TAG, "LLM Response: $responseBody")
                    return responseBody
                }
            } catch (e: SocketTimeoutException) {
                lastException = IOException("Request timeout", e)
                Log.w(TAG, "Timeout on attempt ${attempt + 1}/${maxRetries + 1}", e)

                if (attempt < maxRetries) {
                    // Exponential backoff
                    val delay = Math.pow(2.0, attempt.toDouble()).toLong() * 1000 // 1s, 2s, 4s, etc.
                    try {
                        Thread.sleep(delay)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw IOException("Interrupted during retry delay", ie)
                    }
                }
            } catch (e: IOException) {
                lastException = e
                Log.w(TAG, "IO error on attempt ${attempt + 1}/${maxRetries + 1}", e)

                if (attempt < maxRetries) {
                    // Linear backoff for non-timeout errors
                    try {
                        Thread.sleep(1000)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw IOException("Interrupted during retry delay", ie)
                    }
                }
            }
        }

        // If we get here, all retries failed
        throw AIException(
            AIErrorType.TIMEOUT,
            "API call failed after ${maxRetries + 1} attempts",
            lastException!!
        )
    }

    /**
     * Handles HTTP error responses
     *
     * @param response HTTP response
     * @throws IOException with error details
     * @throws AIException for specific AI error types
     */
    @Throws(IOException::class, AIException::class)
    private fun handleErrorResponse(response: Response) {
        val errorBody = response.body?.string() ?: ""
        var errorMessage = "API error ${response.code}: ${response.message}"

        Log.e(
            TAG,
            "API Error - Code: ${response.code}, Message: ${response.message}, Body: $errorBody"
        )

        val errorType = when (response.code) {
            400 -> {
                errorMessage = "Bad Request (400): ${response.message}. " +
                        "Please check your LLM configuration including model name, API key, and base URL. " +
                        "Response body: $errorBody"
                AIErrorType.API_ERROR
            }
            401 -> {
                errorMessage = "Unauthorized (401): Invalid API key or authentication failed. " +
                        "Please check your API key in the LLM configuration."
                AIErrorType.API_ERROR
            }
            404 -> {
                errorMessage = "Not Found (404): The requested endpoint was not found. " +
                        "Please check your base URL and endpoint configuration."
                AIErrorType.API_ERROR
            }
            429 -> {
                errorMessage = "Rate limit exceeded. Please try again later."
                AIErrorType.RATE_LIMIT
            }
            in 500..599 -> {
                errorMessage = "Server error. Please try again later."
                AIErrorType.SERVER_ERROR
            }
            else -> AIErrorType.API_ERROR
        }

        throw AIException(errorType, errorMessage)
    }

    companion object {
        private const val TAG = "AIService"
        private val JSON = "application/json; charset=utf-8".toMediaType()
        private const val DEFAULT_TIMEOUT_SECONDS = 30
        private const val MAX_RETRIES = 3
    }
}
