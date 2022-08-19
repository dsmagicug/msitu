package com.dsmagic.kibira.data.LocationDependant

class LocationDependantFunctions {


    fun facingDirection(InitialBearing: Float, bearing: Float): String {
        var d = ""
        var rangeRight = 10 + InitialBearing
        var rangeLeft = InitialBearing - 10
        var CurrentBearing: Float = 0F
        if (bearing < 0) {
            CurrentBearing = bearing * -1
        }
//Outside the "when" , read the documentation to know why!

        if (CurrentBearing < rangeRight || CurrentBearing < rangeLeft) {
            d = "Stop"
        }

        when {
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

}