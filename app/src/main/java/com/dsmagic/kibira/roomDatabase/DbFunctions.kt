package com.dsmagic.kibira.roomDatabase
/*
 *  This file is part of Msitu.
 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions
 *
 *  Msitu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Msitu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Msitu. If not, see <http://www.gnu.org/licenses/>
 */

import android.util.Log
import com.dsmagic.kibira.activities.CreateProjectDialog.appdbInstance
import com.dsmagic.kibira.activities.MainActivity.Companion.appdb
import com.dsmagic.kibira.activities.MainActivity.Companion.listOfMarkedPoints
import com.dsmagic.kibira.roomDatabase.Entities.AreaCoordinates

import com.dsmagic.kibira.roomDatabase.Entities.Coordinates
import com.dsmagic.kibira.roomDatabase.Entities.Project
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DbFunctions {
    companion object {

        var ProjectID: Long = 1

        fun savePoints(point: LatLng, PID: Int) {

            val lat = point.latitude
            val lng = point.longitude

            val points = Coordinates(null, lat, lng, PID)

            GlobalScope.launch(Dispatchers.IO) {
                appdb.kibiraDao().insertCoordinates(points)
            }

        }

        fun saveAreaPoints(point: LatLng, PID: Int) {

            val lat = point.latitude
            val lng = point.longitude

            val points = AreaCoordinates(null, lat, lng, PID)

            GlobalScope.launch(Dispatchers.IO) {
                appdb.kibiraDao().insertAreaCoordinates(points)
            }

        }

        fun saveProject(
            name: String,
            GAPSIZE: Double,
            LineLength: Double,
            UID: Int,
            Meshtype: String,
            gapUnits: String,

            meshUnits: String,
            plantingDirection:String
        ): Long {

            val project =
                Project(null, name, GAPSIZE, LineLength, UID, Meshtype, gapUnits, meshUnits,plantingDirection)


            GlobalScope.launch(Dispatchers.IO) {
                ProjectID = appdbInstance.kibiraDao().insertProject(project)
                Log.d("PID", "$ProjectID")
                ProjectID
            }

            return ProjectID
        }

        fun retrieveMarkedPoints(PID: Int): MutableList<LatLng> {
            var ListOfProjects: MutableList<Coordinates>
            GlobalScope.launch(Dispatchers.IO) {
                ListOfProjects =
                    appdb.kibiraDao().getCoordinatesForProject(PID) as MutableList<Coordinates>
                for (cods in ListOfProjects) {
                    val point = LatLng(cods.lat, cods.lng)
                    listOfMarkedPoints.add(point)

                }

            }

            return listOfMarkedPoints

        }

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
                appdb.kibiraDao().deleteSavedPoints(lat, lng)

            }

        }

        fun deleteProject(ProjectID: Int): Int {
            var deletedRow: Int = 0
            GlobalScope.launch(Dispatchers.IO) {

                deletedRow = appdb.kibiraDao().deleteProject(ProjectID)

            }
            return deletedRow
        }

    }
}