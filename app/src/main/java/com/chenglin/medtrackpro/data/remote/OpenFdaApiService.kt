package com.chenglin.medtrackpro.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

// A3: OpenFDA drug information lookup through Retrofit
interface OpenFdaApiService {
    @GET("drug/label.json")
    suspend fun searchDrugLabel(
        @Query("search") search: String,
        @Query("limit") limit: Int = 1
    ): OpenFdaLabelResponse
}
