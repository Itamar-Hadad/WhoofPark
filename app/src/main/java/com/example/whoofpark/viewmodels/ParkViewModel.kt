package com.example.whoofpark.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.whoofpark.model.DogPresence
import com.example.whoofpark.utilities.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ParkViewModel : ViewModel() {

    /**
     * Without a ViewModel, screen rotation destroys fragment data and forces a costly Firebase re-fetch
     * with a ViewModel, data persists in RAM, allowing the recreated fragment to reconnect instantly.
     */

    private val database = FirebaseFirestore.getInstance()
    private var parkListener: ListenerRegistration? = null


    //the secret list that holds the data in the phone memory
    //list of dogs in the park right now
    private val dogsInParkList = MutableLiveData<List<DogPresence>>(emptyList())


    //the list the fragment listen
    val dogsInPark: LiveData<List<DogPresence>> get() = dogsInParkList


    //function that starts listening to a specific park in Firebase
    fun startListeningToPark(parkId: String) {

        //clean listener if there was one before (for example we change park)
        stopListening()

        parkListener = database.collection(Constants.FIRESTORE.LIVE_PRESENCE_REF)
            .whereEqualTo("parkId", parkId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener


                // Convert the Firestore documents to a list of DogPresence objects
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DogPresence::class.java)
                } ?: emptyList()

                dogsInParkList.value = list
            }
    }

    private fun stopListening() {
        parkListener?.remove()
        parkListener = null
    }

    //when we close the fragment/ ViewModel, stop listening to the data in the database
    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}