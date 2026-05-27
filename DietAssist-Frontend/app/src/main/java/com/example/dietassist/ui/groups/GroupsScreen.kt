package com.example.dietassist.ui.groups

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietassist.ui.theme.*

data class GroupItem(
    val id: Int,
    val name: String,
    val members: String,
    val description: String,
    val emoji: String,
    val emojiBg: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier
) {
    var joinedGroupIds by remember { mutableStateOf(setOf<Int>()) }

    val groups = remember {
        listOf(
            GroupItem(
                id = 1,
                name = "Fitness & Workouts",
                members = "12,123 members",
                description = "Share workouts that match your calorie goals.",
                emoji = "🏃‍♂️",
                emojiBg = Color(0xFFEFF6FF)
            ),
            GroupItem(
                id = 2,
                name = "New to Calorie Tracking",
                members = "16,409 members",
                description = "Beginner questions, tips, and first wins.",
                emoji = "🥗",
                emojiBg = Color(0xFFF0FDF4)
            ),
            GroupItem(
                id = 3,
                name = "New Year's Resolutions",
                members = "1,251 members",
                description = "Share your resolutions, stay on track, celebrate wins.",
                emoji = "📝",
                emojiBg = Color(0xFFFFFBEB)
            ),
            GroupItem(
                id = 4,
                name = "Muscle Gain & Bulking",
                members = "10,249 members",
                description = "Eat in a surplus and build muscle together.",
                emoji = "💪",
                emojiBg = Color(0xFFFEF2F2)
            ),
            GroupItem(
                id = 5,
                name = "Weight Loss Support",
                members = "14,246 members",
                description = "Stay accountable, share progress, ask questions.",
                emoji = "🏋️‍♀️",
                emojiBg = Color(0xFFF5F3FF)
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Groups",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkSlate
                    )
                },
                actions = {
                    IconButton(
                        onClick = { /* Notification click */ },
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(44.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = DarkSlate
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgWhite)
            )
        },
        containerColor = BgWhite,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Discover Groups",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate
                    )
                    TextButton(onClick = { /* Create private group */ }) {
                        Text(
                            text = "+ Private Group",
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            items(groups) { group ->
                val isJoined = joinedGroupIds.contains(group.id)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(group.emojiBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = group.emoji, fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Group Details
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = group.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate
                            )
                            Text(
                                text = group.members,
                                fontSize = 12.sp,
                                color = TextGray,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                            Text(
                                text = group.description,
                                fontSize = 12.sp,
                                color = TextGray,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Join Button
                        Button(
                            onClick = {
                                joinedGroupIds = if (isJoined) {
                                    joinedGroupIds - group.id
                                } else {
                                    joinedGroupIds + group.id
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isJoined) Color(0xFFF1F5F9) else Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = if (isJoined) null else BorderStroke(1.dp, BorderGray),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = if (isJoined) "Joined" else "+ Join",
                                color = if (isJoined) TextGray else DarkSlate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
