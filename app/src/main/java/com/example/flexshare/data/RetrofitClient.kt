package com.example.flexshare.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // NOTE: We use "10.0.2.2" instead of "localhost".
    // To an Android Emulator, "localhost" means the phone itself!
    // "10.0.2.2" is a special fallback IP that tells the emulator to look at your actual computer's localhost.
    private const val BASE_URL = "http://192.168.100.128:8080/"

    val apiService: FlexShareApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Automatically parse JSON strings
            .build()
            .create(FlexShareApi::class.java)
    }
}