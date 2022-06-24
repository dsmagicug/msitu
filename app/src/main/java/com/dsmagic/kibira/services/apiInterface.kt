package com.dsmagic.kibira.services

import retrofit2.Call
import retrofit2.http.*


interface apiInterface {

   // @GET(" http://192.168.100.17:8000/api/projects")
    @GET("projects")
    fun getProjectsList(@Header("Token" ) ApiToken:String):Call<List<retrieveProjectsDataClass>>


    @POST("register")
    fun registerUser(@Body dataModal:RegisterDataclassX):Call<RegisterDataclassX>

    @POST("login")
    fun loginUser(@Body dataModal: LoginDataClassX):Call<loginDataclass>

    @POST("create_project")
    fun createProject(@Body dataModal:createProjectDataClass):Call<ResponseProjectDataClass>

}