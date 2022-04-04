package com.dsmagic.kibira


import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.app.Dialog
import android.content.Context
import android.widget.Toast
import android.content.DialogInterface
import android.content.SharedPreferences
import android.util.Log
import android.widget.EditText
import android.widget.TextView


import androidx.appcompat.app.AlertDialog
import com.dsmagic.kibira.R.*



//Returning a layout as a dialog box
class CreateProjectDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return this.activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflate
            val inflater = requireActivity().layoutInflater
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout

            if(tag == "create"){

                builder.setView(inflater.inflate(layout.create_project, null))
                    // Add action buttons
                   // .setNegativeButton(R.string.cancel, onDialogPositiveClick{ dialog, id ->})
                    .setNegativeButton(string.cancel,DialogInterface.OnClickListener{ dialog, id ->

                        Log.d("click","I reach here")
                    })
                    .setPositiveButton(string.create_new_project, DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int ->

//                       var dia = MainActivity.dialogPrivate()
//                        dia.
                       oncreateclick()

                       Log.d("click","I reach here positive")
                    })

                builder.create()

            }
            else {

                builder.setView(inflater.inflate(layout.list_projects, null))

                builder.create()

            }
        } ?: throw IllegalStateException("Activity cannot be null")

    }
    fun oncreateclick() {
        val sharedPrefFile = "kibirasharedfile"
//         val sharedPreferences: SharedPreferences =
//          this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//
        val projectname = dialog?.findViewById<EditText>(R.id.projectName)

        val gapsize = dialog?.findViewById<EditText>(R.id.gapSize)


        val gap_size = Integer.parseInt(gapsize?.text.toString())
        val project_name: String = projectname?.text.toString()
        val sharedPreferences: SharedPreferences =
            activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!
        val editor = sharedPreferences.edit()
        editor.putInt("size_key", gap_size)
        editor.putString("name_key", project_name)
        editor.apply()
        editor.commit()
        if(editor.commit()){

//        val toast = Toast.makeText(
//            applicationcontext?,
//            "This is a message displayed in a Toast",
//            Toast.LENGTH_SHORT
//       )
            Log.d("saved","saved data")
        } else{
            Log.d("not","Not saved")
        }
/*

val gap_size = Integer.parseInt(gapsize.text.toString())
val project_name: String = projectname.text.toString()

editor.putInt("size_key", gap_size)
editor.putString("name_key", project_name)
//
editor.apply()
editor.commit()
if(editor.commit()){
Log.d("saved","saved data")
} else{
Log.d("not","Not saved")
}
*/

        Log.d("click","sanity check")
    }


//    private fun oncreateclick() {
//        Log.d("click","I reach here called function again")
//    }


    //interface for handling the callback functions...MainActivity must implement this interface
//    internal lateinit var listener: ProjectsMenu
//
//    interface ProjectsMenu {
//
//        fun onDialogPositiveClick(dialog: DialogFragment)
//
//        fun onDialogNegativeClick(dialog: DialogFragment)
//    }
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        try {
//            listener = context as ProjectsMenu
//
//
//
//        }catch (e:ClassCastException){
//            throw ClassCastException((context.toString() +
//                    " must implement interface ProjectsMenu"))
//
//        }
   // }



}