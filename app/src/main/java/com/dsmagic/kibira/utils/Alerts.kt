package com.dsmagic.kibira.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.dsmagic.kibira.MainActivity
import com.dsmagic.kibira.R
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.deleteBasePoints
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.deleteProject

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

                       var deletedRow = deleteProject(I)
                        if(deletedRow > 0 ){
                            SuccessAlert("Successfully Deleted",context)
                        }

                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { _, _ ->
                    })

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
//                .setNegativeButton(
//                    "Edit Project",
//                    DialogInterface.OnClickListener{
//                        dialog,id ->
//
//                    }
//                )


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

        fun SuccessAlert(S: String,context:Context) {
            AlertDialog.Builder(context)
                .setTitle("Success")
                .setIcon(R.drawable.tick)
                .setMessage(S)
                .show()
        }

    }

}