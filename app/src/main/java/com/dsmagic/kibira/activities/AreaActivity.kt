
import com.dsmagic.kibira.dataReadings.LongLat

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

// ordered points
internal class Point(var x: Double, var y: Double)
internal object GFG {

    fun orientation(
   ListLat:MutableList<Double>,ListLong: MutableList<Double>
    ): Double {
        val p1 = Point(ListLat[0],ListLong[0])
        val p2 = Point(ListLat[1],ListLong[1])
        val p3 = Point(ListLat[2],ListLong[2])

        // for derivation of the formula
        val points = (p2.y - p1.y) * (p3.x - p2.x) -
                (p2.x - p1.x) * (p3.y - p2.y)
        ListLat.removeAt(0)
        ListLong.removeAt(0)
        if (points == 0.0) return 0.0 // collinear

        // clockwise or counterclockwise
        return if (points > 0.0) 1.0 else 2.0

    }


    fun getOrientation(points: MutableList<LongLat>): Double {

        val size = points.size
        var crossProduct = 0.0

        for (i in 0 until size) {

            val current = Point(points[i].lat,points[i].long)
            val n =  points[(i + 1) % size]
            val next = Point(n.lat,n.long)

//            val next = points[(i + 1) % size]
            crossProduct += (next.x - current.x) * (next.y + current.y)
        }

        return crossProduct

    }


}
