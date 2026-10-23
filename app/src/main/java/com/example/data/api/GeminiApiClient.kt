package com.example.data.api

import com.example.data.local.MessageEntity
import com.example.data.preferences.ConnectionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class GeminiApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val currentCall = AtomicReference<okhttp3.Call?>(null)

    fun cancelActiveRequest() {
        currentCall.getAndSet(null)?.cancel()
    }

    suspend fun testConnection(apiKey: String, model: String = "gemini-3.5-flash"): ConnectionStatus =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext ConnectionStatus.NOT_CONFIGURED
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val bodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "Ping") })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("maxOutputTokens", 5)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val respBody = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        ConnectionStatus.CONNECTED
                    } else {
                        parseErrorStatus(response.code, respBody)
                    }
                }
            } catch (e: Exception) {
                if (e is java.net.UnknownHostException || e is java.io.IOException) {
                    ConnectionStatus.NETWORK_ERROR
                } else {
                    ConnectionStatus.SERVICE_UNAVAILABLE
                }
            }
        }

    suspend fun generateContentStream(
        apiKey: String,
        model: String,
        history: List<MessageEntity>,
        systemInstruction: String?,
        temperature: Float = 0.7f,
        maxTokens: Int = 4096,
        onChunk: (String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Connect your Gemini API to start chatting."))
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"
        val requestJson = buildRequestBodyJson(history, systemInstruction, temperature, maxTokens)

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val call = client.newCall(request)
        currentCall.set(call)

        try {
            val response = call.execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                val humanMsg = formatErrorMessage(response.code, errorBody)
                return@withContext Result.failure(Exception(humanMsg))
            }

            val fullTextBuilder = StringBuilder()
            val inputStream = response.body?.byteStream()
                ?: return@withContext Result.failure(Exception("Empty response received from service."))

            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val trimmed = line?.trim() ?: continue
                    if (trimmed.startsWith("data:")) {
                        val jsonPart = trimmed.removePrefix("data:").trim()
                        if (jsonPart.isNotEmpty() && jsonPart != "[DONE]") {
                            try {
                                val json = JSONObject(jsonPart)
                                val candidates = json.optJSONArray("candidates")
                                val firstCandidate = candidates?.optJSONObject(0)
                                val content = firstCandidate?.optJSONObject("content")
                                val parts = content?.optJSONArray("parts")
                                val chunkText = parts?.optJSONObject(0)?.optString("text", "") ?: ""
                                if (chunkText.isNotEmpty()) {
                                    fullTextBuilder.append(chunkText)
                                    onChunk(chunkText)
                                }
                            } catch (_: Exception) {
                                // Ignore json parsing chunk error
                            }
                        }
                    }
                }
            }

            val fullResult = fullTextBuilder.toString()
            if (fullResult.isEmpty()) {
                Result.failure(Exception("No response was generated by the model."))
            } else {
                Result.success(fullResult)
            }
        } catch (e: Exception) {
            if (call.isCanceled()) {
                Result.failure(Exception("Generation was stopped."))
            } else {
                Result.failure(Exception(formatException(e)))
            }
        } finally {
            currentCall.compareAndSet(call, null)
        }
    }

    private fun buildRequestBodyJson(
        history: List<MessageEntity>,
        systemInstruction: String?,
        temperature: Float,
        maxTokens: Int
    ): JSONObject {
        val root = JSONObject()

        // System Instruction
        if (!systemInstruction.isNullOrBlank()) {
            root.put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            })
        }

        // Contents
        val contentsArray = JSONArray()
        // Take up to last 20 messages for context
        val contextMessages = history.takeLast(20)
        for (msg in contextMessages) {
            val role = if (msg.role == "user") "user" else "model"
            val partsArray = JSONArray()

            // Optional attachment
            if (msg.attachmentType == "image" && !msg.attachmentData.isNullOrBlank()) {
                partsArray.put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", msg.attachmentData)
                    })
                })
            }

            if (msg.content.isNotBlank()) {
                partsArray.put(JSONObject().apply {
                    put("text", msg.content)
                })
            }

            if (partsArray.length() > 0) {
                contentsArray.put(JSONObject().apply {
                    put("role", role)
                    put("parts", partsArray)
                })
            }
        }
        root.put("contents", contentsArray)

        // Generation Config
        root.put("generationConfig", JSONObject().apply {
            put("temperature", temperature)
            put("maxOutputTokens", maxTokens)
        })

        return root
    }

    private fun parseErrorStatus(code: Int, errorBody: String): ConnectionStatus {
        return when {
            code == 400 || code == 403 || errorBody.contains("API_KEY_INVALID", ignoreCase = true) || errorBody.contains("API key not valid", ignoreCase = true) ->
                ConnectionStatus.INVALID_KEY
            code == 429 || errorBody.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ->
                ConnectionStatus.QUOTA_EXCEEDED
            code in 500..599 ->
                ConnectionStatus.SERVICE_UNAVAILABLE
            else ->
                ConnectionStatus.NETWORK_ERROR
        }
    }

    private fun formatErrorMessage(code: Int, errorBody: String): String {
        return when {
            code == 400 || code == 403 || errorBody.contains("API_KEY_INVALID", ignoreCase = true) ->
                "The API key could not be authenticated. Check your key and try again."
            code == 429 || errorBody.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ->
                "Your API quota or billing limit has been reached. Check your Google AI Studio settings."
            code in 500..599 ->
                "The AI service is temporarily unavailable. Please try again shortly."
            else ->
                "AI request failed ($code). Check your connection and retry."
        }
    }

    private fun formatException(e: Exception): String {
        return when (e) {
            is java.net.UnknownHostException -> "Unable to reach the AI service. Check your internet connection."
            is java.net.SocketTimeoutException -> "Request timed out. The server took too long to respond."
            else -> e.message ?: "An unexpected network error occurred."
        }
    }
}
