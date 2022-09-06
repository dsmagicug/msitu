package com.dsmagic.kibira.data.LocationDependant

import android.widget.TextView
import android.widget.Toast
import com.dsmagic.kibira.MainActivity
import com.dsmagic.kibira.MainActivity.Companion.context
import com.dsmagic.kibira.MainActivity.Companion.initialTimeValue
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
    fun pace(textview:TextView,startTime:Long) {
        try {
            val start = System.currentTimeMillis()
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
            Toast.makeText(context, "No points marked on that line", Toast.LENGTH_SHORT).show()
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
        //var seconds = timeSplit[2].toDouble()
        return hours + minutes
    }

}