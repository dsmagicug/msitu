package com.dsmagic.kibira.services

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

object AppModule {
    @Singleton
    fun httpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder().addInterceptor(myServiceInterceptor("BearerToken","fb52d2c949b26d1c026f3a6576c97a54.ade9fb8c9716ca5a12166bc2ac0ad4c7")
        ).addInterceptor(interceptor)
            .build()

    }


}