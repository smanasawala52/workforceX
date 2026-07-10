package com.workforcex.employer

import android.app.Application
import com.workforcex.shared_employer.RetrofitClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(BuildConfig.BASE_URL)
    }
}