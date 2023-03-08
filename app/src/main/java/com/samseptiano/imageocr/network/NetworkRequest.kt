package com.samseptiano.imageocr.network

import com.samseptiano.imageocr.util.Constants.BASE_URL_MAP
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class NetworkRequest {
    companion object {
        fun createApi(): ApiInterface {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_MAP)
                .client(
                    OkHttpClient
                        .Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .addInterceptor(getInterceptor())
                        .build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ApiInterface::class.java)
        }

        private fun getInterceptor(): HttpLoggingInterceptor {
            val interceptor = HttpLoggingInterceptor()
            return interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        }
    }
}