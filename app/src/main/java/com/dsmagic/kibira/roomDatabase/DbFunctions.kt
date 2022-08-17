package com.dsmagic.kibira.roomDatabase

import android.util.Log
import android.widget.Toast
import com.dsmagic.kibira.*
import com.dsmagic.kibira.MainActivity.Companion.appdb
import com.dsmagic.kibira.MainActivity.Companion.lineInS2Format
import com.dsmagic.kibira.roomDatabase.Entities.Coordinates
import com.dsmagic.kibira.roomDatabase.Entities.Project
import com.dsmagic.kibira.services.*
import com.google.android.gms.maps.model.LatLng
import dilivia.s2.S2LatLng
import dilivia.s2.index.point.PointData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DbFunctions {
    companion object {


        fun savePoints(point: LatLng, UID: Int, PID: Int) {

            val lat = point.latitude
            val lng = point.longitude

            val points = Coordinates(null, lat, lng, PID)

            GlobalScope.launch(Dispatchers.IO) {
                val d = appdb.kibiraDao().insertCoordinates(points)
                Log.d("data", "${d}")
                val s = 8
                val S2point = S2LatLng.fromDegrees(lat, lng)
                val pointData = PointData(S2point.toPoint(), S2point)
                lineInS2Format.remove(pointData)
            }


        }


        fun saveProject(name:String,GAPSIZE:Double,LineLength:Double, UID: Int) {

            val project = Project(null, name, GAPSIZE, LineLength, UID)

       GlobalScope.launch(Dispatchers.IO) {
                var d = appdb.kibiraDao().insertProject(project)
                Log.d("data", "${d}")
            }
        }
        fun getProjects(UID:Int){


            GlobalScope.launch(Dispatchers.IO) {
                val d = appdb.kibiraDao().getProjectsForUser(UID)
                for (g in d){
                  var projects = g.projects
                }

                Log.d("data", "${d}")
                val s = 8
            }

        }


        var id: Int = 0
        fun projectID(gp:Double,name:String):Int {
            GlobalScope.launch(Dispatchers.IO) {
                val d = appdb.kibiraDao().getProjectID(gp, name)
id = d
            }
return id
        }

        fun deleteProjectFunc(ID: Int, PID: Int) {


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
                            Toast.makeText(
                                MainActivity().applicationContext,
                                "Project deleted",
                                Toast.LENGTH_LONG
                            )
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
}