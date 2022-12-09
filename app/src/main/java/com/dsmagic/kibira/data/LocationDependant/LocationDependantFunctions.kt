package com.dsmagic.kibira.data.LocationDependant


/*
 *  This file is part of Kibira.
 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions
 *
 *  Kibira is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Kibira is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Kibira. If not, see <http://www.gnu.org/licenses/>
 */

import android.content.Context
import android.widget.TextView
import com.dsmagic.kibira.activities.MainActivity
import com.dsmagic.kibira.activities.MainActivity.Companion.initialTimeValue
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class LocationDependantFunctions {
    fun facingDirection(InitialBearing: Float, bearing: Float): String {

        var d = ""
        val rangeRight = 5 + InitialBearing
        val rangeLeft = InitialBearing - 5
        var currentBearing = 0F
        if (bearing < 0) {
            currentBearing = bearing * -1
        }

        when {
           rangeRight > currentBearing && currentBearing > rangeLeft -> {
                d = "Stop"
            }
            //going too far right
            currentBearing > InitialBearing -> {
                d = "Right"
            }
//going too far left
            currentBearing < InitialBearing -> {
                d = "Left"
            }


        }

        return d
    }
    fun pace(textview:TextView,startTime:Long,context: Context) {
        try {
            val sdf = SimpleDateFormat(" HH:mm:ss ")
            val time: String = sdf.format(Date())      //time on switching lines
            val currentTime = convertToMinutes(time)
            val startTime =
                convertToMinutes(initialTimeValue)   //time captured right after planting started

            val diff = currentTime - startTime
            val pace = MainActivity.listOfMarkedPoints.size / diff
            val paceValue = pace.roundToInt()

            textview.text = paceValue.toString()
        } catch (e: UninitializedPropertyAccessException) {

        }

    }
    private var markedLinesCounter = 0
fun markedLines(Line:Polyline,ListOfMarkedPoints:MutableList<LatLng>,textview: TextView){
    if(ListOfMarkedPoints.isEmpty()){
        return
    }
    val pointsOnLine = Line.tag as MutableList<*>
    val commonElements = ListOfMarkedPoints.intersect(pointsOnLine.toSet())
    if(commonElements.size >= 5){
        markedLinesCounter +=1
        textview.text = markedLinesCounter.toString()
    }


}

    private fun convertToMinutes(time: String): Double {
        val timeSplit = time.split(":")
        val hours = timeSplit[0].toDouble() * 60
        val minutes = timeSplit[1].toDouble()
        return hours + minutes
    }

}