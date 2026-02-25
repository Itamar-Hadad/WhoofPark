package com.example.whoofpark.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whoofpark.R
import com.example.whoofpark.adapters.ChatAdapter
import com.example.whoofpark.databinding.FragmentChatRoomBinding
import com.example.whoofpark.interfaces.ChatCallback
import com.example.whoofpark.model.ChatMessage
import com.example.whoofpark.utilities.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ChatRoomFragment : Fragment(), ChatCallback {


    private lateinit var binding: FragmentChatRoomBinding
    private lateinit var chatAdapter: ChatAdapter
    private var parkId: String? = null
    private var parkName: String? = null
    private var parkImageUrl: String? = null

    private val database = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var dogName: String = "Unknown Dog"
    private var dogPhotoUrl: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<View>(R.id.bottom_nav)?.visibility = View.GONE

        // שליפת נתונים באמצעות ה-BundleKeys שלך
        parkId = arguments?.getString(Constants.BundleKeys.PARK_ID_KEY)
        parkName = arguments?.getString(Constants.BundleKeys.PARK_NAME_KEY)
        parkImageUrl = arguments?.getString(Constants.BundleKeys.PARK_IMAGE_URL_KEY)


        initViews()
        setupChatRealtime()
        fetchDogProfile()
        setupKeyboardListener()
    }

    private fun setupKeyboardListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            val finalPadding = if (imeHeight > 0) {
                (imeHeight - navigationBarHeight).coerceAtLeast(0)
            } else {
                0
            }

            view.setPadding(0, 0, 0, finalPadding)
            insets
        }
    }


    private fun initViews() {
        binding.chatRoomLBLParkName.text = parkName ?: "Park Chat"

        chatAdapter = ChatAdapter(mutableListOf())
        binding.chatRoomRVMessages.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true //new messages down
            }
        }

        chatAdapter.chatCallback = this

        binding.chatRoomBTNSend.setOnClickListener {
            val text = binding.chatRoomEDTMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                binding.chatRoomEDTMessage.text?.clear()
            }
        }

        binding.chatRoomBTNBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupChatRealtime() {
        val id = parkId ?: return

        database.collection(Constants.FIRESTORE.PARKS_REF)
            .document(id)
            .collection(Constants.FIRESTORE.MESSAGES_SUB_REF)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val messages = snapshot.toObjects(ChatMessage::class.java)
                    chatAdapter.updateMessages(messages)
                    if (messages.isNotEmpty()) {
                        binding.chatRoomRVMessages.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
    }

    private fun sendMessage(text: String) {
        val user = auth.currentUser ?: return
        val id = parkId ?: return

        val newMessage = ChatMessage(
            senderId = user.uid,
            senderName = dogName,
            senderPhotoUrl = dogPhotoUrl,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        // Save in Park
        database.collection(Constants.FIRESTORE.PARKS_REF)
            .document(id)
            .collection(Constants.FIRESTORE.MESSAGES_SUB_REF)
            .add(newMessage)


        //update "my chats" under the dog profiles
        val conversationSummary = hashMapOf(
            "parkId" to id,
            "parkName" to parkName,
            "parkImageUrl" to parkImageUrl,
            "lastMessage" to text,
            "timestamp" to System.currentTimeMillis()
        )

        database.collection(Constants.FIRESTORE.DOG_PROFILES_REF)
            .document(user.uid)
            .collection(Constants.FIRESTORE.CONVERSATIONS_SUB_REF)
            .document(id)
            .set(conversationSummary)
    }

    private fun fetchDogProfile() {
        val uid = auth.currentUser?.uid ?: return
        database.collection(Constants.FIRESTORE.DOG_PROFILES_REF)
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    dogName = document.getString("name") ?: "Unknown Dog"
                    dogPhotoUrl = document.getString("photoUrl") ?: ""
                }
            }
    }

    override fun onUserPhotoClicked(userId: String) {

        //if (userId == auth.currentUser?.uid) return

        val bundle = Bundle().apply {
            putString(Constants.BundleKeys.USER_ID_KEY, userId)
        }

        findNavController().navigate(
            R.id.action_chatRoomFragment_to_otherDogProfileFragment,
            bundle
        )
    }



}