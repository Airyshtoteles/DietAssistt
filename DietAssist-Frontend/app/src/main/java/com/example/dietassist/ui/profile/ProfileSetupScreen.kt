package com.example.dietassist.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietassist.ui.theme.DarkSlate
import com.example.dietassist.ui.theme.MintGreen
import com.example.dietassist.ui.theme.PrimaryTeal
import com.example.dietassist.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val setupState by viewModel.setupState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var name by remember { mutableStateOf("") }
    var dailyTarget by remember { mutableStateOf("2000") }
    var weight by remember { mutableStateOf("60") }
    var height by remember { mutableStateOf("165") }

    // Mengambil nilai inisial dari session user jika sudah ada
    LaunchedEffect(currentUser) {
        currentUser?.let {
            if (name.isEmpty()) name = it.name
            if (dailyTarget == "2000" && it.dailyCalorieTarget != 0) dailyTarget = it.dailyCalorieTarget.toString()
            if (weight == "60" && it.weight != 0.0f) weight = it.weight.toString()
            if (height == "165" && it.height != 0.0f) height = it.height.toString()
        }
    }

    LaunchedEffect(setupState) {
        when (setupState) {
            is SetupState.Success -> {
                Toast.makeText(context, "Profil Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                onNavigateToDashboard()
                viewModel.resetState()
            }
            is SetupState.Error -> {
                Toast.makeText(context, (setupState as SetupState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF8FAFC), Color(0xFFF0FDF4))
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Setup Profil Gizi",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryTeal
            )

            Text(
                text = "Sesuaikan informasi fisik untuk target yang presisi",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    
                    // Input Nama
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Pengguna") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen,
                            focusedLabelColor = PrimaryTeal
                        )
                    )

                    // Input Target Kalori Harian
                    OutlinedTextField(
                        value = dailyTarget,
                        onValueChange = { dailyTarget = it },
                        label = { Text("Target Kalori Harian (kkal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen,
                            focusedLabelColor = PrimaryTeal
                        )
                    )

                    // Input Berat Badan (kg)
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Berat Badan (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen,
                            focusedLabelColor = PrimaryTeal
                        )
                    )

                    // Input Tinggi Badan (cm)
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Tinggi Badan (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen,
                            focusedLabelColor = PrimaryTeal
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Save Button
            Button(
                onClick = {
                    val targetCalories = dailyTarget.toIntOrNull() ?: 2000
                    val weightKg = weight.toFloatOrNull() ?: 60f
                    val heightCm = height.toFloatOrNull() ?: 165f

                    if (name.isBlank()) {
                        Toast.makeText(context, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveProfileSetup(name, targetCalories, weightKg, heightCm)
                    }
                },
                enabled = setupState !is SetupState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (setupState is SetupState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Simpan & Lanjutkan",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
