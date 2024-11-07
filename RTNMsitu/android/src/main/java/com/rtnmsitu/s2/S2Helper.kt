package com.rtnmsitu.s2

/*

 *  This file is part of Msitu.

 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions

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

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import dilivia.s2.S2LatLng
import dilivia.s2.S2Point
import dilivia.s2.index.point.S2ClosestPointQuery
import dilivia.s2.index.point.S2PointIndex
import dilivia.s2.index.shape.MutableS2ShapeIndex
import dilivia.s2.index.shape.S2ClosestEdgeQuery
import dilivia.s2.region.S2Polyline


class S2Helper {
    // Helper class for S2 geometry stuff
    companion object {
        fun makeS2PointFromLngLat(loc: LatLng): S2Point {
            return S2LatLng.fromDegrees(loc.latitude, loc.longitude).toPoint()
        }

        fun makeS2PolyLine(l: List<LatLng>, pointsIndex: S2PointIndex<S2LatLng>): S2Polyline {
            return S2Polyline(l.map {
                val p = S2LatLng.fromDegrees(it.latitude, it.longitude)
                val pt = p.toPoint()
                pointsIndex.add(pt, p)
                pt // Val returned...
            })
        }

        fun s2Point(x: Double, y: Double, z: Double): S2Point {
            return S2Point(x,y,z)
        }

        fun addS2Polyline2Index(i: Int, index: MutableS2ShapeIndex, line: S2Polyline) {
            val s = S2Polyline.Shape(i, line)
            index.add(s)
        }

        fun findClosestLine(index: MutableS2ShapeIndex, loc: LatLng, ptList: List<Any?>): Any? {
            val q = S2ClosestEdgeQuery(index)
            val p = S2ClosestEdgeQuery.PointTarget(makeS2PointFromLngLat(loc))
            val r = q.findClosestEdge(p)
            if (r.isEmpty() || r.shapeId < 0)
                return null
            else if (r.shapeId > ptList.size)
                return null
            Log.d("closest", "Found closest point $r")
            return ptList[r.shapeId]
        }

        fun findClosestPointOnLine(index: S2PointIndex<*>, loc: LatLng): Any? {
            val q = S2ClosestPointQuery(index)
            val p = S2ClosestPointQuery.S2ClosestPointQueryPointTarget(makeS2PointFromLngLat(loc))
            val r = q.findClosestPoint(p)
            if (r.isEmpty() || r.pointData == null)
                return null
            return r.data()
        }

    }
}