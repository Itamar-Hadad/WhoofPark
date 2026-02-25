package com.example.whoofpark.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.whoofpark.model.DogProfile

class DogViewModel : ViewModel() {

    //viewmodel sane the data on the RAM

    //profileStorage is the secret variable that holds the data in the phone memory
    private val profileStorage = MutableLiveData<DogProfile>(DogProfile())

    //profileToDisplay is the variable that the fragment see and listen to him
    val profileToDisplay: LiveData<DogProfile> get() = profileStorage

    //function that simply updates the storage with new data
    fun saveChanges(newInfo: DogProfile) {
        profileStorage.value = newInfo
    }
}