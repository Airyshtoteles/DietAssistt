package com.example.dietassist.ui.detail

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dietassist.data.repository.AuthRepository
import com.example.dietassist.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    viewModel: FoodDetailViewModel,
    logId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val detailState by viewModel.detailState.collectAsState()
    val foodLog by viewModel.foodLog.collectAsState()
    val userSession = AuthRepository.currentUser.value

    // Inisialisasi pengambilan data makanan dari server
    LaunchedEffect(key1 = logId) {
        userSession?.let {
            viewModel.loadFoodDetail(it.id, logId)
        }
    }

    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }

    // Isi formulir jika data log makanan berhasil dimuat
    LaunchedEffect(foodLog) {
        foodLog?.let {
            name = it.foodName
            calories = it.calories.toInt().toString()
            protein = it.protein.toInt().toString()
            carbs = it.carbs.toInt().toString()
            fats = it.fats.toInt().toString()
        }
    }

    LaunchedEffect(detailState) {
        when (detailState) {
            is DetailUiState.SaveSuccess -> {
                Toast.makeText(context, "Log makanan diperbarui!", Toast.LENGTH_SHORT).show()
                onNavigateBack()
                viewModel.resetState()
            }
            is DetailUiState.DeleteSuccess -> {
                Toast.makeText(context, "Log makanan dihapus dari diary!", Toast.LENGTH_SHORT).show()
                onNavigateBack()
                viewModel.resetState()
            }
            is DetailUiState.Error -> {
                Toast.makeText(context, (detailState as DetailUiState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail & Edit Makanan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Kembali", color = PrimaryTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgWhite)
            )
        },
        containerColor = BgWhite
    ) { paddingValues ->
        
        if (detailState is DetailUiState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MintGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                
                // 1. TAMPILAN FOTO MAKANAN (Menggunakan Coil)
                if (!foodLog?.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = foodLog!!.imageUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(LightMint, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍲", fontSize = 56.sp)
                    }
                }

                // 2. KARTU HAK EDIT (CRUD: UPDATE)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Rincian Nutrisi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DarkSlate
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nama Makanan") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = calories,
                            onValueChange = { calories = it },
                            label = { Text("Kalori (kkal)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = protein,
                                onValueChange = { protein = it },
                                label = { Text("Protein (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = carbs,
                                onValueChange = { carbs = it },
                                label = { Text("Karbo (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = fats,
                                onValueChange = { fats = it },
                                label = { Text("Lemak (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. ACTION BUTTONS: SIMPAN (UPDATE) & HAPUS (DELETE)
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Simpan
                    Button(
                        onClick = {
                            viewModel.updateFoodEntry(
                                foodName = name,
                                calories = calories.toFloatOrNull() ?: 0f,
                                protein = protein.toFloatOrNull() ?: 0f,
                                carbs = carbs.toFloatOrNull() ?: 0f,
                                fats = fats.toFloatOrNull() ?: 0f
                            )
                        },
                        enabled = detailState !is DetailUiState.Saving && detailState !is DetailUiState.Deleting,
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (detailState is DetailUiState.Saving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Simpan Perubahan", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Hapus
                    Button(
                        onClick = {
                            viewModel.deleteFoodEntry()
                        },
                        enabled = detailState !is DetailUiState.Saving && detailState !is DetailUiState.Deleting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (detailState is DetailUiState.Deleting) {
                            CircularProgressIndicator(color = CoralPink, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Hapus Log Makanan", color = CoralPink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
