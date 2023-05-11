package com.dsmagic.kibira.activities

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.dsmagic.kibira.R
import com.dsmagic.kibira.activities.MainActivity.Companion.VerticePoints
import com.dsmagic.kibira.activities.MainActivity.Companion.latVertexList
import com.dsmagic.kibira.activities.MainActivity.Companion.longVertexList
import com.dsmagic.kibira.activities.MainActivity.Companion.map
import com.dsmagic.kibira.dataReadings.GFG.polygonArea
import com.dsmagic.kibira.roomDatabase.AppDatabase
import com.google.android.gms.maps.model.*


object AreaDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return this.activity?.let { it ->

            CreateProjectDialog.appdbInstance =
                AppDatabase.dbInstance(activity?.applicationContext!!)

            val builder = AlertDialog.Builder(it)


            val inflater = requireActivity().layoutInflater

            val r = inflater.inflate(R.layout.activity_area, null)
            val areaField = r.findViewById<Button>(R.id.area_btn)
            val areaValue = r.findViewById<TextView>(R.id.area_value)
            val pointsValue = r.findViewById<TextView>(R.id.points_value)
            val verticesValue = r.findViewById<TextView>(R.id.vertice_value)
            val visual = r.findViewById<TextView>(R.id.visual)

            //units to be used for the area.
            //1-for acres and 2- for hectares
            var areaUnitsInt: Int = 1

            areaField?.setOnClickListener {

                val n = latVertexList.size
                val units = r.findViewById<RadioGroup>(R.id.area_units)?.checkedRadioButtonId
                val selectedUnits =
                    units?.let { it1 -> dialog?.findViewById<RadioButton>(it1)?.text.toString() }

                Log.d("AREA", "$n, $latVertexList , $longVertexList")
                if (n < 3) {
                    dialog?.dismiss()
                    Toast.makeText(
                        context,
                        "Few vertices provided.More than 2 are expected",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    when (selectedUnits) {
                        " " -> {
                            areaUnitsInt = 1
                        }
                        "In hectares" -> {
                            areaUnitsInt = 2
                        }
                        "In acres" -> {
                            areaUnitsInt = 1
                        }
                    }
                    val area = polygonArea(latVertexList, longVertexList, n)
                    val convertedArea: Double = convert(area, areaUnitsInt)
                    areaValue.text = convertedArea.toString()
                    pointsValue.text = VerticePoints.toString()
                    verticesValue.text = n.toString()
                    Log.d("area", "$area")
                }

            }

            visual?.setOnClickListener {
                dialog?.dismiss()
                val polygon1 = map?.addPolygon(
                    PolygonOptions().clickable(true)

                        .addAll(VerticePoints)
                )
                polygon1?.tag = "alpha"
                stylePolygon(polygon1!!)

            }

            builder.setView(r).setNegativeButton(" ", DialogInterface.OnClickListener { _, _ -> })

                .setPositiveButton("",
                    DialogInterface.OnClickListener { _: DialogInterface?, _: Int ->

                    })

            builder.create()


        } ?: throw IllegalStateException("Activity cannot be null")


    }

    fun convert(area: Double, units: Int): Double {
        when (units) {
            1 -> {
                Log.d("here", "acres")
            }
            2 -> {
                Log.d("here", "hectares")
            }
        }
        return area
    }


    private val COLOR_LIGHT_GREEN_ARGB = -0x7e387c
    private val POLYGON_STROKE_WIDTH_PX = 8
    private val PATTERN_DASH_LENGTH_PX = 20

    private val DASH: PatternItem = Dash(PATTERN_DASH_LENGTH_PX.toFloat())
    private val PATTERN_GAP_LENGTH_PX = 20
    private val DOT: PatternItem = Dot()
    private val GAP: PatternItem = Gap(PATTERN_GAP_LENGTH_PX.toFloat())

    // Create a stroke pattern of a gap followed by a dash.
    private val PATTERN_POLYGON_ALPHA = listOf(DOT, GAP, DASH, GAP)

    private fun stylePolygon(polygon: Polygon) {
        // Get the data object stored with the polygon.
        val type = polygon.tag?.toString() ?: ""
        var pattern: List<PatternItem>? = null
        var strokeColor = Color.BLACK
        var fillColor = Color.WHITE
        when (type) {
            "alpha" -> {
                // Apply a stroke pattern to render a dashed line, and define colors.
                pattern = PATTERN_POLYGON_ALPHA
                strokeColor = Color.BLACK
                fillColor = COLOR_LIGHT_GREEN_ARGB
            }

        }
        polygon.strokePattern = pattern
        polygon.strokeWidth = POLYGON_STROKE_WIDTH_PX.toFloat()
        polygon.strokeColor = strokeColor
        polygon.fillColor = fillColor
    }

}