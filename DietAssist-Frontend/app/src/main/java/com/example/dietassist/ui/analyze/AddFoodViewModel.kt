package com.example.dietassist.ui.analyze

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dietassist.data.model.AIAnalysisResult
import com.example.dietassist.data.repository.AuthRepository
import com.example.dietassist.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddFoodViewModel : ViewModel() {

    private val foodRepo = FoodRepository
    private val authRepo = AuthRepository

    private val _uiState = MutableStateFlow<AddFoodUiState>(AddFoodUiState.Idle)
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    private val _analysisResult = MutableStateFlow<AIAnalysisResult?>(null)
    val analysisResult: StateFlow<AIAnalysisResult?> = _analysisResult.asStateFlow()

    /**
     * Mengirimkan foto (byte array) atau deskripsi teks makanan ke Gemini 2.5 Flash di backend.
     */
    fun analyzeWithAI(
        textDescription: String?,
        imageBytes: ByteArray?
    ) {
        _uiState.value = AddFoodUiState.Loading
        _analysisResult.value = null

        viewModelScope.launch {
            var base64Str: String? = null
            
            // Konversi gambar ke base64 string jika diunggah via kamera
            if (imageBytes != null) {
                base64Str = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            }

            foodRepo.analyzeFoodWithAI(textDescription, base64Str)
                .onSuccess { result ->
                    _analysisResult.value = result
                    _uiState.value = AddFoodUiState.AnalysisSuccess(result)
                }
                .onFailure { error ->
                    _uiState.value = AddFoodUiState.Error(error.localizedMessage ?: "AI Gagal menganalisis makanan")
                }
        }
    }

    /**
     * Alur penyimpanan:
     * 1. Unggah biner foto ke Supabase Storage (jika ada hasil tangkapan kamera).
     * 2. Simpan entri gizi terverifikasi ke Next.js database food_logs.
     */
    fun confirmAndSaveFood(
        foodName: String,
        calories: Float,
        protein: Float,
        carbs: Float,
        fats: Float,
        imageBytes: ByteArray?
    ) {
        val user = authRepo.currentUser.value ?: run {
            _uiState.value = AddFoodUiState.Error("Sesi login telah habis")
            return
        }

        _uiState.value = AddFoodUiState.Saving
        viewModelScope.launch {
            foodRepo.uploadAndSaveFoodLog(
                userId = user.id,
                foodName = foodName,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats,
                imageBytes = imageBytes
            ).onSuccess {
                _uiState.value = AddFoodUiState.SaveSuccess
            }.onFailure { error ->
                _uiState.value = AddFoodUiState.Error(error.localizedMessage ?: "Gagal menyimpan log makanan")
            }
        }
    }

    fun resetState() {
        _uiState.value = AddFoodUiState.Idle
        _analysisResult.value = null
    }
}

sealed interface AddFoodUiState {
    object Idle : AddFoodUiState
    object Loading : AddFoodUiState
    data class AnalysisSuccess(val result: AIAnalysisResult) : AddFoodUiState
    object Saving : AddFoodUiState
    object SaveSuccess : AddFoodUiState
    data class Error(val message: String) : AddFoodUiState
}
