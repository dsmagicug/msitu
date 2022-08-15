package com.dsmagic.kibira.data.LocationDependant

import android.location.Location

class LocationDependantFunctions {

    fun getBearing(roverPoint:Location, nextPoint:Location):String{
        var position:String =""
val n = true
        val bearing = nextPoint.bearingTo(roverPoint)
        when {
            bearing < 65.0f -> {
                position = "Ahead"
            }
            66.0f < bearing && bearing < 70.0f -> {
              position = "Left"
            }
            70.0f < bearing && bearing < 90.0f  -> {
                position = "Right"

            }

        }

return bearing.toString()
    }

    fun facingDirection(InitialBearing:Float,CurrentBearing:Float):String{
        var d = ""
        var rangeRight = 5 + InitialBearing
        var rangeLeft = InitialBearing - 5
        when{
            //going too far right
            CurrentBearing > InitialBearing -> {
                d = "Right"
            }
//going too far left
            CurrentBearing < InitialBearing -> {
                d ="Left"
            }

             CurrentBearing <  rangeRight || CurrentBearing  < rangeLeft ->{
                d = "Stop"
            }
        }

        return d
    }

}