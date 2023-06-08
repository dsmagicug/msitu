package com.dsmagic.kibira.dataReadings

import android.util.Log
import com.dsmagic.kibira.activities.CreateProjectDialog
import kotlin.math.abs
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