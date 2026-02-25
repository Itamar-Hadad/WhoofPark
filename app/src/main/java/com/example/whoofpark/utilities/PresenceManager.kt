package com.example.whoofpark.utilities

import android.content.Context

import androidx.work.WorkManager

object PresenceManager {
    var isDialogShowing = false

    var isDemoMode = false
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    private val database = com.google.firebase.firestore.FirebaseFirestore.getInstance()



    fun checkPresence(context: Context, onExpired: (userId: String, parkId: String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        database.collection(Constants.FIRESTORE.LIVE_PRESENCE_REF).document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val entryTime = doc.getLong("entryTime") ?: 0L
                    val duration = doc.getLong("durationMinutes") ?: 0L
                    val parkId = doc.getString("parkId") ?: ""

                    val currentTime = System.currentTimeMillis()
                    val expirationTime = entryTime + (duration * Constants.Timer.MILLIS_PER_MINUTE)

                    // בדיקה אם הזמן עבר
                    if (currentTime > expirationTime) {
                        val timeSinceExpiration = currentTime - expirationTime
                        val gracePeriodMillis =
                            Constants.Timer.GRACE_PERIOD_MINUTES * Constants.Timer.MILLIS_PER_MINUTE

                        if (timeSinceExpiration < gracePeriodMillis) {
                            // עברו פחות מ-5 דקות מהתפוגה -> מציגים דיאלוג למשתמש
                            onExpired(uid, parkId)
                        } else {
                            // עברו יותר מ-5 דקות -> מחיקה שקטה מה-Database ללא דיאלוג
                            removePresence(context, uid)
                            android.util.Log.d(
                                "PresenceManager",
                                "Silent removal: Grace period exceeded"
                            )
                        }
                    }
                }
            }
    }
    fun removePresence(context: Context, userId: String) {
        //Delete from FireStore
        database.collection(Constants.FIRESTORE.LIVE_PRESENCE_REF).document(userId).delete()
        //cancel the notifications
        WorkManager
            .getInstance(context)
            .cancelAllWorkByTag("checkout_$userId")
    }

}