package com.example.whoofpark.adapters

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whoofpark.databinding.ItemChatReceivedBinding
import com.example.whoofpark.databinding.ItemChatSentBinding
import com.example.whoofpark.interfaces.ChatCallback
import com.example.whoofpark.model.ChatMessage
import com.example.whoofpark.utilities.ImageLoader
import com.google.firebase.auth.FirebaseAuth
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private var messageList: MutableList<ChatMessage> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var chatCallback: ChatCallback? = null
    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // פונקציית עדכון הדומה לזו שב-DogPresenceAdapter
    fun updateMessages(newList: List<ChatMessage>) {
        this.messageList.clear()
        this.messageList.addAll(newList)
        notifyDataSetChanged()
    }

    // choose which xml to inflate based on the senderId
    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            SentViewHolder(ItemChatSentBinding.inflate(inflater, parent, false))
        } else {
            ReceivedViewHolder(ItemChatReceivedBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]


        // do the bind according to the type of holder
        when (holder) {
            is SentViewHolder -> holder.bind(message)
            is ReceivedViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messageList.size

    // function to format the time
    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // view holder for the right messages (my messages)
    inner class SentViewHolder(val binding: ItemChatSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.chatItemLBLMessage.text = message.text
            binding.chatItemLBLSenderName.text = message.senderName
            binding.chatItemLBLTime.text = formatTime(message.timestamp)

            ImageLoader.getInstance().loadImage(
                message.senderPhotoUrl,
                binding.chatItemIMGProfile
            )

            binding.chatItemIMGProfile.setOnClickListener {
                chatCallback?.onUserPhotoClicked(message.senderId)
            }
        }
    }

    // view holder for the left messages (others messages)
    inner class ReceivedViewHolder(val binding: ItemChatReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.chatItemLBLMessage.text = message.text
            binding.chatItemLBLSenderName.text = message.senderName
            binding.chatItemLBLTime.text = formatTime(message.timestamp)

            ImageLoader.getInstance().loadImage(
                message.senderPhotoUrl,
                binding.chatItemIMGProfile
            )

            binding.chatItemIMGProfile.setOnClickListener {
                chatCallback?.onUserPhotoClicked(message.senderId)
            }
        }
    }
}