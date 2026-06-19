package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.PandaForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

enum class ScreenState {
    Splash, Onboarding, Permissions, MainApp
}

enum class BackgroundStyle { Orbs, Ripple, Gradient, Pulse }

enum class BottomTab {
    Home, Chat, Tools, Settings
}

enum class AiState {
    Idle, Listening, Thinking
}

data class PendingDangerousAction(
    val action: DeviceAction,
    val description: String
)

class PandaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PandaRepository

    // Screen state flows
    private val _screen = MutableStateFlow(ScreenState.Splash)
    val screen: StateFlow<ScreenState> = _screen.asStateFlow()

    private val _currentTab = MutableStateFlow(BottomTab.Home)
    val currentTab: StateFlow<BottomTab> = _currentTab.asStateFlow()

    // Database flows for UI
    val memories: StateFlow<List<Memory>>
    val chatMessages: StateFlow<List<ChatMessage>>

    // Live States
    private val _userName = MutableStateFlow("Harsh")
    val userName = _userName.asStateFlow()

    private val _activeModelName = MutableStateFlow("gemini-2.0-flash")
    val activeModelName = _activeModelName.asStateFlow()

    private val _userApiKey = MutableStateFlow("")
    val userApiKey = _userApiKey.asStateFlow()

    private val _isAssistantActive = MutableStateFlow(true)
    val isAssistantActive = _isAssistantActive.asStateFlow()

    private val _preferredVoice = MutableStateFlow("Kore")
    val preferredVoice = _preferredVoice.asStateFlow()

    private val _answerStyle = MutableStateFlow("Concise")
    val answerStyle = _answerStyle.asStateFlow()

    private val _backgroundStyle = MutableStateFlow(BackgroundStyle.Orbs)
    val backgroundStyle = _backgroundStyle.asStateFlow()

    // Listening/Speech animation state
    private val _aiState = MutableStateFlow(AiState.Idle)
    val aiState = _aiState.asStateFlow()

    // Permissions state
    private val _isAccessibilityGranted = MutableStateFlow(true)
    val isAccessibilityGranted = _isAccessibilityGranted.asStateFlow()

    private val _isOverlayGranted = MutableStateFlow(true)
    val isOverlayGranted = _isOverlayGranted.asStateFlow()

    private val _isMicGranted = MutableStateFlow(true)
    val isMicGranted = _isMicGranted.asStateFlow()

    private val _isNotificationsGranted = MutableStateFlow(false)
    val isNotificationsGranted = _isNotificationsGranted.asStateFlow()

    // Pending dangerous action awaiting user confirmation
    private val _pendingAction = MutableStateFlow<PendingDangerousAction?>(null)
    val pendingAction: StateFlow<PendingDangerousAction?> = _pendingAction.asStateFlow()

    // MediaProjection screenshot support
    private val _requestScreenCapture = MutableStateFlow(false)
    val requestScreenCapture: StateFlow<Boolean> = _requestScreenCapture.asStateFlow()

    private val _screenshotAvailable = MutableStateFlow(false)
    val screenshotAvailable: StateFlow<Boolean> = _screenshotAvailable.asStateFlow()

    fun requestScreenshotPermission() {
        _requestScreenCapture.value = true
    }

    fun onScreenCaptureRequestHandled() {
        _requestScreenCapture.value = false
    }

    fun refreshScreenshotStatus() {
        _screenshotAvailable.value = com.example.data.MediaProjectionHelper.isAvailable()
    }

    private val confirmations = setOf("yes", "yeah", "yep", "sure", "ok", "okay", "confirm", "go ahead", "do it", "proceed", "y", "correct")

    init {
        val database = PandaDatabase.getDatabase(application)
        val dao = database.pandaDao()
        repository = PandaRepository(dao)

        memories = MutableStateFlow<List<Memory>>(emptyList())
        chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())

        viewModelScope.launch {
            repository.allMemories.collect {
                (memories as MutableStateFlow).value = it
            }
        }

        viewModelScope.launch {
            repository.allMessages.collect {
                (chatMessages as MutableStateFlow).value = it
            }
        }

        viewModelScope.launch {
            _userName.value = repository.getSetting("user_name", "Harsh")
            _activeModelName.value = repository.getSetting("active_model", "gemini-2.0-flash")
            _userApiKey.value = repository.getSetting("user_api_key", "")
            _isAssistantActive.value = repository.getSetting("is_active", "true").toBoolean()
            _preferredVoice.value = repository.getSetting("pref_voice", "Kore")
            _answerStyle.value = repository.getSetting("answer_style", "Concise")
            _backgroundStyle.value = try {
                BackgroundStyle.valueOf(repository.getSetting("background_style", "Orbs"))
            } catch (e: Exception) {
                BackgroundStyle.Orbs
            }

            seedInitialData()
        }
    }

    private suspend fun seedInitialData() {
        val currentMemories = (memories as MutableStateFlow).value
        if (currentMemories.isEmpty()) {
            repository.insertMemory(Memory(content = "You like Tesla cars", category = "Important"))
            repository.insertMemory(Memory(content = "Your work email is harsh@example.com", category = "Important"))
            repository.insertMemory(Memory(content = "You live in Mumbai", category = "All"))
            repository.insertMemory(Memory(content = "Remind you to call mom every Sunday, 10:00 AM", category = "Conversations"))
        }

        val currentMessages = (chatMessages as MutableStateFlow).value
        if (currentMessages.isEmpty()) {
            repository.insertMessage(ChatMessage(text = "Hi Harsh! 👋 How can I help you today?", isUser = false))
        }
    }

    // --- State Actions ---

    fun navigateTo(state: ScreenState) {
        _screen.value = state
    }

    fun selectTab(tab: BottomTab) {
        _currentTab.value = tab
    }

    fun updateUserName(name: String) {
        _userName.value = name
        viewModelScope.launch { repository.saveSetting("user_name", name) }
    }

    fun updateActiveModel(model: String) {
        _activeModelName.value = model
        viewModelScope.launch { repository.saveSetting("active_model", model) }
    }

    fun updateUserApiKey(key: String) {
        _userApiKey.value = key
        viewModelScope.launch { repository.saveSetting("user_api_key", key) }
    }

    fun setAssistantActive(active: Boolean) {
        _isAssistantActive.value = active
        viewModelScope.launch {
            repository.saveSetting("is_active", active.toString())
            if (active) {
                PandaForegroundService.start(getApplication())
            } else {
                PandaForegroundService.stop(getApplication())
            }
        }
    }

    fun setPreferredVoice(voice: String) {
        _preferredVoice.value = voice
        viewModelScope.launch { repository.saveSetting("pref_voice", voice) }
    }

    fun setAnswerStyle(style: String) {
        _answerStyle.value = style
        viewModelScope.launch { repository.saveSetting("answer_style", style) }
    }

    fun updateBackgroundStyle(style: BackgroundStyle) {
        _backgroundStyle.value = style
        viewModelScope.launch { repository.saveSetting("background_style", style.name) }
    }

    private var voiceRecognizer: VoiceRecognizer? = null
    private var voiceScope: CoroutineScope? = null

    fun startVoiceListening(context: Context) {
        _aiState.value = AiState.Listening
        voiceRecognizer = VoiceRecognizer(context.applicationContext)
        voiceScope = CoroutineScope(Dispatchers.IO)
        voiceScope?.launch {
            val channel = voiceRecognizer?.startListening()
            if (channel != null) {
                for (text in channel) {
                    stopVoiceListeningAndSend(text)
                }
            }
        }
    }

    fun stopVoiceListening() {
        voiceRecognizer?.stopListening()
        voiceScope?.cancel()
        voiceRecognizer = null
        voiceScope = null
        if (_aiState.value == AiState.Listening) {
            _aiState.value = AiState.Idle
        }
    }

    fun stopVoiceListeningAndSend(text: String) {
        if (text.isNotBlank()) {
            sendMessage(text)
        }
        voiceRecognizer?.destroy()
        voiceScope?.cancel()
        voiceRecognizer = null
        voiceScope = null
        _aiState.value = AiState.Idle
    }

    // --- Permissions Toggles ---

    fun toggleAccessibility(granted: Boolean? = null) {
        _isAccessibilityGranted.value = granted ?: !_isAccessibilityGranted.value
    }

    fun toggleOverlay(granted: Boolean? = null) {
        _isOverlayGranted.value = granted ?: !_isOverlayGranted.value
    }

    fun toggleMic(granted: Boolean? = null) {
        _isMicGranted.value = granted ?: !_isMicGranted.value
    }

    fun toggleNotifications(granted: Boolean? = null) {
        _isNotificationsGranted.value = granted ?: !_isNotificationsGranted.value
    }

    fun cancelPendingAction() {
        _pendingAction.value = null
    }

    // --- Memories Operations ---

    fun addMemory(content: String, category: String = "Important") {
        if (content.isNotBlank()) {
            viewModelScope.launch {
                repository.insertMemory(Memory(content = content, category = category))
            }
        }
    }

    fun deleteMemory(memory: Memory) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun clearMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    // --- Chat + Command Execution Pipeline ---

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMessage(ChatMessage(text = text, isUser = true))
            _aiState.value = AiState.Thinking

            // Check if user is confirming/cancelling a pending dangerous action
            val pending = _pendingAction.value
            if (pending != null) {
                handlePendingConfirmation(text, pending)
                return@launch
            }

            // Build content history for Gemini from recent messages
            val currentMessages = (chatMessages as MutableStateFlow).value
            val recentMessages = currentMessages.takeLast(20)

            // Inject memories into Gemini system instruction
            val allMemories = repository.getAllMemoriesSync()
            val memoryTexts = allMemories.map { "${it.content} (${it.category})" }
            GeminiNetwork.setUserMemories(memoryTexts)

            val contentHistory = mutableListOf<JsonObject>()
            for (msg in recentMessages) {
                contentHistory.add(buildJsonObject {
                    put("role", JsonPrimitive(if (msg.isUser) "user" else "model"))
                    put("parts", JsonArray(listOf(buildJsonObject {
                        put("text", JsonPrimitive(msg.text))
                    })))
                })
            }

            // Execute the AI + Action loop
            val executor = ActionExecutor(getApplication())
            var finalResponse: String? = null
            var iterations = 0
            val maxIterations = 25

            try {
                while (iterations < maxIterations) {
                    iterations++

                    when (val result = GeminiNetwork.queryWithTools(
                        contents = contentHistory,
                        userApiKey = _userApiKey.value,
                        activeModel = _activeModelName.value
                    )) {
                        is GeminiResult.FunctionCallResult -> {
                            val action = parseFunctionCall(result.name, result.args)

                            // Handle screenshot - may need permission request
                            if (action is DeviceAction.TakeScreenshot) {
                                if (!MediaProjectionHelper.isAvailable()) {
                                    _requestScreenCapture.value = true
                                    finalResponse = "Please grant screen capture permission to take a screenshot."
                                    break
                                }
                            }

                            // Handle memory actions directly in ViewModel (Room ops)
                            if (action is DeviceAction.GetMemories) {
                                val mems = repository.getAllMemoriesSync()
                                val memText = if (mems.isEmpty()) "No saved memories."
                                else mems.joinToString("\n") { "- ${it.content} (${it.category})" }
                                contentHistory.add(buildJsonObject {
                                    put("role", JsonPrimitive("model"))
                                    put("parts", JsonArray(listOf(buildJsonObject {
                                        put("functionCall", buildJsonObject {
                                            put("name", JsonPrimitive(result.name))
                                            put("args", JsonObject(result.args))
                                        })
                                    })))
                                })
                                contentHistory.add(buildJsonObject {
                                    put("role", JsonPrimitive("function"))
                                    put("parts", JsonArray(listOf(buildJsonObject {
                                        put("functionResponse", buildJsonObject {
                                            put("name", JsonPrimitive(result.name))
                                            put("response", buildJsonObject {
                                                put("success", JsonPrimitive("true"))
                                                put("memories", JsonPrimitive(memText))
                                            })
                                        })
                                    })))
                                })
                                continue
                            }
                            if (action is DeviceAction.SaveMemory) {
                                repository.insertMemory(Memory(content = action.content, category = action.category))
                                contentHistory.add(buildJsonObject {
                                    put("role", JsonPrimitive("model"))
                                    put("parts", JsonArray(listOf(buildJsonObject {
                                        put("functionCall", buildJsonObject {
                                            put("name", JsonPrimitive(result.name))
                                            put("args", JsonObject(result.args))
                                        })
                                    })))
                                })
                                contentHistory.add(buildJsonObject {
                                    put("role", JsonPrimitive("function"))
                                    put("parts", JsonArray(listOf(buildJsonObject {
                                        put("functionResponse", buildJsonObject {
                                            put("name", JsonPrimitive(result.name))
                                            put("response", buildJsonObject {
                                                put("success", JsonPrimitive("true"))
                                                put("message", JsonPrimitive("Saved memory: ${action.content}"))
                                            })
                                        })
                                    })))
                                })
                                continue
                            }

                            // Dangerous action - ask for confirmation before executing
                            if (action.isDangerous()) {
                                _pendingAction.value = PendingDangerousAction(
                                    action = action,
                                    description = action.description()
                                )
                                finalResponse = "⚠️ I need your confirmation to: ${action.description()}. Should I proceed?"
                                break
                            }

                            // Normal action - execute immediately
                            val actionResult = executor.execute(action)

                            contentHistory.add(buildJsonObject {
                                put("role", JsonPrimitive("model"))
                                put("parts", JsonArray(listOf(buildJsonObject {
                                    put("functionCall", buildJsonObject {
                                        put("name", JsonPrimitive(result.name))
                                        put("args", JsonObject(result.args))
                                    })
                                })))
                            })

                            val responseMap = when (actionResult) {
                                is ActionResult.Success -> buildJsonObject {
                                    put("success", JsonPrimitive("true"))
                                    put("message", JsonPrimitive(actionResult.message))
                                }
                                is ActionResult.Error -> buildJsonObject {
                                    put("success", JsonPrimitive("false"))
                                    put("error", JsonPrimitive(actionResult.message))
                                }
                                is ActionResult.ScreenContent -> buildJsonObject {
                                    put("success", JsonPrimitive("true"))
                                    put("screen_content", JsonPrimitive(actionResult.content))
                                }
                            }

                            contentHistory.add(buildJsonObject {
                                put("role", JsonPrimitive("function"))
                                put("parts", JsonArray(listOf(buildJsonObject {
                                    put("functionResponse", buildJsonObject {
                                        put("name", JsonPrimitive(result.name))
                                        put("response", responseMap)
                                    })
                                })))
                            })
                        }
                        is GeminiResult.TextResponse -> {
                            finalResponse = result.text
                            break
                        }
                    }
                }

                if (finalResponse == null) {
                    finalResponse = "I couldn't complete that task. Please try again or be more specific."
                }

                repository.insertMessage(ChatMessage(text = finalResponse, isUser = false))

            } catch (e: Exception) {
                repository.insertMessage(
                    ChatMessage(text = "Error: ${e.message}", isUser = false)
                )
            } finally {
                _aiState.value = AiState.Idle
            }
        }
    }

    private suspend fun handlePendingConfirmation(text: String, pending: PendingDangerousAction) {
        val clean = text.trim().lowercase().trimEnd('.', '!', '?')
        if (clean in confirmations) {
            val executor = ActionExecutor(getApplication())
            val actionResult = executor.execute(pending.action, dangerousConfirmed = true)
            val response = when (actionResult) {
                is ActionResult.Success -> "✅ ${actionResult.message}"
                is ActionResult.Error -> "❌ ${actionResult.message}"
                is ActionResult.ScreenContent -> actionResult.content
            }
            repository.insertMessage(ChatMessage(text = response, isUser = false))
        } else {
            // User said something other than confirmation - cancel
            repository.insertMessage(ChatMessage(text = "Cancelled: ${pending.description}", isUser = false))
        }
        _pendingAction.value = null
        _aiState.value = AiState.Idle
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
            _pendingAction.value = null
            repository.insertMessage(ChatMessage(text = "Hello ${_userName.value}! Welcome back. What can I automate for you today?", isUser = false))
        }
    }

    private fun parseFunctionCall(name: String, args: Map<String, JsonElement>): DeviceAction {
        return when (name) {
            // ── UI Interaction ──
            "click_text" -> DeviceAction.ClickText(
                text = args["text"]?.jsonPrimitive?.content ?: ""
            )
            "click_desc" -> DeviceAction.ClickDesc(
                desc = args["desc"]?.jsonPrimitive?.content ?: ""
            )
            "click_id" -> DeviceAction.ClickId(
                id = args["id"]?.jsonPrimitive?.content ?: ""
            )
            "click_at" -> DeviceAction.ClickAt(
                x = args["x"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                y = args["y"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            )
            "long_press_text" -> DeviceAction.LongPressText(
                text = args["text"]?.jsonPrimitive?.content ?: ""
            )
            "type_text" -> DeviceAction.TypeText(
                text = args["text"]?.jsonPrimitive?.content ?: "",
                intoHint = args["into_hint"]?.jsonPrimitive?.content
            )

            // ── Navigation ──
            "go_back" -> DeviceAction.GoBack
            "go_home" -> DeviceAction.GoHome
            "go_recents" -> DeviceAction.GoRecents
            "scroll_down" -> DeviceAction.ScrollDown
            "scroll_up" -> DeviceAction.ScrollUp
            "scroll_to_text" -> DeviceAction.ScrollToText(
                text = args["text"]?.jsonPrimitive?.content ?: ""
            )

            // ── Gestures ──
            "swipe_up" -> DeviceAction.SwipeUp
            "swipe_down" -> DeviceAction.SwipeDown
            "swipe_left" -> DeviceAction.SwipeLeft
            "swipe_right" -> DeviceAction.SwipeRight

            // ── Apps ──
            "open_app" -> DeviceAction.OpenApp(
                packageName = args["package_name"]?.jsonPrimitive?.content ?: ""
            )
            "open_url" -> DeviceAction.OpenUrl(
                url = args["url"]?.jsonPrimitive?.content ?: ""
            )

            // ── Communication (dangerous) ──
            "make_call" -> DeviceAction.MakeCall(
                phoneNumber = args["phone_number"]?.jsonPrimitive?.content ?: ""
            )
            "send_sms" -> DeviceAction.SendSms(
                phoneNumber = args["phone_number"]?.jsonPrimitive?.content ?: "",
                message = args["message"]?.jsonPrimitive?.content ?: ""
            )
            "call_contact" -> DeviceAction.CallContact(
                name = args["name"]?.jsonPrimitive?.content ?: ""
            )
            "sms_contact" -> DeviceAction.SmsContact(
                name = args["name"]?.jsonPrimitive?.content ?: "",
                message = args["message"]?.jsonPrimitive?.content ?: ""
            )
            "search_contact" -> DeviceAction.SearchContact(
                name = args["name"]?.jsonPrimitive?.content ?: ""
            )

            // ── Device Controls ──
            "flashlight_toggle" -> DeviceAction.FlashlightToggle
            "set_wifi" -> DeviceAction.SetWifi(
                enable = args["enable"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
            )
            "set_bluetooth" -> DeviceAction.SetBluetooth(
                enable = args["enable"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
            )
            "maximize_volume" -> DeviceAction.MaximizeVolume
            "set_volume" -> DeviceAction.SetVolume(
                level = args["level"]?.jsonPrimitive?.content?.toIntOrNull() ?: 50
            )
            "toggle_media" -> DeviceAction.ToggleMediaPlayback
            "vibrate" -> DeviceAction.Vibrate
            "read_battery" -> DeviceAction.ReadBattery
            "copy_clipboard" -> DeviceAction.CopyClipboard(
                text = args["text"]?.jsonPrimitive?.content ?: ""
            )

            // ── Productivity (dangerous) ──
            "set_alarm" -> DeviceAction.SetAlarm(
                hour = args["hour"]?.jsonPrimitive?.content?.toIntOrNull() ?: 8,
                minute = args["minute"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                label = args["label"]?.jsonPrimitive?.content ?: "Alarm"
            )
            "add_calendar_event" -> DeviceAction.AddCalendarEvent(
                title = args["title"]?.jsonPrimitive?.content ?: "",
                description = args["description"]?.jsonPrimitive?.content ?: "",
                startTime = args["start_time"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                endTime = args["end_time"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
            )
            "open_maps" -> DeviceAction.OpenMaps(
                location = args["location"]?.jsonPrimitive?.content ?: ""
            )

            // ── Screen Info ──
            "get_screen_content" -> DeviceAction.GetScreenContent

            // ── Speech ──
            "speak_text" -> DeviceAction.SpeakText(
                text = args["text"]?.jsonPrimitive?.content ?: ""
            )

            // ── File ──
            "read_file" -> DeviceAction.ReadFile(
                path = args["path"]?.jsonPrimitive?.content ?: ""
            )
            "write_file" -> DeviceAction.WriteFile(
                path = args["path"]?.jsonPrimitive?.content ?: "",
                content = args["content"]?.jsonPrimitive?.content ?: ""
            )

            // ── Memory ──
            "get_memories" -> DeviceAction.GetMemories
            "save_memory" -> DeviceAction.SaveMemory(
                content = args["content"]?.jsonPrimitive?.content ?: "",
                category = args["category"]?.jsonPrimitive?.content ?: "All"
            )

            // ── Screenshot ──
            "take_screenshot" -> DeviceAction.TakeScreenshot

            // ── Notifications ──
            "read_notifications" -> DeviceAction.ReadNotifications

            else -> DeviceAction.Wait(0)
        }
    }
}
