package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageEntity
import com.example.ui.theme.YounovaBorder
import com.example.ui.theme.YounovaBrightPurple
import com.example.ui.theme.YounovaError
import com.example.ui.theme.YounovaPrimaryPurple
import com.example.ui.theme.YounovaSuccess
import com.example.ui.theme.YounovaSurfaceCard
import com.example.ui.theme.YounovaSurfaceDark
import com.example.ui.theme.YounovaSurfaceElevated
import com.example.ui.theme.YounovaTextMuted
import com.example.ui.theme.YounovaTextPrimary
import com.example.ui.theme.YounovaTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageItem(
    message: MessageEntity,
    isStreaming: Boolean,
    onRegenerate: () -> Unit,
    onEdit: (newText: String) -> Unit,
    onFeedback: (isPositive: Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(message.content) { mutableStateOf(message.content) }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        if (message.role == "user") {
            // USER MESSAGE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Attached image if any
                if (message.attachmentType == "image" && !message.attachmentData.isNullOrBlank()) {
                    val bitmap = remember(message.attachmentData) {
                        try {
                            val decoded = Base64.decode(message.attachmentData, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = message.attachmentName ?: "Attachment",
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .size(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, YounovaBorder, RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Bubble container
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp))
                        .background(YounovaSurfaceCard)
                        .border(1.dp, YounovaBorder, RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.content,
                        color = YounovaTextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }

                // Actions below user message
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = YounovaTextMuted,
                        modifier = Modifier.padding(end = 6.dp)
                    )

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("YOUNOVA message", message.content))
                            isCopied = true
                            coroutineScope.launch {
                                delay(2000)
                                isCopied = false
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = if (isCopied) YounovaSuccess else YounovaTextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        } else if (message.role == "error") {
            // ERROR MESSAGE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(YounovaSurfaceDark)
                    .border(1.dp, YounovaError.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = "Error",
                    tint = YounovaError,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = message.content,
                        color = YounovaTextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ElevatedButton(
                        onClick = onRegenerate,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = YounovaSurfaceElevated,
                            contentColor = YounovaBrightPurple
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            // ASSISTANT MESSAGE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                YounovaAtomSymbol(
                    size = 28.dp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "YOUNOVA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = YounovaBrightPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formattedTime,
                            fontSize = 11.sp,
                            color = YounovaTextMuted
                        )
                    }

                    if (message.content.isBlank() && isStreaming) {
                        // Pulsing streaming placeholder
                        StreamingIndicator()
                    } else {
                        MarkdownContent(markdown = message.content)
                    }

                    // Bottom Action Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Copy message
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("YOUNOVA response", message.content))
                                isCopied = true
                                coroutineScope.launch {
                                    delay(2000)
                                    isCopied = false
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy text",
                                tint = if (isCopied) YounovaSuccess else YounovaTextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Regenerate
                        IconButton(
                            onClick = onRegenerate,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate response",
                                tint = YounovaTextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Thumbs up
                        IconButton(
                            onClick = {
                                val newFeedback = if (message.isFeedbackPositive == true) null else true
                                onFeedback(newFeedback)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (message.isFeedbackPositive == true) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Helpful",
                                tint = if (message.isFeedbackPositive == true) YounovaBrightPurple else YounovaTextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Thumbs down
                        IconButton(
                            onClick = {
                                val newFeedback = if (message.isFeedbackPositive == false) null else false
                                onFeedback(newFeedback)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (message.isFeedbackPositive == false) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                                contentDescription = "Not helpful",
                                tint = if (message.isFeedbackPositive == false) YounovaError else YounovaTextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StreamingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(7.dp)
                    .alpha(if (index == 1) alpha else 1.3f - alpha)
                    .background(YounovaBrightPurple, CircleShape)
            )
        }
    }
}
