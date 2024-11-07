package com.rtnmsitu.geometry

import android.content.Context
import android.widget.Toast
import android.util.Log
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
    // (X[i], Y[i]) are coordinates of the i'th point.
    fun polygonArea(
        X: MutableList<Double>,
        Y: MutableList<Double>,
        n: Int,
        context: Context // Added Context parameter for Toast
    ): Double {
        // Initialize area
        var area = 0.0

        try {
            if (n < 3) {
                // Show toast if less than 3 points
                Toast.makeText(context, "Polygon needs at least 3 points", Toast.LENGTH_SHORT).show()
                return 0.0
            }

            // Calculate value of shoelace formula
            var j = n - 1
            for (i in 0 until n) {
                area += (X[j] + X[i]) * (Y[j] - Y[i])
                j = i // j is previous vertex to i
            }
        } catch (e: Exception) {
            Log.e("Error", "Exception occurred: ${e.message}")
        }

        // Return absolute value
        return abs(area / 2.0)
    }
}