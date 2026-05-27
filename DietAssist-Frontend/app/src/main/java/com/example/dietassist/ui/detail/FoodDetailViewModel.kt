package com.example.dietassist.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dietassist.data.model.FoodLog
import com.example.dietassist.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodDetailViewModel : ViewModel() {

    private val foodRepo = FoodRepository

    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Idle)
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    private val _foodLog = MutableStateFlow<FoodLog?>(null)
    val foodLog: StateFlow<FoodLog?> = _foodLog.asStateFlow()

    /**
     * Memuat data log makanan spesifik hari ini berdasarkan ID dari navigation.
     */
    fun loadFoodDetail(userId: String, logId: String) {
        _detailState.value = DetailUiState.Loading
        viewModelScope.launch {
            foodRepo.getFoodLogs(userId)
                .onSuccess { logs ->
                    val found = logs.find { it.id == logId }
                    if (found != null) {
                        _foodLog.value = found
                        _detailState.value = DetailUiState.Loaded(found)
                    } else {
                        _detailState.value = DetailUiState.Error("Log makanan tidak ditemukan")
                    }
                }
                .onFailure { error ->
                    _detailState.value = DetailUiState.Error(error.localizedMessage ?: "Gagal memuat detail")
                }
        }
    }

    /**
     * Menyimpan pembaruan porsi/nutrisi log makanan (Fitur Update CRUD).
     */
    fun updateFoodEntry(
        foodName: String,
        calories: Float,
        protein: Float,
        carbs: Float,
        fats: Float
    ) {
        val currentLog = _foodLog.value ?: return
        val updatedLog = currentLog.copy(
            foodName = foodName,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fats = fats
        )

        _detailState.value = DetailUiState.Saving
        viewModelScope.launch {
            foodRepo.updateFoodLog(updatedLog)
                .onSuccess {
                    _foodLog.value = it
                    _detailState.value = DetailUiState.SaveSuccess
                }
                .onFailure { error ->
                    _detailState.value = DetailUiState.Error(error.localizedMessage ?: "Gagal memperbarui")
                }
        }
    }

    /**
     * Menghapus log makanan secara permanen (Fitur Delete CRUD).
     */
    fun deleteFoodEntry() {
        val logId = _foodLog.value?.id ?: return
        _detailState.value = DetailUiState.Deleting
        viewModelScope.launch {
            foodRepo.deleteFoodLog(logId)
                .onSuccess {
                    _detailState.value = DetailUiState.DeleteSuccess
                }
                .onFailure { error ->
                    _detailState.value = DetailUiState.Error(error.localizedMessage ?: "Gagal menghapus")
                }
        }
    }

    fun resetState() {
        _detailState.value = DetailUiState.Idle
    }
}

sealed interface DetailUiState {
    object Idle : DetailUiState
    object Loading : DetailUiState
    data class Loaded(val log: FoodLog) : DetailUiState
    object Saving : DetailUiState
    object Deleting : DetailUiState
    object SaveSuccess : DetailUiState
    object DeleteSuccess : DetailUiState
    data class Error(val message: String) : DetailUiState
}
