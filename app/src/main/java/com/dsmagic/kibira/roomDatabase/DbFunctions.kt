package com.dsmagic.kibira.roomDatabase

import android.content.DialogInterface
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.dsmagic.kibira.*
import com.dsmagic.kibira.MainActivity.Companion.appdb
import com.dsmagic.kibira.MainActivity.Companion.lineInS2Format
import com.dsmagic.kibira.roomDatabase.Entities.Coordinates
import com.dsmagic.kibira.roomDatabase.Entities.Project
import com.dsmagic.kibira.services.AppModule
import com.dsmagic.kibira.services.deleteProjectDataClass
import com.dsmagic.kibira.services.deleteProjectResponse
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


        fun saveProject(name: String, GAPSIZE: Double, LineLength: Double, UID: Int): Int {
            var ProjectID: Long = 0
            val project = Project(null, name, GAPSIZE, LineLength, UID)

            GlobalScope.launch(Dispatchers.IO) {
                var d = appdb.kibiraDao().insertProject(project)
                ProjectID = d
            }

            return ProjectID.toInt()
        }


        fun getProjects(UID: Int) {

            var ProjectList = mutableListOf<Project>()
            GlobalScope.launch(Dispatchers.IO) {
                try{
                    val listOfProjects = appdb.kibiraDao().getAllProjects(UID)
                  for(project in listOfProjects){
                      ProjectList.add(project)
                  }


                   MainActivity().displayProjects()


                        Log.d("Projects", "$ProjectList")


                } catch (e:NullPointerException){
                    Log.d("Projects", "Empty Project")

                }

            }


            }

        var selectedProject = " "
//        fun displayProjects() {
//            var l: Array<String>
//            var checkedItemIndex = -1
//
//            val larray = projectList.toTypedArray()
//            if (larray.size > 5 || larray.size == 5) {
//                l = larray.sliceArray(0..4)
//            } else {
//                l = larray
//            }
//
//
//                AlertDialog.Builder()
//                    .setTitle("Projects")
//                    .setSingleChoiceItems(l, checkedItemIndex,
//                        DialogInterface.OnClickListener { dialog, which ->
//                            checkedItemIndex = which
//                            selectedProject = larray[which]
//                        })
//                    .setNegativeButton("Delete",
//                        DialogInterface.OnClickListener { dialog, id ->
//                            for (j in larray) {
//                                if (j == selectedProject) {
//                                    val index = larray.indexOf(j)
//                                    val id = projectIDList[index]
//
//                                    DeleteAlert(
//                                        "\nProject '$selectedProject' $id  will be deleted permanently.\n\nAre you sure?",
//                                        id
//                                    )
//                                }
//
//                            }
//
//
//                        })
//                    .setNeutralButton("More..",
//                        DialogInterface.OnClickListener { dialog, id ->
//
//                            AlertDialog.Builder(this)
//                                .setTitle("All Projects")
//                                // .setMessage(s)
//                                .setSingleChoiceItems(larray, checkedItemIndex,
//                                    DialogInterface.OnClickListener { dialog, which ->
//                                        checkedItemIndex = which
//                                        selectedProject = larray[which]
//                                    })
//                                .setNegativeButton("Delete",
//                                    DialogInterface.OnClickListener { dialog, id ->
//                                        for (j in larray) {
//                                            if (j == selectedProject) {
//                                                val index = larray.indexOf(j)
//                                                val id = projectIDList[index]
//
//                                                DeleteAlert(
//                                                    "\nProject '$selectedProject' $id will be deleted permanently.\n\nAre you sure?",
//                                                    id
//                                                )
//                                            }
//
//                                        }
//
//
//                                    })
//                                .setPositiveButton("Open",
//
//                                    DialogInterface.OnClickListener { dialog, id ->
//
//                                        if (selectedProject == "") {
//
//                                        } else {
//                                            for (j in larray) {
//                                                if (j == selectedProject) {
//                                                    val index = larray.indexOf(j)
//                                                    val id = projectIDList[index]
//                                                    var gap_size = projectSizeList[index]
//                                                    var mesh_size = projectMeshSizeList[index]
//                                                   MainActivity().getPoints(id)
//                                                    MainActivity().loadProject(id, mesh_size, gap_size)
//                                                }
//
//                                            }
//
//
//                                        }
//
//                                    })
//
//                                .show()
//
//                        })
//                    .setPositiveButton("Open",
//
//                        DialogInterface.OnClickListener { dialog, id ->
//
//                            if (selectedProject == "") {
//
//                            } else {
//                                for (j in l) {
//                                    if (j == selectedProject) {
//                                        val index = l.indexOf(j)
//                                        val id = projectIDList[index]
//                                        var gap_size = projectSizeList[index]
//                                        var mesh_size = projectMeshSizeList[index]
//                                        MainActivity().getPoints(id)
//                                       MainActivity().loadProject(id, mesh_size, gap_size)
//                                    }
//
//                                }
//
//
//                            }
//
//                        })
//
//                    .show()
//
//        }


        fun projectID(gp: Double, name: String): Int {
            var id: Int = 0
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