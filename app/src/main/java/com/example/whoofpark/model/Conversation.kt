package com.example.whoofpark.model

data class Conversation(
    val parkId: String = "",
    val parkName: String = "",
    val parkImageUrl: String? = null,
    val lastMessage: String = "",
    val timestamp: Long = 0
)

