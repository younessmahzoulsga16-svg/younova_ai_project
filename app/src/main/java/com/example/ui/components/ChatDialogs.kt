package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YounovaBorder
import com.example.ui.theme.YounovaBrightPurple
import com.example.ui.theme.YounovaCodeBackground
import com.example.ui.theme.YounovaError
import com.example.ui.theme.YounovaPrimaryPurple
import com.example.ui.theme.YounovaSuccess
import com.example.ui.theme.YounovaSurfaceElevated
import com.example.ui.theme.YounovaTextPrimary
import com.example.ui.theme.YounovaTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RenameConversationDialog(
    isOpen: Boolean,
    titleText: String,
    onTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Rename Conversation", color = YounovaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column {
                Text("Enter a new title for this chat:", color = YounovaTextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = titleText,
                    onValueChange = onTitleChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
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
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = titleText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = YounovaPrimaryPurple)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = YounovaTextSecondary)
            }
        },
        containerColor = YounovaSurfaceElevated
    )
}

@Composable
fun DeleteConversationDialog(
    isOpen: Boolean,
    conversationTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Delete Conversation?", color = YounovaError, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Text(
                "Are you sure you want to delete \"$conversationTitle\"? This conversation and its messages will be removed permanently.",
                color = YounovaTextSecondary,
                fontSize = 13.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = YounovaError)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = YounovaTextSecondary)
            }
        },
        containerColor = YounovaSurfaceElevated
    )
}

@Composable
fun ExportMarkdownDialog(
    isOpen: Boolean,
    markdownContent: String,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Export Conversation",
                    color = YounovaTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        text = {
            val vScroll = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(YounovaCodeBackground)
                    .border(1.dp, YounovaBorder, RoundedCornerShape(8.dp))
                    .verticalScroll(vScroll)
                    .padding(12.dp)
            ) {
                Text(
                    text = markdownContent,
                    color = YounovaTextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 17.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("YOUNOVA Export", markdownContent))
                    isCopied = true
                    coroutineScope.launch {
                        delay(2000)
                        isCopied = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = YounovaPrimaryPurple)
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isCopied) "Copied!" else "Copy Markdown")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = YounovaTextSecondary)
            }
        },
        containerColor = YounovaSurfaceElevated
    )
}
