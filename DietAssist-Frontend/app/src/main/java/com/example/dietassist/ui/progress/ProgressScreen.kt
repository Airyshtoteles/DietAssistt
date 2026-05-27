package com.example.dietassist.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietassist.ui.components.DailyCalorie
import com.example.dietassist.ui.components.WeeklyBarChart
import com.example.dietassist.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    weeklyHistoryState: androidx.compose.runtime.State<List<DailyCalorie>>,
    targetCalories: Float,
    modifier: Modifier = Modifier
) {
    val weeklyHistory by weeklyHistoryState
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Progress",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkSlate
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgWhite)
            )
        },
        containerColor = BgWhite,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Chart Display
            if (weeklyHistory.isNotEmpty()) {
                WeeklyBarChart(
                    dailyData = weeklyHistory,
                    targetCalorie = targetCalories
                )
            } else {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No history data available yet.",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 2. Statistics/Overview Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Overview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate
                    )

                    val averageCalories = if (weeklyHistory.isNotEmpty()) {
                        weeklyHistory.map { it.calories }.average().toInt()
                    } else 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Weekly Average",
                                fontSize = 13.sp,
                                color = TextGray
                            )
                            Text(
                                text = "$averageCalories kcal",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }

                        Column {
                            Text(
                                text = "Daily Target",
                                fontSize = 13.sp,
                                color = TextGray
                            )
                            Text(
                                text = "${targetCalories.toInt()} kcal",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate
                            )
                        }
                    }

                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp)

                    // Brief advice / analysis
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (averageCalories > targetCalories) {
                                "You are averaging slightly above your daily calorie target. Try incorporating light exercises."
                            } else {
                                "Excellent! You are staying within your caloric target range. Keep up the good work!"
                            },
                            fontSize = 12.sp,
                            color = DarkSlate,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
