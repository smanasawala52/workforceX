package com.workforcex.worker

import android.app.Application
import android.util.Log
import com.workforcex.shared.RetrofitClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("MyApplication", "onCreate: Start")
        try {
            RetrofitClient.init(BuildConfig.BASE_URL)
        } catch (e: Exception) {
            Log.e("MyApplication", "Error initializing Retrofit", e)
        }
        Log.d("MyApplication", "onCreate: End")
    }
}