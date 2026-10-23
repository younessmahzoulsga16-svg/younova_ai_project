package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ConnectionStatus(val label: String) {
    NOT_CONFIGURED("Not Configured"),
    CHECKING("Checking Connection..."),
    CONNECTED("Connected"),
    INVALID_KEY("Invalid API Key"),
    QUOTA_EXCEEDED("Quota Exceeded"),
    RATE_LIMIT("Rate Limit Reached"),
    NETWORK_ERROR("Connection Failed"),
    SERVICE_UNAVAILABLE("Service Temporarily Unavailable")
}

data class YounovaSettings(
    val customApiKey: String = "",
    val activeModel: String = "gemini-3.5-flash",
    val systemInstruction: String = "You are YOUNOVA, a professional, highly capable, and intelligent AI workspace assistant. Your AI. Your Universe.",
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 4096,
    val enterToSend: Boolean = true,
    val reducedMotion: Boolean = false
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("younova_preferences", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<YounovaSettings> = _settings.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.CHECKING)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private fun loadSettings(): YounovaSettings {
        return YounovaSettings(
            customApiKey = prefs.getString(KEY_CUSTOM_API_KEY, "") ?: "",
            activeModel = prefs.getString(KEY_ACTIVE_MODEL, "gemini-3.5-flash") ?: "gemini-3.5-flash",
            systemInstruction = prefs.getString(
                KEY_SYSTEM_INSTRUCTION,
                "You are YOUNOVA, a professional, highly capable, and intelligent AI workspace assistant. Your AI. Your Universe."
            ) ?: "",
            temperature = prefs.getFloat(KEY_TEMPERATURE, 0.7f),
            maxOutputTokens = prefs.getInt(KEY_MAX_TOKENS, 4096),
            enterToSend = prefs.getBoolean(KEY_ENTER_TO_SEND, true),
            reducedMotion = prefs.getBoolean(KEY_REDUCED_MOTION, false)
        )
    }

    fun getEffectiveApiKey(): String {
        val customKey = _settings.value.customApiKey.trim()
        if (customKey.isNotEmpty()) {
            return customKey
        }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Exception) {
            ""
        }
        return if (buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    fun hasAnyApiKey(): Boolean {
        return getEffectiveApiKey().isNotEmpty()
    }

    fun isUsingCustomKey(): Boolean {
        return _settings.value.customApiKey.trim().isNotEmpty()
    }

    fun saveCustomApiKey(key: String) {
        val trimmed = key.trim()
        prefs.edit().putString(KEY_CUSTOM_API_KEY, trimmed).apply()
        _settings.value = _settings.value.copy(customApiKey = trimmed)
    }

    fun removeCustomApiKey() {
        prefs.edit().remove(KEY_CUSTOM_API_KEY).apply()
        _settings.value = _settings.value.copy(customApiKey = "")
    }

    fun updateActiveModel(model: String) {
        prefs.edit().putString(KEY_ACTIVE_MODEL, model).apply()
        _settings.value = _settings.value.copy(activeModel = model)
    }

    fun updateSystemInstruction(instruction: String) {
        prefs.edit().putString(KEY_SYSTEM_INSTRUCTION, instruction).apply()
        _settings.value = _settings.value.copy(systemInstruction = instruction)
    }

    fun updateTemperature(temp: Float) {
        prefs.edit().putFloat(KEY_TEMPERATURE, temp).apply()
        _settings.value = _settings.value.copy(temperature = temp)
    }

    fun updateMaxTokens(tokens: Int) {
        prefs.edit().putInt(KEY_MAX_TOKENS, tokens).apply()
        _settings.value = _settings.value.copy(maxOutputTokens = tokens)
    }

    fun updateEnterToSend(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENTER_TO_SEND, enabled).apply()
        _settings.value = _settings.value.copy(enterToSend = enabled)
    }

    fun updateReducedMotion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCED_MOTION, enabled).apply()
        _settings.value = _settings.value.copy(reducedMotion = enabled)
    }

    fun setConnectionStatus(status: ConnectionStatus) {
        _connectionStatus.value = status
    }

    companion object {
        private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
        private const val KEY_ACTIVE_MODEL = "active_model"
        private const val KEY_SYSTEM_INSTRUCTION = "system_instruction"
        private const val KEY_TEMPERATURE = "temperature"
        private const val KEY_MAX_TOKENS = "max_tokens"
        private const val KEY_ENTER_TO_SEND = "enter_to_send"
        private const val KEY_REDUCED_MOTION = "reduced_motion"
    }
}
