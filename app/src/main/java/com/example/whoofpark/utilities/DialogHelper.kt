package com.example.whoofpark.utilities

import android.view.LayoutInflater
import com.example.whoofpark.databinding.DialogStayOrLeaveBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogHelper {
    fun showStayOrLeaveDialog(
        context: android.content.Context,
        onExtend: () -> Unit,
        onLeave: () -> Unit
    ) {

        val binding = DialogStayOrLeaveBinding.inflate(LayoutInflater.from(context))


        val dialog = MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setCancelable(false)
            .create()


        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        binding.dialogBTNExtend.setOnClickListener {
            dialog.dismiss()
            onExtend()
        }

        binding.dialogBTNLeave.setOnClickListener {
            dialog.dismiss()
            onLeave()
        }

        dialog.show()
    }
}