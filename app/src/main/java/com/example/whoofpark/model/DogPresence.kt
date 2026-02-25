package com.example.whoofpark.model

data class DogPresence(
    val userId: String = "",
    val dogName: String = "",
    val dogImageUrl: String = "",
    val parkId: String = "",
    val entryTime: Long = 0,
    val durationMinutes: Int = 0
)
