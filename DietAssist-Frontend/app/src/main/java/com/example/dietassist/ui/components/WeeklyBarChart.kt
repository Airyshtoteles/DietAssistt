package com.example.dietassist.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietassist.ui.theme.CardWhite
import com.example.dietassist.ui.theme.DarkSlate
import com.example.dietassist.ui.theme.MintGreen
import com.example.dietassist.ui.theme.TextGray

data class DailyCalorie(
    val dayName: String,
    val calories: Float
)

@Composable
fun WeeklyBarChart(
    dailyData: List<DailyCalorie>,
    targetCalorie: Float,
    modifier: Modifier = Modifier
) {
    val maxCalorie = (dailyData.maxOfOrNull { it.calories } ?: 0f).coerceAtLeast(targetCalorie).coerceAtLeast(1000f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardWhite)
            .padding(20.dp)
    ) {
        Text(
            text = "Tren Kalori Mingguan",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = DarkSlate
        )
        
        Text(
            text = "Asupan kalori harian Anda 7 hari terakhir",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Canvas Area untuk grafik
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                val barWidth = 26.dp.toPx()
                val gap = (canvasWidth - (barWidth * dailyData.size)) / (dailyData.size + 1)
                
                // 1. Gambar Garis Dotted Target Kalori
                val targetY = canvasHeight - (targetCalorie / maxCalorie) * canvasHeight
                drawLine(
                    color = Color(0xFFF43F5E), // Coral untuk target line
                    start = Offset(0f, targetY),
                    end = Offset(canvasWidth, targetY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // 2. Menggambar Batang Batang Kalori
                dailyData.forEachIndexed { index, data ->
                    val x = gap + index * (barWidth + gap)
                    
                    // Hitung tinggi berdasarkan skala maxCalorie
                    val barHeightFraction = data.calories / maxCalorie
                    val currentHeight = barHeightFraction * canvasHeight
                    val y = canvasHeight - currentHeight

                    // Tentukan warna batang: Hijau jika di bawah target, Coral merah jika melampaui target
                    val barColor = if (data.calories > targetCalorie) {
                        Color(0xFFF43F5E)
                    } else {
                        MintGreen
                    }

                    // Background Batang (Abu abu redup)
                    drawRoundRect(
                        color = Color(0xFFF1F5F9),
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, canvasHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )

                    // Batang Progress Utama
                    if (currentHeight > 0) {
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, currentHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Label Nama Hari di bawah grafik
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dailyData.forEachIndexed { _, data ->
                Box(
                    modifier = Modifier.width(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = data.dayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                }
            }
        }
    }
}
