package com.example.dietassist.ui.dashboard

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dietassist.data.model.FoodLog
import com.example.dietassist.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddFood: () -> Unit,
    onNavigateToFoodDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val foodLogs by viewModel.foodLogs.collectAsState()
    val totalWaterMl by viewModel.totalWaterMl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    var selectedDayIndex by remember { mutableStateOf(5) } // Thursday (Thu) as default index
    var carouselPage by remember { mutableStateOf(0) } // 0, 1, 2

    // Refresh data on entry
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            onNavigateToLogin()
        } else {
            viewModel.refreshData()
        }
    }

    val targetCalories = currentUser?.dailyCalorieTarget?.toFloat() ?: 2000f
    val totalCalories = foodLogs.sumOf { it.calories.toDouble() }.toFloat()
    val remainingCalories = (targetCalories - totalCalories).coerceAtLeast(0f)

    // Macro nutrients
    val totalProtein = foodLogs.sumOf { it.protein.toDouble() }.toFloat()
    val totalCarbs = foodLogs.sumOf { it.carbs.toDouble() }.toFloat()
    val totalFats = foodLogs.sumOf { it.fats.toDouble() }.toFloat()

    // Targets estimates
    val targetProtein = targetCalories * 0.30f / 4f // 30% Protein
    val targetCarbs = targetCalories * 0.50f / 4f  // 50% Carbs
    val targetFats = targetCalories * 0.20f / 9f   // 20% Fats

    val daysOfWeek = listOf(
        Pair("Sat", "16"),
        Pair("Sun", "17"),
        Pair("Mon", "18"),
        Pair("Tue", "19"),
        Pair("Wed", "20"),
        Pair("Thu", "21"),
        Pair("Fri", "22")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🍎",
                        fontSize = 28.sp
                    )
                    Text(
                        text = "DietAssist",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkSlate
                    )
                }

                // Active Streak Pill
                Row(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🔥", fontSize = 16.sp)
                    Text(
                        text = "0",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate
                    )
                }
            }
        }

        // 2. Day Selector Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                daysOfWeek.forEachIndexed { index, day ->
                    val isSelected = index == selectedDayIndex
                    Column(
                        modifier = Modifier
                            .width(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) BorderGray else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                selectedDayIndex = index
                                if (index != 5) {
                                    Toast.makeText(context, "Showing mock data for ${day.first} ${day.second}", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.refreshData()
                                }
                            }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day.first,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) DarkSlate else TextGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.second,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        )
                    }
                }
            }
        }

        // 3. Large Calories Remaining Card
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${remainingCalories.toInt()}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkSlate
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "Adjust calorie target in Profile", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = "Calories left",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextGray
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = TextGray
                            )
                        }
                    }

                    // Circle Progress Ring with flame symbol inside
                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val progressFraction = if (targetCalories > 0) (totalCalories / targetCalories).coerceIn(0f, 1f) else 0f
                        CircularProgressIndicator(
                            progress = progressFraction,
                            color = DarkSlate,
                            trackColor = Color(0xFFF1F5F9),
                            strokeWidth = 8.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(text = "🔥", fontSize = 28.sp)
                    }
                }
            }
        }

        // 4. Horizontal Pager Carousel for metrics
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedContent(
                    targetState = carouselPage,
                    transitionSpec = {
                        slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
                    },
                    label = "CarouselPage"
                ) { page ->
                    when (page) {
                        0 -> {
                            // Page 1: Protein, Carbs, Fat Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MiniMetricCard(
                                    label = "Protein left",
                                    amount = "${(targetProtein - totalProtein).coerceAtLeast(0f).toInt()}g",
                                    progress = if (targetProtein > 0) (totalProtein / targetProtein).coerceIn(0f, 1f) else 0f,
                                    emoji = "🍗",
                                    emojiBg = Color(0xFFFEF2F2),
                                    modifier = Modifier.weight(1f)
                                )
                                MiniMetricCard(
                                    label = "Carbs left",
                                    amount = "${(targetCarbs - totalCarbs).coerceAtLeast(0f).toInt()}g",
                                    progress = if (targetCarbs > 0) (totalCarbs / targetCarbs).coerceIn(0f, 1f) else 0f,
                                    emoji = "🌾",
                                    emojiBg = Color(0xFFFFFBEB),
                                    modifier = Modifier.weight(1f)
                                )
                                MiniMetricCard(
                                    label = "Fat left",
                                    amount = "${(targetFats - totalFats).coerceAtLeast(0f).toInt()}g",
                                    progress = if (targetFats > 0) (totalFats / targetFats).coerceIn(0f, 1f) else 0f,
                                    emoji = "🥑",
                                    emojiBg = Color(0xFFEFF6FF),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        1 -> {
                            // Page 2: Fiber, Sugar, Sodium & Health Score below
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    MiniMetricCard(
                                        label = "Fiber left",
                                        amount = "38g",
                                        progress = 0f,
                                        emoji = "🍎",
                                        emojiBg = Color(0xFFFDF2F8),
                                        modifier = Modifier.weight(1f)
                                    )
                                    MiniMetricCard(
                                        label = "Sugar left",
                                        amount = "83g",
                                        progress = 0f,
                                        emoji = "🥄",
                                        emojiBg = Color(0xFFF5F5F5),
                                        modifier = Modifier.weight(1f)
                                    )
                                    MiniMetricCard(
                                        label = "Sodium left",
                                        amount = "2300mg",
                                        progress = 0f,
                                        emoji = "🧂",
                                        emojiBg = Color(0xFFFFFBEB),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Health Score card below
                                Card(
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Health Score",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkSlate
                                            )
                                            Text(
                                                text = "N/A",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextGray
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = 0f,
                                            color = MintGreen,
                                            trackColor = Color(0xFFF1F5F9),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(CircleShape)
                                        )
                                        Text(
                                            text = "Track a few foods to generate your health score for today. Your score reflects nutritional content and how processed your meals are.",
                                            fontSize = 12.sp,
                                            color = TextGray,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Page 3: Fit Connect, Calories Burned & Water Tracker below
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Connect Health
                                    Card(
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(180.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(Color(0xFFFFF1F2), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("❤️", fontSize = 18.sp)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Connect Google Fit",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkSlate,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "Track your steps",
                                                fontSize = 10.sp,
                                                color = TextGray,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = { Toast.makeText(context, "Google Fit Connected!", Toast.LENGTH_SHORT).show() },
                                                colors = ButtonDefaults.buttonColors(containerColor = DarkSlate),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Connect", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Calories Burned
                                    Card(
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(180.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Calories burned",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextGray
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.Bottom,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "0",
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = DarkSlate
                                                    )
                                                    Text(
                                                        text = " cal",
                                                        fontSize = 12.sp,
                                                        color = TextGray,
                                                        modifier = Modifier.padding(bottom = 2.dp)
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text("🚶", fontSize = 16.sp)
                                                Column {
                                                    Text("Steps", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                                                    Text("0 cal", fontSize = 10.sp, color = TextGray)
                                                }
                                            }
                                        }
                                    }
                                }

                                // Water Tracker below
                                Card(
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🥛", fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "Water",
                                                fontSize = 12.sp,
                                                color = TextGray,
                                                fontWeight = FontWeight.Bold
                                            )
                                            val cups = (totalWaterMl / 250)
                                            Text(
                                                text = "$totalWaterMl ml ($cups cups)",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkSlate
                                            )
                                        }
                                        Button(
                                            onClick = { viewModel.addWaterIntake(250) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, BorderGray),
                                            shape = RoundedCornerShape(16.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Log Water", color = DarkSlate, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Page Indicator Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0..2) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (carouselPage == i) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (carouselPage == i) DarkSlate else TextGray.copy(alpha = 0.4f))
                                .clickable { carouselPage = i }
                        )
                    }
                }
            }
        }

        // 5. Ask AI Consult Button Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = LightMint),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToChat() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("💬", fontSize = 24.sp)
                        Column {
                            Text(
                                text = "Tanya DietAssistAi",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                            Text(
                                text = "Konsultasi gizi instan & sehat.",
                                fontSize = 11.sp,
                                color = PrimaryTeal.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Button(
                        onClick = onNavigateToChat,
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Tanya", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 6. Title Area: Recently uploaded / Diary Makanan
        item {
            Text(
                text = "Recently uploaded",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkSlate,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // 7. List of logs or Placeholder Illustration
        if (foodLogs.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAddFood() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Food Plate Illustration
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color(0xFFF8FAFC), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🥗", fontSize = 44.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Skeleton lines mockup
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Tap + to add your first meal of the day",
                            color = TextGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            items(foodLogs) { log ->
                FoodLogCard(
                    log = log,
                    onClick = { log.id?.let { onNavigateToFoodDetail(it) } }
                )
            }
        }
    }
}

@Composable
fun MiniMetricCard(
    label: String,
    amount: String,
    progress: Float,
    emoji: String,
    emojiBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = amount,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkSlate
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Bottom row: circle progress + icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(emojiBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                }

                // Small progress indicator
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = progress,
                        color = PrimaryTeal,
                        trackColor = Color(0xFFF1F5F9),
                        strokeWidth = 3.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun FoodLogCard(
    log: FoodLog,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Image/Emoji Placeholder
            if (!log.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = log.imageUrl,
                    contentDescription = log.foodName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🍲", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = log.foodName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlate
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "P: ${log.protein.toInt()}g  •  C: ${log.carbs.toInt()}g  •  F: ${log.fats.toInt()}g",
                    fontSize = 12.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
            }

            // Calories Label
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${log.calories.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkSlate
                )
                Text(
                    text = "kcal",
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

