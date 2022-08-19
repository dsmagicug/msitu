package com.dsmagic.kibira


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.dsmagic.kibira.MainActivity.Companion.card
import com.dsmagic.kibira.MainActivity.Companion.directionCardLayout
import com.dsmagic.kibira.MainActivity.Companion.meshDone
import com.dsmagic.kibira.MainActivity.Companion.plantingRadius
import com.dsmagic.kibira.R.layout
import com.dsmagic.kibira.R.string
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.saveProject
import com.google.android.gms.maps.SupportMapFragment
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt


//Returning a layout as a dialog box
object CreateProjectDialog : DialogFragment() {


    val sharedPrefFile = "kibirasharedfile"
    var clean = false

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return this.activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflate
            val inflater = requireActivity().layoutInflater
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout

            val r = inflater.inflate(layout.activity_create_project, null)

            //dropdown menu
            val gapsize_units = resources.getStringArray(R.array.gapsizeUnits)
            val plotsize_units = resources.getStringArray(R.array.plotSizeUnits)

            val plotsizeAdapter =
                ArrayAdapter(requireContext(), layout.plotsize_layout, plotsize_units)
            val gapsizeAdapter =
                ArrayAdapter(requireContext(), layout.gapsizeunits, gapsize_units)

            val viewGapSize = r.findViewById<AutoCompleteTextView>(R.id.gapsizeDropDown)
            val viewPlotSize = r.findViewById<AutoCompleteTextView>(R.id.plotsizeDropDown)
            viewGapSize.setAdapter(gapsizeAdapter)
            viewPlotSize.setAdapter(plotsizeAdapter)


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


    var plotUnit = ""
    var gapUnit = ""

    fun oncreateclick() {

        val projectname = dialog?.findViewById<EditText>(R.id.ProjectName)
        val meshSize = dialog?.findViewById<EditText>(R.id.MeshSize)
        var progressbar = activity?.findViewById<ProgressBar>(R.id.progressBar)

        val gapsize = dialog?.findViewById<EditText>(R.id.gapSize)
        val displayProjectName = activity?.findViewById<TextView>(R.id.display_project_name)

        val gap_size_string = gapsize?.text.toString()

        val project_name: String = projectname?.text.toString()
        val mesh_size: String = meshSize?.text.toString()

        if (project_name == "" || gap_size_string == "") {
            alertfail("Please fill all fields")
        } else {
            //  progressbar?.isVisible = true
            val sharedPreferences: SharedPreferences =
                activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

            val editor = sharedPreferences.edit()
            editor.putString("size_key", gap_size_string)
            editor.putString("name_key", project_name)
            editor.putString("mesh_key", mesh_size)
            editor.apply()
            editor.commit()
            // progressbar?.isVisible = true
            if (editor.commit()) {
                val saved_project_name: String? =
                    sharedPreferences.getString("name_key", "defaultValue")
                val gap_size: String? = sharedPreferences.getString("size_key", "0")
                val saved_gap_size = gap_size!!
                val UID: String? = sharedPreferences.getString("userid_key", "0")
                val userID = UID!!.toInt()
                val mesh_size_string: String? =
                    sharedPreferences.getString("mesh_key", "0")
                var MeshSize: Double = 0.0
                var GapSize: Double = 0.0

                val decimalFormat = DecimalFormat("##.##")
                decimalFormat.roundingMode = RoundingMode.DOWN

                val n = plotUnit
                val gp = gapUnit

                //if nothing is selected then ft is the default
                if (plotUnit == "") {
                    var r = mesh_size_string!!.toDouble()
                    MeshSize = (r * 0.3048)
                }
                if (gapUnit == "") {
                    var r = saved_gap_size.toDouble()
                    GapSize = (r * 0.3048)
                }


                when (n) {
                    "Metres" -> {
                        MeshSize = mesh_size_string!!.toDouble()

                    }
                    "Ft" -> {
                        var r = mesh_size_string!!.toDouble()
                        MeshSize = (r * 0.3048)
                    }
                    "Miles" -> {
                        var r = mesh_size_string!!.toDouble()
                        MeshSize = (r * 1609.34)

                    }
                    "Acres" -> {
                        var r = mesh_size_string!!.toDouble()
                        MeshSize = (r * 4046.86)
                    }
                }
                when (gp) {
                    "metres" -> {
                        GapSize = saved_gap_size.toDouble()
                    }
                    "ft" -> {
                        var r = saved_gap_size.toDouble()
                        GapSize = (r * 0.3048)
                    }
                    "Inches" -> {
                        var r = saved_gap_size.toDouble()
                        GapSize = (r * 0.0254)

                    }

                }


                Geogmesh_size = MeshSize
                Geoggapsize = GapSize

                displayProjectName?.text = saved_project_name
                val pid = saveProject(
                    saved_project_name!!,
                    GapSize,
                    MeshSize,
                    userID
                )
                // var pid = DbFunctions.projectID( Geoggapsize!!.toDouble(),saved_project_name!!)
                editor.putString("productID_key", pid.toString())
                editor.apply()
                editor.commit()
                if(editor.commit()){
                    var r = 50
                }

                meshDone = false

                for (item in MainActivity.polyLines) {
                    item!!.remove()
                }
                MainActivity.mapFragment =
                    (activity?.supportFragmentManager!!.findFragmentById(R.id.mapFragment) as SupportMapFragment?)!!
                //mapFragment?.getMapAsync(callback)

                for (l in MainActivity.listofmarkedcircles) {
                    l.remove()
                }
                for (l in MainActivity.unmarkedCirclesList) {
                    l.remove()
                }
                if (MainActivity.listOfPlantingLines.isNotEmpty()) {
                    MainActivity.listOfPlantingLines.clear()
                }
                directionCardLayout.isVisible = false
                card.isVisible = false

                clean = true


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

//                val CreateProjectRetrofitObject = AppModule.retrofitInstance()
//                val modal =
//                        createProjectDataClass(saved_gap_size, saved_project_name!!, userID, MeshSize)
//                val retrofitData = CreateProjectRetrofitObject.createProject(modal)
//                retrofitData.enqueue(object : Callback<ResponseProjectDataClass?> {
//                    override fun onResponse(
//                            call: Call<ResponseProjectDataClass?>,
//                            response: Response<ResponseProjectDataClass?>
//                    ) {
//                        if (response.isSuccessful) {
//                            if (response.body() != null) {
//                                val ProjectName = response.body()!!.name
//                                val ProjectIDInt = response.body()!!.projectID
//                                val ProjectID = ProjectIDInt.toString()
//
//                                if (response.body()!!.message == "created") {
//
//                                    editor.putString("productID_key", ProjectID)
//                                    editor.apply()
//                                    editor.commit()
//
//                                    clean = true
//                                    SuccessAlert("Project $ProjectName created")
//
//                                } else {
//
//                                    alertfail("Project $ProjectName not created")
//                                }
//                            } else {
//
//                                alertfail("Project not created")
//                            }
//
//                        } else {
//
//                            alertfail("Project not created")
//                        }
//                    }
//
//                    override fun onFailure(call: Call<ResponseProjectDataClass?>, t: Throwable) {
//                        alertfail(("Response failed, invalid data"))
//                    }
//                })
    // progressbar?.isVisible = false

//                editor.putString("productID_key", ProjectID)
////                                    editor.apply()
////                                    editor.commit()
}




