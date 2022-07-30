package com.dsmagic.kibira.data.LocationDependant

import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.dsmagic.kibira.Geoggapsize
import com.dsmagic.kibira.MainActivity
import com.dsmagic.kibira.S2Helper
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import dilivia.s2.S2LatLng

class LocationDependantFunctions {
    companion object{
       lateinit var currentLocation: Location
        lateinit var currentPlantingLine:Polyline



         fun plotFunc(){
var plotfuncLooper = Looper.getMainLooper()

         }
    }

    fun getBearing(roverPoint:Location, nextPoint:Location):String{
        var position:String

        val bearing = roverPoint.bearingTo(nextPoint)

        if(bearing > 0 ){
            position = "Left"
        }else{
            position = "Right"
        }
return position
    }
}