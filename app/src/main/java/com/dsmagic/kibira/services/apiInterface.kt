package com.dsmagic.kibira.services

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


interface apiInterface {

    // @GET(" http://192.168.100.17:8000/api/projects")
//    @GET("projects")
//    fun getProjectsList(@Header("Token" ) ApiToken:String):Call<List<ResponseProjectsDataClass>>


    @POST("register")
    fun registerUser(@Body dataModal: RegisterDataclassX): Call<ResponseRegister>

    @POST("login")
    fun loginUser(@Body dataModal: LoginDataClassX): Call<loginDataclass>

    @POST("create_project")
    fun createProject(@Body dataModal: createProjectDataClass): Call<ResponseProjectDataClass>

    //UserProjects

    @GET("myProjects")
    suspend fun getProjectsList(@Header("Token") ApiToken: String): Response<List<ResponseProjectsDataClass>>

    @POST("savePoints")
    suspend fun storePoints(@Body dataModal: savePointsDataClass): Response<savePointsResponse>

    @POST("savebasepoints")
    fun storeBasePoints(@Body dataModal: SaveBasePointsClass): Call<SaveBasePointsResponse>

    @POST("points")
    suspend fun retrievePoints(@Body dataModal: RequestPoints): Response<ResponsePoints>

    @POST("getBasepoints")
    suspend fun retrieveBasePoints(@Body dataClass: RequestBasePointsDataClass): Response<RetrieveBasePointsDataClass>

    @POST("deleteProject")
    fun deleteProject(@Body dataClass: deleteProjectDataClass): Call<deleteProjectResponse>

    @POST("deleteCoords")
    fun unmarkPoint(@Body dataClass: DeleteCoords): Call<DeleteCoordsResponse>

}