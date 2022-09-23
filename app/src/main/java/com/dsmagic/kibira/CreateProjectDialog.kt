package com.dsmagic.kibira


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.dsmagic.kibira.MainActivity.Companion.MeshType
import com.dsmagic.kibira.MainActivity.Companion.gapUnits
import com.dsmagic.kibira.MainActivity.Companion.map
import com.dsmagic.kibira.MainActivity.Companion.meshUnits
import com.dsmagic.kibira.MainActivity.Companion.onLoad
import com.dsmagic.kibira.MainActivity.Companion.projectList
import com.dsmagic.kibira.R.layout
import com.dsmagic.kibira.R.string
import com.dsmagic.kibira.roomDatabase.AppDatabase
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.ProjectID
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.saveProject
import com.dsmagic.kibira.utils.Conversions
import com.google.android.gms.maps.GoogleMap


//Returning a layout as a dialog box
@SuppressLint("StaticFieldLeak")
object CreateProjectDialog : DialogFragment() {
    lateinit var appdbInstance: AppDatabase
    lateinit var radiogroup: RadioGroup
    lateinit var previewSquare: TextView
    lateinit var previewTriangular: TextView

    val sharedPrefFile = "kibirasharedfile"
    var clean = false

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return this.activity?.let {

            appdbInstance = AppDatabase.dbInstance(activity?.applicationContext!!)

            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater


            val l: Array<String> = projectList.toTypedArray().reversedArray()
            val ar: Array<String>


            if (onLoad) {
                if (l.isNotEmpty()) {
                    val r = inflater.inflate(layout.activity_create_project, null)
                    radiogroup = r.findViewById<RadioGroup>(R.id.meshType)
                    previewSquare = r.findViewById(R.id.previewSquare)
                    previewTriangular = r.findViewById(R.id.previewTriangular)

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

                    viewGapSize.setOnItemClickListener { adapterView, _, i, _ ->
                        val gapsizeUnit = adapterView!!.getItemAtPosition(i).toString()
                        gapUnit = gapsizeUnit
                    }

                    viewPlotSize.setOnItemClickListener { adapterView, _, i, _ ->
                        val plotsizeUnit = adapterView!!.getItemAtPosition(i).toString()
                        plotUnit = plotsizeUnit
                    }

                    builder.setView(r)
                        // Add action buttons

                        .setNegativeButton(
                            string.cancel, DialogInterface.OnClickListener { _, _ -> })

                        .setPositiveButton(
                            string.create_new_project,
                            DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> createProject() })

                    builder.create()
                } else {

                    MainActivity().displayProjects()
                }


            }

            if (tag == "create") {
                val r = inflater.inflate(layout.activity_create_project, null)
                radiogroup = r.findViewById<RadioGroup>(R.id.meshType)
                previewSquare = r.findViewById(R.id.previewSquare)
                previewTriangular = r.findViewById(R.id.previewTriangular)

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

                viewGapSize.setOnItemClickListener { adapterView, _, i, _ ->
                    val gapsizeUnit = adapterView!!.getItemAtPosition(i).toString()
                    gapUnit = gapsizeUnit
                }

                viewPlotSize.setOnItemClickListener { adapterView, _, i, _ ->
                    val plotsizeUnit = adapterView!!.getItemAtPosition(i).toString()
                    plotUnit = plotsizeUnit
                }
                var previewImageSquare: ImageView = r.findViewById(R.id.smesh)
                var previewImageTriangle: ImageView = r.findViewById(R.id.tmesh)

                previewSquare.setOnClickListener {
                    previewImageSquare.isVisible = true
                    previewSquare.text = "close"
                }
                previewTriangular.setOnClickListener {
                    previewImageTriangle.isVisible = true
                    previewTriangular.text = "close"
                }

                builder.setView(r)
                    // Add action buttons

                    .setNegativeButton(
                        string.cancel, DialogInterface.OnClickListener { _, _ -> })

                    .setPositiveButton(
                        string.create_new_project,
                        DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> createProject() })

                builder.create()

            } else {

                builder.create()

            }
        } ?: throw IllegalStateException("Activity cannot be null")


    }


    var plotUnit = ""
    var gapUnit = ""

    private fun createProject() {

        val meshID = radiogroup.checkedRadioButtonId
        val selectedType = dialog?.findViewById<RadioButton>(meshID)?.text.toString()
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
            val sharedPreferences: SharedPreferences =
                activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

            val editor = sharedPreferences.edit().apply {
                putString("size_key", gap_size_string)
                putString("name_key", project_name)
                putString("mesh_key", mesh_size)
                putString("mesh_Type", selectedType)
                putString("gapsize_Units", gapUnit)
                putString("meshsize_Units", plotUnit)
                apply()
                commit()

            }

            if (editor.commit()) {
                sharedPreferences.apply {
                    val saved_project_name: String? = getString("name_key", "defaultValue")
                    val gapsize: String? = getString("size_key", "0")
                    val savedGapSize = gapsize!!
                    val UID: String? = getString("userid_key", "0")
                    val userID = UID!!.toInt()
                    val meshSizeString: String? = getString("mesh_key", "0")

                    val meshSize: Double
                    val gapSize: Double

                    val n = plotUnit
                    val gp = gapUnit
                    meshSize = Conversions.ftToMeters(meshSizeString!!, n)
                    gapSize = Conversions.ftToMeters(savedGapSize, gp)

                MAX_MESH_SIZE = meshSize
                GAP_SIZE_METRES = gapSize
                MeshType = selectedType
                gapUnits = if (gapUnit == "") {
                    " Ft"
                } else {
                    gapUnit
                }
                meshUnits = if (plotUnit == "") {
                    " Ft"
                } else {
                    plotUnit
                }

                displayProjectName?.text = saved_project_name
                saveProject(
                    saved_project_name!!,
                    gapSize,
                    meshSize,
                    userID,
                    selectedType,
                    gapUnits,
                    meshUnits

                )
                ProjectID
                editor.putString("productID_key", ProjectID.toString())
                editor.apply()
                editor.commit()
                   MainActivity.created = true

                map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                MainActivity().cleanUpExistingFragment()

            }
            }

        }

    }

    fun alertfail(S: String) {
       this.activity?.let {
            AlertDialog.Builder(it)
                .setTitle("Error")
                .setIcon(R.drawable.cross)
                .setMessage(S)
                .show()

        }
    }

    override fun onDestroy(){
    super.onDestroy()
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





