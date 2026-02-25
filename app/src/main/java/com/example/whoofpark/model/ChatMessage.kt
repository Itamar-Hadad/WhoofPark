package com.example.whoofpark.model

data class ChatMessage(
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
