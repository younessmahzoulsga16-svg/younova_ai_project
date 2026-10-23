package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.ui.theme.YounovaBorder
import com.example.ui.theme.YounovaBrightPurple
import com.example.ui.theme.YounovaDeepPurple
import com.example.ui.theme.YounovaError
import com.example.ui.theme.YounovaPrimaryPurple
import com.example.ui.theme.YounovaSurfaceCard
import com.example.ui.theme.YounovaSurfaceDark
import com.example.ui.theme.YounovaSurfaceElevated
import com.example.ui.theme.YounovaTextMuted
import com.example.ui.theme.YounovaTextPrimary
import com.example.ui.theme.YounovaTextSecondary
import java.util.Calendar

@Composable
fun SidebarDrawer(
    conversations: List<ConversationEntity>,
    archivedConversations: List<ConversationEntity>,
    selectedId: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    onRename: (ConversationEntity) -> Unit,
    onTogglePin: (ConversationEntity) -> Unit,
    onToggleArchive: (ConversationEntity) -> Unit,
    onExport: (ConversationEntity) -> Unit,
    onDelete: (ConversationEntity) -> Unit,
    onOpenSettings: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showArchivedSection by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(310.dp)
            .background(YounovaSurfaceDark)
            .border(1.dp, YounovaBorder)
            .padding(top = 16.dp, bottom = 12.dp)
    ) {
        // Top Header: Logo + Close Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            YounovaWordmark(iconSize = 28.dp, fontSize = 17)

            IconButton(
                onClick = onCloseDrawer,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close sidebar",
                    tint = YounovaTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // New Chat Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(YounovaPrimaryPurple.copy(alpha = 0.12f))
                .border(1.dp, YounovaPrimaryPurple.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .clickable {
                    onNewChat()
                    onCloseDrawer()
                }
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New chat",
                    tint = YounovaBrightPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "New Chat",
                    color = YounovaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Chats input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search chats...", color = YounovaTextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = YounovaTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YounovaPrimaryPurple,
                unfocusedBorderColor = YounovaBorder,
                focusedContainerColor = YounovaSurfaceCard,
                unfocusedContainerColor = YounovaSurfaceCard,
                cursorColor = YounovaBrightPurple,
                focusedTextColor = YounovaTextPrimary,
                unfocusedTextColor = YounovaTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Conversations Grouped List
        val grouped = remember(conversations, searchQuery) {
            groupConversations(conversations)
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            if (grouped.pinned.isNotEmpty()) {
                item {
                    SectionHeader("PINNED")
                }
                items(grouped.pinned, key = { it.id }) { conv ->
                    ConversationRow(
                        conversation = conv,
                        isSelected = conv.id == selectedId,
                        onClick = {
                            onSelectConversation(conv.id)
                            onCloseDrawer()
                        },
                        onRename = { onRename(conv) },
                        onTogglePin = { onTogglePin(conv) },
                        onToggleArchive = { onToggleArchive(conv) },
                        onExport = { onExport(conv) },
                        onDelete = { onDelete(conv) }
                    )
                }
            }

            if (grouped.today.isNotEmpty()) {
                item {
                    SectionHeader("TODAY")
                }
                items(grouped.today, key = { it.id }) { conv ->
                    ConversationRow(
                        conversation = conv,
                        isSelected = conv.id == selectedId,
                        onClick = {
                            onSelectConversation(conv.id)
                            onCloseDrawer()
                        },
                        onRename = { onRename(conv) },
                        onTogglePin = { onTogglePin(conv) },
                        onToggleArchive = { onToggleArchive(conv) },
                        onExport = { onExport(conv) },
                        onDelete = { onDelete(conv) }
                    )
                }
            }

            if (grouped.yesterday.isNotEmpty()) {
                item {
                    SectionHeader("YESTERDAY")
                }
                items(grouped.yesterday, key = { it.id }) { conv ->
                    ConversationRow(
                        conversation = conv,
                        isSelected = conv.id == selectedId,
                        onClick = {
                            onSelectConversation(conv.id)
                            onCloseDrawer()
                        },
                        onRename = { onRename(conv) },
                        onTogglePin = { onTogglePin(conv) },
                        onToggleArchive = { onToggleArchive(conv) },
                        onExport = { onExport(conv) },
                        onDelete = { onDelete(conv) }
                    )
                }
            }

            if (grouped.previous7Days.isNotEmpty()) {
                item {
                    SectionHeader("PREVIOUS 7 DAYS")
                }
                items(grouped.previous7Days, key = { it.id }) { conv ->
                    ConversationRow(
                        conversation = conv,
                        isSelected = conv.id == selectedId,
                        onClick = {
                            onSelectConversation(conv.id)
                            onCloseDrawer()
                        },
                        onRename = { onRename(conv) },
                        onTogglePin = { onTogglePin(conv) },
                        onToggleArchive = { onToggleArchive(conv) },
                        onExport = { onExport(conv) },
                        onDelete = { onDelete(conv) }
                    )
                }
            }

            if (grouped.older.isNotEmpty()) {
                item {
                    SectionHeader("OLDER")
                }
                items(grouped.older, key = { it.id }) { conv ->
                    ConversationRow(
                        conversation = conv,
                        isSelected = conv.id == selectedId,
                        onClick = {
                            onSelectConversation(conv.id)
                            onCloseDrawer()
                        },
                        onRename = { onRename(conv) },
                        onTogglePin = { onTogglePin(conv) },
                        onToggleArchive = { onToggleArchive(conv) },
                        onExport = { onExport(conv) },
                        onDelete = { onDelete(conv) }
                    )
                }
            }

            // Archived section toggle
            if (archivedConversations.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showArchivedSection = !showArchivedSection }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = null,
                            tint = YounovaTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Archived (${archivedConversations.size})",
                            color = YounovaTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (showArchivedSection) {
                    items(archivedConversations, key = { it.id }) { conv ->
                        ConversationRow(
                            conversation = conv,
                            isSelected = conv.id == selectedId,
                            onClick = {
                                onSelectConversation(conv.id)
                                onCloseDrawer()
                            },
                            onRename = { onRename(conv) },
                            onTogglePin = { onTogglePin(conv) },
                            onToggleArchive = { onToggleArchive(conv) },
                            onExport = { onExport(conv) },
                            onDelete = { onDelete(conv) }
                        )
                    }
                }
            }
        }

        Divider(color = YounovaBorder, thickness = 1.dp)

        // Bottom Profile & Settings Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onOpenSettings()
                    onCloseDrawer()
                }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(YounovaSurfaceCard)
                    .border(1.dp, YounovaBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = YounovaBrightPurple,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Settings",
                    color = YounovaTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "API Key, Models, Privacy",
                    color = YounovaTextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = YounovaTextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp)
    )
}

@Composable
fun ConversationRow(
    conversation: ConversationEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) YounovaPrimaryPurple.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (conversation.isPinned) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Pinned",
                tint = YounovaBrightPurple,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = conversation.title.ifBlank { "New Chat" },
            color = if (isSelected) YounovaTextPrimary else YounovaTextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = YounovaTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier
                    .background(YounovaSurfaceElevated)
                    .border(1.dp, YounovaBorder)
            ) {
                DropdownMenuItem(
                    text = { Text("Rename", color = YounovaTextPrimary, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = YounovaTextSecondary, modifier = Modifier.size(16.dp))
                    },
                    onClick = {
                        menuExpanded = false
                        onRename()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(if (conversation.isPinned) "Unpin" else "Pin", color = YounovaTextPrimary, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            if (conversation.isPinned) Icons.Outlined.PushPin else Icons.Default.PushPin,
                            contentDescription = null,
                            tint = YounovaTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onTogglePin()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(if (conversation.isArchived) "Unarchive" else "Archive", color = YounovaTextPrimary, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            if (conversation.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                            contentDescription = null,
                            tint = YounovaTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onToggleArchive()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Export Markdown", color = YounovaTextPrimary, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = YounovaTextSecondary, modifier = Modifier.size(16.dp))
                    },
                    onClick = {
                        menuExpanded = false
                        onExport()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = YounovaError, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = YounovaError, modifier = Modifier.size(16.dp))
                    },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}

data class GroupedConversations(
    val pinned: List<ConversationEntity>,
    val today: List<ConversationEntity>,
    val yesterday: List<ConversationEntity>,
    val previous7Days: List<ConversationEntity>,
    val older: List<ConversationEntity>
)

fun groupConversations(list: List<ConversationEntity>): GroupedConversations {
    val pinned = mutableListOf<ConversationEntity>()
    val today = mutableListOf<ConversationEntity>()
    val yesterday = mutableListOf<ConversationEntity>()
    val prev7 = mutableListOf<ConversationEntity>()
    val older = mutableListOf<ConversationEntity>()

    val now = Calendar.getInstance()
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val yesterdayStart = todayStart - (24 * 60 * 60 * 1000)
    val prev7Start = todayStart - (7 * 24 * 60 * 60 * 1000)

    for (c in list) {
        if (c.isPinned) {
            pinned.add(c)
        } else if (c.updatedAt >= todayStart) {
            today.add(c)
        } else if (c.updatedAt >= yesterdayStart) {
            yesterday.add(c)
        } else if (c.updatedAt >= prev7Start) {
            prev7.add(c)
        } else {
            older.add(c)
        }
    }

    return GroupedConversations(pinned, today, yesterday, prev7, older)
}
