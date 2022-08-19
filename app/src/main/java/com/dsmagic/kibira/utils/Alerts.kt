package com.dsmagic.kibira.utils

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.dsmagic.kibira.MainActivity
import com.dsmagic.kibira.MainActivity.Companion.context
import com.dsmagic.kibira.R
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.ProjectID
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.deleteBasePoints
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.deleteProjectFunc

class Alerts {
    companion object{
        fun DeleteAlert(S: String, I: Int) {
            AlertDialog.Builder(context)
                .setTitle("Caution")
                .setIcon(R.drawable.caution)
                .setMessage(S)
                .setPositiveButton(
                    "Delete",

                    DialogInterface.OnClickListener { dialog, id ->

                        deleteProjectFunc(I)

                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                    })

                .show()
        }

        fun alertfail(S: String) {
            AlertDialog.Builder(context)
                .setTitle("Error")
                .setIcon(R.drawable.cross)
                .setMessage(S)
                .show()
        }

        fun undoAlertWarning(PID:Int){

            AlertDialog.Builder(context)
                .setTitle("Warning")
                .setIcon(R.drawable.caution)
                .setMessage("Un do drawn lines? This will clear all current lines!")
                .setPositiveButton(
                    "Undo",

                    DialogInterface.OnClickListener { dialog, id ->
                        for (l in MainActivity.polyLines) {
                            l!!.remove()
                        }
                        deleteBasePoints(PID)
                        MainActivity.meshDone = false
                    })
                .setNegativeButton(
                    "Edit Project",
                    DialogInterface.OnClickListener{
                        dialog,id ->

                    }
                )


                .show()

        }

    }

}