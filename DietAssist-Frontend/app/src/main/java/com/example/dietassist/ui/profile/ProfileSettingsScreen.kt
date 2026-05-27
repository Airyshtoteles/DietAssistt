package com.example.dietassist.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietassist.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    currentUserState: androidx.compose.runtime.State<com.example.dietassist.data.model.UserProfile?>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by currentUserState

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. User Info Header Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToEditProfile() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (currentUser?.name?.take(1) ?: "P").uppercase(),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Premium Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "👑",
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Premium",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmAmber
                            )
                        }

                        Text(
                            text = currentUser?.name ?: "Tap to set name",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = if (currentUser != null) "Target: ${currentUser?.dailyCalorieTarget} kcal" else "and username",
                            fontSize = 13.sp,
                            color = TextGray
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Edit Profile",
                        tint = TextGray
                    )
                }
            }

            // 2. Invite Friends Section
            Text(
                text = "Invite Friends",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(context, "Referral code copied!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF1F5F9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤+", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Refer a friend and earn $10",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        )
                        Text(
                            text = "Earn $10 per friend that signs up with your promo code.",
                            fontSize = 12.sp,
                            color = TextGray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Share",
                        tint = TextGray
                    )
                }
            }

            // 3. Account Settings Section
            Text(
                text = "Account",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileRowItem(
                        icon = "🪪",
                        title = "Personal Details",
                        onClick = { onNavigateToEditProfile() }
                    )
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileRowItem(
                        icon = "⚙️",
                        title = "Preferences",
                        onClick = { Toast.makeText(context, "Preferences clicked", Toast.LENGTH_SHORT).show() }
                    )
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileRowItem(
                        icon = "🌐",
                        title = "Language",
                        onClick = { Toast.makeText(context, "Language selection clicked", Toast.LENGTH_SHORT).show() }
                    )
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileRowItem(
                        icon = "👨‍👩‍👧‍👦",
                        title = "Upgrade to Family Plan",
                        onClick = { Toast.makeText(context, "Family Plan clicked", Toast.LENGTH_SHORT).show() }
                    )
                }
            }

            // 4. Goals & Tracking Section
            Text(
                text = "Goals & Tracking",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileRowItem(
                        icon = "🎯",
                        title = "Edit Nutrition Goals",
                        onClick = { onNavigateToEditProfile() }
                    )
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileRowItem(
                        icon = "📤",
                        title = "Export PDF Summary Report",
                        onClick = { Toast.makeText(context, "PDF Export started", Toast.LENGTH_SHORT).show() }
                    )
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    
                    // Sync Data Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast
                                    .makeText(context, "Data Synced!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔄", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Sync Data",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Last Synced: 7:36 AM",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                    
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileRowItem(
                        icon = "📝",
                        title = "Terms and Conditions",
                        onClick = { Toast.makeText(context, "Terms & Conditions clicked", Toast.LENGTH_SHORT).show() }
                    )
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileRowItem(
                        icon = "🛡️",
                        title = "Privacy Policy",
                        onClick = { Toast.makeText(context, "Privacy Policy clicked", Toast.LENGTH_SHORT).show() }
                    )
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileRowItem(
                        icon = "✋",
                        title = "Manage Personalisation Preferences",
                        onClick = { Toast.makeText(context, "Preferences settings clicked", Toast.LENGTH_SHORT).show() }
                    )
                }
            }

            // 5. Account Actions Section
            Text(
                text = "Account Actions",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column {
                    ProfileRowItem(
                        icon = "🚪",
                        title = "Logout",
                        onClick = onLogout,
                        tintColor = CoralPink
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileRowItem(
    icon: String,
    title: String,
    onClick: () -> Unit,
    tintColor: Color = DarkSlate
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = tintColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Arrow",
            tint = TextGray
        )
    }
}
