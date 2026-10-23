package com.example.data.repository

import com.example.data.api.GeminiApiClient
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.preferences.ConnectionStatus
import com.example.data.preferences.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(
    private val database: AppDatabase,
    val preferencesManager: PreferencesManager,
    private val apiClient: GeminiApiClient = GeminiApiClient()
) {
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()

    val activeConversations: Flow<List<ConversationEntity>> =
        conversationDao.getActiveConversations()

    val archivedConversations: Flow<List<ConversationEntity>> =
        conversationDao.getArchivedConversations()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingMessageId = MutableStateFlow<String?>(null)
    val streamingMessageId: StateFlow<String?> = _streamingMessageId.asStateFlow()

    fun searchConversations(query: String): Flow<List<ConversationEntity>> {
        return conversationDao.searchConversations(query)
    }

    suspend fun createConversation(model: String = preferencesManager.settings.value.activeModel): String =
        withContext(Dispatchers.IO) {
            val id = UUID.randomUUID().toString()
            val conv = ConversationEntity(
                id = id,
                title = "New Chat",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                model = model
            )
            conversationDao.insertConversation(conv)
            id
        }

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    suspend fun getConversation(conversationId: String): ConversationEntity? =
        withContext(Dispatchers.IO) {
            conversationDao.getConversationById(conversationId)
        }

    suspend fun sendMessage(
        conversationId: String,
        userPrompt: String,
        attachmentType: String? = null,
        attachmentData: String? = null,
        attachmentName: String? = null
    ) = withContext(Dispatchers.IO) {
        if (userPrompt.isBlank() && attachmentData.isNullOrBlank()) return@withContext

        // 1. Insert user message
        val userMsgId = UUID.randomUUID().toString()
        val userMsg = MessageEntity(
            id = userMsgId,
            conversationId = conversationId,
            role = "user",
            content = userPrompt.trim(),
            timestamp = System.currentTimeMillis(),
            attachmentType = attachmentType,
            attachmentData = attachmentData,
            attachmentName = attachmentName
        )
        messageDao.insertMessage(userMsg)

        // 2. Generate title if it's the first message or default "New Chat"
        val existing = conversationDao.getConversationById(conversationId)
        if (existing != null && (existing.title == "New Chat" || existing.title.isBlank())) {
            val titleCandidate = generateTitleFromPrompt(userPrompt)
            conversationDao.updateTitle(conversationId, titleCandidate)
        } else {
            conversationDao.updateTitle(conversationId, existing?.title ?: "Chat")
        }

        // 3. Initiate AI response
        executeAiGeneration(conversationId)
    }

    suspend fun regenerateLastResponse(conversationId: String) = withContext(Dispatchers.IO) {
        val messages = messageDao.getMessagesList(conversationId)
        val lastMsg = messages.lastOrNull() ?: return@withContext

        // If last message is assistant or error, remove it before regenerating
        if (lastMsg.role == "assistant" || lastMsg.role == "error") {
            messageDao.deleteMessageById(lastMsg.id)
        }

        executeAiGeneration(conversationId)
    }

    suspend fun editAndResubmitUserMessage(
        conversationId: String,
        messageId: String,
        newContent: String
    ) = withContext(Dispatchers.IO) {
        val messages = messageDao.getMessagesList(conversationId)
        val targetIndex = messages.indexOfFirst { it.id == messageId }
        if (targetIndex == -1) return@withContext

        // Delete all subsequent messages following this user message
        for (i in (targetIndex + 1) until messages.size) {
            messageDao.deleteMessageById(messages[i].id)
        }

        // Update target message
        messageDao.updateMessageContent(messageId, newContent)

        // Execute generation
        executeAiGeneration(conversationId)
    }

    private suspend fun executeAiGeneration(conversationId: String) {
        val apiKey = preferencesManager.getEffectiveApiKey()
        if (apiKey.isBlank()) {
            val errorMsgId = UUID.randomUUID().toString()
            messageDao.insertMessage(
                MessageEntity(
                    id = errorMsgId,
                    conversationId = conversationId,
                    role = "error",
                    content = "Connect your Gemini API to start chatting. Open Settings to configure your API key.",
                    timestamp = System.currentTimeMillis()
                )
            )
            return
        }

        val allMessages = messageDao.getMessagesList(conversationId)
        val conv = conversationDao.getConversationById(conversationId)
        val model = conv?.model ?: preferencesManager.settings.value.activeModel
        val settings = preferencesManager.settings.value

        val assistantMsgId = UUID.randomUUID().toString()
        val initialAssistantMsg = MessageEntity(
            id = assistantMsgId,
            conversationId = conversationId,
            role = "assistant",
            content = "",
            timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(initialAssistantMsg)

        _isGenerating.value = true
        _streamingMessageId.value = assistantMsgId

        val contentAccumulator = StringBuilder()

        val result = apiClient.generateContentStream(
            apiKey = apiKey,
            model = model,
            history = allMessages.filter { it.role != "error" },
            systemInstruction = settings.systemInstruction,
            temperature = settings.temperature,
            maxTokens = settings.maxOutputTokens,
            onChunk = { chunk ->
                contentAccumulator.append(chunk)
                CoroutineScope(Dispatchers.IO).launch {
                    messageDao.updateMessageContent(assistantMsgId, contentAccumulator.toString())
                }
            }
        )

        _isGenerating.value = false
        _streamingMessageId.value = null

        result.onFailure { error ->
            val accumulated = contentAccumulator.toString()
            if (accumulated.isBlank()) {
                // Replace empty assistant message with clear error message
                messageDao.deleteMessageById(assistantMsgId)
                val errorId = UUID.randomUUID().toString()
                messageDao.insertMessage(
                    MessageEntity(
                        id = errorId,
                        conversationId = conversationId,
                        role = "error",
                        content = error.message ?: "AI generation failed. Please try again.",
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else {
                // Keep partial text and append notification
                messageDao.updateMessageContent(
                    assistantMsgId,
                    "$accumulated\n\n*(Stopped or interrupted: ${error.message})*"
                )
            }
        }
    }

    fun stopActiveGeneration() {
        apiClient.cancelActiveRequest()
        _isGenerating.value = false
        _streamingMessageId.value = null
    }

    suspend fun renameConversation(conversationId: String, newTitle: String) =
        withContext(Dispatchers.IO) {
            val title = newTitle.trim().ifBlank { "Untitled Chat" }
            conversationDao.updateTitle(conversationId, title)
        }

    suspend fun togglePin(conversationId: String, isPinned: Boolean) =
        withContext(Dispatchers.IO) {
            conversationDao.updatePinStatus(conversationId, isPinned)
        }

    suspend fun toggleArchive(conversationId: String, isArchived: Boolean) =
        withContext(Dispatchers.IO) {
            conversationDao.updateArchiveStatus(conversationId, isArchived)
        }

    suspend fun deleteConversation(conversationId: String) = withContext(Dispatchers.IO) {
        conversationDao.deleteConversationById(conversationId)
    }

    suspend fun clearAllConversations() = withContext(Dispatchers.IO) {
        conversationDao.deleteAllConversations()
    }

    suspend fun updateFeedback(messageId: String, isPositive: Boolean?) =
        withContext(Dispatchers.IO) {
            messageDao.updateFeedback(messageId, isPositive)
        }

    suspend fun testApiConnection(): ConnectionStatus = withContext(Dispatchers.IO) {
        preferencesManager.setConnectionStatus(ConnectionStatus.CHECKING)
        val apiKey = preferencesManager.getEffectiveApiKey()
        val status = apiClient.testConnection(apiKey, preferencesManager.settings.value.activeModel)
        preferencesManager.setConnectionStatus(status)
        status
    }

    suspend fun exportConversationMarkdown(conversationId: String): String =
        withContext(Dispatchers.IO) {
            val conv = conversationDao.getConversationById(conversationId)
            val messages = messageDao.getMessagesList(conversationId)
            val sb = StringBuilder()
            sb.append("# ${conv?.title ?: "YOUNOVA Chat"}\n")
            sb.append("*Exported from YOUNOVA AI — ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}*\n\n---\n\n")

            for (m in messages) {
                val author = if (m.role == "user") "### 👤 User" else "### ⚛️ YOUNOVA"
                sb.append("$author\n\n")
                if (!m.attachmentName.isNullOrBlank()) {
                    sb.append("📎 *Attachment: ${m.attachmentName}*\n\n")
                }
                sb.append("${m.content}\n\n---\n\n")
            }
            sb.toString()
        }

    private fun generateTitleFromPrompt(prompt: String): String {
        val clean = prompt.lineSequence().firstOrNull()?.trim() ?: "Chat"
        return if (clean.length > 36) {
            clean.take(33).trim() + "..."
        } else {
            clean
        }
    }
}
