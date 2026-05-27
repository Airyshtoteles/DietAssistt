package com.example.dietassist.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dietassist.data.model.UserProfile
import com.example.dietassist.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val authRepo = AuthRepository

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    val currentUser = authRepo.currentUser

    /**
     * Memproses masuk via Google Sign-In.
     * Menerima idToken (OAuth), profil dasar, dan melakukan sinkronisasi dengan database di Backend.
     */
    fun onGoogleSignInSuccess(
        idToken: String?,
        name: String?,
        email: String?,
        googleUserId: String
    ) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            authRepo.loginWithGoogle(idToken, name, email, googleUserId)
                .onSuccess { profile ->
                    // Jika profil default target belum terisi (weight/height masih 0.0, anggap profil baru)
                    if (profile.weight == 0.0f || profile.height == 0.0f) {
                        _loginState.value = LoginState.SuccessNeedOnboarding(profile)
                    } else {
                        _loginState.value = LoginState.Success(profile)
                    }
                }
                .onFailure { error ->
                    _loginState.value = LoginState.Error(error.localizedMessage ?: "Autentikasi Gagal")
                }
        }
    }

    /**
     * Simulasi login cepat tanpa OAuth Google untuk keperluan pengujian.
     */
    fun performDemoLogin() {
        onGoogleSignInSuccess(
            idToken = "demo_token_12345",
            name = "Budi Raharjo",
            email = "budi.raharjo@mahasiswa.ac.id",
            googleUserId = "user_budi_123"
        )
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    data class Success(val user: UserProfile) : LoginState
    data class SuccessNeedOnboarding(val user: UserProfile) : LoginState
    data class Error(val message: String) : LoginState
}
