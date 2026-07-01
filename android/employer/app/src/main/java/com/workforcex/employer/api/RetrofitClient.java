package com.workforcex.employer.api;

import com.workforcex.employer.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton Retrofit client.
 * BASE_URL comes from BuildConfig (set per build type in app/build.gradle).
 * - Debug:   http://10.0.2.2:8080/ (emulator localhost)
 * - Release: your Railway URL
 */
public class RetrofitClient {

    private static WorkforceXApi instance;

    public static WorkforceXApi get() {
        if (instance == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // show full request/response in Logcat

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(WorkforceXApi.class);
        }
        return instance;
    }
}
