package com.example.dietassist.data.repository

import com.example.dietassist.data.model.AIAnalysisResult
import com.example.dietassist.data.model.FoodLog
import com.example.dietassist.data.remote.AIAnalyzeRequest
import com.example.dietassist.data.remote.RetrofitClient
import com.example.dietassist.data.remote.SupabaseStorageService
import java.util.UUID

object FoodRepository {

    private val api = RetrofitClient.apiService

    /**
     * Mengambil daftar log makanan pengguna berdasarkan userId dan tanggal (opsional).
     */
    suspend fun getFoodLogs(userId: String, date: String? = null): Result<List<FoodLog>> {
        return try {
            val response = api.getFoodLogs(userId, date)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.logs)
            } else {
                Result.failure(Exception("Gagal mengambil log makanan: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Menganalisis gambar makanan (base64) atau deskripsi teks melalui Gemini 2.5 Flash API di backend.
     */
    suspend fun analyzeFoodWithAI(
        textDescription: String?,
        imageBytesBase64: String?,
        mimeType: String? = "image/jpeg"
    ): Result<AIAnalysisResult> {
        return try {
            val response = api.analyzeFood(AIAnalyzeRequest(textDescription, imageBytesBase64, mimeType))
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.analysis)
            } else {
                Result.failure(Exception("Gagal menganalisis gizi: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Alur Unggah Gambar + Simpan Database (Mandatori):
     * 1. Mengunggah gambar makanan langsung ke Supabase Storage.
     * 2. Mendapatkan URL Publik.
     * 3. Menyimpan entri log makanan ke REST API Next.js.
     */
    suspend fun uploadAndSaveFoodLog(
        userId: String,
        foodName: String,
        calories: Float,
        protein: Float,
        carbs: Float,
        fats: Float,
        imageBytes: ByteArray?
    ): Result<FoodLog> {
        return try {
            val base64Image = if (imageBytes != null) {
                android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
            } else {
                null
            }

            val newLog = FoodLog(
                userId = userId,
                foodName = foodName,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats,
                image = base64Image
            )

            val response = api.addFoodLog(newLog)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.log)
            } else {
                Result.failure(Exception("Gagal menyimpan log makanan ke database: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Memperbarui log makanan.
     */
    suspend fun updateFoodLog(log: FoodLog): Result<FoodLog> {
        return try {
            val logId = log.id ?: return Result.failure(Exception("ID log makanan kosong"))
            val response = api.updateFoodLog(logId, log)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.log)
            } else {
                Result.failure(Exception("Gagal memperbarui log makanan: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Menghapus log makanan.
     */
    suspend fun deleteFoodLog(id: String): Result<Boolean> {
        return try {
            val response = api.deleteFoodLog(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Gagal menghapus log makanan: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
