package com.example.dietassist.ui.dashboard

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietassist.ui.groups.GroupsScreen
import com.example.dietassist.ui.profile.ProfileSettingsScreen
import com.example.dietassist.ui.profile.ProfileSetupViewModel
import com.example.dietassist.ui.progress.ProgressScreen
import com.example.dietassist.ui.theme.*

enum class NavigationTab {
    Home, Progress, Groups, Profile
}

@Composable
fun MainShellScreen(
    dashboardViewModel: DashboardViewModel,
    profileSetupViewModel: ProfileSetupViewModel,
    onNavigateToAddFood: () -> Unit,
    onNavigateToFoodDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(NavigationTab.Home) }
    var isOverlayOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentUserState = dashboardViewModel.currentUser.collectAsState()
    val foodLogsState = dashboardViewModel.foodLogs.collectAsState()
    val weeklyHistoryState = dashboardViewModel.weeklyHistory.collectAsState()

    // Animasi rotasi FAB "+" menjadi "X"
    val rotationAngle by animateFloatAsState(
        targetValue = if (isOverlayOpen) 45f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "FabRotation"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // Beri ruang untuk floating bottom bar
        ) {
            when (currentTab) {
                NavigationTab.Home -> {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToAddFood = onNavigateToAddFood,
                        onNavigateToFoodDetail = onNavigateToFoodDetail,
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToChat = onNavigateToChat
                    )
                }
                NavigationTab.Progress -> {
                    ProgressScreen(
                        weeklyHistoryState = weeklyHistoryState,
                        targetCalories = currentUserState.value?.dailyCalorieTarget?.toFloat() ?: 2000f
                    )
                }
                NavigationTab.Groups -> {
                    GroupsScreen()
                }
                NavigationTab.Profile -> {
                    ProfileSettingsScreen(
                        onNavigateToEditProfile = {
                            // Navigasi ke halaman onboarding/profile setup
                            onNavigateToLogin() // Panggil flow onboarding melalui login redirect atau setup
                        },
                        onLogout = {
                            dashboardViewModel.logout()
                            onNavigateToLogin()
                        },
                        currentUserState = currentUserState
                    )
                }
            }
        }

        // 2. Dimmed Overlay Menu (2x2 Grid)
        if (isOverlayOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { isOverlayOpen = false }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp, start = 20.dp, end = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OverlayMenuCard(
                            emoji = "🏋️",
                            title = "Log exercise",
                            onClick = {
                                Toast.makeText(context, "Log exercise clicked", Toast.LENGTH_SHORT).show()
                                isOverlayOpen = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                        OverlayMenuCard(
                            emoji = "🔖",
                            title = "Saved foods",
                            onClick = {
                                Toast.makeText(context, "Saved foods clicked", Toast.LENGTH_SHORT).show()
                                isOverlayOpen = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OverlayMenuCard(
                            emoji = "🔍",
                            title = "Food Database",
                            onClick = {
                                Toast.makeText(context, "Food Database clicked", Toast.LENGTH_SHORT).show()
                                isOverlayOpen = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                        OverlayMenuCard(
                            emoji = "📸",
                            title = "Scan food",
                            onClick = {
                                isOverlayOpen = false
                                onNavigateToAddFood()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Floating Bottom Navigation & FAB Row
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Floating Navigation Pill
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .shadow(8.dp, RoundedCornerShape(32.dp))
                    .background(Color.White, RoundedCornerShape(32.dp))
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    emoji = "🏠",
                    label = "Home",
                    isActive = currentTab == NavigationTab.Home,
                    onClick = { currentTab = NavigationTab.Home }
                )
                BottomNavItem(
                    emoji = "📊",
                    label = "Progress",
                    isActive = currentTab == NavigationTab.Progress,
                    onClick = { currentTab = NavigationTab.Progress }
                )
                BottomNavItem(
                    emoji = "👥",
                    label = "Groups",
                    isActive = currentTab == NavigationTab.Groups,
                    onClick = { currentTab = NavigationTab.Groups }
                )
                BottomNavItem(
                    emoji = "👤",
                    label = "Profile",
                    isActive = currentTab == NavigationTab.Profile,
                    onClick = { currentTab = NavigationTab.Profile }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Floating Shutter-style FAB Shutter Button
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(8.dp, CircleShape)
                    .background(Color(0xFF0F172A), CircleShape) // DarkSlate black color
                    .clickable { isOverlayOpen = !isOverlayOpen },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Menu Toggle",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotationAngle)
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    emoji: String,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isActive) Color(0xFFEFF6FF) else Color.Transparent,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 18.sp
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) PrimaryTeal else TextGray
        )
    }
}

@Composable
fun OverlayMenuCard(
    emoji: String,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkSlate
            )
        }
    }
}
