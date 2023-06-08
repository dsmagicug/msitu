package com.dsmagic.kibira.activities


/*
 *  This file is part of Msitu.

 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions
 *
 *  Msitu is free software: you can redistribute it and/or modify

 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Msitu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Msitu. If not, see <http://www.gnu.org/licenses/>

 */

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
import com.dsmagic.kibira.R
import com.dsmagic.kibira.activities.MainActivity.Companion.MeshType
import com.dsmagic.kibira.activities.MainActivity.Companion.areaToggle
import com.dsmagic.kibira.activities.MainActivity.Companion.gapUnits
import com.dsmagic.kibira.activities.MainActivity.Companion.map
import com.dsmagic.kibira.activities.MainActivity.Companion.meshUnits
import com.dsmagic.kibira.activities.MainActivity.Companion.onCreation
import com.dsmagic.kibira.activities.MainActivity.Companion.onLoad
import com.dsmagic.kibira.activities.MainActivity.Companion.plantingDirection
import com.dsmagic.kibira.activities.MainActivity.Companion.projectList
import com.dsmagic.kibira.dataReadings.GAP_SIZE_METRES
import com.dsmagic.kibira.dataReadings.MAX_MESH_SIZE
import com.dsmagic.kibira.roomDatabase.AppDatabase
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.ProjectID
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.saveProject
import com.dsmagic.kibira.utils.Conversions
import com.google.android.gms.maps.GoogleMap

import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray



//Returning a layout as a dialog box

 object CreateProjectDialog : DialogFragment() {

     var basePointsJsonArray: JSONArray = JSONArray()
      lateinit var appdbInstance: AppDatabase
     var selectedPlantingDirection = " "

    val sharedPrefFile = "kibirasharedfile"

    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return this.activity?.let { it ->


            appdbInstance = AppDatabase.dbInstance(activity?.applicationContext!!)

            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            //Display projects starting from the most recently created one
            val l: Array<String> = projectList.toTypedArray().reversedArray()

            var checked = false


            if (onLoad) {
                if (l.isNotEmpty()) {
                    val r = inflater.inflate(R.layout.activity_create_project, null)

                    //dropdown menu
                    val gapsize_units = resources.getStringArray(R.array.gapsizeUnits)
                    val plotsize_units = resources.getStringArray(R.array.plotSizeUnits)

                    val plotsizeAdapter =
                        ArrayAdapter(requireContext(), R.layout.plotsize_layout, plotsize_units)
                    val gapsizeAdapter =
                        ArrayAdapter(requireContext(), R.layout.gapsizeunits, gapsize_units)

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
                            R.string.cancel, DialogInterface.OnClickListener { _, _ -> })

                        .setPositiveButton(
                           R.string.create_new_project,

                            DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> createProject(checked) })


                    builder.create()
                } else {

                    MainActivity().displayProjects()
                }


            }
            //creation of new project
            if (tag == "create") {
                val r = inflater.inflate(R.layout.activity_create_project, null)

               val previewSquare:TextView = r.findViewById(R.id.previewSquare)
               val previewTriangular:TextView = r.findViewById(R.id.previewTriangular)


                //dropdown menu
                val gapsize_units = resources.getStringArray(R.array.gapsizeUnits)
                val plotsize_units = resources.getStringArray(R.array.plotSizeUnits)

                val plotsizeAdapter =
                    ArrayAdapter(requireContext(), R.layout.plotsize_layout, plotsize_units)
                val gapsizeAdapter =
                    ArrayAdapter(requireContext(), R.layout.gapsizeunits, gapsize_units)

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

                val BpCheckbox =  r?.findViewById<CheckBox>(R.id.checkpoint_basepoints)
                BpCheckbox?.setOnCheckedChangeListener { _, isChecked ->
                        val basepointsLayout = r.findViewById<TextInputLayout>(R.id.base)
                            if (isChecked) {
                                basepointsLayout.isVisible = true
                                checked = true
                        } else {
                            checked = false
                                basepointsLayout.isVisible = false

                            }
                    }

                val previewImageSquare: ImageView = r.findViewById(R.id.smesh)
                val previewImageTriangle: ImageView = r.findViewById(R.id.tmesh)

                previewSquare.setOnClickListener {

                    previewImageSquare.isVisible = !previewImageSquare.isVisible
                    previewSquare.text = "Tap to close/open"
                }
                previewTriangular.setOnClickListener {
                    previewImageTriangle.isVisible = !previewImageTriangle.isVisible
                    previewTriangular.text = "Tap to close/open"

                }

                builder.setView(r)
                    // Add action buttons
                    .setNegativeButton(
                        R.string.cancel, DialogInterface.OnClickListener { _, _ -> })

                    .setPositiveButton(
                       R.string.create_new_project,

                        DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> createProject(checked) })


                builder.create()

            } else {

                builder.create()

            }
        } ?: throw IllegalStateException("Activity cannot be null")


    }

    var plotUnit = ""
    var gapUnit = ""


    private fun createProject(checked:Boolean) {

        val meshID = this.dialog?.findViewById<RadioGroup>(R.id.meshType)?.checkedRadioButtonId
        val directionId = this.dialog?.findViewById<RadioGroup>(R.id.direction)?.checkedRadioButtonId
        val selectedType = dialog?.findViewById<RadioButton>(meshID!!)!!.text.toString()
         selectedPlantingDirection = dialog?.findViewById<RadioButton>(directionId!!)!!.text.toString()

        val projectname = dialog?.findViewById<EditText>(R.id.ProjectName)
        val meshSize = dialog?.findViewById<EditText>(R.id.MeshSize)

        val gapsize = dialog?.findViewById<EditText>(R.id.gapSize)
        val displayProjectName = activity?.findViewById<TextView>(R.id.display_project_name)

        val gap_size_string = gapsize?.text.toString()

        val project_name: String = projectname?.text.toString()
        val mesh_size: String = meshSize?.text.toString()

        val basepointsTextView =  dialog?.findViewById<TextView>(R.id.basepoints)
        var pasted = false
        var basepointsError = false

        val cleanedText:String

                val basepointsValue  = basepointsTextView?.text.toString()

                if(checked){
                    if(basepointsValue != " "){
                        pasted = true
                    }
                }
                if(pasted){
                     cleanedText = "[${basepointsValue.trim().removeSuffix(",")}]"
                    try {
                         basePointsJsonArray = JSONArray(cleanedText)

                        if(basePointsJsonArray.length() != 2){
                            dialog?.dismiss()
                            Toast.makeText(context,"Incorrect number of basepoints",Toast.LENGTH_LONG).show()
                            basepointsError = true

                        }
                        onCreation = true
                    } catch (e:java.lang.Exception){
                        basepointsError = true
                        alertfail("Incorrect number of Base points Entered\nTwo sets of Lat/Lng coordinates are expected.")
                    }

                }

        when(selectedPlantingDirection){
            "To The Left" -> {
                plantingDirection = "L"
            }
            "To The Right" -> {
                plantingDirection = "R"

            }
        }

        if (project_name == "" || gap_size_string == "" ) {
            alertfail("Errors present. Check possible issues\nEmpty Fields\nIncorrect number of Base points Entered\\nTwo sets of Lat/Lng coordinates are expected.")

        }
        //if all fields are filled, store values in shared preference file, to be used throughout the whole app
        else {
            val sharedPreferences: SharedPreferences =
                activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

            val editor = sharedPreferences.edit().apply {
                putString("size_key", gap_size_string)
                putString("name_key", project_name)
                putString("mesh_key", mesh_size)
                putString("mesh_Type", selectedType)
                putString("gapsize_Units", gapUnit)
                putString("meshsize_Units", plotUnit)

                putString("plantingDirection",plantingDirection)

                apply()

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

                    val plantingDirection:String? = getString("plantingDirection","0")


                    val n = plotUnit
                    val gp = gapUnit
                    meshSize = Conversions.ftToMeters(meshSizeString!!, n)
                    gapSize = Conversions.ftToMeters(savedGapSize, gp)

                MAX_MESH_SIZE = meshSize
                GAP_SIZE_METRES = gapSize
                MeshType = selectedType
                    //Ft is the default units: Note: Leave as is " Ft"
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

                    meshUnits,
                     plantingDirection!!

                )
                ProjectID
                editor.putString("productID_key", ProjectID.toString())
                editor.apply()

                    MainActivity.created = true
                   MainActivity.areaMode = false
                    areaToggle.isChecked = true
                    map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    Log.d("Null"," creation  + $onCreation ")

                    dialog?.dismiss()

                MainActivity().cleanUpExistingFragment()
                    dismiss()

            }
            } else {
                alertfail("Project Details were not successfully saved.Please re create this project.")
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





