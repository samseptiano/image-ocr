package com.samseptiano.imageocr.network

import com.samseptiano.imageocr.model.DistanceMatrix
import com.samseptiano.imageocr.util.Constants.MAPS_APIKEY
import com.samseptiano.imageocr.util.Constants.MAPS_DESTINATION
import com.samseptiano.imageocr.util.Constants.MAPS_MODE
import com.samseptiano.imageocr.util.Constants.MAPS_ORIGIN
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("maps/api/distancematrix/json")
    fun getDistance(
        @Query("destinations") destinations: String? = MAPS_DESTINATION,
        @Query("origins") origins: String? = MAPS_ORIGIN,
        @Query("mode") mode: String? = MAPS_MODE,
        @Query("key") key: String? = MAPS_APIKEY,
    ): Call<DistanceMatrix>
}