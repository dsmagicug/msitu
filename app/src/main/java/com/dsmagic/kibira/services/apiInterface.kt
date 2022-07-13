package com.dsmagic.kibira.services

import retrofit2.Call
import retrofit2.http.*


interface apiInterface {

   // @GET(" http://192.168.100.17:8000/api/projects")
//    @GET("projects")
//    fun getProjectsList(@Header("Token" ) ApiToken:String):Call<List<ResponseProjectsDataClass>>


    @POST("register")
    fun registerUser(@Body dataModal:RegisterDataclassX):Call<ResponseRegister>

    @POST("login")
    fun loginUser(@Body dataModal: LoginDataClassX):Call<loginDataclass>

    @POST("create_project")
    fun createProject(@Body dataModal:createProjectDataClass):Call<ResponseProjectDataClass>

 //UserProjects

 @GET("myProjects")
 fun getProjectsList(@Header("Token" ) ApiToken:String):Call<List<ResponseProjectsDataClass>>

 @POST("savePoints")
 fun storePoints( @Body dataModal: savePointsDataClass ):Call<savePointsResponse>

    @POST("savebasepoints")
    fun storeBasePoints( @Body dataModal: SaveBasePointsClass ):Call<SaveBasePointsResponse>

 @POST("points")
 fun retrievePoints(@Body dataModal: RequestPoints):Call<ResponsePoints>

    @POST("getBasepoints")
 fun retrieveBasePoints(@Body dataClass: RequestBasePointsDataClass):Call<RetrieveBasePointsDataClass>

 @POST("deleteProject")
 fun deleteProject(@Body dataClass: deleteProjectDataClass):Call<deleteProjectResponse>

 @POST("deleteCoords")
 fun unmarkPoint(@Body dataClass: DeleteCoords):Call<DeleteCoordsResponse>

}