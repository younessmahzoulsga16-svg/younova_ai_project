package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YounovaBrightPurple
import com.example.ui.theme.YounovaDeepPurple
import com.example.ui.theme.YounovaPrimaryPurple
import com.example.ui.theme.YounovaTextPrimary

@Composable
fun YounovaAtomSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    primaryColor: Color = YounovaPrimaryPurple,
    accentColor: Color = YounovaBrightPurple
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val nucleusRadius = this.size.width * 0.15f
            val ellipseWidth = this.size.width * 0.88f
            val ellipseHeight = this.size.height * 0.40f
            val strokeWidth = this.size.width * 0.055f

            // Three orbital paths rotated at 0, 60, and 120 degrees
            val angles = listOf(0f, 60f, 120f)
            for (angle in angles) {
                rotate(degrees = angle, pivot = center) {
                    drawOval(
                        color = accentColor.copy(alpha = 0.85f),
                        topLeft = Offset(center.x - ellipseWidth / 2f, center.y - ellipseHeight / 2f),
                        size = Size(ellipseWidth, ellipseHeight),
                        style = Stroke(width = strokeWidth)
                    )
                }
            }

            // Outer soft glow for nucleus
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.6f), Color.Transparent),
                    center = center,
                    radius = nucleusRadius * 2.2f
                ),
                radius = nucleusRadius * 2.2f,
                center = center
            )

            // Central solid glowing nucleus
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentColor, YounovaDeepPurple),
                    center = Offset(center.x - nucleusRadius * 0.2f, center.y - nucleusRadius * 0.2f),
                    radius = nucleusRadius
                ),
                radius = nucleusRadius,
                center = center
            )
        }
    }
}

@Composable
fun YounovaWordmark(
    modifier: Modifier = Modifier,
    iconSize: Dp = 28.dp,
    fontSize: Int = 18
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        YounovaAtomSymbol(size = iconSize)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "YOUNOVA",
            color = YounovaTextPrimary,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 2.sp
        )
    }
}
