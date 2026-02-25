package com.example.whoofpark.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whoofpark.databinding.ItemDogPresenceBinding
import com.example.whoofpark.interfaces.DogCallback
import com.example.whoofpark.model.DogPresence
import com.example.whoofpark.utilities.Constants
import com.example.whoofpark.utilities.ImageLoader

class DogPresenceAdapter(private val dogs: List<DogPresence>) :
    RecyclerView.Adapter<DogPresenceAdapter.DogViewHolder>() {
    var dogCallback: DogCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val binding = ItemDogPresenceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        with(holder) {
            with(getItem(position)) {

                binding.dogItemLBLName.text = dogName

                //calculate the remaining time in minutes
                val minutesPassed = (System.currentTimeMillis() - entryTime) / Constants.Timer.MILLIS_PER_MINUTE
                val remaining = (durationMinutes - minutesPassed).coerceAtLeast(0)
                binding.dogItemLBLTimeLeft.text = "$remaining\nmin left"

                ImageLoader
                    .getInstance()
                    .loadImage(
                    dogImageUrl,
                    binding.dogItemIMGProfile
                )

                //press on dog item to see more details
                binding.root.setOnClickListener {
                    dogCallback?.onDogClicked(this, absoluteAdapterPosition)
                }
            }
        }
    }

    override fun getItemCount(): Int = dogs.size
    fun getItem(position: Int): DogPresence = dogs[position]

    inner class DogViewHolder(val binding: ItemDogPresenceBinding) :
        RecyclerView.ViewHolder(binding.root)
}
