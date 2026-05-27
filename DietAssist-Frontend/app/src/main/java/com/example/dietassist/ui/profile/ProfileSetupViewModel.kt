package com.example.dietassist.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dietassist.data.model.UserProfile
import com.example.dietassist.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileSetupViewModel : ViewModel() {

    private val authRepo = AuthRepository

    private val _setupState = MutableStateFlow<SetupState>(SetupState.Idle)
    val setupState: StateFlow<SetupState> = _setupState.asStateFlow()

    val currentUser = authRepo.currentUser

    /**
     * Memproses penyimpanan target gizi awal pengguna baru.
     */
    fun saveProfileSetup(
        name: String,
        dailyTarget: Int,
        weight: Float,
        height: Float
    ) {
        val user = currentUser.value ?: run {
            _setupState.value = SetupState.Error("User session tidak ditemukan")
            return
        }

        _setupState.value = SetupState.Loading
        viewModelScope.launch {
            val updatedProfile = UserProfile(
                id = user.id,
                name = name,
                email = user.email,
                dailyCalorieTarget = dailyTarget,
                weight = weight,
                height = height
            )

            authRepo.updateProfile(updatedProfile)
                .onSuccess {
                    _setupState.value = SetupState.Success
                }
                .onFailure { error ->
                    _setupState.value = SetupState.Error(error.localizedMessage ?: "Gagal memperbarui profil")
                }
        }
    }

    fun resetState() {
        _setupState.value = SetupState.Idle
    }
}

sealed interface SetupState {
    object Idle : SetupState
    object Loading : SetupState
    object Success : SetupState
    data class Error(val message: String) : SetupState
}
