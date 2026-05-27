package com.example.dietassist.data.repository

import com.example.dietassist.data.model.UserProfile
import com.example.dietassist.data.remote.AuthRequest
import com.example.dietassist.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthRepository {

    private val api = RetrofitClient.apiService

    // Session State sederhana dalam memori
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    /**
     * Memverifikasi login Google ke backend Next.js.
     */
    suspend fun loginWithGoogle(
        idToken: String?,
        name: String?,
        email: String?,
        userId: String
    ): Result<UserProfile> {
        return try {
            val response = api.verifyGoogleAuth(AuthRequest(idToken, name, email, userId))
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!.user
                _currentUser.value = profile
                Result.success(profile)
            } else {
                Result.failure(Exception("Gagal melakukan verifikasi ke server: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Memperbarui profil target gizi pengguna (seperti Target Kalori, Berat, Tinggi).
     */
    suspend fun updateProfile(profile: UserProfile): Result<UserProfile> {
        return try {
            // Dalam sistem ini, pembaruan target profil dapat dikirim via API Google Auth yang melakukan sinkronisasi database
            val response = api.verifyGoogleAuth(
                AuthRequest(
                    idToken = null,
                    name = profile.name,
                    email = profile.email,
                    userId = profile.id,
                    dailyCalorieTarget = profile.dailyCalorieTarget,
                    weight = profile.weight,
                    height = profile.height
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val updatedProfile = response.body()!!.user
                _currentUser.value = updatedProfile
                Result.success(updatedProfile)
            } else {
                Result.failure(Exception("Gagal memperbarui profil: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        _currentUser.value = null
    }
}
