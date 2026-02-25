package com.example.whoofpark.interfaces

import com.example.whoofpark.model.DogPresence

interface DogCallback {
    fun onDogClicked(dog: DogPresence, position: Int)
}
