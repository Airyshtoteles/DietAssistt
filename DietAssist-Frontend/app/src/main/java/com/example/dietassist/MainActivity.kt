package com.example.dietassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dietassist.ui.analyze.AddFoodScreen
import com.example.dietassist.ui.analyze.AddFoodViewModel
import com.example.dietassist.ui.auth.LoginScreen
import com.example.dietassist.ui.auth.LoginViewModel
import com.example.dietassist.ui.auth.SplashScreen
import com.example.dietassist.ui.dashboard.DashboardScreen
import com.example.dietassist.ui.dashboard.DashboardViewModel
import com.example.dietassist.ui.dashboard.MainShellScreen
import com.example.dietassist.ui.detail.FoodDetailScreen
import com.example.dietassist.ui.detail.FoodDetailViewModel
import com.example.dietassist.ui.profile.ProfileSetupScreen
import com.example.dietassist.ui.profile.ProfileSetupViewModel
import com.example.dietassist.ui.chat.HealthChatScreen
import com.example.dietassist.ui.chat.HealthChatViewModel
import com.example.dietassist.ui.theme.DietAssistTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DietAssistTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color(0xFFF8FAFC)
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // ViewModels inisialisasi di tingkat atas NavHost agar mudah di-share atau diakses
    val loginViewModel: LoginViewModel = viewModel()
    val profileSetupViewModel: ProfileSetupViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()
    val addFoodViewModel: AddFoodViewModel = viewModel()
    val foodDetailViewModel: FoodDetailViewModel = viewModel()
    val healthChatViewModel: HealthChatViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        
        // 1. Splash Screen
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    val user = loginViewModel.currentUser.value
                    if (user != null) {
                        navController.navigate("dashboard") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        // 2. Login & Auth Screen
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate("onboarding") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 3. Onboarding / Profile Setup Screen
        composable("onboarding") {
            ProfileSetupScreen(
                viewModel = profileSetupViewModel,
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // 4. Main Dashboard Screen (Halaman Utama dengan Shell Bottom Nav)
        composable("dashboard") {
            MainShellScreen(
                dashboardViewModel = dashboardViewModel,
                profileSetupViewModel = profileSetupViewModel,
                onNavigateToAddFood = {
                    navController.navigate("add_food")
                },
                onNavigateToFoodDetail = { logId ->
                    navController.navigate("food_detail/$logId")
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate("health_chat")
                }
            )
        }

        // 4.5. DietAssistAi Health Chat Screen
        composable("health_chat") {
            HealthChatScreen(
                viewModel = healthChatViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 5. Add & AI Analyze Food Screen
        composable("add_food") {
            AddFoodScreen(
                viewModel = addFoodViewModel,
                onNavigateBack = {
                    // Memicu refresh data agar dashboard secara dinamis ter-update pasca penyimpanan
                    dashboardViewModel.refreshData()
                    navController.popBackStack()
                }
            )
        }

        // 6. Detail & Edit Food Screen (CRUD: Update & Delete)
        composable(
            route = "food_detail/{logId}",
            arguments = listOf(navArgument("logId") { type = NavType.StringType })
        ) { backStackEntry ->
            val logId = backStackEntry.arguments?.getString("logId") ?: ""
            FoodDetailScreen(
                viewModel = foodDetailViewModel,
                logId = logId,
                onNavigateBack = {
                    // Memicu refresh data agar dashboard secara dinamis ter-update pasca mutasi (Update/Delete)
                    dashboardViewModel.refreshData()
                    navController.popBackStack()
                }
            )
        }
    }
}
