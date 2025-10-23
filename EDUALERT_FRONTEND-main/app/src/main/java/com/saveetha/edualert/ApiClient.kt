package com.saveetha.edualert

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    const val BASE_URL = "http://14.139.187.229:8081/PDD-2025(9thmonth)/edualert/"

    private val interceptor : HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }

    // 🔹 Step 1: Create OkHttpClient with custom timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(interceptor)
//        .connectTimeout(30, TimeUnit.SECONDS)  // time to establish connection
//        .readTimeout(30, TimeUnit.SECONDS)     // time to wait for server response
//        .writeTimeout(30, TimeUnit.SECONDS)    // time to send data to server
        .build()

    // 🔹 Step 2: Build Retrofit instance with this client
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)  // attach custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}
