package com.dsmagic.kibira.services.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object AppModule {
   // var BaseUrl = "http://192.168.100.2:8000/api/"
    var ip = "192.168.1.104"
   // http://192.168.1.104:8000
    var port = 8000
    var BaseUrl = "http://$ip:$port/api/"


    fun retrofitInstance(): apiInterface {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BaseUrl)
            .build()
           .create(apiInterface::class.java)
    }

}