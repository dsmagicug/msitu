package com.dsmagic.kibira


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.dsmagic.kibira.R.*
import org.json.JSONException
import org.json.JSONObject

//Returning a layout as a dialog box
class CreateProjectDialog : DialogFragment() {

    @SuppressLint("SetTextI18n")
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

                    .setNegativeButton(string.cancel,DialogInterface.OnClickListener{ dialog, id ->


                    })
                    .setPositiveButton(string.create_new_project, DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int ->

                        oncreateclick()

                    })

                builder.create()

            }
            else {
                val projects = activity?.findViewById<TextView>(R.id.projectOne)

                val str ="{\"employee\":{\"name\":\"Abhishek Saini\",\"salary\":65000}}"

                try {

                    val obj: JSONObject = JSONObject(str)
                    val names: JSONObject = obj.getJSONObject("employee")

                    val name = names.getString("name")

                    // set employee name and salary in TextView's
                    //projects?.setText = "Name: $name";


                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                // listProjects()

                builder.setView(inflater.inflate(layout.list_projects, null))
//
                builder.create()

            }
        } ?: throw IllegalStateException("Activity cannot be null")

    }
    val sharedPrefFile = "kibirasharedfile"


    fun oncreateclick() {

        val sharedPrefFile = "kibirasharedfile"

        val projectname = dialog?.findViewById<EditText>(R.id.projectName)

        val gapsize = dialog?.findViewById<EditText>(R.id.gapSize)
        var displayProjectName = activity?.findViewById<TextView>(R.id.display_project_name)
        var displayGapSize = activity?.findViewById<TextView>(R.id.display_gap_size)


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
            val saved_project_name: String? = sharedPreferences.getString("name_key", "defaultValue")
            var saved_gap_size: Int? = sharedPreferences.getInt("gap_size", 0)

            displayProjectName?.text = saved_project_name

            //displayGapSize?.setText(String.valueOf(saved_gap_size))

            Log.d("values","Project name is: $saved_project_name")

//MainActivity().showFragment()
        } else{
            Log.d("not","Not saved")
        }

    }




}
