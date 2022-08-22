package com.dsmagic.kibira.roomDatabase

import android.content.DialogInterface
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.dsmagic.kibira.*
import com.dsmagic.kibira.CreateProjectDialog.appdbInstance
import com.dsmagic.kibira.MainActivity.Companion.appdb

import com.dsmagic.kibira.MainActivity.Companion.lineInS2Format
import com.dsmagic.kibira.MainActivity.Companion.listOfMarkedPoints
import com.dsmagic.kibira.roomDatabase.Entities.Basepoints
import com.dsmagic.kibira.roomDatabase.Entities.Coordinates
import com.dsmagic.kibira.roomDatabase.Entities.Project
import com.dsmagic.kibira.services.AppModule
import com.dsmagic.kibira.services.deleteProjectDataClass
import com.dsmagic.kibira.services.deleteProjectResponse
import com.dsmagic.kibira.utils.Alerts.Companion.DeleteAlert
import com.dsmagic.kibira.utils.Alerts.Companion.alertfail
import com.google.android.gms.maps.model.LatLng
import dilivia.s2.S2LatLng
import dilivia.s2.index.point.PointData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DbFunctions {
    companion object {

        var ProjectID: Long = 0
        fun savePoints(point: LatLng, PID: Int) {
            Log.d("LOSE","${lineInS2Format}")

            val lat = point.latitude
            val lng = point.longitude

            val points = Coordinates(null, lat, lng, PID)

            GlobalScope.launch(Dispatchers.IO) {
                val d = appdb.kibiraDao().insertCoordinates(points)
                val s = 8
                val S2point = S2LatLng.fromDegrees(lat, lng)
                val pointData = PointData(S2point.toPoint(), S2point)
                lineInS2Format.remove(pointData)
                Log.d("LOSE","${lineInS2Format}")
            }


        }


        fun saveProject(name: String, GAPSIZE: Double, LineLength: Double, UID: Int): Long {

            val project = Project(null, name, GAPSIZE, LineLength, UID)

            GlobalScope.launch(Dispatchers.IO) {
                ProjectID = appdbInstance.kibiraDao().insertProject(project)
                Log.d("PID", "$ProjectID")
                ProjectID
            }


            return ProjectID
        }

        fun retrieveMarkedpoints(PID: Int): MutableList<LatLng> {
            var ListOfProjects: MutableList<Coordinates>
            GlobalScope.launch(Dispatchers.IO) {
                val coordinates = appdb.kibiraDao().getCoordinatesForProject(PID)
                for (c in coordinates) {
                    ListOfProjects = c.coordinates as MutableList<Coordinates>
                    for (cods in ListOfProjects) {
                        val point = LatLng(cods.lat, cods.lng)
                        listOfMarkedPoints.add(point)
                    }
                }


            }
            return listOfMarkedPoints
        }

//        fun retrieveBasepoints(PID:Int):MutableList<LatLng>{
//            var ListOfBasePoints: MutableList<Basepoints>
//            GlobalScope.launch(Dispatchers.IO) {
//                val coordinates = appdb.kibiraDao().getBasepointsForProject(PID)
//                for (c in coordinates) {
//                    ListOfBasePoints = c.basepoints as MutableList<Basepoints>
//                    for (cods in ListOfBasePoints) {
//
//                        val firstPoint = LongLat(cods.lng.toDouble(), cods.lat.toDouble())
//                        val secondPoint = LongLat(cods.lng.toDouble(), cods.lat.toDouble())
//                    }
//                }
//
//            }
//            return listOfMarkedPoints
//        }

        var id: Int = 0
        fun getProjectID(gp: Double, name: String): Int {

            GlobalScope.launch(Dispatchers.IO) {
               id = appdb.kibiraDao().getProjectID(gp, name)

            }
            return id
        }

        fun deleteBasePoints(ID: Int) {
            GlobalScope.launch(Dispatchers.IO) {
                appdb.kibiraDao().deleteBasePoints(ID)

            }

        }

        fun deleteSavedPoints(Point: LatLng) {
            GlobalScope.launch(Dispatchers.IO) {
                val lat = Point.latitude
                val lng = Point.longitude
                appdb.kibiraDao().deleteSavedPoints(lat,lng)

            }

        }

        fun deleteProject(ProjectID: Int) {
            GlobalScope.launch(Dispatchers.IO) {

                appdb.kibiraDao().deleteProject(ProjectID)

            }

        }

        fun deleteProjectFunc(ID: Int) {


            if (ID == 0) {
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
                            alertfail("Could not delete project :(")
                        }
                    } else {
                        alertfail("Error!! We all have bad days!! :( $response")
                    }
                }

                override fun onFailure(call: Call<deleteProjectResponse?>, t: Throwable) {
                    alertfail("Error ${t.message}")
                }
            })

        }
    }
}