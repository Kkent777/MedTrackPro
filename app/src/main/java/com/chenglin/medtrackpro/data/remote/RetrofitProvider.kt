package com.chenglin.medtrackpro.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    private const val OPEN_FDA_BASE_URL = "https://api.fda.gov/"
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"

    val openFdaApiService: OpenFdaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(OPEN_FDA_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFdaApiService::class.java)
    }

    val geminiApiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}
