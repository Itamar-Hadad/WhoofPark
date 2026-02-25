package com.example.whoofpark.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.whoofpark.MainActivity
import com.example.whoofpark.databinding.DialogCheckInBinding
import com.example.whoofpark.databinding.FragmentParkDetailBinding
import com.example.whoofpark.model.DogPresence
import com.example.whoofpark.utilities.Constants
import com.example.whoofpark.utilities.SignalManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.whoofpark.R
import com.example.whoofpark.interfaces.DogCallback
import com.example.whoofpark.viewmodels.ParkViewModel
import com.example.whoofpark.adapters.DogPresenceAdapter
import com.example.whoofpark.interfaces.ParkPresenceCallback
import com.example.whoofpark.utilities.ImageLoader
import com.example.whoofpark.utilities.PresenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ParkDetailFragment : Fragment(), ParkPresenceCallback {

    private lateinit var binding: FragmentParkDetailBinding
    private var parkId: String? = null
    private var parkName: String? = null
    private var parkHours: String? = null
    private var parkAddress: String? = null
    private val viewModel: ParkViewModel by viewModels()
    private var dogAdapter: DogPresenceAdapter = DogPresenceAdapter(mutableListOf())
    private val auth = FirebaseAuth.getInstance()
    val database = FirebaseFirestore.getInstance()



    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var parkLat: Double = 0.0
    private var parkLon: Double = 0.0



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParkDetailBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        parkId?.let { viewModel.startListeningToPark(it) }

        //listen to changes in the dog list in the ViewModel
        //when a dog enters or leaves the park, the ViewModel will update this list automatic
        viewModel.dogsInPark.observe(viewLifecycleOwner) { dogs ->
            dogAdapter.updateDogs(dogs)
        }



        checkNotificationPermission()
        val myActivity = activity as? MainActivity
        myActivity?.parkPresenceCallback = this


        val shouldAutoOpen = arguments?.getBoolean("AUTO_OPEN_CHECKIN", false) ?: false

        if (shouldAutoOpen) {
        // השהיה של 300 מילישניות כדי שה-UI יספיק להתרנדר לפני הדיאלוג
            view.postDelayed({
                showCheckInDialog()
            }, 300)
            arguments?.remove("AUTO_OPEN_CHECKIN")
        }

        fusedLocationClient = getFusedLocationProviderClient(requireActivity())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        val myActivity = activity as? MainActivity
        myActivity?.parkPresenceCallback = this
    }


    override fun onExtendStay(parkId: String) {
        showCheckInDialog()
    }


    private fun initViews() {
        parkId = arguments?.getString(Constants.BundleKeys.PARK_ID_KEY)
        parkName = arguments?.getString(Constants.BundleKeys.PARK_NAME_KEY)
        parkHours = arguments?.getString(Constants.BundleKeys.PARK_HOURS_KEY) ?: "24/7"
        parkAddress = arguments?.getString(Constants.BundleKeys.PARK_ADDRESS_KEY) ?: "No address available"


        binding.parkDetailLBLNamePark.text = parkName
        binding.parkDetailLBLOpeningHours.text = "Opening Hours: $parkHours"
        binding.parkDetailLBLAddress.text = parkAddress

        parkLat = arguments?.getDouble(Constants.BundleKeys.PARK_LAT_KEY) ?: 0.0
        parkLon = arguments?.getDouble(Constants.BundleKeys.PARK_LON_KEY) ?: 0.0



        binding.parkDetailBTMBack.setOnClickListener {
            findNavController().popBackStack()
        }

        dogAdapter.dogCallback = object : DogCallback {
            override fun onDogClicked(dog: DogPresence, position: Int) {
                navigateToOtherDogProfile(dog)
            }
        }

        binding.parkDetailRVList.adapter = dogAdapter
        binding.parkDetailRVList.layoutManager = LinearLayoutManager(requireContext())

        loadParkImage()
        setupActionButtons()
    }

    private fun loadParkImage() {
        // 1. בניית ה-URL הישיר לתמונה ב-Firebase Storage
        // הערה: החלפתי את ה-URL לכתובת ה-Bucket שמופיעה אצלך בצילום המסך
        val bucketName = "whoofpark-5d456.firebasestorage.app"
        val imageUrl = "https://firebasestorage.googleapis.com/v0/b/$bucketName/o/park_images%2F${parkId}.jpeg?alt=media"

        // 2. שימוש ב-ImageLoader שלך לטעינה לתוך ה-ImageView
        ImageLoader.getInstance().loadImage(
            source = imageUrl,
            imageView = binding.parkDetailIMGParkImage, // ה-ID מה-XML ששלחת
            placeHolder = R.drawable.unavailable_photo  // תמונת ברירת המחדל
        )
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            SignalManager
                .getInstance()
                .toast(
                    "Notifications enabled! 🐾",
                    SignalManager.ToastLength.SHORT
                )
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            SignalManager
                .getInstance()
                .toast("Location permission granted!", SignalManager.ToastLength.SHORT)
        } else {
            SignalManager
                .getInstance()
                .toast("Location is required for check-in", SignalManager.ToastLength.SHORT)
        }
    }


    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }


    private fun setupActionButtons() {
        binding.parkDetailBTNImHere.setOnClickListener {
            if (androidx.core.app.ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                // אם אין הרשאה - מבקשים אותה
                requestLocationPermissionLauncher
                    .launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                return@setOnClickListener
            }

            // 2. משיכת המיקום הנוכחי של המשתמש מה-GPS
            fusedLocationClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
                if (location != null) {
                    // הצלחנו להשיג מיקום! עכשיו בודקים מרחק מול הגינה
                    checkDistanceAndPerformCheckIn(
                        userLat = location.latitude,
                        userLon = location.longitude,
                        parkLat = parkLat,
                        parkLon = parkLon
                    )
                } else {
                    // מקרה קצה: אם ה-GPS כבוי או לא מחזיר מיקום
                    // נבדוק אם ה-Demo Mode דלוק כדי לא לתקוע אותך במצגת
                    val isDemo = requireActivity()
                        .getSharedPreferences("WhoofPrefs", Context.MODE_PRIVATE)
                        .getBoolean("is_demo_mode", false)

                    if (isDemo) {
                        showCheckInDialog()
                    } else {
                        SignalManager.getInstance().toast(
                            "Please turn on GPS to check in",
                            SignalManager.ToastLength.SHORT
                        )
                    }
                }
            }
        }

        binding.parkDetailBTNChat.setOnClickListener {
            val bucketName = "whoofpark-5d456.firebasestorage.app"
            val imageUrl = "https://firebasestorage.googleapis.com/v0/b/$bucketName/o/park_images%2F${parkId}.jpeg?alt=media"
            val bundle = Bundle().apply {
                putString(Constants.BundleKeys.PARK_ID_KEY, parkId)
                putString(Constants.BundleKeys.PARK_NAME_KEY, parkName)
                putString(Constants.BundleKeys.PARK_IMAGE_URL_KEY, imageUrl)
        }
            findNavController().navigate(R.id.action_parkDetailFragment_to_chatRoomFragment, bundle)
        }

    }


    private fun navigateToOtherDogProfile(dog: DogPresence) {
        val bundle = Bundle().apply {
            putString(Constants.BundleKeys.USER_ID_KEY, dog.userId)
        }
        findNavController()
            .navigate(R.id.action_parkDetailFragment_to_otherDogProfileFragment, bundle)
    }


    private fun showCheckInDialog() {
        val dialogBinding = DialogCheckInBinding.inflate(layoutInflater)
        // start glideclock
        dialogBinding.dialogNPHours.apply {
            minValue = 0
            maxValue = 5
            value = 0
        }

        dialogBinding.dialogNPMinutes.apply {
            minValue = 0
            maxValue = 59
            value = 30 // Default value
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogBinding.dialogBTNCancel.setOnClickListener {
            PresenceManager.isDialogShowing = false
            dialog.dismiss()
        }

        dialogBinding.dialogBTNConfirm.setOnClickListener {
            val hours = dialogBinding.dialogNPHours.value
            val minutes = dialogBinding.dialogNPMinutes.value
            val totalMinutes = (hours * 60) + minutes

            if (totalMinutes > 0) {
                performCheckIn(totalMinutes)
                dialog.dismiss()
            } else {
                SignalManager
                    .getInstance()
                    .toast(
                        "Please select a valid duration",
                        SignalManager.ToastLength.SHORT
                    )
            }
        }
        dialog.show()
    }


    private fun performCheckIn(duration: Int) {
        val currentUserId = auth.currentUser?.uid ?: return

        val parkData = hashMapOf(
            "name" to (parkName ?: "Dog Park"),
            "address" to (parkAddress ?: "No address"),
            "hours" to (parkHours ?: "24/7")
        )


        parkId?.let { id ->
            database.collection(Constants.FIRESTORE.PARKS_REF).document(id).set(parkData)
        }

        database.collection(Constants.FIRESTORE.DOG_PROFILES_REF)
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                PresenceManager.isDialogShowing = false
                if (document != null && document.exists()) {
                    val presence = DogPresence(
                        userId = currentUserId,
                        dogName = document.getString("name") ?: "Dog",
                        dogImageUrl = document.getString("photoUrl") ?: "",
                        parkId = parkId ?: "",
                        entryTime = System.currentTimeMillis(),
                        durationMinutes = duration
                    )

                    //save in the database
                    database
                        .collection(Constants.FIRESTORE.LIVE_PRESENCE_REF)
                        .document(currentUserId)
                        .set(presence)
                        .addOnSuccessListener {
                            PresenceManager.isDialogShowing = false
                            SignalManager
                                .getInstance()
                                .toast(
                                    "Enjoy the park! 🐾",
                                    SignalManager.ToastLength.SHORT
                                )

                            WorkManager
                                .getInstance(requireContext())
                                .cancelAllWorkByTag("checkout_$currentUserId")


                            val workRequest =
                                OneTimeWorkRequestBuilder<com.example.whoofpark.workers.CheckOutWorker>()

                                    .setInitialDelay(
                                        duration.toLong(),
                                        java.util.concurrent.TimeUnit.MINUTES
                                    )
                                    .setInputData(androidx.work.workDataOf("dogName" to presence.dogName))
                                    .addTag("checkout_$currentUserId")
                                    .build()

                            WorkManager.getInstance(requireContext()).enqueue(workRequest)


                            // If the user doesn't extend their stay, this worker removes their presence record from the database
                            val cleanupDelay = (duration + 5).toLong()

                            val autoRemoveRequest = OneTimeWorkRequestBuilder<com.example.whoofpark.workers.AutoRemovePresenceWorker>()
                                .setInitialDelay(cleanupDelay, java.util.concurrent.TimeUnit.MINUTES)
                                .setInputData(androidx.work.workDataOf(
                                    com.example.whoofpark.workers.AutoRemovePresenceWorker.KEY_USER_ID to currentUserId
                                ))
                                .addTag("cleanup_$currentUserId")
                                .build()

                            WorkManager.getInstance(requireContext()).enqueue(autoRemoveRequest)
                        }

                        .addOnFailureListener { e ->
                            SignalManager
                                .getInstance()
                                .toast(
                                    "Check-in failed: ${e.message}",
                                    SignalManager.ToastLength.SHORT
                                )
                        }
                }
            }
    }

    private fun checkDistanceAndPerformCheckIn(userLat: Double, userLon: Double, parkLat: Double, parkLon: Double) {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(userLat, userLon, parkLat, parkLon, results)
        val distanceInMeters = results[0]

        if (distanceInMeters <= Constants.LOCATION.CHECK_IN_DISTANCE_THRESHOLD || PresenceManager.isDemoMode) {
            showCheckInDialog()
        } else {
            SignalManager.getInstance().toast(
                "You're a bit too far! 🐾\nPlease get closer to the park",
                SignalManager.ToastLength.LONG
            )
        }
    }
}
