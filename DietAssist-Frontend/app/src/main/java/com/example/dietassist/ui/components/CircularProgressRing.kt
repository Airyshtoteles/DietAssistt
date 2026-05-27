package com.example.dietassist.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietassist.ui.theme.DarkSlate
import com.example.dietassist.ui.theme.MintGreen
import com.example.dietassist.ui.theme.PrimaryTeal
import com.example.dietassist.ui.theme.TextGray

@Composable
fun CircularProgressRing(
    consumedCalories: Float,
    targetCalories: Float,
    modifier: Modifier = Modifier,
    ringWidth: Dp = 16.dp,
    ringSize: Dp = 180.dp
) {
    val remaining = targetCalories - consumedCalories
    val progress = if (targetCalories > 0) (consumedCalories / targetCalories).coerceIn(0f, 1f) else 0f
    
    // Animasi perubahan progress agar halus
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "CalorieProgressAnim"
    )

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val stroke = Stroke(width = ringWidth.toPx(), cap = StrokeCap.Round)
            val sizePx = size.width
            val radius = (sizePx - ringWidth.toPx()) / 2
            val centerOffset = Offset(sizePx / 2, sizePx / 2)

            // 1. Gambar Ring Latar Belakang (Meredup)
            drawCircle(
                color = Color(0xFFE2E8F0),
                radius = radius,
                center = centerOffset,
                style = stroke
            )

            // 2. Gambar Ring Progress Utama dengan Gradient
            val gradient = Brush.sweepGradient(
                colors = listOf(
                    MintGreen,
                    PrimaryTeal,
                    MintGreen
                )
            )

            drawArc(
                brush = gradient,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = Offset(ringWidth.toPx() / 2, ringWidth.toPx() / 2),
                size = Size(sizePx - ringWidth.toPx(), sizePx - ringWidth.toPx()),
                style = stroke
            )
        }

        // Teks Bagian Dalam Ring (Sisa Kalori, dll)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (remaining >= 0) "Sisa" else "Kelebihan",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = Math.abs(remaining).toInt().toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkSlate
            )
            
            Text(
                text = "kkal",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray
            )
        }
    }
}
