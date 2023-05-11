package com.dsmagic.kibira.dataReadings

import android.util.Log
import com.dsmagic.kibira.activities.CreateProjectDialog
import kotlin.math.abs


object GFG {
    // (X[i], Y[i]) are coordinates of i'th point.
    fun polygonArea(
        X: MutableList<Double>, Y: MutableList<Double>,
        n: Int
    ): Double {
        // Initialize area
        var area = 0.0

        try{
            if( n < 3){
                CreateProjectDialog.alertfail("Few vertices.\n Make sure more than 2 points are selected")
                return 0.0
            }
            // Calculate value of shoelace formula
            var j = n - 1
            for (i in 0 until n) {
                area += (X[j] + X[i]) * (Y[j] - Y[i])

                // j is previous vertex to i
                j = i
            }

        }
        catch (e:java.lang.Exception){
            Log.d("Error","e")
        }

        // Return absolute value
        return abs(area / 2.0)
    }

}