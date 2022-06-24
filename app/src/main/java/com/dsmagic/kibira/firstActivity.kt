package com.dsmagic.kibira


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle

import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.dsmagic.kibira.R.layout
import com.dsmagic.kibira.R.string

//Returning a layout as a dialog box
class firstActivity : DialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            //val selectedItem = ArrayList<Int>()
           var selectedProject: String = ""
            //val ar = arrayOf(MainActivity().)
           val ar = arrayOf("Project one","Project two","Project three")
           // val ar = emptyArray<String>()
            var checkedItemIndex = -1
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
//            val mDialogView: View = inflater.inflate(layout.activity_main, null)
//              builder.setView(mDialogView)
            if (ar.isEmpty()) {
                builder.setView(inflater.inflate(layout.activity_create_project, null))
                    // Add action buttons

                    .setNegativeButton(string.cancel,
                        DialogInterface.OnClickListener { dialog, id ->


                        })
                    .setPositiveButton(string.create_new_project,
                        DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int ->

                            oncreateclick()

                        })

                builder.create()

            } else {
               // val mDialogView: View = MainActivity().setContentView(layout.activity_main)
//              builder.setView(mDialogView)
                builder.setTitle("Choose Project")
                    .setSingleChoiceItems(ar, checkedItemIndex,
                        DialogInterface.OnClickListener { dialog, which ->
                            checkedItemIndex = which
                            selectedProject = ar[which]
                        })

//                    .setNeutralButton("New Project",
//                        DialogInterface.OnClickListener { dialog, id ->
//                            dialog.dismiss()
//
//                                val builder = AlertDialog.Builder(it)
//                                val inflater = requireActivity().layoutInflater
//
//
//                                builder.setView(inflater.inflate(layout.create_project, null))
//                                    // Add action buttons
//
//                                    .setNegativeButton(string.cancel,DialogInterface.OnClickListener{ dialog, id ->
//
//
//                                    })
//                                    .setPositiveButton(string.create_new_project, DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int ->
//
//                                        oncreateclick()
//
//                                    })
//                                builder.create()
//
//                        })
                    .setNegativeButton(R.string.cancel,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                        })
                    .setPositiveButton("ok",

                        DialogInterface.OnClickListener { dialog, id ->
                            val newvalues = "[\n" +
                                    "  [{\"lat\":8.4,\"lng\":43.9},{\"lat\":8,\"lng\":80}],\n" +
                                    "  {\"Base points\":{\"first\":[{\"lat\":8,\"lng\":9}],\n" +
                                    "    \"second\":[{\"lat\":9,\"lng\":10}]}\n" +
                                    "  },\n" +
                                    "  {\"name\":\"Project one\"},\n" +
                                    "  {\"gap size\":4},\n" +
                                    "  {\"mesh\":600.0}\n" +
                                    "\n" +
                                    "\n" +
                                    "]"
                            val project2 =  "[\n" +
                                "  [{\"lat\":8.4,\"lng\":43.9},{\"lat\":8,\"lng\":80}],\n" +
                                "  {\"Base points\":{\"first\":[{\"lat\":8,\"lng\":9}],\n" +
                                "    \"second\":[{\"lat\":9,\"lng\":10}]}\n" +
                                "  },\n" +
                                "  {\"name\":\"Project two\"},\n" +
                                "  {\"gap size\":4},\n" +
                                "  {\"mesh\":600.0}\n" +
                                "\n" +
                                "\n" +
                                "]"
                            if(selectedProject == ""){
                                //MainActivity().showSnackBar(mDialogView)
                            }else
                            {
                                val displayProjectName: TextView? = activity?.findViewById(R.id.display_project_name)
                                displayProjectName?.text = selectedProject
                            }
                          crea(selectedProject)

//                            val intent = Intent(context,Geometry::class.java)
//                            intent.putExtra("values",project2)
//                            startActivity(intent)



Log.d("selected","$selectedProject")

                        })

                // Create the AlertDialog object and return it
                builder.create()
            }

        } ?: throw IllegalStateException("Activity cannot be null")
    }

fun crea(name:String) {
    val newvalues = "[\n" +
            "  [{\"lat\":8.4,\"lng\":43.9},{\"lat\":8,\"lng\":80}],\n" +
            "  {\"Base points\":{\"first\":[{\"lat\":8,\"lng\":9}],\n" +
            "    \"second\":[{\"lat\":9,\"lng\":10}]}\n" +
            "  },\n" +
            "  {\"name\":\"Project one\"},\n" +
            "  {\"gap size\":4},\n" +
            "  {\"mesh\":600.0}\n" +
            "\n" +
            "\n" +
            "]"
    val project2 =  "[\n" +
            "  [{\"lat\":8.4,\"lng\":43.9},{\"lat\":8,\"lng\":80}],\n" +
            "  {\"Base points\":{\"first\":[{\"lat\":8,\"lng\":9}],\n" +
            "    \"second\":[{\"lat\":9,\"lng\":10}]}\n" +
            "  },\n" +
            "  {\"name\":\"Project two\"},\n" +
            "  {\"gap size\":4},\n" +
            "  {\"mesh\":600.0}\n" +
            "\n" +
            "\n" +
            "]"

}

    fun oncreateclick() {

        val sharedPrefFile = "kibirasharedfile"

        val projectname = dialog?.findViewById<EditText>(R.id.projectName)

        val gapsize = dialog?.findViewById<EditText>(R.id.gapSize)
        var displayProjectName = activity?.findViewById<TextView>(R.id.display_project_name)
        //var displayGapSize = activity?.findViewById<TextView>(R.id.display_gap_size)


        val gap_size = Integer.parseInt(gapsize?.text.toString())
        val project_name: String = projectname?.text.toString()

        val sharedPreferences: SharedPreferences =
            activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!
        val editor = sharedPreferences.edit()
        editor.putInt("size_key", gap_size)
        editor.putString("name_key", project_name)
        editor.apply()

        editor.commit()
        if (editor.commit()) {
            val saved_project_name: String? =
                sharedPreferences.getString("name_key", "defaultValue")
            var saved_gap_size: Int? = sharedPreferences.getInt("gap_size", 0)

            displayProjectName?.text = saved_project_name

            //displayGapSize?.setText(String.valueOf(saved_gap_size))

            Log.d("values", "Project name is: $saved_project_name")


        } else {
            Log.d("not", "Not saved")
        }

    }

}
private fun DialogFragment.show(s: String) {

}
