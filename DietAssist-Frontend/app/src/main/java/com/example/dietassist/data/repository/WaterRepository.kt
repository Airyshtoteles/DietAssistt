package com.example.dietassist.data.repository

import com.example.dietassist.data.model.WaterLog
import com.example.dietassist.data.remote.RetrofitClient

object WaterRepository {

    private val api = RetrofitClient.apiService

    /**
     * Mengambil log air minum hari ini dan total mililiter dikonsumsi.
     */
    suspend fun getDailyWater(userId: String, date: String): Result<Pair<List<WaterLog>, Int>> {
        return try {
            val response = api.getWaterLogs(userId, date)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(Pair(body.logs, body.total_ml))
            } else {
                Result.failure(Exception("Gagal mengambil log air: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Menambahkan log air minum (misal +250ml atau +500ml).
     */
    suspend fun logWater(userId: String, amountMl: Int): Result<WaterLog> {
        return try {
            val response = api.addWaterLog(WaterLog(userId = userId, amountMl = amountMl))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.log)
            } else {
                Result.failure(Exception("Gagal menyimpan log air minum: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
