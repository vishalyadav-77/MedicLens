package com.example.mediclens
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val BASE_URL= "https://api.ocr.space/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(40,TimeUnit.SECONDS)
        .readTimeout(40,TimeUnit.SECONDS)
        .build()

    val instance: OcrSpaceApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OcrSpaceApi::class.java)
    }
}