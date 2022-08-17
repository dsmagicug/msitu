package com.dsmagic.kibira.services

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


object AppModule {
   // var BaseUrl = "http://192.168.100.2:8000/api/"
    var ip = "192.168.1.104"
   // http://192.168.1.104:8000
    var port = 8000
    var BaseUrl = "http://${ip}:${port}/api/"


    fun retrofitInstance(): apiInterface {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BaseUrl)
            .build()
           .create(apiInterface::class.java)
    }

  /*  fun g(){
        // Define the interceptor, add authentication headers
        val interceptor = Interceptor { chain ->
            val newRequest: Request =
                chain.request().newBuilder().addHeader("User-Agent", "Retrofit-Sample-App").build()
            chain.proceed(newRequest)
        }

        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }*/
}