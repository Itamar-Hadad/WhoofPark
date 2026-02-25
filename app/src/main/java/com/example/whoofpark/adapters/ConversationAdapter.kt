package com.example.whoofpark.adapters

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whoofpark.databinding.ItemConversationBinding
import com.example.whoofpark.model.Conversation
import com.example.whoofpark.utilities.ImageLoader
import java.util.Date
import java.util.Locale

class ConversationAdapter(
    private var conversations: List<Conversation>,
    private val onChatClicked: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: ItemConversationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = conversations[position]

        with(holder.binding) {

            itemConvLBLParkName.text = conversation.parkName
            itemConvLBLLastMessage.text = conversation.lastMessage

            ImageLoader.getInstance().loadImage(
                conversation.parkImageUrl ?: "",
                itemConvIMGPark)

            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            itemConvLBLTime.text = sdf.format(Date(conversation.timestamp))


            root.setOnClickListener {
                onChatClicked(conversation)
            }
        }
    }

    override fun getItemCount(): Int = conversations.size

    //function to update the list if there is a new messages
    fun updateList(newList: List<Conversation>) {
        conversations = newList
        notifyDataSetChanged()
    }
}