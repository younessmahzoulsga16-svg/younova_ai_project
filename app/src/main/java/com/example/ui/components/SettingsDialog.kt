package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.preferences.ConnectionStatus
import com.example.data.preferences.YounovaSettings
import com.example.ui.theme.YounovaBorder
import com.example.ui.theme.YounovaBorderFocused
import com.example.ui.theme.YounovaBrightPurple
import com.example.ui.theme.YounovaDeepPurple
import com.example.ui.theme.YounovaError
import com.example.ui.theme.YounovaPrimaryPurple
import com.example.ui.theme.YounovaSuccess
import com.example.ui.theme.YounovaSurfaceCard
import com.example.ui.theme.YounovaSurfaceDark
import com.example.ui.theme.YounovaSurfaceElevated
import com.example.ui.theme.YounovaTextMuted
import com.example.ui.theme.YounovaTextPrimary
import com.example.ui.theme.YounovaTextSecondary
import com.example.ui.theme.YounovaWarning

@Composable
fun SettingsDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    settings: YounovaSettings,
    connectionStatus: ConnectionStatus,
    isTestingConnection: Boolean,
    testResultMessage: String?,
    onSaveApiKey: (String) -> Unit,
    onRemoveApiKey: () -> Unit,
    onTestConnection: () -> Unit,
    onModelChange: (String) -> Unit,
    onSystemInstructionChange: (String) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onMaxTokensChange: (Int) -> Unit,
    onEnterToSendChange: (Boolean) -> Unit,
    onReducedMotionChange: (Boolean) -> Unit,
    onClearAllConversations: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("API Key", "AI Models", "General", "Data & Privacy", "About")

    var keyInput by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }
    var showRemoveKeyConfirm by remember { mutableStateOf(false) }
    var showClearChatsConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, YounovaBorder, RoundedCornerShape(16.dp)),
            color = YounovaSurfaceDark
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        YounovaAtomSymbol(size = 24.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Settings",
                            color = YounovaTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = YounovaTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Divider(color = YounovaBorder)

                // Scrollable Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = YounovaSurfaceDark,
                    contentColor = YounovaBrightPurple,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = YounovaPrimaryPurple,
                            height = 2.5.dp
                        )
                    },
                    divider = { Divider(color = YounovaBorder) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) YounovaBrightPurple else YounovaTextMuted,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                // Tab Content Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        when (selectedTab) {
                            0 -> {
                                // API CONFIGURATION
                                ApiConfigTab(
                                    settings = settings,
                                    connectionStatus = connectionStatus,
                                    isTestingConnection = isTestingConnection,
                                    testResultMessage = testResultMessage,
                                    keyInput = keyInput,
                                    onKeyInputChange = { keyInput = it },
                                    isKeyVisible = isKeyVisible,
                                    onToggleKeyVisibility = { isKeyVisible = !isKeyVisible },
                                    onSaveKey = {
                                        if (keyInput.isNotBlank()) {
                                            onSaveApiKey(keyInput)
                                            keyInput = ""
                                        }
                                    },
                                    onRequestRemoveKey = { showRemoveKeyConfirm = true },
                                    onTestConnection = onTestConnection,
                                    onOpenAiStudio = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/"))
                                        context.startActivity(intent)
                                    }
                                )
                            }
                            1 -> {
                                // AI MODELS & PARAMETERS
                                AiModelsTab(
                                    settings = settings,
                                    onModelChange = onModelChange,
                                    onSystemInstructionChange = onSystemInstructionChange,
                                    onTemperatureChange = onTemperatureChange,
                                    onMaxTokensChange = onMaxTokensChange
                                )
                            }
                            2 -> {
                                // GENERAL
                                GeneralTab(
                                    settings = settings,
                                    onEnterToSendChange = onEnterToSendChange,
                                    onReducedMotionChange = onReducedMotionChange
                                )
                            }
                            3 -> {
                                // DATA & PRIVACY
                                DataPrivacyTab(
                                    onRequestClearAll = { showClearChatsConfirm = true }
                                )
                            }
                            4 -> {
                                // ABOUT
                                AboutTab()
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation: Remove API Key
    if (showRemoveKeyConfirm) {
        AlertDialog(
            onDismissRequest = { showRemoveKeyConfirm = false },
            title = { Text("Remove Saved API Key?", color = YounovaTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to remove your custom Gemini API key? YOUNOVA will revert to server-provided keys or prompt for a key.",
                    color = YounovaTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveApiKey()
                        showRemoveKeyConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YounovaError)
                ) {
                    Text("Remove Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveKeyConfirm = false }) {
                    Text("Cancel", color = YounovaTextSecondary)
                }
            },
            containerColor = YounovaSurfaceElevated
        )
    }

    // Confirmation: Clear All Chats
    if (showClearChatsConfirm) {
        AlertDialog(
            onDismissRequest = { showClearChatsConfirm = false },
            title = { Text("Delete All Chats?", color = YounovaError, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This action cannot be undone. All your saved conversations and message histories stored on this device will be permanently deleted.",
                    color = YounovaTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllConversations()
                        showClearChatsConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YounovaError)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatsConfirm = false }) {
                    Text("Cancel", color = YounovaTextSecondary)
                }
            },
            containerColor = YounovaSurfaceElevated
        )
    }
}

@Composable
fun ApiConfigTab(
    settings: YounovaSettings,
    connectionStatus: ConnectionStatus,
    isTestingConnection: Boolean,
    testResultMessage: String?,
    keyInput: String,
    onKeyInputChange: (String) -> Unit,
    isKeyVisible: Boolean,
    onToggleKeyVisibility: () -> Unit,
    onSaveKey: () -> Unit,
    onRequestRemoveKey: () -> Unit,
    onTestConnection: () -> Unit,
    onOpenAiStudio: () -> Unit
) {
    val context = LocalContext.current
    val hasCustomKey = settings.customApiKey.isNotBlank()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Provider Info Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(YounovaSurfaceCard)
                .border(1.dp, YounovaBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = YounovaBrightPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google Gemini AI Provider",
                        color = YounovaTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Connect YOUNOVA to Google's official Gemini AI models. Your key is stored securely on-device and used for direct API communication.",
                    color = YounovaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onOpenAiStudio,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YounovaBrightPurple),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(YounovaPrimaryPurple))
                ) {
                    Text("Get API Key at Google AI Studio", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Connection Status Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(YounovaSurfaceElevated)
                .border(1.dp, YounovaBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusColor = when (connectionStatus) {
                ConnectionStatus.CONNECTED -> YounovaSuccess
                ConnectionStatus.CHECKING -> YounovaBrightPurple
                ConnectionStatus.NOT_CONFIGURED -> YounovaTextMuted
                ConnectionStatus.RATE_LIMIT, ConnectionStatus.QUOTA_EXCEEDED -> YounovaWarning
                else -> YounovaError
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(statusColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Status: ${connectionStatus.label}",
                    color = YounovaTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (!testResultMessage.isNullOrBlank()) {
                    Text(
                        text = testResultMessage,
                        color = statusColor,
                        fontSize = 11.sp
                    )
                }
            }

            if (isTestingConnection) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = YounovaBrightPurple,
                    strokeWidth = 2.dp
                )
            } else {
                TextButton(onClick = onTestConnection) {
                    Text("Test Connection", color = YounovaBrightPurple, fontSize = 12.sp)
                }
            }
        }

        // Saved Key Indicator if key is present
        if (hasCustomKey) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(YounovaSurfaceCard)
                    .border(1.dp, YounovaSuccess.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = YounovaSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "API key saved: ••••••••••••${settings.customApiKey.takeLast(4)}",
                            color = YounovaTextPrimary,
                            fontSize = 13.sp
                        )
                    }

                    TextButton(onClick = onRequestRemoveKey) {
                        Text("Remove Key", color = YounovaError, fontSize = 12.sp)
                    }
                }
            }
        }

        // Input New / Update Key
        Column {
            Text(
                text = if (hasCustomKey) "Update Gemini API Key" else "Enter Gemini API Key",
                color = YounovaTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = keyInput,
                onValueChange = onKeyInputChange,
                placeholder = {
                    Text(
                        text = if (hasCustomKey) "Paste new key to replace existing" else "AIzaSy...",
                        color = YounovaTextMuted,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Paste button
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                            if (!clip.isNullOrBlank()) {
                                onKeyInputChange(clip.trim())
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = YounovaTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Toggle visibility
                        IconButton(onClick = onToggleKeyVisibility) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle visibility",
                                tint = YounovaTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YounovaPrimaryPurple,
                    unfocusedBorderColor = YounovaBorder,
                    cursorColor = YounovaBrightPurple,
                    focusedTextColor = YounovaTextPrimary,
                    unfocusedTextColor = YounovaTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onSaveKey,
                enabled = keyInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YounovaPrimaryPurple,
                    contentColor = YounovaTextPrimary
                )
            ) {
                Text(if (hasCustomKey) "Update Key" else "Save Key")
            }
        }
    }
}

@Composable
fun AiModelsTab(
    settings: YounovaSettings,
    onModelChange: (String) -> Unit,
    onSystemInstructionChange: (String) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onMaxTokensChange: (Int) -> Unit
) {
    val supportedModels = listOf(
        Pair("gemini-3.5-flash", "Gemini 3.5 Flash — Fast & efficient for reasoning, chat and coding (Recommended)"),
        Pair("gemini-3.1-pro-preview", "Gemini 3.1 Pro Preview — Advanced deep reasoning and complex coding"),
        Pair("gemini-3.1-flash-lite-preview", "Gemini 3.1 Flash Lite Preview — Ultra low latency responses"),
        Pair("gemini-flash-latest", "Gemini Flash Latest — General everyday tasks")
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Model Selection
        Column {
            Text(
                text = "Active Gemini Model",
                color = YounovaTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            for ((modelId, description) in supportedModels) {
                val isSelected = settings.activeModel == modelId
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) YounovaPrimaryPurple.copy(alpha = 0.15f) else YounovaSurfaceCard)
                        .border(
                            1.dp,
                            if (isSelected) YounovaPrimaryPurple else YounovaBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onModelChange(modelId) }
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isSelected) YounovaBrightPurple else Color.Transparent,
                                        CircleShape
                                    )
                                    .border(1.dp, if (isSelected) YounovaBrightPurple else YounovaTextMuted, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = modelId,
                                color = if (isSelected) YounovaBrightPurple else YounovaTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            color = YounovaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Temperature Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Temperature: ${String.format("%.1f", settings.temperature)}",
                    color = YounovaTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (settings.temperature < 0.4f) "Precise" else if (settings.temperature > 0.8f) "Creative" else "Balanced",
                    color = YounovaBrightPurple,
                    fontSize = 12.sp
                )
            }

            Slider(
                value = settings.temperature,
                onValueChange = onTemperatureChange,
                valueRange = 0.0f..1.0f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = YounovaBrightPurple,
                    activeTrackColor = YounovaPrimaryPurple,
                    inactiveTrackColor = YounovaBorder
                )
            )
        }

        // Max Tokens
        Column {
            Text(
                text = "Max Output Tokens: ${settings.maxOutputTokens}",
                color = YounovaTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1024, 2048, 4096, 8192).forEach { tokens ->
                    val isSelected = settings.maxOutputTokens == tokens
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) YounovaPrimaryPurple else YounovaSurfaceCard)
                            .border(1.dp, if (isSelected) YounovaBrightPurple else YounovaBorder, RoundedCornerShape(6.dp))
                            .clickable { onMaxTokensChange(tokens) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$tokens",
                            color = if (isSelected) YounovaTextPrimary else YounovaTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // System Instruction
        Column {
            Text(
                text = "System Instruction",
                color = YounovaTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = settings.systemInstruction,
                onValueChange = onSystemInstructionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YounovaPrimaryPurple,
                    unfocusedBorderColor = YounovaBorder,
                    cursorColor = YounovaBrightPurple,
                    focusedTextColor = YounovaTextPrimary,
                    unfocusedTextColor = YounovaTextPrimary
                )
            )
        }
    }
}

@Composable
fun GeneralTab(
    settings: YounovaSettings,
    onEnterToSendChange: (Boolean) -> Unit,
    onReducedMotionChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Theme description
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(YounovaSurfaceCard)
                .border(1.dp, YounovaBorder, RoundedCornerShape(10.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Visual Theme",
                    color = YounovaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Pure Black (#000000) with Purple Atomic accents",
                    color = YounovaTextSecondary,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Black, CircleShape)
                    .border(2.dp, YounovaPrimaryPurple, CircleShape)
            )
        }

        // Enter to Send
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Press Enter to Send",
                    color = YounovaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "When active, Enter key submits prompt directly. Shift+Enter adds line break.",
                    color = YounovaTextMuted,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = settings.enterToSend,
                onCheckedChange = onEnterToSendChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = YounovaBrightPurple,
                    checkedTrackColor = YounovaDeepPurple
                )
            )
        }

        Divider(color = YounovaBorder)

        // Reduced Motion
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Reduced Motion",
                    color = YounovaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Minimize streaming pulses and animated UI transitions.",
                    color = YounovaTextMuted,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = settings.reducedMotion,
                onCheckedChange = onReducedMotionChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = YounovaBrightPurple,
                    checkedTrackColor = YounovaDeepPurple
                )
            )
        }
    }
}

@Composable
fun DataPrivacyTab(
    onRequestClearAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(YounovaSurfaceCard)
                .border(1.dp, YounovaBorder, RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = YounovaBrightPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Local Device Storage",
                        color = YounovaTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "All your conversations, message history, and custom settings are stored locally on your device in a secure SQLite Room database. They are not transmitted to any third-party telemetry or cloud servers other than direct Gemini API requests.",
                    color = YounovaTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Button(
            onClick = onRequestClearAll,
            colors = ButtonDefaults.buttonColors(containerColor = YounovaError),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete All Local Chat History", fontSize = 13.sp)
        }
    }
}

@Composable
fun AboutTab() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        YounovaAtomSymbol(size = 56.dp)

        Text(
            text = "YOUNOVA",
            color = YounovaTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Text(
            text = "Your AI. Your Universe.",
            color = YounovaBrightPurple,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Version 1.0.0",
            color = YounovaTextMuted,
            fontSize = 12.sp
        )

        Divider(color = YounovaBorder, modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Powered by Google Gemini official API intelligence.\nDesigned with a pure black aesthetic and atomic purple accents.",
            color = YounovaTextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
