plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.whoofpark"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.whoofpark"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    android {
        buildFeatures {
            viewBinding = true
        }
    }


}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Navigation
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    //Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)

    // location:
    implementation(libs.location)
    implementation(libs.google.maps)

    //firebase:
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    //Auth UI:
    implementation(libs.firebase.ui.auth)

    //Storage:
    implementation(libs.firebase.storage)

    //Firestore:
    implementation(libs.firebase.firestore)

    //Image Cropper
    implementation(libs.android.image.cropper)

    //Glide:
    implementation(libs.glide)

    //gson:
    implementation(libs.gson)

    //okhttp3
    implementation(libs.okhttp3)

    //WorkManager for notification
    implementation(libs.androidx.work.runtime.ktx)

    //Lottie
    implementation(libs.lottie)

}