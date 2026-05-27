package com.example.dietassist.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dietassist.data.model.ChatMessage
import com.example.dietassist.data.remote.ChatDtoMessage
import com.example.dietassist.data.remote.ChatRequest
import com.example.dietassist.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HealthChatViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = "assistant",
                content = "Halo! Saya **DietAssistAi**, konsultan kesehatan, diet, dan nutrisi digital Anda.\n\nAda yang bisa saya bantu hari ini? Anda bisa bertanya tentang menu diet sehat, target kalori, kebutuhan air harian, atau tips olahraga."
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(role = "user", content = text)
        // Tambahkan ke daftar pesan
        _messages.value = _messages.value + userMessage
        _isSending.value = true

        viewModelScope.launch {
            try {
                // Konversi seluruh riwayat pesan ke format DTO
                val dtoMessages = _messages.value.map {
                    ChatDtoMessage(role = it.role, content = it.content)
                }

                val response = api.chatWithAI(ChatRequest(dtoMessages))
                if (response.isSuccessful && response.body() != null) {
                    val replyText = response.body()!!.reply
                    _messages.value = _messages.value + ChatMessage(role = "assistant", content = replyText)
                } else {
                    _messages.value = _messages.value + ChatMessage(
                        role = "assistant",
                        content = "Maaf, saya sedang mengalami kendala jaringan (Error: ${response.code()}). Silakan coba sesaat lagi!"
                    )
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    role = "assistant",
                    content = "Koneksi ke server terputus. Pastikan server backend Anda menyala dan Anda memiliki akses internet."
                )
            } finally {
                _isSending.value = false
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                role = "assistant",
                content = "Halo! Saya **DietAssistAi**, konsultan kesehatan, diet, dan nutrisi digital Anda.\n\nAda yang bisa saya bantu hari ini? Anda bisa bertanya tentang menu diet sehat, target kalori, kebutuhan air harian, atau tips olahraga."
            )
        )
    }
}
