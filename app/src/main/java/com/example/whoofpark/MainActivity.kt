package com.example.whoofpark

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.whoofpark.databinding.ActivityMainBinding
import com.example.whoofpark.interfaces.ParkPresenceCallback
import com.example.whoofpark.utilities.Constants
import com.example.whoofpark.utilities.DialogHelper
import com.example.whoofpark.utilities.PresenceManager
import com.example.whoofpark.utilities.SignalManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var parkPresenceCallback: ParkPresenceCallback? = null

    private var globalTimerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initViews()
        startGlobalTimer()
        
        // Dismiss notification when app opens
        dismissTimerNotification()
        
        // Check if timer expired when activity is created (e.g., from notification click)
        if (intent.getBooleanExtra("TIMER_EXPIRED", false)) {
            checkPresenceAndShowDialog()
        }

        showWelcomeToast()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Check if timer expired when activity receives new intent (e.g., app already running)
        if (intent.getBooleanExtra("TIMER_EXPIRED", false)) {
            checkPresenceAndShowDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        // Dismiss notification when app comes to foreground
        dismissTimerNotification()
        // Check immediately when app comes to foreground
        checkPresenceAndShowDialog()
    }

    override fun onResume() {
        super.onResume()
        // Dismiss notification when app resumes
        dismissTimerNotification()
        // Check immediately when app resumes (including when notification is clicked)
        checkPresenceAndShowDialog()
    }
    
    private fun dismissTimerNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Dismiss the "Time's Up" notification (ID = 1) when app opens
        notificationManager.cancel(1)
    }

    private fun checkPresenceAndShowDialog() {
        if (!PresenceManager.isDialogShowing) {
            PresenceManager.checkPresence(this) { userId, parkId ->
                showGlobalStayOrLeaveDialog(userId, parkId)
            }
        }
    }
    private fun initViews() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        //if we move to park details fragment we hide the navigation bar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.parkDetailFragment) {
                binding.bottomNav.visibility = android.view.View.GONE
            } else {
                binding.bottomNav.visibility = android.view.View.VISIBLE
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, 0)
            insets
        }
    }


    fun showGlobalStayOrLeaveDialog(userId: String, parkId: String) {
        if (PresenceManager.isDialogShowing) return

        PresenceManager.isDialogShowing = true
        PresenceManager.removePresence(this, userId)

        DialogHelper.showStayOrLeaveDialog(
            context = this,
            onExtend = {
                try {
                    // Always navigate to ensure fragment is properly initialized
                    // Navigate to mapFragment first, then to ParkDetailFragment
                    navigateToParkDetailForExtend(parkId)
                } catch (e: Exception) {
                    // Reset dialog flag on error
                    PresenceManager.isDialogShowing = false
                    e.printStackTrace()
                }
            },
            onLeave = {
                PresenceManager.isDialogShowing = false
                SignalManager.getInstance().toast("Goodbye! 🐾", SignalManager.ToastLength.SHORT)
            }
        )
    }


    private fun navigateToParkDetailForExtend(parkId: String) {
        // Fetch park details from Firestore first, then navigate
        val database = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        database.collection(Constants.FIRESTORE.PARKS_REF)
            .document(parkId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val parkName = document.getString("name") ?: "Dog Park"
                    val parkAddress = document.getString("address") ?: "No address available"
                    val parkHours = document.getString("hours") ?: "24/7"

                    android.util.Log.d("WhoofCheck", "Found Park: $parkName")

                    // Navigate to mapFragment first, then to ParkDetailFragment
                    // This ensures the navigation chain is correct and fragment is properly initialized
                    val bundle = Bundle().apply {
                        // Also include bundle keys for fragment compatibility
                        putString(Constants.BundleKeys.PARK_ID_KEY, parkId)
                        putString(Constants.BundleKeys.PARK_NAME_KEY, parkName)
                        putString(Constants.BundleKeys.PARK_HOURS_KEY, parkHours)
                        putString(Constants.BundleKeys.PARK_ADDRESS_KEY, parkAddress)
                        putBoolean("AUTO_OPEN_CHECKIN", true)
                    }

                    navigateWithBundle(bundle)
                } else {
                    android.util.Log.e("WhoofCheck", "Document with ID $parkId NOT FOUND!")
                 }
                

            }
            .addOnFailureListener { e ->
                // If fetch fails, use default values
                android.util.Log.e("MainActivity", "Failed to fetch park details: ${e.message}")
                val bundle = Bundle().apply {
                    putString(Constants.BundleKeys.PARK_ID_KEY, parkId)
                    putString(Constants.BundleKeys.PARK_NAME_KEY, "Dog Park")
                    putString(Constants.BundleKeys.PARK_HOURS_KEY, "24/7")
                    putString(Constants.BundleKeys.PARK_ADDRESS_KEY, "No address available")
                    putBoolean("AUTO_OPEN_CHECKIN", true)
                }
                navigateWithBundle(bundle)
            }
    }
    
    private fun navigateWithBundle(bundle: Bundle) {
        try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as? NavHostFragment
            val navController = navHostFragment?.navController
            
            if (navController != null) {
                runOnUiThread {
                    try {
                        // First navigate to mapFragment if not already there
                        val currentDestination = navController.currentDestination?.id
                        if (currentDestination != R.id.mapFragment) {
                            // Navigate to mapFragment first, then navigate to ParkDetailFragment after a short delay
                            navController.navigate(R.id.mapFragment)
                            // Wait a bit for mapFragment to be ready, then navigate to ParkDetailFragment
                            findViewById<android.view.View>(R.id.nav_host)?.postDelayed({
                                try {
                                    navController.navigate(R.id.action_mapFragment_to_parkDetailFragment, bundle)
                                    android.util.Log.d("MainActivity", "Navigation to parkDetailFragment initiated")
                                } catch (e: Exception) {
                                    android.util.Log.e("MainActivity", "Navigation to ParkDetailFragment failed: ${e.message}", e)
                                    PresenceManager.isDialogShowing = false
                                }
                            }, 100) // Small delay to ensure mapFragment is ready
                        } else {
                            // Already on mapFragment, navigate directly
                            navController.navigate(R.id.action_mapFragment_to_parkDetailFragment, bundle)
                            android.util.Log.d("MainActivity", "Navigation to parkDetailFragment initiated")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Navigation failed: ${e.message}", e)
                        PresenceManager.isDialogShowing = false
                    }
                }
            } else {
                android.util.Log.e("MainActivity", "NavController is null")
                PresenceManager.isDialogShowing = false
            }
        } catch (e: Exception) {
            // Reset dialog flag on navigation error
            android.util.Log.e("MainActivity", "Navigation failed: ${e.message}", e)
            PresenceManager.isDialogShowing = false
            e.printStackTrace()
        }
    }

    private fun startGlobalTimer() {
        globalTimerJob?.cancel() // ביטול טיימר קודם אם היה
        globalTimerJob = lifecycleScope.launch {
            while (true) {
                // בדיקה: אם אין דיאלוג מוצג כרגע, תבדוק נוכחות ב-Firebase
                if (!PresenceManager.isDialogShowing) {
                    PresenceManager.checkPresence(this@MainActivity) { userId, parkId ->
                        showGlobalStayOrLeaveDialog(userId, parkId)
                    }
                }
                // המתנה של 5 שניות בין בדיקה לבדיקה (במקום דקה) כדי שהדיאלוג יופיע מיד כשהזמן נגמר
                delay(Constants.Timer.PRESENCE_CHECK_INTERVAL)
            }
        }
    }

    private fun showWelcomeToast() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val name = user.displayName ?: "Friend"

            SignalManager.getInstance().toast(
                "Hello $name!",
                SignalManager.ToastLength.LONG
            )
        }
    }


}