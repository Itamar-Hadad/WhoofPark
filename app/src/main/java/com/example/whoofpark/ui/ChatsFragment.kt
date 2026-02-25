package com.example.whoofpark.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whoofpark.R
import com.example.whoofpark.adapters.ConversationAdapter
import com.example.whoofpark.databinding.FragmentChatsBinding
import com.example.whoofpark.model.Conversation
import com.example.whoofpark.utilities.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ChatsFragment : Fragment() {


    private lateinit var binding: FragmentChatsBinding

    private lateinit var conversationAdapter: ConversationAdapter
    private val database = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchConversations()
    }

    private fun setupRecyclerView() {
        // Start the adapter
        conversationAdapter = ConversationAdapter(emptyList()) { conversation ->
            val bundle = Bundle().apply {
                putString(Constants.BundleKeys.PARK_ID_KEY, conversation.parkId)
                putString(Constants.BundleKeys.PARK_NAME_KEY, conversation.parkName)
            }
            findNavController().navigate(R.id.action_chatsFragment_to_chatRoomFragment, bundle)
        }

        binding.chatsRVConversations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conversationAdapter
        }
    }

    private fun fetchConversations() {
        val uid = auth.currentUser?.uid ?: return

        // take the conversation from the dog profile
        database.collection(Constants.FIRESTORE.DOG_PROFILES_REF)
            .document(uid)
            .collection(Constants.FIRESTORE.CONVERSATIONS_SUB_REF)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val list = snapshot.toObjects(Conversation::class.java)
                    conversationAdapter.updateList(list)
                }
            }
    }
}


