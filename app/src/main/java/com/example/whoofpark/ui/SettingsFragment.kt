package com.example.whoofpark.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.example.whoofpark.LoginActivity
import com.example.whoofpark.databinding.DialogLogoutBinding
import com.example.whoofpark.databinding.FragmentSettingsBinding
import com.example.whoofpark.utilities.PresenceManager
import com.example.whoofpark.utilities.SignalManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {

        binding.settingsLBLContact.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@whoofpark.com")
                putExtra(Intent.EXTRA_SUBJECT, "Feedback for WhoofPark")
            }
            startActivity(Intent.createChooser(intent, "Send Email"))
        }


        binding.settingsLBLShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Join me and my dog on WhoofPark! The best app for dog park lovers. 🐾")
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        binding.settingsBTNLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.settingsSWITCHDemoMode.isChecked = PresenceManager.isDemoMode
        binding.settingsSWITCHDemoMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != PresenceManager.isDemoMode) {

            PresenceManager.isDemoMode = isChecked

            val status = if (isChecked) "ON" else "OFF"
            SignalManager
                .getInstance()
                .toast(
                    "Demo Mode: $status",
                    SignalManager.ToastLength.SHORT)
            }
        }
    }

    private fun showLogoutDialog() {
        val dialogBinding = DialogLogoutBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.dialogBTNCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.dialogBTNConfirm.setOnClickListener {
            auth.signOut()


            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            dialog.dismiss()
            requireActivity().finish()
        }

        dialog.show()
    }
}


