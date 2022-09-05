package com.dsmagic.kibira


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.dsmagic.kibira.R.layout
import com.dsmagic.kibira.R.string
import com.dsmagic.kibira.roomDatabase.DbFunctions
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.saveProject
import com.dsmagic.kibira.roomDatabase.Entities.Project
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt
import kotlin.math.roundToLong


//Returning a layout as a dialog box


class firstActivity : DialogFragment() {


    //    private fun saveBasepoints(loc: LongLat) {
//        val lat = loc.getLatitude()
//        val lng = loc.getLongitude()
//
//        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
//            CreateProjectDialog.sharedPrefFile,
//            Context.MODE_PRIVATE
//        )!!
//
//        val ProjectIDString: String? = sharedPreferences.getString("productID_key", "0")
//
//        val ProjectID = ProjectIDString!!.toInt()
//
//        if (ProjectID == 0) {
//            Toast.makeText(
//                applicationContext,
//                "You did not create a project!! \n create one and continue",
//                Toast.LENGTH_LONG
//            ).show()
//            return
//        }
//
//        val basePoints = Basepoints( null,lat,lng,ProjectID)
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val d = appdb.kibiraDao().insertBasepoints(basePoints)
//            Log.d("data", "$d")
//            val s = 8
//        }
//
//        val modal = SaveBasePointsClass(lat, lng, ProjectID)
//        val retrofitDataObject = AppModule.retrofitInstance()
//
//        val retrofitData = retrofitDataObject.storeBasePoints(modal)
//        retrofitData.enqueue(object : Callback<SaveBasePointsResponse?> {
//            override fun onResponse(
//                call: Call<SaveBasePointsResponse?>,
//                response: Response<SaveBasePointsResponse?>
//            ) {
//                if (response.isSuccessful) {
//                    if (response.body() != null) {
//                        if (response.body()!!.message == "success") {
////
////                            Toast.makeText(
////                                applicationContext, "Bpoints Saved!! " +
////                                        "", Toast.LENGTH_SHORT
////                            ).show()
//                        } else {
//                            val m = response.body()!!.meta
//                            alertfail(m)
//                        }
//                    } else {
//                        alertfail("BODY IS NULL")
//                    }
//                } else {
//                    meshDone = false
//                    Toast.makeText(
//                        applicationContext,
//                        "You did not create a project!! \n create one and continue",
//                        Toast.LENGTH_LONG
//                    ).show()
//                   // alertfail("You did not create a project!! \n create one and continue")
//                }
//            }
//
//            override fun onFailure(call: Call<SaveBasePointsResponse?>, t: Throwable) {
//                TODO("Not yet implemented")
//            }
//        })
//    }

    //saving points in the db
//    fun savePoints(l: MutableList<LatLng>) {
//
//        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
//            CreateProjectDialog.sharedPrefFile,
//            Context.MODE_PRIVATE
//        )!!
//
//        val userIDString: String? = sharedPreferences.getString("userid_key", "0")!!
//        val ProjectIDString: String? = sharedPreferences.getString("productID_key", "0")
//
//        val userID = userIDString!!.toInt()
//        val ProjectID = ProjectIDString!!.toInt()
//
//
//
//
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val modal = savePointsDataClass(l, ProjectID, userID)
//            runOnUiThread {
//                progressBar.isVisible = true
//                Toast.makeText(
//                    applicationContext, "Saving points " +
//                            "", Toast.LENGTH_SHORT
//                ).show()
//            }
//            val retrofitDataObject = AppModule.retrofitInstance()
//
//            val retrofitData = retrofitDataObject.storePoints(modal)
//            if (retrofitData.isSuccessful) {
//                if (retrofitData.body() != null) {
//                    if (retrofitData.body()!!.message == "success") {
//                        runOnUiThread {
//                            progressBar.isVisible = false
//                        }
//
//                        Log.d("Loper", Thread().name + "savedb")
//                        // convert to S2 and remove it from queryset
//
//
//                    } else {
//                        Toast.makeText(
//                            applicationContext, "Something Went wrong!! " +
//                                    "", Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            } else {
//                alertfail("Not saved!")
//            }
//        }
//
//
//    }

}

