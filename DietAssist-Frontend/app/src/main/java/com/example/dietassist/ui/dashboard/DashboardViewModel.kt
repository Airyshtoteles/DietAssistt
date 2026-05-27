package com.example.dietassist.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dietassist.data.model.FoodLog
import com.example.dietassist.data.model.UserProfile
import com.example.dietassist.data.model.WaterLog
import com.example.dietassist.data.repository.AuthRepository
import com.example.dietassist.data.repository.FoodRepository
import com.example.dietassist.data.repository.WaterRepository
import com.example.dietassist.ui.components.DailyCalorie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel : ViewModel() {

    private val authRepo = AuthRepository
    private val foodRepo = FoodRepository
    private val waterRepo = WaterRepository

    val currentUser = authRepo.currentUser

    private val _foodLogs = MutableStateFlow<List<FoodLog>>(emptyList())
    val foodLogs: StateFlow<List<FoodLog>> = _foodLogs.asStateFlow()

    private val _waterLogs = MutableStateFlow<List<WaterLog>>(emptyList())
    val waterLogs: StateFlow<List<WaterLog>> = _waterLogs.asStateFlow()

    private val _totalWaterMl = MutableStateFlow(0)
    val totalWaterMl: StateFlow<Int> = _totalWaterMl.asStateFlow()

    private val _weeklyHistory = MutableStateFlow<List<DailyCalorie>>(emptyList())
    val weeklyHistory: StateFlow<List<DailyCalorie>> = _weeklyHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Memanggil ulang seluruh API untuk menyinkronkan data Android dengan Web Server secara real-time.
     */
    fun refreshData() {
        val user = currentUser.value ?: return
        val todayStr = dateFormat.format(Date())

        _isLoading.value = true
        viewModelScope.launch {
            // 1. Ambil Log Makanan Hari Ini
            foodRepo.getFoodLogs(user.id, todayStr)
                .onSuccess { logs ->
                    _foodLogs.value = logs
                }

            // 2. Ambil Log Air Hari Ini
            waterRepo.getDailyWater(user.id, todayStr)
                .onSuccess { pair ->
                    _waterLogs.value = pair.first
                    _totalWaterMl.value = pair.second
                }

            // 3. Ambil Log Sejarah Mingguan (Untuk grafik Canvas)
            loadWeeklyHistory(user.id)

            _isLoading.value = false
        }
    }

    /**
     * Menambahkan entri air minum secara instan dan memperbarui StateFlow harian.
     */
    fun addWaterIntake(amountMl: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            waterRepo.logWater(user.id, amountMl)
                .onSuccess {
                    refreshData() // Refresh untuk sinkronisasi total air minum dan data server terbaru
                }
        }
    }

    /**
     * Membuat tren kalori mingguan (dummy data dikombinasikan dengan kalori hari ini) untuk rendering Canvas.
     */
    private fun loadWeeklyHistory(userId: String) {
        // Pada aplikasi nyata, backend menyediakan rata-rata mingguan.
        // Di sini kita membuat dataset 7 hari terakhir demi demonstrasi visual yang memikat.
        val days = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
        val todayCal = _foodLogs.value.sumOf { it.calories.toDouble() }.toFloat()
        
        val history = days.mapIndexed { index, day ->
            // Untuk hari terakhir (misal hari ke 7), gunakan data kalori real hari ini
            if (index == 6) {
                DailyCalorie(day, todayCal)
            } else {
                // Dataset demo variatif agar grafik Canvas terlihat menakjubkan
                DailyCalorie(day, (1200 + (index * 130) % 800).toFloat())
            }
        }
        _weeklyHistory.value = history
    }

    fun logout() {
        authRepo.logout()
    }
}
