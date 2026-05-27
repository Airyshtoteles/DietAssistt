package com.example.dietassist.ui.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietassist.R
import com.example.dietassist.ui.theme.BgWhite
import com.example.dietassist.ui.theme.MintGreen
import com.example.dietassist.ui.theme.PrimaryTeal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val logoScale = remember { Animatable(0f) }
    val textWidthPercent = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Step 1: Scale up the logo in the center
        logoScale.animateTo(
            targetValue = 1.1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = { it * it * (3f - 2f * it) }
            )
        )
        logoScale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 150)
        )
        
        delay(200)
        
        // Step 2: Slide logo left and fade-reveal text
        coroutineScope {
            launch {
                textWidthPercent.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 800)
                )
            }
            launch {
                textAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, delayMillis = 150)
                )
            }
        }
        
        delay(1200) // Delay splash screen
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgWhite, Color(0xFFECFDF5))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_app),
                    contentDescription = "DietAssist Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            // Text side reveal animation
            if (textWidthPercent.value > 0f) {
                Spacer(modifier = Modifier.width((20 * textWidthPercent.value).dp))

                Column(
                    modifier = Modifier
                        .graphicsLayer(
                            alpha = textAlpha.value,
                            translationX = (30 * (1f - textWidthPercent.value)).dp.value
                        )
                ) {
                    Text(
                        text = "DietAssist",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryTeal,
                        lineHeight = 36.sp
                    )

                    Text(
                        text = "Asisten Gizi Cerdas AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
