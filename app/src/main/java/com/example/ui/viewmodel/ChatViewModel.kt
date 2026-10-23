package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.preferences.ConnectionStatus
import com.example.data.preferences.PreferencesManager
import com.example.data.preferences.YounovaSettings
import com.example.data.repository.ChatRepository
import com.example.ui.model.AttachmentItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val preferencesManager = PreferencesManager(application)
    val repository = ChatRepository(database, preferencesManager)

    val settings: StateFlow<YounovaSettings> = preferencesManager.settings
    val connectionStatus: StateFlow<ConnectionStatus> = preferencesManager.connectionStatus
    val isGenerating: StateFlow<Boolean> = repository.isGenerating

    val activeConversations: StateFlow<List<ConversationEntity>> = repository.activeConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedConversations: StateFlow<List<ConversationEntity>> = repository.archivedConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<MessageEntity>> = _currentConversationId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery = MutableStateFlow("")

    val filteredConversations: StateFlow<List<ConversationEntity>> = combine(
        activeConversations,
        searchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter { it.title.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPromptInput = MutableStateFlow("")
    val currentAttachment = MutableStateFlow<AttachmentItem?>(null)

    // Dialog & UI State
    val isSettingsOpen = MutableStateFlow(false)
    val isRenameDialogOpen = MutableStateFlow(false)
    val conversationToRename = MutableStateFlow<ConversationEntity?>(null)
    val renameText = MutableStateFlow("")

    val isDeleteConfirmDialogOpen = MutableStateFlow(false)
    val conversationToDelete = MutableStateFlow<ConversationEntity?>(null)

    val isExportDialogOpen = MutableStateFlow(false)
    val exportMarkdown = MutableStateFlow("")

    val isTestingConnection = MutableStateFlow(false)
    val testResultMessage = MutableStateFlow<String?>(null)

    init {
        // Startup connection verification
        viewModelScope.launch {
            if (preferencesManager.hasAnyApiKey()) {
                repository.testApiConnection()
            } else {
                preferencesManager.setConnectionStatus(ConnectionStatus.NOT_CONFIGURED)
            }
        }
    }

    fun selectConversation(id: String) {
        _currentConversationId.value = id
    }

    fun startNewChat() {
        _currentConversationId.value = null
        userPromptInput.value = ""
        currentAttachment.value = null
    }

    fun sendMessage(customPrompt: String? = null) {
        val promptToSend = customPrompt ?: userPromptInput.value
        val attachment = currentAttachment.value
        if (promptToSend.isBlank() && attachment == null) return

        viewModelScope.launch {
            var convId = _currentConversationId.value
            if (convId == null) {
                convId = repository.createConversation()
                _currentConversationId.value = convId
            }

            userPromptInput.value = ""
            currentAttachment.value = null

            repository.sendMessage(
                conversationId = convId,
                userPrompt = promptToSend,
                attachmentType = attachment?.type,
                attachmentData = attachment?.data,
                attachmentName = attachment?.name
            )
        }
    }

    fun regenerateLastResponse() {
        val convId = _currentConversationId.value ?: return
        viewModelScope.launch {
            repository.regenerateLastResponse(convId)
        }
    }

    fun editAndResubmit(messageId: String, newContent: String) {
        val convId = _currentConversationId.value ?: return
        viewModelScope.launch {
            repository.editAndResubmitUserMessage(convId, messageId, newContent)
        }
    }

    fun stopGeneration() {
        repository.stopActiveGeneration()
    }

    fun openRenameDialog(conversation: ConversationEntity) {
        conversationToRename.value = conversation
        renameText.value = conversation.title
        isRenameDialogOpen.value = true
    }

    fun confirmRename() {
        val conv = conversationToRename.value ?: return
        val newTitle = renameText.value.trim()
        if (newTitle.isNotEmpty()) {
            viewModelScope.launch {
                repository.renameConversation(conv.id, newTitle)
                isRenameDialogOpen.value = false
                conversationToRename.value = null
            }
        }
    }

    fun togglePin(conversation: ConversationEntity) {
        viewModelScope.launch {
            repository.togglePin(conversation.id, !conversation.isPinned)
        }
    }

    fun toggleArchive(conversation: ConversationEntity) {
        viewModelScope.launch {
            val willArchive = !conversation.isArchived
            repository.toggleArchive(conversation.id, willArchive)
            if (willArchive && _currentConversationId.value == conversation.id) {
                startNewChat()
            }
        }
    }

    fun openDeleteDialog(conversation: ConversationEntity) {
        conversationToDelete.value = conversation
        isDeleteConfirmDialogOpen.value = true
    }

    fun confirmDelete() {
        val conv = conversationToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteConversation(conv.id)
            if (_currentConversationId.value == conv.id) {
                startNewChat()
            }
            isDeleteConfirmDialogOpen.value = false
            conversationToDelete.value = null
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            repository.clearAllConversations()
            startNewChat()
        }
    }

    fun exportConversation(conversation: ConversationEntity) {
        viewModelScope.launch {
            val md = repository.exportConversationMarkdown(conversation.id)
            exportMarkdown.value = md
            isExportDialogOpen.value = true
        }
    }

    fun updateFeedback(messageId: String, isPositive: Boolean?) {
        viewModelScope.launch {
            repository.updateFeedback(messageId, isPositive)
        }
    }

    fun setModel(model: String) {
        preferencesManager.updateActiveModel(model)
    }

    fun saveCustomApiKey(key: String) {
        preferencesManager.saveCustomApiKey(key)
        testConnection()
    }

    fun removeCustomApiKey() {
        preferencesManager.removeCustomApiKey()
        viewModelScope.launch {
            if (preferencesManager.hasAnyApiKey()) {
                repository.testApiConnection()
            } else {
                preferencesManager.setConnectionStatus(ConnectionStatus.NOT_CONFIGURED)
            }
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            isTestingConnection.value = true
            testResultMessage.value = null
            val status = repository.testApiConnection()
            isTestingConnection.value = false
            testResultMessage.value = when (status) {
                ConnectionStatus.CONNECTED -> "Connection successful! Gemini API is active."
                ConnectionStatus.INVALID_KEY -> "Authentication failed. Check your API key."
                ConnectionStatus.QUOTA_EXCEEDED -> "API quota exceeded for this key."
                ConnectionStatus.RATE_LIMIT -> "Rate limit reached. Please wait."
                ConnectionStatus.NOT_CONFIGURED -> "No API key configured."
                ConnectionStatus.NETWORK_ERROR -> "Network error reaching Google API."
                else -> "Connection test failed: ${status.label}"
            }
        }
    }
}
