package com.dsmagic.kibira.utils
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

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.dsmagic.kibira.activities.MainActivity
import com.dsmagic.kibira.R
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.deleteBasePoints
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.deleteProject
import com.dsmagic.kibira.roomDatabase.sharing.ExportToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Alerts {
    companion object{
        fun DeleteAlert(S: String, I: Int,context:Context) {
            AlertDialog.Builder(context)
                .setTitle("Caution")
                .setIcon(R.drawable.caution)
                .setMessage(S)
                .setPositiveButton(
                    "Delete",

                    DialogInterface.OnClickListener { _, _ ->

                       val deletedRow = deleteProject(I)
                        if(deletedRow > 0 ){
                            SuccessAlert("Successfully Deleted",context)
                        }

                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { _, _ ->
                    })

                .show()
        }
        fun requestLocation(S:String,context: Context){
            AlertDialog.Builder(context)
                .setTitle("Turn on location")
                .setMessage(S)
                .show()

        }

        fun alertfail(S: String,context:Context) {
            AlertDialog.Builder(context)
                .setTitle("Error")
                .setIcon(R.drawable.cross)
                .setMessage(S)
                .show()
        }

        fun undoAlertWarning(PID:Int,context:Context){

            AlertDialog.Builder(context)
                .setTitle("Warning")
                .setIcon(R.drawable.caution)
                .setMessage("Un do drawn lines? This will clear all current lines!")
                .setPositiveButton(
                    "Undo",

                    DialogInterface.OnClickListener { _, _ ->
                        for (l in MainActivity.polyLines) {
                            l!!.remove()
                        }
                        deleteBasePoints(PID)
                        MainActivity.meshDone = false
                    })

                .show()

        }

        fun warningAlert(S: String, I: Int,context:Context) {
            AlertDialog.Builder(context)
                .setTitle("Warning")
                .setIcon(R.drawable.caution)
                .setMessage(S)
                .setPositiveButton(
                    "Delete",
                    DialogInterface.OnClickListener { _, _ ->
                        deleteProject(I)
                    })
                .setNegativeButton("Continue",
                    DialogInterface.OnClickListener { _, _ ->
                        MainActivity.meshDone = false
                    })
                .show()
        }


        fun confirmAlert(S: String, I: Int,context:Context) {
            AlertDialog.Builder(context)
                .setTitle("Confirm")
                .setIcon(R.drawable.caution)
                .setMessage(S)
                .setPositiveButton(
                    "Cancel",
                    DialogInterface.OnClickListener { _, _ ->
                        // do nothins
                    })
                .setNegativeButton("Yes Export",
                    DialogInterface.OnClickListener { _, _ ->
                        GlobalScope.launch(Dispatchers.IO) {
                            ExportToFile.exportProjectById(I,context)
                        }

                    })
                .show()
        }

        fun SuccessAlert(S: String,context:Context) {
            AlertDialog.Builder(context)
                .setTitle("Success")
                .setIcon(R.drawable.tick)
                .setMessage(S)
                .show()
        }

        lateinit var alertDialog: AlertDialog

        fun exitAlert(S: String,a: Activity) {
            alertDialog =
                AlertDialog.Builder(a.applicationContext).setTitle("Warning").setIcon(R.drawable.caution).setMessage(S)
                    .setPositiveButton("Exit",

                        DialogInterface.OnClickListener { _, _ ->

                            a.finish()

                        }).setNegativeButton("Stay",

                        DialogInterface.OnClickListener { _, _ ->

                        })

                    .show()
        }

    }


}