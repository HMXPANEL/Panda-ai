package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    init {
        val database = PandaDatabase.getDatabase(application)
        val dao = database.pandaDao()
        repository = PandaRepository(dao)

        memories = MutableStateFlow<List<Memory>>(emptyList())
        chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())

        // Launch queries
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

        // Load saved states from Room database
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

            // Pre-seed mock data if database is empty
            seedInitialData()
        }
    }

    private suspend fun seedInitialData() {
        // Seed initial mock memories matching Screen 11: Memories Screen from the image
        val currentMemories = (memories as MutableStateFlow).value
        if (currentMemories.isEmpty()) {
            repository.insertMemory(Memory(content = "You like Tesla cars", category = "Important"))
            repository.insertMemory(Memory(content = "Your work email is harsh@example.com", category = "Important"))
            repository.insertMemory(Memory(content = "You live in Mumbai", category = "All"))
            repository.insertMemory(Memory(content = "Remind you to call mom every Sunday, 10:00 AM", category = "Conversations"))
        }

        // Seed initial message if chat is empty
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
        viewModelScope.launch { repository.saveSetting("is_active", active.toString()) }
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
            voiceRecognizer?.startListening()?.consumeEach { text ->
                stopVoiceListeningAndSend(text)
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
        voiceRecognizer?.destroy()
        voiceScope?.cancel()
        voiceRecognizer = null
        voiceScope = null
        _aiState.value = AiState.Idle
        if (text.isNotBlank()) {
            sendMessage(text)
        }
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

    // --- Chat Operations ---

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // 1. Insert user message
            repository.insertMessage(ChatMessage(text = text, isUser = true))

            // 2. Transition listening -> thinking states
            _aiState.value = AiState.Thinking

            // 3. Prepare streaming response
            val botMessageId = repository.insertMessage(ChatMessage(text = "...", isUser = false)).toInt()
            
            var fullResponse = ""
            GeminiNetwork.queryGeminiStream(text, _userApiKey.value, _activeModelName.value)
                .collect { chunk ->
                    fullResponse += chunk
                    repository.updateMessage(ChatMessage(id = botMessageId, text = fullResponse, isUser = false))
                }
            
            _aiState.value = AiState.Idle
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
            repository.insertMessage(ChatMessage(text = "Hello ${_userName.value}! Welcome back. What can I automate for you today?", isUser = false))
        }
    }
}
