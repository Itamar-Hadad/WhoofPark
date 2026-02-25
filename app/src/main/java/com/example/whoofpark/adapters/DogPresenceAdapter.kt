package com.example.whoofpark.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whoofpark.databinding.ItemDogPresenceBinding
import com.example.whoofpark.interfaces.DogCallback
import com.example.whoofpark.model.DogPresence
import com.example.whoofpark.utilities.ImageLoader
import java.util.concurrent.TimeUnit

class DogPresenceAdapter(
    private var presenceList: MutableList<DogPresence> = mutableListOf()
) : RecyclerView.Adapter<DogPresenceAdapter.ViewHolder>() {


    var dogCallback: DogCallback? = null

    fun updateDogs(newList: List<DogPresence>) {
        this.presenceList.clear()
        this.presenceList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDogPresenceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dog = presenceList[position]

        holder.binding.dogItemLBLName.text = dog.dogName


        //calculate the time left (optional to display in dogItem_LBL_timeLeft)
        val timeLeft = calculateTimeLeft(dog)
        holder.binding.dogItemLBLTimeLeft.text = "$timeLeft min\nleft"

        ImageLoader
            .getInstance()
            .loadImage(
                dog.dogImageUrl,
                holder.binding.dogItemIMGProfile)

        holder.itemView.setOnClickListener {
            dogCallback?.onDogClicked(dog, position)
        }
    }

    override fun getItemCount(): Int = presenceList.size

    //function that calculates the time left for each dog
    private fun calculateTimeLeft(dog: DogPresence): Long {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - dog.entryTime
        val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
        val remaining = dog.durationMinutes - elapsedMinutes
        return if (remaining > 0) remaining else 0
    }

    inner class ViewHolder(val binding: ItemDogPresenceBinding) : RecyclerView.ViewHolder(binding.root)

}