package com.dsmagic.kibira.data

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.dsmagic.kibira.R
import com.dsmagic.kibira.roomDatabase.DbFunctions

class alerts {
    fun warningAlert(S: String, I: Int,c: Context,PID:Int) {
        AlertDialog.Builder(c)
            .setTitle("Warning")
            .setIcon(R.drawable.caution)
            .setMessage(S)
            .setPositiveButton(
                "Delete",
                DialogInterface.OnClickListener { dialog, id ->
                    DbFunctions().deleteProjectFunc(I,PID)
                })
            .setNegativeButton("Just leave it alone",
                DialogInterface.OnClickListener { dialog, id ->

                })
            .show()
    }
}