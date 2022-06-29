package com.dsmagic.kibira.services

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Singleton
class ServiceInterceptor constructor(
    private val tokenType: String, private var sessionToken: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        request = request.newBuilder().header("Authorization", "$tokenType $sessionToken")
            .build()
        return chain.proceed(request)


    }


    @Singleton
    fun httpClient(Token: String): apiInterface {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)

        val client = OkHttpClient.Builder()
            .addInterceptor(ServiceInterceptor("Bearer", Token))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(AppModule.BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(apiInterface::class.java)

        return retrofit

    }


}

@Singleton
class storeInterceptor constructor(
    private val tokenType: String, private var sessionToken: String, var m: savePointsDataClass
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        request = request.newBuilder().header("Authorization", "$tokenType $sessionToken")

            .build()
        return chain.proceed(request)


    }


    @Singleton
    fun httpClientStore(Token: String, m: savePointsDataClass): apiInterface {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)

        val client = OkHttpClient.Builder()
            .addInterceptor(storeInterceptor("Bearer", Token, m))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(AppModule.BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(apiInterface::class.java)

        return retrofit

    }
}
