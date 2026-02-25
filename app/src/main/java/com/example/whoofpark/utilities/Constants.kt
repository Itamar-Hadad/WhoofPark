package com.example.whoofpark.utilities

class Constants {

    object LOCATION {
        const val CHECK_IN_DISTANCE_THRESHOLD = 100.0f
    }

    object GENDER{
        const val MALE = "Male"
        const val FEMALE = "Female"
    }

    object Mode{
        const val EDIT_MODE_ON: Boolean = true
        const val EDIT_MODE_OFF: Boolean = false
    }

    object FIRESTORE {
        const val DOG_PROFILES_REF = "DogProfiles"
        const val PARKS_REF = "Parks"
        const val LIVE_PRESENCE_REF = "LivePresence"

        const val MESSAGES_SUB_REF = "Messages"
        const val CONVERSATIONS_SUB_REF = "my_conversations"
    }


    object BundleKeys {
        const val PARK_ID_KEY: String = "PARK_ID_KEY"
        const val PARK_NAME_KEY: String = "PARK_NAME_KEY"
        const val PARK_HOURS_KEY: String = "PARK_HOURS_KEY"
        const val PARK_IMAGE_URL_KEY: String = "PARK_IMAGE_URL_KEY"

        const val PARK_ADDRESS_KEY: String = "PARK_ADDRESS_KEY"
        const val USER_ID_KEY: String = "USER_ID_KEY"

        const val PARK_LAT_KEY = "PARK_LAT_KEY"
        const val PARK_LON_KEY = "PARK_LON_KEY"

    }

    object Timer{
        const val PRESENCE_CHECK_INTERVAL: Long = 5000 // 5 seconds - interval for checking if timer expired

        const val MILLIS_PER_MINUTE: Long = 60000 // Milliseconds in one minute - used for time conversions

        /** Minutes after timer expires before auto-removing user from park list if they ignore the notification */
        const val GRACE_PERIOD_MINUTES: Long = 5
    }


}