package com.workforcex.shared_employer

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private var instance: WorkforceXApi? = null
    private lateinit var baseUrl: String

    fun init(baseUrl: String) {
        this.baseUrl = baseUrl
    }

    fun get(): WorkforceXApi {
        if (instance == null) {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            instance = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WorkforceXApi::class.java)
        }
        return instance!!
    }
}