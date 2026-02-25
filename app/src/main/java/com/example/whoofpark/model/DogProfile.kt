package com.example.whoofpark.model

data class DogProfile(
    val name: String = "Unknown",
    val breed: String = "",
    val gender: String = "",
    val age: Int = 0,
    val size: String = "",
    val hobbies: String = "",
    val photoLocalUri: String? = null, // content://...
    val photoUrl: String? = null // https://... (Firebase Storage)
)
