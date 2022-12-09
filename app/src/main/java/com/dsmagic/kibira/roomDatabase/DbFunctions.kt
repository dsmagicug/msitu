package com.dsmagic.kibira.roomDatabase

/*
 *  This file is part of Kibira.
 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions
 *
 *  Kibira is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Kibira is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Kibira. If not, see <http://www.gnu.org/licenses/>
 */

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.dsmagic.kibira.activities.CreateProjectDialog.appdbInstance
import com.dsmagic.kibira.activities.MainActivity
import com.dsmagic.kibira.activities.MainActivity.Companion.appdb

import com.dsmagic.kibira.activities.MainActivity.Companion.listOfMarkedPoints
import com.dsmagic.kibira.roomDatabase.Entities.Coordinates
import com.dsmagic.kibira.roomDatabase.Entities.Project
import com.dsmagic.kibira.services.retrofit.AppModule
import com.dsmagic.kibira.services.retrofit.deleteProjectDataClass
import com.dsmagic.kibira.services.retrofit.deleteProjectResponse
import com.dsmagic.kibira.utils.Alerts.Companion.alertfail
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DbFunctions {
    companion object {

        var ProjectID: Long = 0

        fun savePoints(point: LatLng, PID: Int) {

            val lat = point.latitude
            val lng = point.longitude

            val points = Coordinates(null, lat, lng, PID)

            GlobalScope.launch(Dispatchers.IO) {
                appdb.kibiraDao().insertCoordinates(points)
            }

        }

        fun saveProject(name: String, GAPSIZE: Double, LineLength: Double, UID: Int,Meshtype:String,gapUnits:String,meshUnits:String): Long {

            val project = Project(null, name, GAPSIZE, LineLength, UID, Meshtype, gapUnits, meshUnits)

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
                ListOfProjects = appdb.kibiraDao().getCoordinatesForProject(PID) as MutableList<Coordinates>
                    for (cods in ListOfProjects) {
                        val point = LatLng(cods.lat, cods.lng)
                        listOfMarkedPoints.add(point)

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

        fun deleteProject(ProjectID: Int):Int {
            var deletedRow:Int = 0
            GlobalScope.launch(Dispatchers.IO) {

                deletedRow =  appdb.kibiraDao().deleteProject(ProjectID)

            }
                return deletedRow
        }

        fun deleteProjectFunc(ID: Int,context: Context) {


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
                            alertfail("Could not delete project :(",context)
                        }
                    } else {
                        alertfail("Error!! We all have bad days!! :( $response",context)
                    }
                }

                override fun onFailure(call: Call<deleteProjectResponse?>, t: Throwable) {
                    alertfail("Error ${t.message}",context)
                }
            })

        }
    }
}