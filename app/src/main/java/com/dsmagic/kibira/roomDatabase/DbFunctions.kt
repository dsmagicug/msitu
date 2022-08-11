package com.dsmagic.kibira.roomDatabase

import android.content.Context
import android.widget.Toast
import com.dsmagic.kibira.LongLat
import com.dsmagic.kibira.MainActivity
import com.dsmagic.kibira.services.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DbFunctions {
    //saving points in the db
//    fun savePoints(l:MutableList<LatLng>,UID:Int,PID:Int) {
//
//        for(point in l){
//
//            val lat = point.latitude
//            val lng = point.longitude
//
//            GlobalScope.launch(Dispatchers.IO){
//                val modal = savePointsDataClass(lat, lng, PID, UID)
//
//                val retrofitDataObject = AppModule.retrofitInstance()
//
//                val retrofitData = retrofitDataObject.storePoints(modal)
//                if (retrofitData.isSuccessful) {
//                    if (retrofitData.body() != null) {
//                        if (retrofitData.body()!!.message == "success") {
//
//                            Log.d("Loper", Thread().name + "savedb")
//                            // convert to S2 and remove it from queryset
//                            val point = S2LatLng.fromDegrees(lat, lng)
//                            val pointData = PointData(point.toPoint(), point)
//                           MainActivity().lineInS2Format.remove(pointData)
//
//
//                        } else {
//                            Toast.makeText(
//                                MainActivity().applicationContext, "Something Went wrong!! " +
//                                        "", Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//                } else {
//                    MainActivity().alertfail("Not saved!")
//                }
//            }
//        }
//    }

     fun saveBasepoints(loc: LongLat,PID: Int,c:Context) {
        val lat = loc.getLatitude()
        val lng = loc.getLongitude()

        if (PID == 0) {
            Toast.makeText(
                c,
                "You did not create a project!! \n create one and continue",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val modal = SaveBasePointsClass(lat, lng, PID)
        val retrofitDataObject = AppModule.retrofitInstance()

        val retrofitData = retrofitDataObject.storeBasePoints(modal)
        retrofitData.enqueue(object : Callback<SaveBasePointsResponse?> {
            override fun onResponse(
                call: Call<SaveBasePointsResponse?>,
                response: Response<SaveBasePointsResponse?>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()!!.message == "success") {
//
//                            Toast.makeText(
//                                applicationContext, "Bpoints Saved!! " +
//                                        "", Toast.LENGTH_SHORT
//                            ).show()
                        } else {
                            val m = response.body()!!.meta
                           MainActivity().alertfail(m)
                        }
                    } else {
                        MainActivity().alertfail("BODY IS NULL")
                    }
                } else {
                    MainActivity().alertfail("An error has occured")
                }
            }

            override fun onFailure(call: Call<SaveBasePointsResponse?>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    fun deleteProjectFunc(ID: Int,PID:Int) {


        if (PID == 0) {
            Toast.makeText(
                MainActivity().applicationContext, "Could not load project" +
                        "", Toast.LENGTH_SHORT
            ).show()
        }

        val retrofitDeleteProjectInstance = AppModule.retrofitInstance()

        val modal = deleteProjectDataClass(ID)
        val retrofitData = retrofitDeleteProjectInstance.deleteProject(modal)

        retrofitData.enqueue(object : Callback<deleteProjectResponse?> {
            override fun onResponse(
                call: Call<deleteProjectResponse?>,
                response: Response<deleteProjectResponse?>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.message == "success") {
                        Toast.makeText(MainActivity().applicationContext, "Project deleted", Toast.LENGTH_LONG)
                            .show()

                    } else {
                        MainActivity().alertfail("Could not delete project :(")
                    }
                } else {
                    MainActivity().alertfail("Error!! We all have bad days!! :( $response")
                }
            }

            override fun onFailure(call: Call<deleteProjectResponse?>, t: Throwable) {
                MainActivity().alertfail("Error ${t.message}")
            }
        })

    }
}