package com.dsmagic.kibira


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.dsmagic.kibira.R.layout
import com.dsmagic.kibira.R.string
import com.dsmagic.kibira.services.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.FileWriter
import java.io.PrintWriter
import kotlin.math.roundToInt

//Returning a layout as a dialog box
class CreateProjectDialog : DialogFragment() {

    val sharedPrefFile = "kibirasharedfile"

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return this.activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflate
            val inflater = requireActivity().layoutInflater
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout

            val r  = inflater.inflate(layout.activity_create_project,null)

            //dropdown menu
         val gapsize_units = resources.getStringArray(R.array.gapsizeUnits)
            val arrayAdapter1 = ArrayAdapter(requireContext(), layout.gapsizeunits,gapsize_units)
//            val viewGapSize = r.findViewById<AutoCompleteTextView>(R.id.gapsizeDropDown)
            val viewPlotSize = r.findViewById<AutoCompleteTextView>(R.id.plotsizeDropDown)
            //viewGapSize.setAdapter(arrayAdapter1)
            viewPlotSize.setAdapter(arrayAdapter1)


            if (tag == "create") {

                builder.setView(r)
                    // Add action buttons

                    .setNegativeButton(
                        string.cancel,
                        DialogInterface.OnClickListener { dialog, id ->


                        })
                    .setPositiveButton(
                        string.create_new_project,
                        DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int ->

                            oncreateclick()
                            //dismiss()


                        })

                builder.create()

            } else {
                val projects = activity?.findViewById<TextView>(R.id.projectOne)

                builder.setView(inflater.inflate(layout.list_projects, null))
//
                builder.create()

            }
        } ?: throw IllegalStateException("Activity cannot be null")


    }

    var unit = ""

    fun oncreateclick() {

        // val sharedPrefFile = "kibirasharedfile"

//progressBar.isVisible = true
        val projectname = dialog?.findViewById<EditText>(R.id.ProjectName)
        val meshSize = dialog?.findViewById<EditText>(R.id.MeshSize)

        val gapsize = dialog?.findViewById<EditText>(R.id.gapSize)
        val displayProjectName = activity?.findViewById<TextView>(R.id.display_project_name)

        val gap_size_string = gapsize?.text.toString()

        val project_name: String = projectname?.text.toString()
        val mesh_size: String = meshSize?.text.toString()

        if (project_name == "" || gap_size_string == "") {
            alertfail("Please fill all fields")
        } else {

            val sharedPreferences: SharedPreferences =
                activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

            val editor = sharedPreferences.edit()
            editor.putString("size_key", gap_size_string)
            editor.putString("name_key", project_name)
            editor.putString("mesh_key", mesh_size)
            editor.apply()
            editor.commit()

            if (editor.commit()) {
                val saved_project_name: String? =
                    sharedPreferences.getString("name_key", "defaultValue")
                val gap_size: String? = sharedPreferences.getString("size_key", "0")
                val saved_gap_size = gap_size!!.toInt()
                val UID: String? = sharedPreferences.getString("userid_key", "0")
                val userID = UID!!.toInt()
                val mesh_size_string: String? =
                    sharedPreferences.getString("mesh_key", "0")
                var MeshSize:Int = 0

                val n = unit
                if(unit == ""){
                    MeshSize = mesh_size_string!!.toInt()
                }

                when (n) {
                    "Metres"-> {
                        MeshSize = mesh_size_string!!.toInt()
                    }
                    "Ft"-> {
                        var r = mesh_size_string!!.toInt()
                        MeshSize = (r * 0.3048).roundToInt()
                    }
                    "Miles"-> {
                        var r = mesh_size_string!!.toInt()
                        MeshSize = (r * 1609.34).roundToInt()

                    }
                    "Acres"-> {
                        var r = mesh_size_string!!.toInt()
                        MeshSize = (r * 4046.86).roundToInt()
                    }
                }
                Geogmesh_size = MeshSize.toDouble()
                Geoggapsize = saved_gap_size
                val CreateProjectRetrofitObject = AppModule.retrofitInstance()
                val modal =
                    createProjectDataClass(saved_gap_size, saved_project_name!!, userID, MeshSize)
                val retrofitData = CreateProjectRetrofitObject.createProject(modal)
                retrofitData.enqueue(object : Callback<ResponseProjectDataClass?> {
                    override fun onResponse(
                        call: Call<ResponseProjectDataClass?>,
                        response: Response<ResponseProjectDataClass?>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                val ProjectName = response.body()!!.name
                                val ProjectIDInt = response.body()!!.projectID
                                val ProjectID = ProjectIDInt.toString()

                                if (response.body()!!.message == "created") {

                                    editor.putString("productID_key", ProjectID)
                                    editor.apply()
                                    editor.commit()


                                    SuccessAlert("Project $ProjectName created")

                                } else {

                                    alertfail("Project $ProjectName not created")
                                }
                            } else {

                                alertfail("Project not created")
                            }

                        } else {

                            alertfail("Project not created")
                        }
                    }

                    override fun onFailure(call: Call<ResponseProjectDataClass?>, t: Throwable) {
                        alertfail(("Response failed, invalid data"))
                    }
                })

                displayProjectName?.text = saved_project_name



            } else {
                Log.d("not", "Not saved")
            }
        }

    }

    //val c = dialog?.context
    fun alertfail(S: String) {
        this.activity?.let {
            AlertDialog.Builder(it)
                .setTitle("Error")
                .setIcon(R.drawable.cross)
                .setMessage(S)
                .show()
        }
    }

    fun SuccessAlert(S: String) {


        this.activity?.let {
            AlertDialog.Builder(it)
                .setTitle("Success")
                .setIcon(R.drawable.tick)
                .setMessage(S)
                .show()
        }


    }


}

