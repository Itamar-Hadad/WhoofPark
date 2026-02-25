package com.example.whoofpark

import android.app.Application
import com.example.whoofpark.utilities.ImageLoader
import com.example.whoofpark.utilities.SignalManager

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        SignalManager.init(this)
        ImageLoader.init(this)
    }
}
