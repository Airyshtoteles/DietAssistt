package com.example.dietassist.ui.analyze

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.dietassist.ui.components.ShimmerLoading
import com.example.dietassist.ui.theme.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.graphics.Bitmap
import android.graphics.BitmapFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    viewModel: AddFoodViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // CameraX tools
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    var capturedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isImageCaptured by remember { mutableStateOf(false) }

    // Input deskripsi teks manual
    var textDescription by remember { mutableStateOf("") }

    // State Bottom Sheet Konfirmasi AI
    var showBottomSheet by remember { mutableStateOf(false) }

    // Form data yang diisi dari Gemini AI
    var editFoodName by remember { mutableStateOf("") }
    var editCalories by remember { mutableStateOf("") }
    var editProtein by remember { mutableStateOf("") }
    var editCarbs by remember { mutableStateOf("") }
    var editFats by remember { mutableStateOf("") }

    // Slider tab state: "Scan Food", "Barcode", "Food label"
    var selectedScanMode by remember { mutableStateOf("Scan Food") }
    var isFlashOn by remember { mutableStateOf(false) }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    capturedImageBytes = bytes
                    isImageCaptured = true
                    Toast.makeText(context, "Foto makanan dari galeri berhasil dimuat!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memuat gambar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddFoodUiState.AnalysisSuccess -> {
                editFoodName = state.result.foodName
                editCalories = state.result.calories.toInt().toString()
                editProtein = state.result.protein.toInt().toString()
                editCarbs = state.result.carbs.toInt().toString()
                editFats = state.result.fats.toInt().toString()
                showBottomSheet = true
            }
            is AddFoodUiState.SaveSuccess -> {
                Toast.makeText(context, "Makanan berhasil disimpan ke Diary!", Toast.LENGTH_SHORT).show()
                onNavigateBack()
                viewModel.resetState()
            }
            is AddFoodUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        if (hasCameraPermission && !isImageCaptured) {
            // Full Screen Camera Preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        try {
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                            )
                            // Enable flash control if needed
                            camera.cameraControl.enableTorch(isFlashOn)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Viewfinder Center Bracket
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(240.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
            )

            // Top Overlay Buttons (Close & Help)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button X
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                // Help button ?
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .clickable {
                            Toast.makeText(context, "Posisikan makanan di tengah kotak lalu klik tombol jepret.", Toast.LENGTH_LONG).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Bottom controls overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Scan Mode Slider Tabs
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Scan Food", "Barcode", "Food label").forEach { mode ->
                        val isSelected = mode == selectedScanMode
                        Text(
                            text = mode,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedScanMode = mode
                                    if (mode != "Scan Food") {
                                        Toast.makeText(context, "$mode mode is coming soon!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Main Shutter / Controls row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Flash toggle
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .clickable {
                                isFlashOn = !isFlashOn
                                Toast.makeText(context, if (isFlashOn) "Flash ON" else "Flash OFF", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isFlashOn) "⚡" else "💡", fontSize = 20.sp)
                    }

                    // Center: Shutter Button
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(4.dp)
                            .background(Color.White, CircleShape)
                            .clickable {
                                val photoFile = File(context.cacheDir, "temp_capture.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                imageCapture.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            capturedImageBytes = compressImageFile(photoFile)
                                            isImageCaptured = true
                                            Toast.makeText(context, "Foto berhasil diambil!", Toast.LENGTH_SHORT).show()
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            Toast.makeText(context, "Gagal mengambil foto: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                    )

                    // Right: Gallery Picker
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .clickable { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🖼️", fontSize = 20.sp)
                    }
                }
            }
        } else {
            // Confirm captured image view & Text input section (if camera permission not granted or image captured)
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Konfirmasi Analisis", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            TextButton(onClick = {
                                isImageCaptured = false
                                capturedImageBytes = null
                                viewModel.resetState()
                            }) {
                                Text("Ulangi", color = PrimaryTeal)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = BgWhite)
                    )
                },
                containerColor = BgWhite
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (isImageCaptured) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightMint),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("📸", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Foto Makanan Berhasil Dimuat",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryTeal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(
                                    onClick = {
                                        isImageCaptured = false
                                        capturedImageBytes = null
                                    }
                                ) {
                                    Text("Ambil Ulang Foto", color = CoralPink, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Request Permission / No Camera state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Akses Kamera Dibutuhkan", fontWeight = FontWeight.Bold, color = DarkSlate)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen)
                                ) {
                                    Text("Izinkan Kamera")
                                }
                            }
                        }
                    }

                    // Input Deskripsi Teks Manual
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Ketik Deskripsi Makanan (Opsional)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DarkSlate
                            )

                            OutlinedTextField(
                                value = textDescription,
                                onValueChange = { textDescription = it },
                                placeholder = { Text("Contoh: Nasi goreng kampung pake telur mata sapi") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MintGreen,
                                    focusedLabelColor = PrimaryTeal
                                )
                            )
                        }
                    }

                    // Shimmer/Skeleton loading saat AI menganalisis
                    AnimatedVisibility(visible = uiState is AddFoodUiState.Loading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderGray, RoundedCornerShape(24.dp))
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = MintGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("DietAssistAi Sedang Menganalisis Nutrisi...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryTeal)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            ShimmerLoading(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(6.dp)))
                            ShimmerLoading(modifier = Modifier.fillMaxWidth(0.7f).height(18.dp).clip(RoundedCornerShape(6.dp)))
                            ShimmerLoading(modifier = Modifier.fillMaxWidth(0.5f).height(18.dp).clip(RoundedCornerShape(6.dp)))
                        }
                    }

                    // Main Action Button
                    Button(
                        onClick = {
                            if (!isImageCaptured && textDescription.isBlank()) {
                                Toast.makeText(context, "Silakan ambil foto atau ketik deskripsi makanan!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.analyzeWithAI(textDescription, capturedImageBytes)
                            }
                        },
                        enabled = uiState !is AddFoodUiState.Loading && uiState !is AddFoodUiState.Saving,
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Mulai Analisis DietAssistAi",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bottom Sheet nutrition structure editor confirmation
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "Konfirmasi Nutrisi DietAssistAi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )
                    Text(
                        text = "Hasil pembacaan gizi dari DietAssistAi. Silakan edit jika kurang sesuai.",
                        fontSize = 12.sp,
                        color = TextGray
                    )

                    OutlinedTextField(
                        value = editFoodName,
                        onValueChange = { editFoodName = it },
                        label = { Text("Nama Makanan") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editCalories,
                        onValueChange = { editCalories = it },
                        label = { Text("Kalori (kkal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = editProtein,
                            onValueChange = { editProtein = it },
                            label = { Text("Protein (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editCarbs,
                            onValueChange = { editCarbs = it },
                            label = { Text("Karbohidrat (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editFats,
                            onValueChange = { editFats = it },
                            label = { Text("Lemak (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Save to diary
                    Button(
                        onClick = {
                            viewModel.confirmAndSaveFood(
                                foodName = editFoodName,
                                calories = editCalories.toFloatOrNull() ?: 0f,
                                protein = editProtein.toFloatOrNull() ?: 0f,
                                carbs = editCarbs.toFloatOrNull() ?: 0f,
                                fats = editFats.toFloatOrNull() ?: 0f,
                                imageBytes = capturedImageBytes
                            )
                            showBottomSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Simpan ke Diary Makanan",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun compressImageFile(file: File): ByteArray {
    val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return file.readBytes()
    
    val maxDimension = 1024
    val width = bitmap.width
    val height = bitmap.height
    val resizedBitmap = if (width > maxDimension || height > maxDimension) {
        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / aspectRatio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * aspectRatio).toInt()
        }
        Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    } else {
        bitmap
    }
    
    val outputStream = ByteArrayOutputStream()
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val bytes = outputStream.toByteArray()
    
    if (resizedBitmap != bitmap) {
        resizedBitmap.recycle()
    }
    bitmap.recycle()
    
    return bytes
}
