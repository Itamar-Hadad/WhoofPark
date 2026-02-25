package com.example.whoofpark

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whoofpark.databinding.ActivityLoginBinding
import com.example.whoofpark.utilities.SignalManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth


private lateinit var binding: ActivityLoginBinding
private val auth = FirebaseAuth.getInstance()

class LoginActivity : AppCompatActivity() {



    private fun signIn(provider: AuthUI.IdpConfig) {// Choose authentication providers
        val providers = arrayListOf(provider)

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTheme(R.style.Theme_WhoofPark)
            .setAlwaysShowSignInMethodScreen(false)
            .build()
        signInLauncher.launch(signInIntent)
    }


    // See: https://developer.android.com/training/basics/intents/result
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (auth.currentUser != null) {
            transactToNextScreen()
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {

        binding.loginBTNLogin.setOnClickListener {
            val email = binding.loginEDTEmail.text.toString().trim()
            val password = binding.loginEDTPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginWithEmail(email, password)
            } else {
                SignalManager.getInstance().toast("Please fill all fields", SignalManager.ToastLength.SHORT)
            }
        }


        binding.loginBTNGoogle.setOnClickListener {
            signIn(AuthUI.IdpConfig.GoogleBuilder().build())
        }

        binding.loginBTNPhone.setOnClickListener {
            signIn(AuthUI.IdpConfig.PhoneBuilder().build())
        }


        binding.loginLBLRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginWithEmail(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                transactToNextScreen()
            }
            .addOnFailureListener { e ->
                SignalManager.getInstance().toast("Error: ${e.message}", SignalManager.ToastLength.LONG)
            }
    }


    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            transactToNextScreen()

        } else {

            SignalManager
                .getInstance()
                .toast(
                    "Error: Failed logging in!",
                    SignalManager.ToastLength.LONG
                )

        }

    }


    private fun transactToNextScreen() {
        startActivity(
            Intent(this, MainActivity::class.java)
        )
        finish()
    }
}