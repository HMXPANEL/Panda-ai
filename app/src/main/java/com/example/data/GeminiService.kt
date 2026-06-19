package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
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

// --- Gemini Response Types ---

sealed class GeminiResult {
    data class TextResponse(val text: String) : GeminiResult()
    data class FunctionCallResult(
        val name: String,
        val args: Map<String, JsonElement>
    ) : GeminiResult()
}

// --- API Service ---

object GeminiNetwork {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val userMemories = mutableListOf<String>()

    fun setUserMemories(memories: List<String>) {
        userMemories.clear()
        userMemories.addAll(memories)
    }

    private fun buildSystemInstruction(): String {
        val sb = StringBuilder()
        sb.appendLine("You are Panda, an AI assistant that controls the user's Android device via function calls.")
        sb.appendLine()
        sb.appendLine("RULES:")
        sb.appendLine("- If the user asks a general question or chats normally, just respond with text.")
        sb.appendLine("- If the user asks you to DO something on the device, call the appropriate function.")
        sb.appendLine("- For multi-step tasks, call one function at a time. After each function call, you will see the result and can decide the next step.")
        sb.appendLine("- When you want to know what's on screen, call get_screen_content().")
        sb.appendLine("- Some actions (calling, SMS, alarm, calendar, uninstall, write file) require user confirmation. After the user confirms, proceed.")
        sb.appendLine("- Keep responses brief and natural.")
        sb.appendLine("- DANGEROUS ACTIONS (calling, SMS, uninstall, write file) - ALWAYS ask the user to confirm BEFORE calling the function. Wait for their explicit confirmation like 'yes', 'confirm', 'go ahead', 'do it'.")
        sb.appendLine()
        if (userMemories.isNotEmpty()) {
            sb.appendLine("USER MEMORIES (facts you remember about the user):")
            userMemories.forEach { sb.appendLine("- $it") }
            sb.appendLine()
        }
        sb.appendLine("Available packages: com.whatsapp, com.instagram.android, com.twitter.android, com.facebook.katana, com.android.chrome, com.google.android.youtube, com.spotify.music, com.snapchat.android, com.ubercab, com.google.android.gm, com.google.android.apps.maps, com.google.android.apps.docs, com.google.android.apps.dialer, com.google.android.apps.messaging, com.android.vending, com.google.android.calendar, com.google.android.deskclock")
        return sb.toString()
    }

    fun queryGeminiStream(prompt: String, userApiKey: String = "", activeModel: String = "gemini-2.0-flash"): Flow<String> = flow {
        val actualKey = userApiKey.ifEmpty { BuildConfig.GEMINI_API_KEY }
        if (actualKey.isEmpty() || actualKey == "MY_GEMINI_API_KEY") {
            emit("Error: No valid Gemini API key configured. Please add your API key in Settings.")
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
                            } catch (_: Exception) {}
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

    // ─── Tool / Function Calling API ───

    suspend fun queryWithTools(
        contents: List<JsonObject>,
        userApiKey: String = "",
        activeModel: String = "gemini-2.0-flash"
    ): GeminiResult {
        val actualKey = userApiKey.ifEmpty { BuildConfig.GEMINI_API_KEY }
        if (actualKey.isEmpty() || actualKey == "MY_GEMINI_API_KEY") {
            return GeminiResult.TextResponse("Error: No valid Gemini API key configured. Please add your API key in Settings.")
        }

        val requestBody = buildJsonObject {
            put("contents", JsonArray(contents))
            put("systemInstruction", buildJsonObject {
                put("parts", JsonArray(listOf(buildJsonObject {
                    put("text", buildSystemInstruction())
                })))
            })
            put("tools", JsonArray(listOf(buildJsonObject {
                put("functionDeclarations", JsonArray(deviceFunctions.map { it.toJson() }))
            })))
        }

        val request = Request.Builder()
            .url("${BASE_URL}v1beta/models/$activeModel:generateContent")
            .header("x-goog-api-key", actualKey)
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = withContext(Dispatchers.IO) { okHttpClient.newCall(request).execute() }
            val body = response.body?.string() ?: return GeminiResult.TextResponse("Empty response from AI")
            val jsonObj = json.parseToJsonElement(body).jsonObject
            val candidate = jsonObj["candidates"]?.jsonArray?.get(0)?.jsonObject
            val parts = candidate?.get("content")?.jsonObject?.get("parts")?.jsonArray

            if (parts != null && parts.isNotEmpty()) {
                val firstPart = parts[0].jsonObject
                val functionCall = firstPart["functionCall"]?.jsonObject
                if (functionCall != null) {
                    val name = functionCall["name"]?.jsonPrimitive?.content ?: ""
                    val args = functionCall["args"]?.jsonObject?.toMap() ?: emptyMap()
                    return GeminiResult.FunctionCallResult(name, args)
                }
                val text = firstPart["text"]?.jsonPrimitive?.content
                if (!text.isNullOrBlank()) return GeminiResult.TextResponse(text)
            }
            GeminiResult.TextResponse("Sorry, I couldn't process that.")
        } catch (e: java.net.UnknownHostException) {
            GeminiResult.TextResponse("I can't reach the internet right now. Please check your connection.")
        } catch (e: java.net.SocketTimeoutException) {
            GeminiResult.TextResponse("The request timed out. Please try again.")
        } catch (e: Exception) {
            GeminiResult.TextResponse("Something went wrong: ${e.message}")
        }
    }

    // ─── Function Declarations ───

    private data class FuncDecl(
        val name: String,
        val description: String,
        val params: Map<String, Pair<String, String>>,
        val required: List<String> = params.keys.toList()
    ) {
        fun toJson(): JsonObject = buildJsonObject {
            put("name", name)
            put("description", description)
            put("parameters", buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    params.forEach { (key, typeDesc) ->
                        put(key, buildJsonObject {
                            put("type", typeDesc.first)
                            put("description", typeDesc.second)
                        })
                    }
                })
                put("required", JsonArray(required.map { JsonPrimitive(it) }))
            })
        }
    }

    private val deviceFunctions = listOf(
        // ── UI Interaction ──
        FuncDecl("click_text", "Click a button/link/item by its visible text", mapOf("text" to Pair("string", "The visible text to click"))),
        FuncDecl("click_desc", "Click a UI element by its accessibility content description (for icons without text)", mapOf("desc" to Pair("string", "The content description"))),
        FuncDecl("click_id", "Click a UI element by its view ID resource name", mapOf("id" to Pair("string", "The element's view ID"))),
        FuncDecl("click_at", "Click at specific screen coordinates (x, y pixels) - use when no text/desc/id is available", mapOf("x" to Pair("integer", "X coordinate"), "y" to Pair("integer", "Y coordinate"))),
        FuncDecl("long_press_text", "Long press on text to trigger context menu", mapOf("text" to Pair("string", "The visible text to long press"))),
        FuncDecl("type_text", "Type text into an editable field", mapOf("text" to Pair("string", "Text to type"), "into_hint" to Pair("string", "Optional: field hint text to identify which field")), required = listOf("text")),

        // ── Navigation ──
        FuncDecl("go_back", "Navigate back (system back button)", emptyMap()),
        FuncDecl("go_home", "Go to home screen", emptyMap()),
        FuncDecl("go_recents", "Open recent apps overview", emptyMap()),
        FuncDecl("scroll_down", "Scroll forward/up one page", emptyMap()),
        FuncDecl("scroll_up", "Scroll backward/down one page", emptyMap()),
        FuncDecl("scroll_to_text", "Keep scrolling down until specified text appears (up to 8 scrolls)", mapOf("text" to Pair("string", "Text to find by scrolling"))),

        // ── Gestures ──
        FuncDecl("swipe_up", "Swipe upward gesture", emptyMap()),
        FuncDecl("swipe_down", "Swipe downward gesture (e.g., pull notification shade)", emptyMap()),
        FuncDecl("swipe_left", "Swipe left gesture", emptyMap()),
        FuncDecl("swipe_right", "Swipe right gesture (e.g., open drawer)", emptyMap()),

        // ── Apps ──
        FuncDecl("open_app", "Open an Android app by package name", mapOf("package_name" to Pair("string", "The app's package name"))),
        FuncDecl("open_url", "Open a URL in the browser", mapOf("url" to Pair("string", "The full URL to open"))),

        // ── Communication (dangerous - requires confirmation) ──
        FuncDecl("make_call", "Make a direct phone call to a number. REQUIRES USER CONFIRMATION.", mapOf("phone_number" to Pair("string", "The phone number to call"))),
        FuncDecl("send_sms", "Send an SMS silently. REQUIRES USER CONFIRMATION.", mapOf("phone_number" to Pair("string", "Recipient phone number"), "message" to Pair("string", "SMS text content")), required = listOf("phone_number", "message")),
        FuncDecl("call_contact", "Find a contact by name and call them. REQUIRES USER CONFIRMATION.", mapOf("name" to Pair("string", "The contact name to call"))),
        FuncDecl("sms_contact", "Find a contact by name and send them an SMS. REQUIRES USER CONFIRMATION.", mapOf("name" to Pair("string", "Contact name"), "message" to Pair("string", "SMS text content")), required = listOf("name", "message")),
        FuncDecl("search_contact", "Search contacts by name and return their phone numbers", mapOf("name" to Pair("string", "Name or partial name to search"))),

        // ── Device Controls ──
        FuncDecl("flashlight_toggle", "Toggle the flashlight on/off", emptyMap()),
        FuncDecl("set_wifi", "Enable or disable WiFi", mapOf("enable" to Pair("boolean", "true to enable, false to disable"))),
        FuncDecl("set_bluetooth", "Enable or disable Bluetooth", mapOf("enable" to Pair("boolean", "true to enable, false to disable"))),
        FuncDecl("maximize_volume", "Set media volume to maximum", emptyMap()),
        FuncDecl("set_volume", "Set media volume to a specific level (0-100)", mapOf("level" to Pair("integer", "Volume level from 0 to 100"))),
        FuncDecl("toggle_media", "Play or pause current media playback", emptyMap()),
        FuncDecl("vibrate", "Vibrate the device briefly", emptyMap()),
        FuncDecl("read_battery", "Read the current battery level", emptyMap()),
        FuncDecl("copy_clipboard", "Copy text to the device clipboard", mapOf("text" to Pair("string", "Text to copy"))),

        // ── Productivity ──
        FuncDecl("set_alarm", "Set a one-time alarm. REQUIRES USER CONFIRMATION.", mapOf("hour" to Pair("integer", "Hour in 24h format (0-23)"), "minute" to Pair("integer", "Minute (0-59)"), "label" to Pair("string", "Label for the alarm")), required = listOf("hour", "minute", "label")),
        FuncDecl("add_calendar_event", "Add an event to the calendar. REQUIRES USER CONFIRMATION.", mapOf("title" to Pair("string", "Event title"), "description" to Pair("string", "Event description"), "start_time" to Pair("string", "Start time as unix milliseconds"), "end_time" to Pair("string", "End time as unix milliseconds")), required = listOf("title", "start_time", "end_time")),
        FuncDecl("open_maps", "Search a location in Google Maps", mapOf("location" to Pair("string", "Location query (e.g., 'coffee shops near me')"))),

        // ── Screen Info ──
        FuncDecl("get_screen_content", "Get the visible text, buttons, icons, and their positions on screen", emptyMap()),

        // ── Speech ──
        FuncDecl("speak_text", "Use text-to-speech to speak text aloud", mapOf("text" to Pair("string", "Text to speak aloud"))),

        // ── File ──
        FuncDecl("read_file", "Read content from a file in app storage", mapOf("path" to Pair("string", "File path"))),
        FuncDecl("write_file", "Write text content to a file in app storage. REQUIRES USER CONFIRMATION.", mapOf("path" to Pair("string", "File path"), "content" to Pair("string", "Content to write")), required = listOf("path", "content")),

        // ── Memory ──
        FuncDecl("get_memories", "Retrieve all saved user memories/facts", emptyMap()),
        FuncDecl("save_memory", "Save a fact/memory about the user", mapOf("content" to Pair("string", "The memory content"), "category" to Pair("string", "Category: Important, Conversations, or All")), required = listOf("content")),

        // ── Screenshot ──
        FuncDecl("take_screenshot", "Capture the current screen as a screenshot saved to internal storage. Requires screen capture permission to be granted first.", emptyMap()),

        // ── Notifications ──
        FuncDecl("read_notifications", "Read the current notifications from the notification shade", emptyMap()),
    )
}
