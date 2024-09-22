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

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.dsmagic.kibira.R
import com.dsmagic.kibira.activities.MainActivity.Companion.VerticesLongLat
import com.dsmagic.kibira.activities.MainActivity.Companion.latVertexList

import com.dsmagic.kibira.dataReadings.GFG.polygonArea
import com.dsmagic.kibira.dataReadings.LongLat
import com.dsmagic.kibira.roomDatabase.AppDatabase
import com.dsmagic.kibira.utils.Conversions
import com.google.android.gms.maps.model.*
import gov.nasa.worldwind.geom.LatLon
import gov.nasa.worldwind.geom.coords.UTMCoord
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.ceil


object CustomlinesDialog : DialogFragment() {
    var eastingList = mutableListOf<Double>()
    var northingList = mutableListOf<Double>()
    var areaUnitsInt: Int = 1

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return this.activity?.let { it ->

            CreateProjectDialog.appdbInstance =
                AppDatabase.dbInstance(activity?.applicationContext!!)

            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            val r = inflater.inflate(R.layout.activity_customlines, null)

            val areaValue = r.findViewById<TextView>(R.id.area_value)

            val gapsize_units = resources.getStringArray(R.array.gapsizeUnits)
            val gapsizeAdapter = ArrayAdapter(requireContext(), R.layout.gapsizeunits, gapsize_units)
            var unitsValue = " "
            val gapsizeValue = r.findViewById<EditText>(R.id.gapSize)
            val viewGapSize = r.findViewById<AutoCompleteTextView>(R.id.gapsizeDropDown)
            val radioAcres = r.findViewById<RadioButton>(R.id.acres)
            val radioHectares = r.findViewById<RadioButton>(R.id.hectares)


            viewGapSize.setAdapter(gapsizeAdapter)
            viewGapSize.setOnItemClickListener { adapterView, _, i, _ ->
                val gapsizeUnit = adapterView!!.getItemAtPosition(i).toString()
                unitsValue = gapsizeUnit
            }


            //units to be used for the area.
            //1-for acres and 2- for hectares

                val n = VerticesLongLat.size
                val units = r.findViewById<RadioGroup>(R.id.area_units)?.checkedRadioButtonId

                val selectedUnits =
                    units?.let { it1 -> dialog?.findViewById<RadioButton>(it1)?.text.toString() }

                if (n < 3) {
                    dialog?.dismiss()
                    Toast.makeText(
                        context, "Few vertices provided.More than 2 are expected", Toast.LENGTH_LONG
                    ).show()
                }


                    val list = mutableListOf<LongLat>(
                        LongLat(32.463239621666666, 0.04694726166666666),
                        LongLat(32.46321398833334, 0.046928243333333335),
                        LongLat(32.46319326166667, 0.04695351666666667),
                        LongLat(32.46321858666666, 0.046973766666666666)

                    )

            val list2 = mutableListOf<LongLat>(
                LongLat(32.46325419666667, 0.04688487833333333),
                LongLat(32.46325006	, 0.04692236833333333),
                LongLat(32.46322413, 0.046900936666666664),

                LongLat(32.463275073333335, 0.04690800666666666)

            )

                    toUTMCoordinateSystem(VerticesLongLat, areaValue)

            when (selectedUnits) {

                "In hectares" -> {
                    areaUnitsInt = 2
                    val convertedArea: Double = convert(areaValue.text.toString().toDouble(), areaUnitsInt)
                    areaValue.text = convertedArea.toString()
                }
                "In acres" -> {
                    areaUnitsInt = 1

                    val convertedArea: Double = convert(areaValue.text.toString().toDouble(), areaUnitsInt)
                    areaValue.text = convertedArea.toString()
                }

            }

            radioAcres?.setOnClickListener {
                areaUnitsInt = 1

                val convertedArea: Double = convert(area, areaUnitsInt)
                areaValue.text = convertedArea.toString()
            }

            radioHectares?.setOnClickListener {
                areaUnitsInt = 2

                val convertedArea: Double = convert(area, areaUnitsInt)
                areaValue.text = convertedArea.toString()
            }


            val delay: Long = 1000 // 1 seconds after user stops typing

            var last_text_edit: Long = 0
            val handler = Handler()

            val isDoneTyping = Runnable {
                if (System.currentTimeMillis() > last_text_edit + delay - 500) {
                    if (areaValue.text != " " && gapsizeValue.text.toString() != " ") {
                        if (unitsValue == " ") {
                            unitsValue = " Ft"
                        }

                        treeEstimate(
                            gapsizeValue.text.toString().toDouble(),
                            areaValue.text.toString().toDouble(),
                            unitsValue,
                            areaUnitsInt
                        )

                    }
                }
            }

            gapsizeValue?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int, before: Int,
                    count: Int
                ) {
                    //You need to remove this to run only once
                    handler.removeCallbacks(isDoneTyping)
                }

                override fun afterTextChanged(s: Editable) {
                    //avoid triggering event when text is empty
                    if (s.isNotEmpty()) {
                        last_text_edit = System.currentTimeMillis()
                        handler.postDelayed(isDoneTyping, delay)
                    } else {

                    }
                }
            }
            )

            builder.setView(r).setNegativeButton(" ", DialogInterface.OnClickListener { _, _ -> })

                .setPositiveButton(
                    "",
                    DialogInterface.OnClickListener { _: DialogInterface?, _: Int ->

                    })

            builder.create()


        } ?: throw IllegalStateException("Activity cannot be null")


    }

    var area = 0.0
    private fun toUTMCoordinateSystem(List: MutableList<LongLat>, Text: TextView) {
        var easting = 0.0
        var northing = 0.0

        List.forEach {
            val ll = LatLon.fromDegrees(it.getLatitude(), it.getLongitude())
            val utm = UTMCoord.fromLatLon(ll.latitude, ll.longitude)
            easting = utm.easting
            northing = utm.northing
            eastingList.add(easting)
            northingList.add(northing)

        }
       area = polygonArea(eastingList, northingList, List.size)
        Log.d("List","$VerticesLongLat")

        val convertedArea: Double = convert(area, areaUnitsInt)
        Text.text = convertedArea.toString()

    }


    private fun convert(area: Double, units: Int): Double {
        // 1 for acres and 2 for hectares
        var finalArea = 0.0

        val df = DecimalFormat("#.####")
        df.roundingMode = RoundingMode.DOWN

        when (units) {
            1 -> {
                finalArea = df.format((area / 4047)).toDouble()
//                finalArea  = area/4047

            }
            2 -> {
                finalArea = df.format((area / 10000)).toDouble()
//                finalArea  = area/10000
            }
        }
        return finalArea
    }

    private fun treeEstimate(gapSize: Double, area: Double, gapunits: String, areaUnits: Int) {
        val treeValue = dialog?.findViewById<TextView>(R.id.trees_value)
        var num = 0.0
        var areaVal = 0.0

        //Convert area to the same units as the gap size units i.e sq ft or sq meters
        when (areaUnits) {
            2 -> {
                areaVal = Conversions.fromHectares(area.toString(), gapunits)
            }
            1 -> {
                areaVal = Conversions.fromAcres(area.toString(), gapunits)
            }
        }

        //Area occupied by a single plot given the desired gap size ie a 10by10 or 12by12 ft

        val squareArea = gapSize * gapSize
        num = (areaVal  / squareArea)
        treeValue?.text = ceil(num).toInt().toString()
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