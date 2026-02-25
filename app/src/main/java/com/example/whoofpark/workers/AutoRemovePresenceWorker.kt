package com.example.whoofpark.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.whoofpark.utilities.Constants
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Runs a few minutes after the park timer expires. If the user has ignored the notification
 * and not opened the app, this worker removes them from the park list (LivePresence).
 * When they eventually open the app, the "time's up" dialog will not show because
 * they are no longer in the presence list.
 */
class AutoRemovePresenceWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    private val database = FirebaseFirestore.getInstance()

    override fun doWork(): Result {
        val userId = inputData.getString(KEY_USER_ID) ?: return Result.failure()

        val latch = CountDownLatch(1)
        var success = false

        database.collection(Constants.FIRESTORE.LIVE_PRESENCE_REF)
            .document(userId)
            .delete()
            .addOnSuccessListener {
                success = true
                latch.countDown()
            }
            .addOnFailureListener { latch.countDown() }

        latch.await(10, TimeUnit.SECONDS)
        return if (success) Result.success() else Result.failure()
    }

    companion object {
        const val KEY_USER_ID = "userId"
    }
}
