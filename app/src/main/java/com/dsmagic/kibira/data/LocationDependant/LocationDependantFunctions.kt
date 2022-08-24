package com.dsmagic.kibira.data.LocationDependant

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import com.dsmagic.kibira.MainActivity
import com.dsmagic.kibira.MainActivity.Companion.context
import com.dsmagic.kibira.MainActivity.Companion.initialTimeValue
import com.dsmagic.kibira.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class LocationDependantFunctions {
    val looper = Looper.getMainLooper()
    val handler = Handler(looper)

    fun facingDirection(InitialBearing: Float, bearing: Float): String {

        var d = ""
        var rangeRight = 15 + InitialBearing
        var rangeLeft = InitialBearing - 15
        var CurrentBearing: Float = 0F
        if (bearing < 0) {
            CurrentBearing = bearing * -1
        }

        when {
           rangeRight > CurrentBearing && CurrentBearing > rangeLeft -> {
                d = "Stop"
            }
            //going too far right
            CurrentBearing > InitialBearing -> {
                d = "Right"
            }
//going too far left
            CurrentBearing < InitialBearing -> {
                d = "Left"
            }


        }

        return d
    }
    fun pace(textview:TextView) {
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
            Toast.makeText(context, "No points marked on that line", Toast.LENGTH_SHORT).show()
        }

    }
    var markedLinesCounter = 0
fun markedLines(Line:Polyline,ListOfMarkedPoints:MutableList<LatLng>,textview: TextView){
    if(ListOfMarkedPoints.isEmpty()){
        return
    }
    val pointsOnLine = Line.tag as MutableList<*>
    val commonElements = ListOfMarkedPoints.intersect(pointsOnLine.toSet())
    if(commonElements.size > 5){
        markedLinesCounter +=1
        textview.text = markedLinesCounter.toString()
    }


}

    private fun convertToMinutes(time: String): Double {
        val timeSplit = time.split(":")
        val hours = timeSplit[0].toDouble() * 60
        val minutes = timeSplit[1].toDouble()
        var seconds = timeSplit[2].toDouble()
        val totalMinutes = hours + minutes
        return totalMinutes
    }

}