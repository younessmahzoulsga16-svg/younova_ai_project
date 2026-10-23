package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YounovaBorder
import com.example.ui.theme.YounovaBrightPurple
import com.example.ui.theme.YounovaPrimaryPurple
import com.example.ui.theme.YounovaSurfaceCard
import com.example.ui.theme.YounovaTextMuted
import com.example.ui.theme.YounovaTextPrimary
import com.example.ui.theme.YounovaTextSecondary

@Composable
fun WelcomeScreen(
    onSelectSuggestion: (prompt: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing purple atom logo
        YounovaAtomSymbol(
            size = 72.dp,
            modifier = Modifier.padding(bottom = 18.dp)
        )

        Text(
            text = "What can I help you with?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = YounovaTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ask anything. Explore ideas. Build something extraordinary.",
            fontSize = 14.sp,
            color = YounovaTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 4 Suggestion Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SuggestionCard(
                icon = Icons.Outlined.AutoAwesome,
                title = "Explain a complex topic",
                subtitle = "Quantum computing, neural nets, general relativity",
                prompt = "Explain how quantum computing works in simple terms with intuitive real-world analogies",
                onClick = { onSelectSuggestion(it) }
            )

            SuggestionCard(
                icon = Icons.Outlined.Code,
                title = "Write or improve code",
                subtitle = "Optimized algorithms, bug diagnosis, architecture",
                prompt = "Write an optimized Kotlin coroutine function for debouncing fast user input events in Android Jetpack Compose",
                onClick = { onSelectSuggestion(it) }
            )

            SuggestionCard(
                icon = Icons.Outlined.Lightbulb,
                title = "Brainstorm creative ideas",
                subtitle = "Product concepts, workflows, strategies",
                prompt = "Brainstorm 5 innovative product concepts that merge modern AI intelligence with offline-first user privacy",
                onClick = { onSelectSuggestion(it) }
            )

            SuggestionCard(
                icon = Icons.Outlined.MenuBook,
                title = "Summarize and synthesize",
                subtitle = "Key takeaways, trade-offs, structured reports",
                prompt = "Summarize the key architectural differences and trade-offs between Transformer Attention and State-Space Models (Mamba)",
                onClick = { onSelectSuggestion(it) }
            )
        }
    }
}

@Composable
fun SuggestionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    prompt: String,
    onClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(YounovaSurfaceCard)
            .border(1.dp, YounovaBorder, RoundedCornerShape(12.dp))
            .clickable { onClick(prompt) }
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(YounovaBorder.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = YounovaBrightPurple,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = YounovaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = YounovaTextMuted,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}
