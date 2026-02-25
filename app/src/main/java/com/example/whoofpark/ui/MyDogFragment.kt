package com.example.whoofpark.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.whoofpark.R
import com.example.whoofpark.databinding.FragmentMyDogBinding
import com.example.whoofpark.model.DogProfile
import com.example.whoofpark.utilities.Constants
import com.example.whoofpark.utilities.SignalManager
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.whoofpark.utilities.ImageLoader
import com.example.whoofpark.viewmodels.DogViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage


class MyDogFragment : Fragment() {

    private lateinit var binding: FragmentMyDogBinding
    private var isEditing = false
    private var selectedGender: String = ""
    private var selectedPhotoUri: String? = null

    private val viewModel: DogViewModel by viewModels()

    private val database = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val storageRef = Firebase.storage.reference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyDogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        loadDataFromCloud()
        //we need to see the data in the view model once
        viewModel.profileToDisplay.observe(viewLifecycleOwner) { dogData ->
            renderViewMode(dogData)
            if (isEditing) {
                fillInputsFromProfile(dogData)
            }
        }
        setEditingMode()
    }


    private fun initViews() {
        setupEditButtons()
        setupGenderButtons()
        setupPhotoButtons()
    }

    private fun setupEditButtons() {
        binding.myDogBTNEdit.setOnClickListener {
            isEditing = Constants.Mode.EDIT_MODE_ON
            setEditingMode()

            viewModel.profileToDisplay.value?.let { currentProfile ->
                fillInputsFromProfile(currentProfile)
            }

        }
        binding.myDogBTNCancel.setOnClickListener {
            isEditing = Constants.Mode.EDIT_MODE_OFF

            // we fetch the latest data from the ViewModel
            // to make sure the UI reflects what is actually saved in the repository
            viewModel.profileToDisplay.value?.let { lastSavedProfile ->
                renderViewMode(lastSavedProfile)
            }

            setEditingMode()
        }
        binding.myDogBTNSave.setOnClickListener {
            isEditing = Constants.Mode.EDIT_MODE_OFF
            saveFromInputs()
            setEditingMode()
        }

    }

    private fun setupGenderButtons() {
        binding.myDogBTNMale.setOnClickListener { selectGender(Constants.GENDER.MALE) }
        binding.myDogBTNFemale.setOnClickListener { selectGender(Constants.GENDER.FEMALE) }
    }



    private fun selectGender(gender: String) {
        selectedGender = gender
        updateGenderButtonsUI()
    }

    private fun updateGenderButtonsUI() {
        val selectedBg = requireContext().getColor(R.color.gender_selected)
        val unselectedBg = requireContext().getColor(R.color.gender_unselected)

        val maleSelected = selectedGender == Constants.GENDER.MALE
        val femaleSelected = selectedGender == Constants.GENDER.FEMALE

        binding.myDogBTNMale.setBackgroundColor(if (maleSelected) selectedBg else unselectedBg)
        binding.myDogBTNFemale.setBackgroundColor(if (femaleSelected) selectedBg else unselectedBg)
    }

    private fun setEditingMode() {
        val editVisibility = if (isEditing) View.VISIBLE else View.GONE
        val labelVisibility = if (isEditing) View.GONE else View.VISIBLE

        binding.apply {
            myDogBTNEdit.visibility = labelVisibility
            myDogLBLName.visibility = labelVisibility
            myDogLBLBreed.visibility = labelVisibility
            myDogLBLGender.visibility = labelVisibility
            myDogLBLAge.visibility = labelVisibility
            myDogLBLSize.visibility = labelVisibility
            myDogLBLHobbies.visibility = labelVisibility

            myDogEDTXTEditName.visibility = editVisibility
            myDogLBLEDBreed.visibility = editVisibility
            genderButtonsContainer.visibility = editVisibility
            myDogLBLEDAge.visibility = editVisibility
            myDogLBLEDSize.visibility = editVisibility
            myDogLBLEDHobbies.visibility = editVisibility
            myDogIMGBTNEdit.visibility = editVisibility
            myDogLLContainer.visibility = editVisibility
        }
    }

    private fun fillInputsFromProfile(profile: DogProfile) {
        binding.myDogEDTXTEditName.setText(profile.name)
        binding.myDogLBLEDBreed.setText(profile.breed)
        binding.myDogLBLEDAge.setText(if (profile.age == 0) "" else profile.age.toString())
        binding.myDogLBLEDSize.setText(profile.size)
        binding.myDogLBLEDHobbies.setText(profile.hobbies)

        selectedGender = profile.gender
        updateGenderButtonsUI()

        selectedPhotoUri = null

        val imageToLoad = profile.photoUrl ?: profile.photoLocalUri
        imageToLoad?.let {
            ImageLoader.getInstance().loadImage(it, binding.myDogIMGProfile)
        }

    }

    private fun renderViewMode(profile: DogProfile) {
        binding.myDogLBLName.text = profile.name.ifBlank { "Dog Name" }
        binding.myDogLBLBreed.text = getString(R.string.breed_label, profile.breed)
        binding.myDogLBLGender.text = getString(R.string.gender_label, profile.gender)
        binding.myDogLBLAge.text = getString(R.string.age_label, profile.age)
        binding.myDogLBLSize.text = getString(R.string.size_label, profile.size)
        binding.myDogLBLHobbies.text = getString(R.string.hobbies_label, profile.hobbies)

        val imageToLoad = profile.photoUrl ?: profile.photoLocalUri
        imageToLoad?.let {
            ImageLoader
                .getInstance()
                .loadImage(
                    it,
                    binding.myDogIMGProfile)
        }
    }

    private fun saveFromInputs() {
        val name = binding.myDogEDTXTEditName.text.toString().trim()
        if (name.isEmpty()) {
            SignalManager.getInstance().toast(
                "Error: Name required!",
                SignalManager.ToastLength.SHORT)
            return
        }

        val userId = auth.currentUser?.uid ?: return


        val updatedProfile = DogProfile(
            name = name,
            breed = binding.myDogLBLEDBreed.text.toString().trim(),
            gender = selectedGender,
            age = binding.myDogLBLEDAge.text.toString().trim().toIntOrNull() ?: 0,
            size = binding.myDogLBLEDSize.text.toString().trim(),
            hobbies = binding.myDogLBLEDHobbies.text.toString().trim(),
            photoLocalUri = selectedPhotoUri,
            //if we have existing link photo, we keep it
            photoUrl = viewModel.profileToDisplay.value?.photoUrl
        )

        //checking if there is a new photo to upload

        if (selectedPhotoUri != null && !selectedPhotoUri!!.startsWith("http")) {

            // if there is a new photo we upload it to Storage
            uploadImageAndThenSave(userId, updatedProfile)
        } else {

            //there is no a new photo, so we save the text
            saveToFirestore(userId, updatedProfile)
        }
    }


    private fun uploadImageAndThenSave(userId: String, profile: DogProfile) {
        if (profile.photoLocalUri == null) return

        val file_uri = android.net.Uri.parse(profile.photoLocalUri)


        val imageRef = storageRef.child("images/$userId.jpg")


        var uploadTask = imageRef.putFile(file_uri)


        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            //when the image is uploaded successfully, ask for the link
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //get the final url
                val downloadUri = task.result

                //update the profile with the link and save in the database
                val profileWithUrl = profile.copy(photoUrl = downloadUri.toString())
                saveToFirestore(userId, profileWithUrl)

                ImageLoader
                    .getInstance()
                    .loadImage(
                    downloadUri.toString(),
                    binding.myDogIMGProfile)

            } else {

                SignalManager
                    .getInstance()
                    .toast(
                        "Upload failed",
                        SignalManager.ToastLength.SHORT)
            }
        }
    }

    //save to firestore database
    //we create a collection called "DogProfiles" and use the user's ID ad the document ID
    private fun saveToFirestore(userId: String, profile: DogProfile) {
        database.collection(Constants.FIRESTORE.DOG_PROFILES_REF)
            .document(userId)
            .set(profile)
            .addOnSuccessListener {
                //save in the viewmodel after we save in the database and for the screen will update
                viewModel.saveChanges(profile)
                SignalManager.getInstance().toast("Profile synced with cloud!", SignalManager.ToastLength.SHORT)
            }
            .addOnFailureListener { e ->
                SignalManager.getInstance().toast("Cloud sync failed: ${e.message}", SignalManager.ToastLength.SHORT)
            }
    }




    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
           //get the url of the crop image
            val uriContent = result.uriContent
            if (uriContent != null) {
                selectedPhotoUri = uriContent.toString()
                binding.myDogIMGProfile.setImageURI(uriContent)
            }
        } else {
            // when a error happen
            val exception = result.error
            exception?.printStackTrace()
        }
    }

    private fun loadDataFromCloud() {
        val userId = auth.currentUser?.uid ?: return

        database.collection(Constants.FIRESTORE.DOG_PROFILES_REF)
            .document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Converting the data from the cloud back into a DogProfile object
                    val profile = document.toObject(DogProfile::class.java)
                    profile?.let {
                        // Updates the ViewModel and the Observer will display it
                        viewModel.saveChanges(it)
                    }
                }
            }

    }
    private fun setupPhotoButtons() {
        binding.myDogIMGBTNEdit.setOnClickListener {
            cropImage.launch(
                CropImageContractOptions(
                    uri = null, // null because we want the user will choose between camera or library
                    cropImageOptions = CropImageOptions(
                        // define crop as square
                        fixAspectRatio = true,
                        aspectRatioX = 1,
                        aspectRatioY = 1,
                        // guidelines when take of pics
                        guidelines = CropImageView.Guidelines.ON,
                        // define camera or library
                        imageSourceIncludeGallery = true,
                        imageSourceIncludeCamera = true
                    )
                )
            )
        }
    }




}