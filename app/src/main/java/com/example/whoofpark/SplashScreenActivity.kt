package com.example.whoofpark

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.whoofpark.databinding.ActivitySplashScreenBinding
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        startAnimation(binding.splashLOTTIELottie)
    }

    private fun navigateFromSplashScreen() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val intent = if (currentUser != null) {
            //the user is logged in -> send them to the main activity
            Intent(this, MainActivity::class.java)
        } else {
            //the user is not logged in -> send them to the login activity
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
    }

    private fun startAnimation(lottieAnimationView: LottieAnimationView) {
        lottieAnimationView.resumeAnimation()

        lottieAnimationView.addAnimatorListener(
            object: Animator.AnimatorListener{
                override fun onAnimationCancel(p0: Animator) {
                    // Remove all data created and clean memory.
                }

                override fun onAnimationEnd(p0: Animator) {
                    navigateFromSplashScreen()
                }

                override fun onAnimationRepeat(p0: Animator) {
                    // check if data received
                    // if true - stop repeating
                }

                override fun onAnimationStart(p0: Animator) {
                    // go fetch data and apply callback when done.
                }

            }
        )


    }
}