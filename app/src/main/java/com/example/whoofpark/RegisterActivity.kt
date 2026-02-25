package com.example.whoofpark

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whoofpark.databinding.ActivityRegisterBinding
import com.example.whoofpark.utilities.SignalManager
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerBTNSignup.setOnClickListener {
            val email = binding.registerEDTEmail.text.toString().trim()
            val pass = binding.registerEDTPassword.text.toString().trim()

            if (validate(email, pass)) {
                registerUser(email, pass)
            }
        }
    }

    private fun registerUser(email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                SignalManager
                    .getInstance()
                    .toast("Account created! Welcome 🐾", SignalManager.ToastLength.SHORT)

                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                SignalManager.getInstance().toast("Registration failed: ${e.message}", SignalManager.ToastLength.LONG)
            }
    }

    private fun validate(email: String, pass: String): Boolean {
        if (email.isEmpty()) {
            binding.registerEDTEmailLayout.error = "Email required"
            return false
        }
        if (pass.length < 6) {
            binding.registerEDTPasswordLayout.error = "Password must be at least 6 characters"
            return false
        }
        return true
    }
}