package com.example.mediclens

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface OcrSpaceApi {
    @Multipart
    @POST("parse/image")
    fun uploadImage(
        @Header("apikey") apiKey: String,
        @Part image: MultipartBody.Part,
        @Part("language") language: RequestBody  // Change to the desired language
    ): Call<ResponseBody>
}