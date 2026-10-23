package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.preferences.ConnectionStatus
import com.example.ui.components.Composer
import com.example.ui.components.DeleteConversationDialog
import com.example.ui.components.ExportMarkdownDialog
import com.example.ui.components.MessageItem
import com.example.ui.components.RenameConversationDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SidebarDrawer
import com.example.ui.components.WelcomeScreen
import com.example.ui.components.YounovaAtomSymbol
import com.example.ui.components.YounovaWordmark
import com.example.ui.theme.YounovaBorder
import com.example.ui.theme.YounovaBrightPurple
import com.example.ui.theme.YounovaPrimaryPurple
import com.example.ui.theme.YounovaSuccess
import com.example.ui.theme.YounovaSurfaceCard
import com.example.ui.theme.YounovaSurfaceDark
import com.example.ui.theme.YounovaSurfaceElevated
import com.example.ui.theme.YounovaTextMuted
import com.example.ui.theme.YounovaTextPrimary
import com.example.ui.theme.YounovaTextSecondary
import com.example.ui.theme.YounovaWarning
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val activeConversations by viewModel.filteredConversations.collectAsStateWithLifecycle()
    val archivedConversations by viewModel.archivedConversations.collectAsStateWithLifecycle()
    val currentConvId by viewModel.currentConversationId.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val userPromptInput by viewModel.userPromptInput.collectAsStateWithLifecycle()
    val currentAttachment by viewModel.currentAttachment.collectAsStateWithLifecycle()

    // Dialogs state
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val isRenameDialogOpen by viewModel.isRenameDialogOpen.collectAsStateWithLifecycle()
    val renameText by viewModel.renameText.collectAsStateWithLifecycle()
    val isDeleteConfirmDialogOpen by viewModel.isDeleteConfirmDialogOpen.collectAsStateWithLifecycle()
    val conversationToDelete by viewModel.conversationToDelete.collectAsStateWithLifecycle()
    val isExportDialogOpen by viewModel.isExportDialogOpen.collectAsStateWithLifecycle()
    val exportMarkdown by viewModel.exportMarkdown.collectAsStateWithLifecycle()
    val isTestingConnection by viewModel.isTestingConnection.collectAsStateWithLifecycle()
    val testResultMessage by viewModel.testResultMessage.collectAsStateWithLifecycle()

    var modelMenuExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll on new message or chunk
    LaunchedEffect(currentMessages.size, isGenerating) {
        if (currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentMessages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Transparent,
                drawerContentColor = YounovaTextPrimary
            ) {
                SidebarDrawer(
                    conversations = activeConversations,
                    archivedConversations = archivedConversations,
                    selectedId = currentConvId,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.searchQuery.value = it },
                    onSelectConversation = { id -> viewModel.selectConversation(id) },
                    onNewChat = { viewModel.startNewChat() },
                    onRename = { conv -> viewModel.openRenameDialog(conv) },
                    onTogglePin = { conv -> viewModel.togglePin(conv) },
                    onToggleArchive = { conv -> viewModel.toggleArchive(conv) },
                    onExport = { conv -> viewModel.exportConversation(conv) },
                    onDelete = { conv -> viewModel.openDeleteDialog(conv) },
                    onOpenSettings = { viewModel.isSettingsOpen.value = true },
                    onCloseDrawer = {
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            containerColor = Color.Black,
            topBar = {
                // Header Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YounovaSurfaceDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Drawer toggle button
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open sidebar",
                                tint = YounovaTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Logo Wordmark
                        YounovaWordmark(iconSize = 22.dp, fontSize = 16)

                        Spacer(modifier = Modifier.weight(1f))

                        // Model Selector Chip
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(YounovaSurfaceCard)
                                    .border(1.dp, YounovaBorder, RoundedCornerShape(16.dp))
                                    .clickable { modelMenuExpanded = true }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Connection status dot
                                val statusDotColor = when (connectionStatus) {
                                    ConnectionStatus.CONNECTED -> YounovaSuccess
                                    ConnectionStatus.CHECKING -> YounovaBrightPurple
                                    ConnectionStatus.NOT_CONFIGURED -> YounovaTextMuted
                                    ConnectionStatus.RATE_LIMIT, ConnectionStatus.QUOTA_EXCEEDED -> YounovaWarning
                                    else -> com.example.ui.theme.YounovaError
                                }

                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(statusDotColor, CircleShape)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                val shortModelName = when (settings.activeModel) {
                                    "gemini-3.5-flash" -> "3.5 Flash"
                                    "gemini-3.1-pro-preview" -> "3.1 Pro"
                                    "gemini-3.1-flash-lite-preview" -> "Flash Lite"
                                    "gemini-flash-latest" -> "Flash"
                                    else -> settings.activeModel.take(9)
                                }

                                Text(
                                    text = shortModelName,
                                    color = YounovaTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = YounovaTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = modelMenuExpanded,
                                onDismissRequest = { modelMenuExpanded = false },
                                modifier = Modifier
                                    .background(YounovaSurfaceElevated)
                                    .border(1.dp, YounovaBorder)
                            ) {
                                listOf(
                                    Pair("gemini-3.5-flash", "Gemini 3.5 Flash (Fast)"),
                                    Pair("gemini-3.1-pro-preview", "Gemini 3.1 Pro (Advanced)"),
                                    Pair("gemini-3.1-flash-lite-preview", "Gemini 3.1 Flash Lite"),
                                    Pair("gemini-flash-latest", "Gemini Flash Latest")
                                ).forEach { (id, label) ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = label,
                                                color = if (settings.activeModel == id) YounovaBrightPurple else YounovaTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = if (settings.activeModel == id) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.setModel(id)
                                            modelMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // New Chat Button
                        IconButton(
                            onClick = { viewModel.startNewChat() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New chat",
                                tint = YounovaBrightPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Settings Button
                        IconButton(
                            onClick = { viewModel.isSettingsOpen.value = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = YounovaTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Divider(color = YounovaBorder, thickness = 1.dp)
                }
            },
            bottomBar = {
                Composer(
                    input = userPromptInput,
                    onInputChange = { viewModel.userPromptInput.value = it },
                    onSend = { viewModel.sendMessage() },
                    isGenerating = isGenerating,
                    onStop = { viewModel.stopGeneration() },
                    attachment = currentAttachment,
                    onAttachmentSelected = { viewModel.currentAttachment.value = it },
                    enterToSend = settings.enterToSend
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
            ) {
                if (currentMessages.isEmpty()) {
                    // Show Welcome screen with suggestion cards
                    WelcomeScreen(
                        onSelectSuggestion = { prompt ->
                            viewModel.sendMessage(prompt)
                        }
                    )
                } else {
                    // Show conversation messages
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp)
                    ) {
                        items(currentMessages, key = { it.id }) { message ->
                            val isThisStreaming = isGenerating && message.id == currentMessages.lastOrNull()?.id
                            MessageItem(
                                message = message,
                                isStreaming = isThisStreaming,
                                onRegenerate = { viewModel.regenerateLastResponse() },
                                onEdit = { newText ->
                                    viewModel.editAndResubmit(message.id, newText)
                                },
                                onFeedback = { isPos ->
                                    viewModel.updateFeedback(message.id, isPos)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    SettingsDialog(
        isOpen = isSettingsOpen,
        onDismiss = { viewModel.isSettingsOpen.value = false },
        settings = settings,
        connectionStatus = connectionStatus,
        isTestingConnection = isTestingConnection,
        testResultMessage = testResultMessage,
        onSaveApiKey = { viewModel.saveCustomApiKey(it) },
        onRemoveApiKey = { viewModel.removeCustomApiKey() },
        onTestConnection = { viewModel.testConnection() },
        onModelChange = { viewModel.setModel(it) },
        onSystemInstructionChange = { viewModel.preferencesManager.updateSystemInstruction(it) },
        onTemperatureChange = { viewModel.preferencesManager.updateTemperature(it) },
        onMaxTokensChange = { viewModel.preferencesManager.updateMaxTokens(it) },
        onEnterToSendChange = { viewModel.preferencesManager.updateEnterToSend(it) },
        onReducedMotionChange = { viewModel.preferencesManager.updateReducedMotion(it) },
        onClearAllConversations = { viewModel.clearAllConversations() }
    )

    RenameConversationDialog(
        isOpen = isRenameDialogOpen,
        titleText = renameText,
        onTitleChange = { viewModel.renameText.value = it },
        onConfirm = { viewModel.confirmRename() },
        onDismiss = { viewModel.isRenameDialogOpen.value = false }
    )

    DeleteConversationDialog(
        isOpen = isDeleteConfirmDialogOpen,
        conversationTitle = conversationToDelete?.title ?: "",
        onConfirm = { viewModel.confirmDelete() },
        onDismiss = { viewModel.isDeleteConfirmDialogOpen.value = false }
    )

    ExportMarkdownDialog(
        isOpen = isExportDialogOpen,
        markdownContent = exportMarkdown,
        onDismiss = { viewModel.isExportDialogOpen.value = false }
    )
}
