package com.example.dietassist.data.model

data class ChatMessage(
    val role: String, // "user" atau "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
