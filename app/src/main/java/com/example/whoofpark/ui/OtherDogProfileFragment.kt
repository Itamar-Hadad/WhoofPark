package com.example.whoofpark.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.whoofpark.R
import com.example.whoofpark.databinding.FragmentOtherDogProfileBinding
import com.example.whoofpark.utilities.Constants
import com.example.whoofpark.utilities.ImageLoader
import com.google.firebase.firestore.FirebaseFirestore


class OtherDogProfileFragment : Fragment() {

    private lateinit var binding: FragmentOtherDogProfileBinding
    private var targetUserId: String? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtherDogProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        targetUserId = arguments?.getString(Constants.BundleKeys.USER_ID_KEY)
        binding.otherDogBTNBack.setOnClickListener {
            findNavController().popBackStack()
        }
        loadOtherDogData()
    }

    private fun loadOtherDogData() {
        val uid = targetUserId ?: return

        FirebaseFirestore.getInstance()
            .collection(Constants.FIRESTORE.DOG_PROFILES_REF)
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    //update the UI
                    binding.otherDogLBLName.text = document.getString("name")
                    binding.otherDogLBLBreed.text = getString(R.string.breed_label, document.getString("breed") ?: "")
                    binding.otherDogLBLGender.text = getString(R.string.gender_label, document.getString("gender") ?: "")
                    val age = document.getLong("age")?.toInt() ?: 0
                    binding.otherDogLBLAge.text = getString(R.string.age_label, age)
                    binding.otherDogLBLSize.text = getString(R.string.size_label, document.getString("size") ?: "")
                    binding.otherDogLBLHobbies.text = getString(R.string.hobbies_label, document.getString("hobbies") ?: "")


                    val imageUrl = document.getString("photoUrl") ?: ""
                    ImageLoader
                        .getInstance()
                        .loadImage(
                            imageUrl,
                            binding.otherDogIMGProfile)
                }
            }
    }
}