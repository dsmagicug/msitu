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
import com.dsmagic.kibira.MainActivity.Companion.onLoad
import com.dsmagic.kibira.R.layout
import com.dsmagic.kibira.R.string
import com.dsmagic.kibira.roomDatabase.AppDatabase
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.ProjectID
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.saveProject
import java.math.RoundingMode
import java.text.DecimalFormat


//Returning a layout as a dialog box
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
                            DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> oncreateclick() })

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
                        DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> oncreateclick() })

                builder.create()

            } else {

                builder.create()

            }
        } ?: throw IllegalStateException("Activity cannot be null")


    }


    var plotUnit = ""
    var gapUnit = ""

    fun oncreateclick() {


        var meshID = radiogroup.checkedRadioButtonId
        var selectedType = dialog?.findViewById<RadioButton>(meshID)?.text.toString()
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

            val editor = sharedPreferences.edit()
            editor.putString("size_key", gap_size_string)
            editor.putString("name_key", project_name)
            editor.putString("mesh_key", mesh_size)
            editor.putString("mesh_Type",selectedType)
            editor.apply()
            editor.commit()

            if (editor.commit()) {
                val saved_project_name: String? =
                    sharedPreferences.getString("name_key", "defaultValue")
                val gap_size: String? = sharedPreferences.getString("size_key", "0")
                val savedGapSize = gap_size!!
                val UID: String? = sharedPreferences.getString("userid_key", "0")
                val userID = UID!!.toInt()
                val meshSizeString: String? =
                    sharedPreferences.getString("mesh_key", "0")
                var meshSize: Double = 0.0
                var gapSize: Double = 0.0

                val decimalFormat = DecimalFormat("##.##")
                decimalFormat.roundingMode = RoundingMode.DOWN

                val n = plotUnit
                val gp = gapUnit

                //if nothing is selected then ft is the default
                if (plotUnit == "") {
                    val r = meshSizeString!!.toDouble()
                    meshSize = (r * 0.3048)
                }
                if (gapUnit == "") {
                    val r = savedGapSize.toDouble()
                    gapSize = (r * 0.3048)
                }


                when (n) {
                    "Meters" -> {
                        meshSize = meshSizeString!!.toDouble()

                    }
                    "Ft" -> {
                        val r = meshSizeString!!.toDouble()
                        meshSize = (r * 0.3048)
                    }
                    "Miles" -> {
                        val r = meshSizeString!!.toDouble()
                        meshSize = (r * 1609.34)

                    }
                    "Acres" -> {
                        val r = meshSizeString!!.toDouble()
                        meshSize = (r * 4046.86)
                    }
                }
                when (gp) {
                    "Meters" -> {
                        gapSize = savedGapSize.toDouble()
                    }
                    "Ft" -> {
                        val r = savedGapSize.toDouble()
                        gapSize = (r * 0.3048)
                    }
                    "Inches" -> {
                        val r = savedGapSize.toDouble()
                        gapSize = (r * 0.0254)

                    }

                }

                Geogmesh_size = meshSize
                Geoggapsize = gapSize
                MeshType = selectedType

                displayProjectName?.text = saved_project_name
                saveProject(
                    saved_project_name!!,
                    gapSize,
                    meshSize,
                    userID
                )
                ProjectID
                editor.putString("productID_key", ProjectID.toString())
                editor.apply()
                editor.commit()
                if (editor.commit()) {
                    var r = 50
                }
                MainActivity().freshFragment()

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





