package com.workforcex.worker

import android.app.Application
import com.workforcex.shared.RetrofitClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(BuildConfig.BASE_URL)
    }
}