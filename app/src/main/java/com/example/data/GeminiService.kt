package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// --- Serialization Data Classes ---

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class Part(val text: String? = null)

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

// --- API Service ---

object GeminiNetwork {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    
    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun queryGeminiStream(prompt: String, userApiKey: String = "", activeModel: String = "gemini-2.0-flash"): Flow<String> = flow {
        val actualKey = userApiKey.ifEmpty { BuildConfig.GEMINI_API_KEY }
        if (actualKey.isEmpty() || actualKey == "MY_GEMINI_API_KEY") {
            emit("DEMO_PLACEHOLDER")
            return@flow
        }

        val requestBody = json.encodeToString(GenerateContentRequest(
            contents = listOf(Content(listOf(Part(text = prompt)))),
            systemInstruction = Content(listOf(Part(text = "You are Panda, a personal voice and helper AI Agent. Keep your answers brief, colorful, and highly helpful, in 1-3 sentences.")))
        )).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${BASE_URL}v1beta/models/$activeModel:streamGenerateContent")
            .header("x-goog-api-key", actualKey)
            .post(requestBody)
            .build()
        
        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    emit("Sorry, I couldn't process that. The AI service returned an error. Please try again.")
                    return@flow
                }
                
                response.body?.byteStream()?.bufferedReader()?.use { reader ->
                    reader.lineSequence().forEach { line ->
                        if (line.startsWith("data: ")) {
                            val jsonLine = line.removePrefix("data: ")
                            try {
                                val jsonObj = json.parseToJsonElement(jsonLine).jsonObject
                                val text = jsonObj["candidates"]?.jsonArray?.get(0)?.jsonObject
                                    ?.get("content")?.jsonObject
                                    ?.get("parts")?.jsonArray?.get(0)?.jsonObject
                                    ?.get("text")?.jsonPrimitive?.content
                                if (text != null) emit(text)
                            } catch (e: Exception) {
                                // Skip malformed SSE lines
                            }
                        }
                    }
                }
            }
        } catch (e: java.net.UnknownHostException) {
            emit("I can't reach the internet right now. Please check your connection and try again.")
        } catch (e: java.net.SocketTimeoutException) {
            emit("The request timed out. The AI service may be busy. Please try again.")
        } catch (e: Exception) {
            emit("Something went wrong. Please try again in a moment.")
        }
    }.flowOn(Dispatchers.IO)
}
